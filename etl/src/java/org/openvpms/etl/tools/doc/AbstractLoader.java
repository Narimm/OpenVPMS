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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.tools.doc;

import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.File;
import java.util.List;


/**
 * Abstract implementation of the {@link Loader} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * The loader listener. May be <tt>null</tt>
     */
    private LoaderListener listener;

    /**
     * The document rules.
     */
    private final DocumentRules rules;

    /**
     * Creates a new <tt>AbstractLoader</tt>.
     *
     * @param service the archetype service
     * @param factory the document factory
     */
    public AbstractLoader(IArchetypeService service,
                          DocumentFactory factory) {
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
     * @return the listener. May be <tt>null</tt>
     */
    public LoaderListener getListener() {
        return listener;
    }

    /**
     * Creates a new document.
     *
     * @param file     the file to create the document from
     * @param mimeType the file mime type
     * @return a new document
     */
    protected Document createDocument(File file, String mimeType) {
        return factory.create(file, mimeType);
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
     */
    protected void notifyLoaded(File file) {
        if (listener != null) {
            listener.loaded(file);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded as it
     * has already been processed.
     *
     * @param file the file
     */
    protected void notifyAlreadyLoaded(File file) {
        if (listener != null) {
            listener.alreadyLoaded(file);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded as a
     * corresponding act cannot be found.
     *
     * @param file the file
     */
    protected void notifyMissingAct(File file) {
        if (listener != null) {
            listener.missingAct(file);
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
}
