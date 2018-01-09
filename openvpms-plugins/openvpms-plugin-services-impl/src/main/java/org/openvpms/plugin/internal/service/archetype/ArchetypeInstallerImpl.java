/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.internal.service.archetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.service.archetype.ArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.plugin.service.archetype.ArchetypeInstaller;
import org.openvpms.tools.archetype.comparator.ArchetypeComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link ArchetypeInstaller}.
 *
 * @author Tim Anderson
 */
public class ArchetypeInstallerImpl implements ArchetypeInstaller {

    /**
     * The archetype service. Only required as the ArchetypeService does not yet provide query support. TODO
     */
    private final IArchetypeService service;

    /**
     * The plugin archetype service.
     */
    private final ArchetypeService pluginService;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager txnManager;

    /**
     * The archetype comparator.
     */
    private final ArchetypeComparator comparator;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ArchetypeInstallerImpl.class);

    /**
     * Constructs an {@link ArchetypeInstallerImpl}.
     *
     * @param service       the archetype service
     * @param pluginService the plugin archetype service
     * @param txnManager    the transaction manager
     */
    public ArchetypeInstallerImpl(IArchetypeService service, ArchetypeService pluginService,
                                  PlatformTransactionManager txnManager) {
        this.service = service;
        this.pluginService = pluginService;
        this.txnManager = txnManager;
        comparator = new ArchetypeComparator();
    }

    /**
     * Installs archetypes from a stream.
     *
     * @param stream the stream
     */
    @Override
    public void install(final InputStream stream) {
        TransactionTemplate template = new TransactionTemplate(txnManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<ArchetypeDescriptor> list = read(stream);
                install(list);
            }
        });
    }

    /**
     * Installs archetypes at the specified path.
     *
     * @param path the path
     */
    @Override
    public void install(String path) {
        install(new String[]{path});
    }

    /**
     * Installs archetypes at the specified paths.
     *
     * @param paths the paths
     */
    @Override
    public void install(final String[] paths) {
        TransactionTemplate template = new TransactionTemplate(txnManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<ArchetypeDescriptor> list = read(paths);
                install(list);
            }
        });
    }

    /**
     * Installs archetypes.
     *
     * @param descriptors the descriptors
     */
    private void install(List<ArchetypeDescriptor> descriptors) {
        for (ArchetypeDescriptor descriptor : descriptors) {
            // Compare the archetype with the both the cached and persistent instance.
            // If the database has been updated outside OpenVPMS the persistent instance may be different to that
            // cached.
            String shortName = descriptor.getShortName();
            ArchetypeDescriptor persistent = getPersistent(descriptor);
            ArchetypeDescriptor cached = service.getArchetypeDescriptor(shortName);
            if (persistent != null && cached != null
                && (persistent.getId() != cached.getId() || comparator.compare(persistent, cached) != null)) {
                // the database has been updated outside of OpenVPMS, so force the cached instance to refresh.
                // NOTE that this only occurs on transaction commit.
                pluginService.save(persistent);
                cached = null;
            }
            if (persistent != null || cached != null) {
                // only replace the archetype if it hasn't changed
                if (persistent != null && comparator.compare(persistent, descriptor) != null) {
                    replace(persistent, descriptor);
                } else if ((cached != null && comparator.compare(cached, descriptor) != null)) {
                    replace(cached, descriptor);
                }
            } else {
                // archetype doesn't exist
                pluginService.save(descriptor);
            }
        }
    }

    /**
     * Replace an archetype descriptor.
     *
     * @param existing   the existing descriptor
     * @param descriptor the new descriptor
     */
    private void replace(ArchetypeDescriptor existing, ArchetypeDescriptor descriptor) {
        pluginService.remove(existing);
        pluginService.save(descriptor);
    }

    /**
     * Returns the persistent archetype descriptor corresponding to that supplied.
     *
     * @param descriptor the archetype descriptor
     * @return the persistent version, or {@code null} if none is found
     */
    private ArchetypeDescriptor getPersistent(ArchetypeDescriptor descriptor) {
        ArchetypeQuery query = new ArchetypeQuery(descriptor.getArchetypeId().getShortName(), false);
        query.add(Constraints.eq("name", descriptor.getName()));
        query.setMaxResults(1);
        IMObjectQueryIterator<ArchetypeDescriptor> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Reads archetypes from one or more paths.
     *
     * @param paths the paths
     * @return the archetypes
     */
    private List<ArchetypeDescriptor> read(String[] paths) {
        List<ArchetypeDescriptor> list = new ArrayList<>();
        for (String path : paths) {
            ArchetypeDescriptors descriptors = getArchetypeDescriptors(path);
            validateAll(descriptors, list);
        }
        return list;
    }

    /**
     * Reads archetype descriptors from a stream.
     *
     * @param stream the stream
     * @return the archetype descriptors
     */
    private List<ArchetypeDescriptor> read(InputStream stream) {
        ArchetypeDescriptors descriptors = ArchetypeDescriptors.read(stream);
        return validateAll(descriptors, new ArrayList<ArchetypeDescriptor>());
    }

    /**
     * Validates archetype descriptors.
     *
     * @param descriptors the descriptors to validate
     * @param list        the list to add to
     * @return the list
     */
    private List<ArchetypeDescriptor> validateAll(ArchetypeDescriptors descriptors, List<ArchetypeDescriptor> list) {
        for (ArchetypeDescriptor descriptor : descriptors.getArchetypeDescriptorsAsArray()) {
            pluginService.validate(descriptor);
            list.add(descriptor);
        }
        return list;
    }

    /**
     * Reads an archetype descriptor from an {@code .adl}.
     *
     * @param path the path to the resource
     * @return the read archetype descriptors
     */
    private ArchetypeDescriptors getArchetypeDescriptors(String path) {
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            resource = new ClassPathResource(path);
        }
        log.info("Reading archetypes from " + resource.getDescription());
        try (InputStream stream = resource.getInputStream()) {
            return ArchetypeDescriptors.read(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read archetype descriptor: " + path, exception);
        }
    }

}
