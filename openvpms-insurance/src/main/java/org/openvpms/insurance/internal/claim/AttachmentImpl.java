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
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.insurance.claim.Attachment;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.i18n.InsuranceMessages;

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
    private final ActBean act;

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
        this.act = new ActBean(act, service);
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Returns the attachment identifier, issued by the insurer.
     *
     * @return the attachment identifier, or {@code null} if none has been issued
     */
    @Override
    public String getInsurerId() {
        ActIdentity identity = getIdentity();
        return identity != null ? identity.getIdentity() : null;
    }

    /**
     * Sets the attachment identifier, issued by the insurer.
     * <p>
     * An attachment can have a single identifier issued by an insurer. To avoid duplicates, each insurance service must
     * provide a unique archetype.
     *
     * @param archetype the identifier archetype. Must have an <em>actIdentity.insuranceAttachment</em> prefix.
     * @param id        the claim identifier
     */
    @Override
    public void setInsurerId(String archetype, String id) {
        ActIdentity identity = getIdentity();
        if (identity == null) {
            identity = (ActIdentity) service.create(archetype);
            act.addValue("insurerId", identity);
        } else if (!TypeHelper.isA(identity, archetype)) {
            throw new IllegalArgumentException(
                    "Argument 'archetype' must be of the same type as the existing identifier");
        }
        identity.setIdentity(id);
        act.save();
    }

    /**
     * Returns the attachment file name.
     *
     * @return the attachment file name
     */
    @Override
    public String getFileName() {
        return ((DocumentAct) act.getAct()).getFileName();
    }

    /**
     * Returns the attachment mime type
     *
     * @return the mime type
     */
    @Override
    public String getMimeType() {
        return ((DocumentAct) act.getAct()).getMimeType();
    }

    /**
     * Returns the attachment size.
     *
     * @return the attachment size, in bytes
     */
    @Override
    public long getSize() {
        long size = 0;
        IMObjectReference document = ((DocumentAct) act.getAct()).getDocument();
        if (document != null) {
            ArchetypeQuery query = new ArchetypeQuery(document);
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
     * Determines if the attachment has content.
     *
     * @return {@code true} if the attachment has content
     */
    @Override
    public boolean hasContent() {
        IMObjectReference reference = ((DocumentAct) act.getAct()).getDocument();
        return reference != null;
    }

    /**
     * Returns the attachment contents.
     *
     * @return the attachment contents
     */
    @Override
    public InputStream getContent() {
        InputStream result = null;
        DocumentAct documentAct = (DocumentAct) act.getAct();
        IMObjectReference reference = documentAct.getDocument();
        if (reference != null) {
            Document document = (Document) service.get(reference);
            if (document != null) {
                result = handlers.get(document).getContent(document);
            }
        }
        if (result == null) {
            throw new InsuranceException(InsuranceMessages.attachmentHasNoContent(documentAct.getFileName()));
        }
        return result;
    }

    /**
     * Returns the attachment status.
     *
     * @return the attachment status
     */
    @Override
    public Status getStatus() {
        return Status.valueOf(act.getStatus());
    }

    /**
     * Sets the attachment status.
     *
     * @param status the attachment status
     */
    @Override
    public void setStatus(Status status) {
        act.setStatus(status.name());
        act.save();
    }

    /**
     * Returns the claim identity, as specified by the insurance provider.
     *
     * @return the claim identity, or {@code null} if none is registered
     */
    protected ActIdentity getIdentity() {
        return act.getObject("insurerId", ActIdentity.class);
    }
}
