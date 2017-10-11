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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.service.internal.archetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.plugin.service.archetype.ArchetypeInstaller;
import org.openvpms.plugin.service.archetype.PluginArchetypeService;
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
     * The archetype service.
     */
    private final PluginArchetypeService service;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager txnManager;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ArchetypeInstallerImpl.class);

    /**
     * Constructs an {@link ArchetypeInstallerImpl}.
     *
     * @param service    the archetype service
     * @param txnManager the transaction manager
     */
    public ArchetypeInstallerImpl(PluginArchetypeService service, PlatformTransactionManager txnManager) {
        this.service = service;
        this.txnManager = txnManager;
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
            ArchetypeDescriptor existing = service.getArchetypeDescriptor(descriptor.getShortName());
            if (existing != null) {
                // make sure it is the latest instance and not a cached copy
                existing = (ArchetypeDescriptor) service.get(existing.getObjectReference());
            }
            if (existing != null) {
                ArchetypeComparator comparator = new ArchetypeComparator();
                if (comparator.compare(existing, descriptor) != null) {
                    service.remove(existing);
                    service.save(descriptor);
                }
            } else {
                service.save(descriptor);
            }
        }
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
            service.validate(descriptor);
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
