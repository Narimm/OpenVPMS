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
 *  Copyright 2008-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link Loader} interface.
 *
 * @author Tim Anderson
 */
abstract class AbstractLoader implements Loader {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document factory.
     */
    private final DocumentFactory factory;

    /**
     * The loader listener. May be {@code null}
     */
    private LoaderListener listener;

    /**
     * The document rules.
     */
    private final DocumentRules rules;

    /**
     * Constructs an {@code AbstractLoader}.
     *
     * @param service the archetype service
     * @param factory the document factory
     */
    public AbstractLoader(IArchetypeService service, DocumentFactory factory) {
        this.service = service;
        this.factory = factory;
        this.rules = new DocumentRules(service);
    }

    /**
     * Registers a listener.
     *
     * @param listener the listener
     */
    public void setListener(LoaderListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the listener.
     *
     * @return the listener. May be {@code null}
     */
    public LoaderListener getListener() {
        return listener;
    }

    /**
     * Creates a new document.
     *
     * @param file the file to create the document from
     * @return a new document
     */
    protected Document createDocument(File file) {
        return factory.create(file);
    }

    /**
     * Adds a document to a document act, and saves it.
     *
     * @param act      the act
     * @param document the document to add
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    protected void addDocument(DocumentAct act, Document document) {
        List<IMObject> objects = rules.addDocument(act, document);
        service.save(objects);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the document rules.
     *
     * @return the document rules
     */
    protected DocumentRules getRules() {
        return rules;
    }

    /**
     * Notifies any registered listener that a file has been loaded.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    protected void notifyLoaded(File file, long id) {
        if (listener != null) {
            listener.loaded(file, id);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded as it
     * has already been processed.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    protected void notifyAlreadyLoaded(File file, long id) {
        if (listener != null) {
            listener.alreadyLoaded(file, id);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded as a
     * corresponding act cannot be found.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    protected void notifyMissingAct(File file, long id) {
        if (listener != null) {
            listener.missingAct(file, id);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded due to an
     * error.
     *
     * @param file  the file
     * @param error the error
     */
    protected void notifyError(File file, Throwable error) {
        if (listener != null) {
            listener.error(file, error);
        }
    }

    /**
     * Returns all document act archetype short names that match the supplied short name, and have a "document" node.
     *
     * @param shortNames the short names. May be {@code null} or contain wildcards
     * @return the document act archetype short names
     */
    protected String[] getDocumentActShortNames(String[] shortNames) {
        List<String> result = new ArrayList<String>();
        List<ArchetypeDescriptor> descriptors;
        if (shortNames == null || shortNames.length == 0) {
            descriptors = getService().getArchetypeDescriptors();
        } else {
            descriptors = DescriptorHelper.getArchetypeDescriptors(shortNames, getService());
        }
        for (ArchetypeDescriptor descriptor : descriptors) {
            if (DocumentAct.class.isAssignableFrom(descriptor.getClazz())
                && descriptor.getNodeDescriptor("document") != null) {
                result.add(descriptor.getType().getShortName());
            }
        }

        return result.toArray(new String[result.size()]);
    }
}
