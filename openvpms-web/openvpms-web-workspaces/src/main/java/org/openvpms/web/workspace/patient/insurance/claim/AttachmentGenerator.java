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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.insurance.AttachmentStatus;
import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.im.report.TemplatedReporter;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.history.PatientHistoryFilter;
import org.openvpms.web.workspace.patient.history.PatientHistoryIterator;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;

import java.io.InputStream;
import java.util.List;

/**
 * Generates attachments for a claim.
 *
 * @author Tim Anderson
 */
class AttachmentGenerator {

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The reporter factory.
     */
    private final ReporterFactory factory;

    /**
     * Constructs a {@link AttachmentGenerator}.
     *
     * @param context the context
     */
    public AttachmentGenerator(Party customer, Party patient, Context context) {
        this.patient = patient;
        LocalContext local = new LocalContext(context);
        local.setCustomer(customer);
        local.setPatient(patient);
        IMObjectBean customerBean = new IMObjectBean(customer);
        Party location = (Party) customerBean.getNodeTargetObject("practice");
        if (location != null) {
            local.setLocation(location);
        }
        this.context = local;
        factory = ServiceHelper.getBean(ReporterFactory.class);
    }

    /**
     * Generates attachments.
     *
     * @param attachments the attachments editor
     */
    public void generate(AttachmentCollectionEditor attachments) {
        Act history = null;
        List<Act> acts = attachments.getCurrentActs();
        for (Act attachment : acts) {
            if (isHistory(attachment)) {
                history = attachment;
                break;
            }
        }
        if (history == null) {
            attachments.add(createHistory());
        }
        for (Act attachment : acts) {
            generate(attachment);
        }
        attachments.save();
        attachments.refresh();
    }

    /**
     * Generates an attachment.
     *
     * @param attachment the attachment
     */
    private void generate(Act attachment) {
        ActBean bean = new ActBean(attachment);
        String type = bean.getString("type");
        if (PatientArchetypes.CLINICAL_EVENT.equals(type)) {
            generateHistory(bean);
        } else if (bean.getReference("document") == null) {
            Act original = (Act) bean.getNodeTargetObject("original");
            if (TypeHelper.isA(original, CustomerAccountArchetypes.INVOICE)) {
                generateInvoice(bean, original);
            } else if (TypeHelper.isA(original, InvestigationArchetypes.PATIENT_INVESTIGATION)) {
                generateInvestigation(bean, (DocumentAct) original);
            } else if (original instanceof DocumentAct) {
                generateDocument(bean, original);
            } else {
                setStatus(bean, AttachmentStatus.ERROR, "The source document no longer exists");
            }
        }
    }

    private void generateInvestigation(ActBean bean, DocumentAct investigation) {
        IMObjectReference reference = investigation.getDocument();
        IArchetypeRuleService archetypeService = ServiceHelper.getArchetypeService();
        Document document = (reference != null) ? (Document) archetypeService.get(reference) : null;
        if (document == null) {
            setStatus(bean, AttachmentStatus.ERROR, "The investigation has no document");
        } else {
            copy(bean, document);
        }
    }

    private void copy(ActBean bean, Document document) {
        DocumentHandler handler = ServiceHelper.getBean(DocumentHandlers.class).get(document);
        InputStream content = handler.getContent(document);
        Document copy = handler.create(document.getName(), content, document.getMimeType(), document.getDocSize());
        save(bean, copy);
    }

    /**
     * Generates a document, storing it as an attachment.
     *
     * @param bean     the attachment
     * @param original the original document
     */
    private void generateDocument(ActBean bean, Act original) {
        ActBean source = new ActBean(original);
        try {
            IMObjectReference docRef = source.getReference("document");
            if (docRef == null) {
                ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(original, context);
                Reporter<Act> reporter = factory.create(original, locator, TemplatedReporter.class);
                save(bean, reporter);
            } else {
                IArchetypeRuleService archetypeService = ServiceHelper.getArchetypeService();
                Document document = (Document) archetypeService.get(docRef);
                if (document == null) {
                    setStatus(bean, AttachmentStatus.ERROR, "The source document no longer exists");
                } else {
                    copy(bean, document);
                }
            }
        } catch (Throwable exception) {
            setStatus(bean, AttachmentStatus.ERROR, StringUtils.abbreviate(exception.getMessage(),
                                                                           NodeDescriptor.DEFAULT_MAX_LENGTH));
        }
    }

    private void setStatus(ActBean bean, String status, String message) {
        bean.setStatus(status);
        bean.setValue("error", message);
        bean.save();
    }

    /**
     * Generates an invoice, storing it in an attachment.
     *
     * @param bean     the attachment
     * @param original the invoice
     */
    private void generateInvoice(ActBean bean, Act original) {
        ReporterFactory factory = ServiceHelper.getBean(ReporterFactory.class);
        ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(original, context);
        Reporter<Act> reporter = factory.create(original, locator, TemplatedReporter.class);
        save(bean, reporter);
    }

    /**
     * Generates patient history, storing it in an attachment.
     *
     * @param bean the attachment
     */
    private void generateHistory(ActBean bean) {
        PatientHistoryQuery query = new PatientHistoryQuery(patient, true);
        PatientHistoryFilter filter = new PatientHistoryFilter(query.getSelectedItemShortNames());
        PatientHistoryIterator summary = new PatientHistoryIterator(query, filter, 3);
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(PatientArchetypes.CLINICAL_EVENT,
                                                                             context);
        Reporter<Act> reporter = factory.create(summary, locator, TemplatedReporter.class);
        save(bean, reporter);
    }

    /**
     * Determines if an attachment is a history attachment.
     *
     * @param attachment the attachment
     * @return {@code true} if the attachment is a history attachment
     */
    private boolean isHistory(Act attachment) {
        return PatientArchetypes.CLINICAL_EVENT.equals(new ActBean(attachment).getString("type"));
    }

    /**
     * Creates an attachment for patient history.
     *
     * @return a new attachment
     */
    private Act createHistory() {
        Act act = (Act) IMObjectCreator.create(InsuranceArchetypes.ATTACHMENT);
        ActBean bean = new ActBean(act);
        bean.setValue("name", "Patient History");
        bean.setValue("type", PatientArchetypes.CLINICAL_EVENT);
        return act;
    }

    /**
     * Generates a document, storing it in an attachment.
     *
     * @param bean     the attachment
     * @param reporter the reporter used to generate the document
     */
    private void save(ActBean bean, Reporter<Act> reporter) {
        reporter.setFields(ReportContextFactory.create(context));
        Document document = reporter.getDocument(DocFormats.PDF_TYPE, false);
        save(bean, document);
    }

    private void save(ActBean bean, Document document) {
        bean.setStatus(AttachmentStatus.PENDING);
        bean.setValue("error", null);
        bean.setValue("document", document.getObjectReference());
        SaveHelper.save(bean.getAct(), document);
    }

}
