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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.web.component.im.edit.AbstractRemoveConfirmationHandler;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;
import org.openvpms.web.workspace.patient.problem.ProblemBrowser;
import org.openvpms.web.workspace.patient.problem.ProblemQuery;

import java.util.function.Consumer;

/**
 * Editor for a collection of <em>act.patientInsuranceClaimItem</em> acts.
 *
 * @author Tim Anderson
 */
class ClaimItemCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The charges.
     */
    private final Charges charges;

    /**
     * The attachments.
     */
    private final AttachmentCollectionEditor attachments;

    /**
     * Add visit button id.
     */
    private static final String ADD_VISIT = "button.addvisit";

    /**
     * Add problem button id.
     */
    private static final String ADD_PROBLEM = "button.addproblem";

    /**
     * Constructs a {@link ClaimItemCollectionEditor}.
     *
     * @param property    the collection property
     * @param act         the parent act
     * @param customer    the customer
     * @param patient     the patient
     * @param charges     the charges
     * @param attachments the attachments
     * @param context     the layout context
     */
    public ClaimItemCollectionEditor(CollectionProperty property, Act act, Party customer, Party patient,
                                     Charges charges, AttachmentCollectionEditor attachments,
                                     LayoutContext context) {
        super(property, act, new DefaultLayoutContext(context)); // increase layout depth
        this.customer = customer;
        this.patient = patient;
        this.charges = charges;
        this.attachments = attachments;

        // set up a handler to ensure that charge references are removed when deleting a claim item
        setRemoveConfirmationHandler(new AbstractRemoveConfirmationHandler() {
            @Override
            public void apply(IMObject object, IMObjectCollectionEditor collection) {
                IMObjectEditor editor = getEditor(object);
                if (editor instanceof ClaimItemEditor) {
                    ClaimItemCollectionEditor.this.remove((ClaimItemEditor) editor);
                }
                super.apply(object, collection);
            }
        });
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        return new ClaimItemEditor((Act) object, (Act) getObject(), customer, patient, charges, attachments, context);
    }

    /**
     * Enable/disables the buttons.
     * <p>
     * This allows the Add button to be enabled independently of the other buttons.
     * <p>
     * Note that the delete button is enabled if {@link #getCurrentEditor()} or {@link #getSelected()} return non-null.
     *
     * @param buttons   the buttons
     * @param enable    if {@code true}, enable buttons (subject to criteria), otherwise disable them
     * @param enableAdd if {@code true}, enable the add button (subject to criteria), otherwise disable it
     */
    @Override
    protected void enableNavigation(ButtonSet buttons, boolean enable, boolean enableAdd) {
        super.enableNavigation(buttons, enable, enableAdd);
        buttons.setEnabled(ADD_VISIT, enableAdd);
        buttons.setEnabled(ADD_PROBLEM, enableAdd);
    }

    /**
     * Creates the row of controls.
     *
     * @param focus the focus group
     * @return the row of controls
     */
    @Override
    protected ButtonRow createControls(FocusGroup focus) {
        ButtonRow row = super.createControls(focus);
        ButtonSet buttons = row.getButtons();
        Button visit = buttons.create(ADD_VISIT, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onAddVisit();
            }
        });
        buttons.add(visit, 1);
        Button problem = buttons.create(ADD_PROBLEM, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onAddProblem();
            }
        });
        buttons.add(problem, 2);
        return row;
    }


    /**
     * Invoked when the 'Add Visit' is pressed. Displays a browser to select a visit.
     */
    protected void onAddVisit() {
        LayoutContext context = getContext();
        PatientHistoryQuery query = new PatientHistoryQuery(patient, context.getPreferences());
        PatientHistoryBrowser browser = new PatientHistoryBrowser(query, context);
        selectFromBrowser(browser, PatientArchetypes.CLINICAL_EVENT, this::addVisit);
    }

    /**
     * Invoke when the 'Add Problem' is pressed. Displays a browser to select a problem.
     */
    protected void onAddProblem() {
        LayoutContext context = getContext();
        ProblemQuery query = new ProblemQuery(patient, context.getPreferences());
        ProblemBrowser browser = new ProblemBrowser(query, context);
        selectFromBrowser(browser, PatientArchetypes.CLINICAL_PROBLEM, this::addProblem);
    }

    /**
     * Adds a visit.
     */
    protected void addVisit(Act visit) {
        Act object = (Act) create();
        if (object != null) {
            object.setActivityStartTime(visit.getActivityStartTime());
            object.setActivityEndTime(visit.getActivityEndTime());
            IMObjectBean bean = new IMObjectBean(visit);
            addAttachments(bean);
            String description = visit.getTitle();
            if (StringUtils.isEmpty(description)) {
                description = ServiceHelper.getLookupService().getName(visit, "reason");
            }
            object.setDescription(description);
            IMObjectEditor editor = getEditor(object);
            if (editor instanceof ClaimItemEditor) {
                ClaimItemEditor claimItemEditor = (ClaimItemEditor) editor;
                for (Act charge : bean.getTargets("chargeItems", Act.class)) {
                    claimItemEditor.addCharge(charge);
                }
            }
        }
        add(object);
        refresh();
        setSelected(object);
        edit(object);
    }

    /**
     * Adds a problem.
     * <p>
     * This creates a new claim item and:
     * <ul>
     * <li>sets the treatment dates to that of the problem</li>
     * <li>sets the diagnosis</li>
     * <li>sets the description to the presenting complaint</li>
     * <li>sets the status from the problem</li>
     * <li>adds any patient documents</li>
     * <li>adds all charges from the associated visits</li>
     * </ul>
     *
     * @param problem the problem to add
     */
    protected void addProblem(Act problem) {
        Act object = (Act) create();
        if (object != null) {
            object.setActivityStartTime(problem.getActivityStartTime());
            object.setActivityEndTime(problem.getActivityEndTime());
            IMObjectBean bean = new IMObjectBean(problem);
            Lookup diagnosis = bean.getLookup("reason");
            if (diagnosis != null) {
                object.setReason(diagnosis.getCode());
            }
            Lookup presentingComplaint = bean.getLookup("presentingComplaint");
            if (presentingComplaint != null) {
                object.setDescription(presentingComplaint.getName());
            }

            object.setStatus(problem.getStatus());
            addAttachments(bean);
            add(object);
            IMObjectEditor editor = getEditor(object);
            if (editor instanceof ClaimItemEditor) {
                ClaimItemEditor claimItemEditor = (ClaimItemEditor) editor;
                for (Act event : bean.getSources("events", Act.class)) {
                    IMObjectBean eventBean = new IMObjectBean(event);
                    for (Act charge : eventBean.getTargets("chargeItems", Act.class)) {
                        claimItemEditor.addCharge(charge);
                    }
                }
            }
            refresh();
            setSelected(object);
            edit(object);
        }
    }

    /**
     * Removes charges associated with a claim.
     *
     * @param editor the claim editor
     */
    protected void remove(ClaimItemEditor editor) {
        for (Act charge : editor.getCharges()) {
            charges.remove(charge);
        }
    }

    /**
     * Selects an act of the specified archetype from a browser, notifying the supplied consumer on selection.
     *
     * @param browser   the browser
     * @param archetype the type of act to select
     * @param consumer  the consumer to notify
     */
    private void selectFromBrowser(Browser<Act> browser, String archetype, Consumer<Act> consumer) {
        String title = Messages.format("imobject.select.title",
                                       DescriptorHelper.getDisplayName(archetype));
        BrowserDialog<Act> dialog = new BrowserDialog<Act>(title, BrowserDialog.OK_CANCEL, browser,
                                                           getContext().getHelpContext()) {
            @Override
            public boolean isSelected() {
                return TypeHelper.isA(getSelected(), archetype);
            }
        };
        dialog.setCloseOnSelection(false);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                consumer.accept(dialog.getSelected());
            }
        });
        dialog.show();
    }

    /**
     * Adds attachments to the claim that have been linked from a visit or problem.
     *
     * @param bean a bean wrapping the visit/problem
     */
    private void addAttachments(IMObjectBean bean) {
        for (IMObjectReference ref : bean.getNodeTargetObjectRefs("items")) {
            if (TypeHelper.isA(ref, PatientArchetypes.DOCUMENT_FORM, PatientArchetypes.DOCUMENT_IMAGE,
                               PatientArchetypes.DOCUMENT_LETTER, PatientArchetypes.DOCUMENT_ATTACHMENT,
                               InvestigationArchetypes.PATIENT_INVESTIGATION)) {
                DocumentAct document = (DocumentAct) IMObjectHelper.getObject(ref);
                if (document != null) {
                    attachments.addDocument(document);
                }
            }
        }
    }

}
