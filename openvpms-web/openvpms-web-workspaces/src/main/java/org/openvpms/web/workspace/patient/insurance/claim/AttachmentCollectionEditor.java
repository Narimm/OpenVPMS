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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Deletable;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.MultiSelectBrowserDialog;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.document.CustomerPatientDocumentBrowser;

import java.util.ArrayList;
import java.util.List;

/**
 * Editor for the collection of attachments associated with a claim.
 *
 * @author Tim Anderson
 */
class AttachmentCollectionEditor extends ActRelationshipCollectionEditor implements Deletable {

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * Constructs an {@link AttachmentCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public AttachmentCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
        service = ServiceHelper.getArchetypeService();
        setExcludeDefaultValueObject(false);
        getCollectionPropertyEditor().setRemoveHandler(new CollectionPropertyEditor.RemoveHandler() {
            @Override
            public void remove(IMObject object) {
                AttachmentCollectionEditor.this.remove((DocumentAct) object);
            }

            @Override
            public void remove(IMObjectEditor editor) {
                remove(editor.getObject());
            }
        });
    }

    /**
     * Adds a document.
     *
     * @param document the document
     */
    public void addDocument(DocumentAct document) {
        if (!exists(document)) {
            Act attachment = createDocument(document);
            add(attachment);
            refresh();
        }
    }

    /**
     * Adds an invoice attachment, if it doesn't already exist.
     *
     * @param invoice the invoice
     */
    public void addInvoice(FinancialAct invoice) {
        if (!exists(invoice)) {
            Act attachment = createInvoice(invoice);
            add(attachment);
            refresh();
        }
    }

    /**
     * Determines if an attachment is already present.
     *
     * @param attachment the attachment
     * @return {@code true} if it already present
     */
    public boolean exists(Act attachment) {
        boolean result = false;
        for (Act act : getCurrentActs()) {
            ActBean bean = new ActBean(act);
            if (bean.getRelationship(attachment) != null) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Refresh the display of an attachment when it is changed outside of an editor.
     *
     * @param document the attachment to refresh
     */
    public void refresh(DocumentAct document) {
        IMObjectEditor editor = getEditor(document);
        if (editor instanceof AttachmentEditor) {
            ((AttachmentEditor) editor).refresh();
        }
    }

    /**
     * Perform deletion.
     */
    @Override
    public void delete() {
        IMObjectBean bean = new IMObjectBean(getObject());
        List<IMObject> toSave = new ArrayList<>();
        // need to remove relationships to the parent claim and save it, before removing this
        for (Act act : getCurrentActs()) {
            bean.removeTargets("attachments", act, "claim");
            toSave.add(act);
        }
        if (!toSave.isEmpty()) {
            toSave.add(getObject());
            service.save(toSave);

            // remove the attachments
            for (Act act : getCurrentActs()) {
                remove((DocumentAct) act);
            }
        }
    }

    /**
     * Removes an attachment, and its associated document, if any.
     *
     * @param object the attachment act
     */
    protected void remove(DocumentAct object) {
        IMObjectReference reference = object.getDocument();
        if (!object.isNew()) {
            service.remove(object);
        }
        if (reference != null && !reference.isNew()) {
            // TODO - would be improved by OBF-247
            Document document = (Document) service.get(reference);
            if (document != null) {
                service.remove(document);
            }
        }
    }

    /**
     * Invoked when the "Add" button is pressed. Creates a new instance of the selected archetype, and displays it in
     * an editor.
     *
     * @return the new editor, or {@code null} if one could not be created
     */
    @Override
    protected IMObjectEditor onAdd() {
        LayoutContext layout = getContext();
        Context context = layout.getContext();
        Party customer = context.getCustomer();
        Party patient = context.getPatient();
        CustomerPatientDocumentBrowser browser = new CustomerPatientDocumentBrowser(customer, patient, false,
                                                                                    null, null, layout);
        String title = Messages.get("patient.insurance.attach.title");
        final MultiSelectBrowserDialog<Act> dialog = new MultiSelectBrowserDialog<>(title, browser,
                                                                                    layout.getHelpContext());
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                for (Act document : browser.getSelections()) {
                    addDocument((DocumentAct) document);
                }
            }
        });
        dialog.show();
        return null;
    }

    /**
     * Creates an attachment for a document.
     *
     * @param original the original document
     * @return a new attachment
     */
    private Act createDocument(DocumentAct original) {
        ActBean bean = create(original, original.getName());
        bean.setValue("fileName", original.getFileName());
        bean.setValue("mimeType", original.getMimeType());
        return bean.getAct();
    }

    /**
     * Creates an attachment for an invoice.
     *
     * @param original the original invoice
     * @return a new attachment
     */
    private Act createInvoice(FinancialAct original) {
        ActBean bean = create(original, original.getName() + "  " + original.getId());
        return bean.getAct();
    }

    /**
     * Creates an attachment.
     *
     * @param original the original act
     * @param name     the name
     * @return a new attachment
     */
    private ActBean create(Act original, String name) {
        Act act = (Act) IMObjectCreator.create(InsuranceArchetypes.ATTACHMENT);
        ActBean bean = new ActBean(act);
        bean.setValue("startTime", original.getActivityStartTime());
        bean.setValue("name", name);
        bean.setValue("type", original.getArchetypeId().getShortName());
        bean.addNodeRelationship("original", original);
        return bean;
    }

}
