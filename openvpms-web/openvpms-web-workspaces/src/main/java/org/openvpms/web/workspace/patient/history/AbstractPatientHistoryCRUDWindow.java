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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workspace.AbstractCRUDWindow;
import org.openvpms.web.component.workspace.DocumentActActions;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;
import org.openvpms.web.workspace.patient.PatientRecordCRUDWindow;

import java.util.Arrays;

/**
 * CRUD Window for patient history.
 *
 * @author Tim Anderson
 */
public class AbstractPatientHistoryCRUDWindow extends AbstractCRUDWindow<Act> implements PatientRecordCRUDWindow {

    /**
     * The current <em>act.patientClinicalEvent</em>.
     */
    private Act event;


    /**
     * Determines the operations that can be performed on document acts.
     */
    private final DocumentActActions documentActions = new DocumentActActions();

    /**
     * External edit button identifier.
     */
    private static final String EXTERNAL_EDIT_ID = "button.externaledit";

    /**
     * Constructs a {@link AbstractPatientHistoryCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public AbstractPatientHistoryCRUDWindow(Archetypes<Act> archetypes, IMObjectActions<Act> actions, Context context,
                                            HelpContext help) {
        super(archetypes, actions, context, help);
    }

    /**
     * Sets the current patient clinical event.
     *
     * @param event the current event
     */
    public void setEvent(Act event) {
        this.event = event;
    }

    /**
     * Returns the current patient clinical event.
     *
     * @return the current event. May be {@code null}
     */
    public Act getEvent() {
        return event;
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param object the new object
     */
    @Override
    protected void onCreated(final Act object) {
        if (TypeHelper.isA(object, PatientArchetypes.PATIENT_MEDICATION)) {
            confirmCreation(object, "patient.record.create.medication.title",
                            "patient.record.create.medication.message", "newMedication");
        } else if (TypeHelper.isA(object, InvestigationArchetypes.PATIENT_INVESTIGATION)) {
            confirmCreation(object, "patient.record.create.investigation.title",
                            "patient.record.create.investigation.message", "newInvestigation");
        } else {
            super.onCreated(object);
        }
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(EXTERNAL_EDIT_ID, enable && documentActions.canExternalEdit(getObject()));
    }

    /**
     * Creates a button to externally edit a a document.
     *
     * @return a new button
     */
    protected Button createExternalEditButton() {
        return ButtonFactory.create(EXTERNAL_EDIT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onExternalEdit();
            }
        });
    }

    /**
     * Launches an external editor to edit a document, if external editing of the document is supported.
     */
    protected void onExternalEdit() {
        final DocumentAct act = (DocumentAct) IMObjectHelper.reload(getObject());
        if (act == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        } else if (act.getDocument() != null) {
            documentActions.externalEdit(act);
        } else {
            // the act has no document attached. Try and generate it first.
            DocumentGenerator generator = new DocumentGenerator(
                    act, getContext(), getHelpContext(),
                    new DocumentGenerator.AbstractListener() {
                        @Override
                        public void generated(Document document) {
                            onSaved(act, false);
                            if (documentActions.canExternalEdit(act)) {
                                documentActions.externalEdit(act);
                            }
                        }
                    });
            generator.generate(true, false);
        }
    }

    /**
     * Creates a {@link PatientMedicalRecordLinker} to link medical records.
     *
     * @param event the patient clinical event
     * @param item  the patient record item
     */
    protected PatientMedicalRecordLinker createMedicalRecordLinker(Act event, Act item) {
        return new PatientMedicalRecordLinker(event, item);
    }

    /**
     * Creates a {@link PatientMedicalRecordLinker} to link medical records.
     *
     * @param event   the patient clinical event. May be {@code null}
     * @param problem the patient clinical problem. May be {@code null}
     * @param item    the patient record item. May be {@code null}
     */
    protected PatientMedicalRecordLinker createMedicalRecordLinker(Act event, Act problem, Act item) {
        return new PatientMedicalRecordLinker(event, problem, item);
    }

    /**
     * Creates a new event, making it the current event.
     */
    protected void createEvent() {
        Act event = (Act) IMObjectCreator.create(PatientArchetypes.CLINICAL_EVENT);
        if (event == null) {
            throw new IllegalStateException("Failed to create " + PatientArchetypes.CLINICAL_EVENT);
        }
        LayoutContext layoutContext = createLayoutContext(getHelpContext());
        IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(event, layoutContext);
        editor.getComponent();
        if (editor instanceof AbstractActEditor) {
            ((AbstractActEditor) editor).setStatus(ActStatus.COMPLETED);
        }
        editor.save();
        setEvent(event);
    }

    /**
     * Helper to concatenate the short names for the target of a relationship with those supplied.
     *
     * @param relationship the relationship archetype short name
     * @param shortNames   the short names to add
     * @return the archetype shortnames
     */
    protected String[] getShortNames(String relationship, String... shortNames) {
        String[] targets = RelationshipHelper.getTargetShortNames(relationship);
        String[] result = Arrays.copyOf(targets, targets.length + shortNames.length);
        System.arraycopy(shortNames, 0, result, targets.length, shortNames.length);
        return result;
    }

    /**
     * Confirms creation of an object.
     *
     * @param object  the object
     * @param title   the dialog title key
     * @param message the dialog message key
     * @param help    the help key
     */
    private void confirmCreation(final Act object, String title, String message, String help) {
        ConfirmationDialog dialog = new ConfirmationDialog(Messages.get(title), Messages.get(message),
                                                           getHelpContext().subtopic(help));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                AbstractPatientHistoryCRUDWindow.super.onCreated(object);
            }
        });
        dialog.show();
    }

}
