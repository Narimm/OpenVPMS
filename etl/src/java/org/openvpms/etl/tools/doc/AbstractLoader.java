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

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.File;
import java.util.Arrays;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
abstract class AbstractLoader implements Loader {
    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    private final DocumentFactory factory;

    private LoaderListener listener;


    public AbstractLoader(IArchetypeService service,
                          DocumentFactory factory) {
        this.service = service;
        this.factory = factory;
    }

    public void setListener(LoaderListener listener) {
        this.listener = listener;
    }

    protected Document createDocument(File file, String mimeType) {
        return factory.create(file, mimeType);
    }

    protected void save(DocumentAct act, Document document) {
        service.save(Arrays.asList(act, document));
    }

    protected IArchetypeService getService() {
        return service;
    }

    protected void notifyLoaded(File file) {
        if (listener != null) {
            listener.loaded(file);
        }
    }

    protected void notifyAlreadyLoaded(File file) {
        if (listener != null) {
            listener.alreadyLoaded(file);
        }
    }

    protected void notifyMissingAct(File file) {
        if (listener != null) {
            listener.missingAct(file);
        }
    }

    protected void notifyError(File file, Throwable error) {
        if (listener != null) {
            listener.error(file, error);
        }
    }
}
