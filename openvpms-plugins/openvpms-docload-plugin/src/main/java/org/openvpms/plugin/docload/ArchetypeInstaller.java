/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.docload;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;


/**
 * Helper to install archetypes required by DocumentLoader.
 *
 * @author Tim Anderson
 */
public class ArchetypeInstaller {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager txnManager;

    /**
     * The base path. May be <tt>null</tt>
     */
    private final String basePath;

    /**
     * Constructs an <tt>ArchetypeInstaller</tt>.
     *
     * @param service    the archetype service
     * @param txnManager the transaction manager
     * @param basePath   the base path. May be <tt>null</tt>
     */
    public ArchetypeInstaller(IArchetypeService service, PlatformTransactionManager txnManager, String basePath) {
        this.service = service;
        this.txnManager = txnManager;
        this.basePath = basePath;
    }

    /**
     * Installs archetypes.
     *
     * @throws IOException for any I/O error
     */
    public void install() throws IOException {
        TransactionTemplate template = new TransactionTemplate(txnManager);
        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                install("entity.pluginDocumentLoaderByName", null);
                install("entity.pluginDocumentLoaderById", null);
                return null;
            }
        });
    }

    /**
     * Installs an archetype.
     *
     * @param shortName the archetype short name
     * @param node      the archetype node to determine if an archetype has been installed. May be <tt>null</tt>
     * @throws IllegalStateException if <tt>shortName</tt> doesn't correspond to one of the loaded archetypes
     */
    private void install(String shortName, String node) {
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(shortName);
        ArchetypeDescriptor descriptor = descriptors.getArchetypeDescriptors().get(shortName);
        if (descriptor == null) {
            throw new IllegalStateException("Archetype " + shortName + " not found");
        }
        ArchetypeDescriptor existing = service.getArchetypeDescriptor(shortName);
        if (existing != null) {
            if (existing.getNodeDescriptor(node) == null) {
                service.remove(existing);
                service.save(descriptor);
            }
        } else {
            service.save(descriptor);
        }
    }

    /**
     * Reads an archetype descriptor from a <tt>.adl</tt>.
     *
     * @param shortName the archetype short name
     * @return the read archetype descriptors
     * @throws OpenVPMSException if the descriptor cannot be read
     */
    private ArchetypeDescriptors getArchetypeDescriptors(String shortName) {
        String path = "org/openvpms/plugin/docload/" + shortName + ".adl";
        if (basePath != null) {
            path = basePath + "/" + path;
        }
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            resource = new ClassPathResource(path);
        }
        try {
            return ArchetypeDescriptors.read(resource.getInputStream());
        } catch (IOException exception) {
            throw new DocumentLoaderException(exception.getMessage(), exception);
        }
    }
}
