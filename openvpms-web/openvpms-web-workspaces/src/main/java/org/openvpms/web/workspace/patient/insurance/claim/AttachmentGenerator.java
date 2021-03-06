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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.claim;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.object.Reference;
import org.openvpms.insurance.claim.Attachment;
import org.openvpms.report.DocFormats;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.im.report.TemplatedReporter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.history.PatientHistoryFilter;
import org.openvpms.web.workspace.patient.history.PatientHistoryIterator;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.util.Set;

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
     * The charges being claimed.
     */
    private final Charges charges;

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
    public AttachmentGenerator(Party customer, Party patient, Charges charges, Context context) {
        this.patient = patient;
        this.charges = charges;
        LocalContext local = new LocalContext(context);
        local.setCustomer(customer);
        local.setPatient(patient);
        this.context = local;
        factory = ServiceHelper.getBean(ReporterFactory.class);
    }

    /**
     * Generates attachments.
     *
     * @param claim       the claim
     * @param attachments the attachments editor
     * @param location    the location to use
     * @return {@code true} if all attachments were successfully generated
     */
    public boolean generate(Act claim, AttachmentCollectionEditor attachments, Party location) {
        boolean result = true;

        context.setLocation(location);
        updateHistory(attachments);
        updateInvoices(attachments);

        for (Act attachment : attachments.getCurrentActs()) {
            if (!generate(attachment, claim)) {
                result = false;
                break;
            } else {
                attachments.refresh((DocumentAct) attachment);
            }
        }
        attachments.save();
        attachments.refresh();
        return result;
    }

    /**
     * Updates the patient history.
     * <p/>
     * Any existing history attachment is regenerated, to ensure it contains the latest history.
     *
     * @param attachments the attachments editor
     */
    private void updateHistory(AttachmentCollectionEditor attachments) {
        Act history = attachments.getHistory();
        if (history != null) {
            attachments.remove(history);
        }
        attachments.add(attachments.createHistory());
    }

    /**
     * Updates existing invoice attachments, and removes those that are no longer relevant.
     * <p/>
     * Existing attachments are regenerated to ensure that they reflect the current claimed items and customer balance.
     *
     * @param attachments the attachments editor
     */
    private void updateInvoices(AttachmentCollectionEditor attachments) {
        Set<Reference> expectedInvoices = charges.getInvoiceRefs();
        for (Act attachment : attachments.getCurrentActs()) {
            if (isInvoice(attachment)) {
                // for invoices that are in the final claim, it is easiest to just remove the attachment and
                // recreate, as it handles document deletion
                attachments.remove(attachment);
            }
        }
        for (Reference ref : expectedInvoices) {
            FinancialAct invoice = (FinancialAct) IMObjectHelper.getObject(ref);
            if (invoice != null) {
                attachments.addInvoice(invoice);
            }
        }
    }

    /**
     * Generates an attachment.
     *
     * @param attachment the attachment
     * @param claim      the claim
     * @return {@code true} if the attachment was successfully generated
     */
    private boolean generate(Act attachment, Act claim) {
        boolean result = false;
        IMObjectBean bean = new IMObjectBean(attachment);
        String type = bean.getString("type");
        if (PatientArchetypes.CLINICAL_EVENT.equals(type)) {
            result = generateHistory(bean);
        } else if (bean.getReference("document") == null) {
            Act original = bean.getTarget("original", Act.class);
            if (TypeHelper.isA(original, CustomerAccountArchetypes.INVOICE)) {
                result = generateInvoice(bean, original, claim);
            } else if (TypeHelper.isA(original, InvestigationArchetypes.PATIENT_INVESTIGATION)) {
                result = generateInvestigation(bean, (DocumentAct) original);
            } else if (original instanceof DocumentAct) {
                result = generateDocument(bean, original);
            } else {
                setStatus(bean, Attachment.Status.ERROR, Messages.get("patient.insurance.nodocument"));
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Generates an investigation, storing it as an attachment.
     *
     * @param bean          the attachment
     * @param investigation the investigation
     * @return {@code true} if the investigation was generated successfully
     */
    private boolean generateInvestigation(IMObjectBean bean, DocumentAct investigation) {
        boolean result = false;
        Reference reference = investigation.getDocument();
        IArchetypeRuleService archetypeService = ServiceHelper.getArchetypeService();
        Document document = (reference != null) ? (Document) archetypeService.get(reference) : null;
        if (document == null) {
            setStatus(bean, Attachment.Status.ERROR, Messages.get("patient.insurance.noinvestigation"));
        } else {
            result = copy(bean, document);
        }
        return result;
    }

    /**
     * Generates a document, storing it as an attachment.
     *
     * @param bean     the attachment
     * @param original the original document
     * @return {@code true} if the document was generated successfully
     */
    private boolean generateDocument(IMObjectBean bean, Act original) {
        boolean result = false;
        IMObjectBean source = new IMObjectBean(original);
        try {
            Reference docRef = source.hasNode("document") ? source.getReference("document") : null;
            if (docRef == null) {
                ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(original, context);
                Reporter<Act> reporter = factory.create(original, locator, TemplatedReporter.class);
                result = save(bean, reporter);
            } else {
                IArchetypeRuleService archetypeService = ServiceHelper.getArchetypeService();
                Document document = (Document) archetypeService.get(docRef);
                if (document == null) {
                    setStatus(bean, Attachment.Status.ERROR, Messages.get("patient.insurance.nodocument"));
                } else {
                    String mimeType = source.getString("mimeType");
                    if (!StringUtils.isEmpty(mimeType) && !DocFormats.PDF_TYPE.equals(mimeType)) {
                        Converter converter = ServiceHelper.getBean(Converter.class);
                        if (converter.canConvert(document, DocFormats.PDF_TYPE)) {
                            document = converter.convert(document, DocFormats.PDF_TYPE);
                            result = save(bean, document);
                        } else {
                            result = copy(bean, document);
                        }
                    } else {
                        result = copy(bean, document);
                    }
                }
            }
        } catch (Throwable exception) {
            setStatus(bean, Attachment.Status.ERROR, exception.getMessage());
        }
        return result;
    }

    /**
     * Determines if an attachment is an invoice attachment.
     *
     * @param attachment the attachment
     * @return {@code true} if the attachment is a invoice attachment
     */
    private boolean isInvoice(Act attachment) {
        return CustomerAccountArchetypes.INVOICE.equals(new IMObjectBean(attachment).getString("type"));
    }

    /**
     * Generates an invoice, storing it in an attachment.
     *
     * @param bean     the attachment
     * @param original the invoice
     * @param claim    the claim
     * @return {@code true} if it was saved
     */
    private boolean generateInvoice(IMObjectBean bean, Act original, Act claim) {
        ReporterFactory factory = ServiceHelper.getBean(ReporterFactory.class);
        ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator("INSURANCE_CLAIM_INVOICE", context);
        Reporter<Act> reporter = factory.create(original, locator, TemplatedReporter.class);
        reporter.getParameters().put("claim", claim);
        return save(bean, reporter);
    }

    /**
     * Generates patient history, storing it in an attachment.
     *
     * @param bean the attachment
     * @return {@code true} if it was saved
     */
    private boolean generateHistory(IMObjectBean bean) {
        PatientHistoryQuery query = new PatientHistoryQuery(patient, true);
        PatientHistoryFilter filter = new PatientHistoryFilter(query.getSelectedItemShortNames());
        PatientHistoryIterator summary = new PatientHistoryIterator(query, filter, 3);
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator("INSURANCE_CLAIM_MEDICAL_RECORDS",
                                                                             context);
        Reporter<Act> reporter = factory.create(summary, locator, TemplatedReporter.class);
        return save(bean, reporter);
    }

    /**
     * Generates a document, storing it in an attachment.
     *
     * @param bean     the attachment
     * @param reporter the reporter used to generate the document
     */
    private boolean save(IMObjectBean bean, Reporter<Act> reporter) {
        reporter.setFields(ReportContextFactory.create(context));
        Document document = reporter.getDocument(DocFormats.PDF_TYPE, true); // ensure letterhead is included
        return save(bean, document);
    }

    /**
     * Copies a document to an attachment.
     *
     * @param bean     the attachment
     * @param document the document to copy
     * @return {@code true} if the attachment was saved
     */
    private boolean copy(IMObjectBean bean, Document document) {
        DocumentHandler handler = ServiceHelper.getBean(DocumentHandlers.class).get(document);
        InputStream content = handler.getContent(document);
        Document copy = handler.create(document.getName(), content, document.getMimeType(), document.getDocSize());
        return save(bean, copy);
    }

    /**
     * Saves an attachment.
     *
     * @param bean     the attachment
     * @param document the document
     * @return {@code true} if it was successfully saved
     */
    private boolean save(IMObjectBean bean, Document document) {
        boolean result = false;
        PlatformTransactionManager transactionManager = ServiceHelper.getBean(PlatformTransactionManager.class);
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        IArchetypeRuleService service = ServiceHelper.getArchetypeService();
        try {
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    // need to remove any existing document
                    Document existing = bean.getObject("document", Document.class);
                    if (existing != null) {
                        bean.setValue("document", null);
                        bean.save();
                        service.remove(existing);
                    }
                    bean.setValue("status", Attachment.Status.PENDING.name());
                    bean.setValue("error", null);
                    bean.setValue("document", document.getObjectReference());
                    bean.setValue("fileName", document.getName());
                    bean.setValue("mimeType", document.getMimeType());
                    bean.save(document);
                }
            });
            result = true;
        } catch (Throwable exception) {
            setStatus(bean, Attachment.Status.ERROR, exception.getMessage());
        }
        return result;
    }

    /**
     * Sets the status and message of an attachment.
     *
     * @param bean    the attachment
     * @param status  the status
     * @param message the message. May be {@code null}
     */
    private void setStatus(IMObjectBean bean, Attachment.Status status, String message) {
        bean.setValue("status", status.name());
        if (message != null) {
            message = StringUtils.abbreviate(message, NodeDescriptor.DEFAULT_MAX_LENGTH);
        }
        bean.setValue("error", message);
        bean.save();
    }

}
