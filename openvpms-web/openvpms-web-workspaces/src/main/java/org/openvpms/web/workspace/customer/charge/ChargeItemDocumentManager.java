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

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Component;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.bean.Policies;
import org.openvpms.component.model.object.Reference;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.Cancellable;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.im.doc.FileNameFormatter;
import org.openvpms.web.component.im.doc.ParameterDialog;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditorFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.report.DocumentActReporter;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.macro.MacroVariables;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.mr.PatientDocumentActEditor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper to manage documents associated with a charge item.
 *
 * @author Tim Anderson
 */
class ChargeItemDocumentManager extends AbstractPropertyEditor implements Saveable, Cancellable {

    /**
     * The charge item editor.
     */
    private final CustomerChargeActItemEditor editor;

    /**
     * The layout context.
     */
    private final LayoutContext layoutContext;

    /**
     * The documents collection editor.
     */
    private final ActRelationshipCollectionEditor documents;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The file name formatter.
     */
    private final FileNameFormatter formatter;

    /**
     * The document rules.
     */
    private final DocumentRules rules;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Newly created documents that can should be deleted if {@link #cancel()} is invoked.
     */
    private List<DocumentAct> cancellable = new ArrayList<>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ChargeItemDocumentManager.class);

    /**
     * Constructs a {@link ChargeItemDocumentManager}.
     *
     * @param editor      the editor
     * @param property    the property
     * @param saveContext the save context, used to handle document removal
     * @param context     the layout context
     */
    public ChargeItemDocumentManager(CustomerChargeActItemEditor editor, CollectionProperty property,
                                     ChargeSaveContext saveContext, LayoutContext context) {
        super(property);
        this.editor = editor;
        this.layoutContext = context;
        documents = (ActRelationshipCollectionEditor) IMObjectCollectionEditorFactory.create(
                property, editor.getObject(), new DefaultLayoutContext(context)); // wrap to increase depth
        documents.setExcludeDefaultValueObject(false);
        documents.getEditor().setRemoveHandler(saveContext);
        documents.getComponent();

        // force an editor to be created for each object, to ensure they are deleted correctly
        for (Act act : documents.getCurrentActs()) {
            documents.getEditor(act);
        }
        service = ServiceHelper.getArchetypeService();
        formatter = ServiceHelper.getBean(FileNameFormatter.class);
        rules = new DocumentRules(service);
        lookups = ServiceHelper.getLookupService();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    @Override
    public boolean isModified() {
        return documents.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        documents.clearModified();
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    @Override
    public Component getComponent() {
        return documents.getComponent();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    @Override
    public FocusGroup getFocusGroup() {
        return documents.getFocusGroup();
    }

    /**
     * Updates the documents associated with a charge, when a property changes.
     * <p/>
     * <ul>
     * <li>If the patient or product changes, existing documents will be first deleted, and new documents created
     * for products that have documents</li>
     * <li>if the clinician changes, it will prompt to refresh any patient letters</li>
     * </ul>
     */
    public void update() {
        Product product = editor.getProduct();
        Party patient = editor.getPatient();
        if (product == null || patient == null) {
            for (Act act : documents.getCurrentActs()) {
                documents.remove(act);
            }
        } else {
            User clinician = editor.getClinician();
            Set<Reference> templateRefs = new HashSet<>();
            // For each document, determine if the product or patient has changed. If so, remove it, so it
            // can be recreated if required.
            // If only the clinician has changed for letter acts, prompt to update
            for (Act document : documents.getCurrentActs()) {
                IMObjectBean bean = service.getBean(document);
                if (productChanged(bean, product) || patientChanged(bean, patient)) {
                    documents.remove(document);
                } else {
                    Reference templateRef = bean.getTargetRef("documentTemplate");
                    if (templateRef != null) {
                        templateRefs.add(templateRef);
                    }
                    if (clinicianChanged(bean, clinician)) {
                        if (bean.isA(PatientArchetypes.DOCUMENT_LETTER)) {
                            promptForUpdate(bean, patient, product, clinician);
                        } else {
                            setClinician(document, clinician);
                        }
                    }
                }
            }

            // add any templates associated with the product where there is no corresponding act
            IMObjectBean productBean = service.getBean(product);
            if (productBean.hasNode("documents")) {
                List<Reference> productTemplateRefs = productBean.getTargetRefs("documents");
                productTemplateRefs.removeAll(templateRefs);

                if (!productTemplateRefs.isEmpty()) {
                    for (Reference reference : productTemplateRefs) {
                        Entity template = (Entity) service.get(reference, true);
                        if (template != null) {
                            addDocument(patient, productBean, clinician, new DocumentTemplate(template, service));
                        }
                    }
                }
            }
        }
    }

    /**
     * Save any edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    public void save() {
        documents.save();
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return {@code true} if edits have been saved.
     */
    @Override
    public boolean isSaved() {
        return documents.isSaved();
    }

    /**
     * Cancel any edits.
     */
    @Override
    public void cancel() {
        if (!cancellable.isEmpty()) {
            for (DocumentAct act : cancellable) {
                IMObjectEditor editor = documents.getEditor(act);
                editor.delete();
            }
            cancellable.clear();
        }
    }

    /**
     * Prompts to update a patient letter, when the clinician changes.
     *
     * @param document  the existing patient letter
     * @param patient   the patient
     * @param product   the product
     * @param clinician the clinician. May be {@code null}
     */
    private void promptForUpdate(IMObjectBean document, Party patient, Product product, User clinician) {
        DocumentAct act = (DocumentAct) document.getObject();
        Entity template = document.getTarget("documentTemplate", Entity.class, Policies.active());
        if (template != null) {
            String title = Messages.format("customer.charge.document.refresh.title", document.getDisplayName());
            String message = Messages.format("customer.charge.document.refresh.message", template.getName());
            ConfirmationDialog dialog = new ConfirmationDialog(title, message, ConfirmationDialog.YES_NO);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onYes() {
                    documents.remove(act);
                    addDocument(act, patient, service.getBean(product), clinician,
                                new DocumentTemplate(template, service));
                    super.onYes();
                }

                /**
                 * Invoked when the 'no' button is pressed.
                 */
                @Override
                public void onNo() {
                    setClinician(act, clinician);
                    super.onNo();
                }
            });
            editor.getEditorQueue().queue(dialog);
        } else {
            setClinician(act, clinician);
        }
    }

    /**
     * Updates the clinician on a document.
     *
     * @param document  the document
     * @param clinician the clinician. May be {@code null}
     */
    private void setClinician(Act document, User clinician) {
        IMObjectEditor editor = documents.getEditor(document);
        if (editor instanceof PatientDocumentActEditor) {
            ((PatientDocumentActEditor) editor).setClinician(clinician);
        } else {
            // all editors should be instances of PatientDocumentActEditor.
            log.error("Cannot update clinician on " + editor.getClass().getName());
        }
    }

    /**
     * Adds a document for a template, if the template is a patient document.
     *
     * @param patient   the patient
     * @param product   the product bean
     * @param clinician the clinician. May be {@code null}
     * @param template  the document template
     */
    private void addDocument(Party patient, IMObjectBean product, User clinician, DocumentTemplate template) {
        String archetype = template.getArchetype();
        if (TypeHelper.isA(archetype, "act.patientDocument*")) {
            DocumentAct documentAct = (DocumentAct) service.create(archetype);
            if (documentAct == null) {
                log.error("Failed to create document=" + archetype + " for template=" + template.getName());
            } else {
                addDocument(documentAct, patient, product, clinician, template);
            }
        }
    }

    /**
     * Adds a document.
     *
     * @param act       the document act
     * @param patient   the patient
     * @param product   the product bean
     * @param clinician the clinician. May be {@code null}
     * @param template  the document template
     */
    private void addDocument(final DocumentAct act, Party patient, IMObjectBean product, User clinician,
                             DocumentTemplate template) {
        Context context = layoutContext.getContext();
        HelpContext help = layoutContext.getHelpContext();
        IMObjectBean bean = service.getBean(act);
        act.setActivityStartTime(editor.getStartTime());
        bean.setTarget("patient", patient);
        bean.setTarget("author", context.getUser());
        bean.setTarget("clinician", clinician);
        if (bean.hasNode("documentTemplate")) {
            bean.setTarget("documentTemplate", template.getEntity());
        }
        if (bean.hasNode("product")) {
            bean.setTarget("product", product.getObject());
        }
        if (bean.isA(PatientArchetypes.DOCUMENT_LETTER)) {
            DocumentActReporter reporter = new DocumentActReporter(act, template, formatter, service, lookups);
            Set<ParameterType> parameters = reporter.getParameterTypes();
            if (!parameters.isEmpty()) {
                reporter.setFields(ReportContextFactory.create(context));
                String title = Messages.format("document.input.parameters", template.getName());
                MacroVariables variables = new MacroVariables(context, service, lookups);
                ParameterDialog dialog = new ParameterDialog(title, parameters, act, context, help.subtopic("document"),
                                                             variables, true, true);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        reporter.setParameters(dialog.getValues());
                        addLetter(act, reporter);
                        super.onOK();
                    }
                });
                editor.getEditorQueue().queue(dialog);
            } else {
                addLetter(act, reporter);
            }
        } else {
            addDocument(act);
        }
    }

    /**
     * Adds a patient letter.
     *
     * @param act      the document act
     * @param reporter the reporter, used to generate the letter
     */
    private void addLetter(DocumentAct act, DocumentActReporter reporter) {
        Document document = reporter.getDocument();
        List<IMObject> changes = rules.addDocument(act, document, false);
        service.save(changes);
        addDocument(act);
        cancellable.add(act);
    }

    /**
     * Adds a document.
     *
     * @param document the document act
     */
    private void addDocument(DocumentAct document) {
        documents.add(document);
        // create an editor for the act so that it is deleted correctly
        documents.getEditor(document);
    }

    /**
     * Determines if the product has changed.
     *
     * @param bean    the document act bean
     * @param product the item product
     * @return {@code true} if the product has changed
     */
    private boolean productChanged(IMObjectBean bean, Product product) {
        return bean.hasNode("product")
               && !ObjectUtils.equals(bean.getTargetRef("product"), product.getObjectReference());
    }

    /**
     * Determines if the patient has changed.
     *
     * @param docBean the document act bean
     * @param patient the patient
     * @return {@code true} if the patient has changed
     */
    private boolean patientChanged(IMObjectBean docBean, Party patient) {
        return !ObjectUtils.equals(docBean.getTargetRef("patient"), patient.getObjectReference());
    }

    /**
     * Determines if the clinician has changed.
     *
     * @param docBean   the document act bean
     * @param clinician the clinician. May be {@code null}
     * @return {@code true} if the clinician has changed
     */
    private boolean clinicianChanged(IMObjectBean docBean, User clinician) {
        IMObjectReference reference = (clinician != null) ? clinician.getObjectReference() : null;
        return !ObjectUtils.equals(docBean.getTargetRef("clinician"), reference);
    }

}
