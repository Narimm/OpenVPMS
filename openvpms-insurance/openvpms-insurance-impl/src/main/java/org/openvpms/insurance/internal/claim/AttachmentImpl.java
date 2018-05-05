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

package org.openvpms.insurance.internal.claim;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.insurance.claim.Attachment;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.internal.i18n.InsuranceMessages;

import java.io.InputStream;
import java.util.Date;

/**
 * Default implementation of the {@link Attachment} interface.
 *
 * @author Tim Anderson
 */
public class AttachmentImpl implements Attachment {

    /**
     * The act.
     */
    private final IMObjectBean act;

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
    public AttachmentImpl(DocumentAct act, IArchetypeRuleService service, DocumentHandlers handlers) {
        this.act = service.getBean(act);
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Returns the OpenVPMS identifier for this attachment.
     *
     * @return the identifier
     */
    @Override
    public long getId() {
        return getAct().getId();
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
        } else if (!identity.isA(archetype)) {
            throw new IllegalArgumentException(
                    "Argument 'archetype' must be of the same type as the existing identifier");
        }
        identity.setIdentity(id);
        act.save();
    }

    /**
     * Returns the date when the attachment was created.
     *
     * @return the date
     */
    @Override
    public Date getDate() {
        return getAct().getActivityStartTime();
    }

    /**
     * Returns the attachment file name.
     *
     * @return the attachment file name
     */
    @Override
    public String getFileName() {
        return getAct().getFileName();
    }

    /**
     * Returns the attachment mime type
     *
     * @return the mime type
     */
    @Override
    public String getMimeType() {
        return getAct().getMimeType();
    }

    /**
     * Returns the attachment size.
     *
     * @return the attachment size, in bytes
     */
    @Override
    public long getSize() {
        long size = 0;
        IMObjectReference document = getAct().getDocument();
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
        IMObjectReference reference = getAct().getDocument();
        return reference != null;
    }

    /**
     * Returns the attachment contents.
     *
     * @return the attachment contents
     * @throws InsuranceException if the content cannot be retrieved
     */
    @Override
    public InputStream getContent() {
        InputStream result = null;
        DocumentAct documentAct = getAct();
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
        return Status.valueOf(getAct().getStatus());
    }

    /**
     * Sets the attachment status.
     *
     * @param status the attachment status
     */
    @Override
    public void setStatus(Status status) {
        getAct().setStatus(status.name());
        act.save();
    }

    /**
     * Returns the type of the attachment.
     *
     * @return the type of the attachment
     */
    @Override
    public Type getType() {
        Type result = Type.DOCUMENT;
        String type = act.getString("type");
        if (PatientArchetypes.CLINICAL_EVENT.equals(type)) {
            result = Type.CLINICAL_HISTORY;
        } else if (CustomerAccountArchetypes.INVOICE.equals(type)) {
            result = Type.INVOICE;
        }
        return result;
    }

    /**
     * Returns the underlying act.
     *
     * @return the act
     */
    protected DocumentAct getAct() {
        return (DocumentAct) act.getObject();
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
