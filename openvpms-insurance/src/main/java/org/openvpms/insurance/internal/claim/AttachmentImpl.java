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

package org.openvpms.insurance.internal.claim;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.insurance.claim.Attachment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Default implementation of the {@link Attachment} interface.
 *
 * @author Tim Anderson
 */
public class AttachmentImpl implements Attachment {

    /**
     * The act.
     */
    private final DocumentAct act;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Constructs a {@link AttachmentImpl}.
     *
     * @param act      the act
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public AttachmentImpl(DocumentAct act, IArchetypeService service, DocumentHandlers handlers) {
        this.act = act;
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Returns the attachment file name.
     *
     * @return the attachment file name
     */
    @Override
    public String getFileName() {
        return act.getFileName();
    }

    /**
     * Returns the attachment mime type
     *
     * @return the mime type
     */
    @Override
    public String getMimeType() {
        return act.getMimeType();
    }

    /**
     * Returns the attachment size.
     *
     * @return the attachment size, in bytes
     */
    @Override
    public long getSize() {
        long size = 0;
        if (act.getDocument() != null) {
            ArchetypeQuery query = new ArchetypeQuery(act.getDocument());
            query.getArchetypeConstraint().setAlias("doc");
            query.add(new NodeSelectConstraint("size"));
            ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
            if (iterator.hasNext()) {
                ObjectSet set = iterator.next();
                size = set.getLong("doc.size");
            }
        }
        return size;
    }

    /**
     * Returns the attachment contents.
     *
     * @return the attachment contents
     */
    @Override
    public InputStream getContent() {
        InputStream result = null;
        if (act.getDocument() != null) {
            Document document = (Document) service.get(act.getDocument());
            if (document != null) {
                result = handlers.get(document).getContent(document);
            }
        }
        if (result == null) {
            result = new ByteArrayInputStream(new byte[0]);
        }
        return result;
    }
}
