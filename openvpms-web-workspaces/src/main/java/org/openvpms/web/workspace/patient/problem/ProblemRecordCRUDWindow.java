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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.problem;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryCRUDWindow;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;


/**
 * CRUD Window for patient record acts in 'problem' view.
 *
 * @author Tim Anderson
 */
public class ProblemRecordCRUDWindow extends AbstractPatientHistoryCRUDWindow {

    /**
     * The current problem.
     */
    private Act problem;


    /**
     * Constructs a {@link ProblemRecordCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public ProblemRecordCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(PatientArchetypes.CLINICAL_PROBLEM, Act.class,
                                Messages.get("patient.record.createtype")), ProblemActions.INSTANCE, context, help);
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Act object) {
        super.setObject(object);
        if (object != null) {
            if (TypeHelper.isA(object, PatientArchetypes.CLINICAL_PROBLEM)) {
                setProblem(object);
            } else {
                setProblem(getSource(object, PatientArchetypes.CLINICAL_PROBLEM));
            }
        }
    }

    /**
     * Sets the current patient clinical problem.
     *
     * @param problem the current problem. May be {@code null}
     */
    public void setProblem(Act problem) {
        this.problem = problem;
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPrintButton());
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
        buttons.setEnabled(PRINT_ID, enable);
    }

    /**
     * Invoked when the 'new' button is pressed.
     *
     * @param archetypes the archetypes
     */
    @Override
    protected void onCreate(Archetypes<Act> archetypes) {
        if (problem != null) {
            // problem is selected, so display all of the possible event item archetypes
            String[] shortNames = getShortNames(PatientArchetypes.CLINICAL_PROBLEM_ITEM,
                                                PatientArchetypes.CLINICAL_PROBLEM);
            archetypes = new Archetypes<Act>(shortNames, archetypes.getType(), PatientArchetypes.CLINICAL_NOTE,
                                             archetypes.getDisplayName());
        }
        super.onCreate(archetypes);
    }

    /**
     * Invoked when a new object has been created.
     * <p/>
     * If the object is an <em>act.patientClinicalProblem</em>, a dialog will be displayed prompting for the visit
     * to link it to.
     *
     * @param object the new object
     */
    @Override
    protected void onCreated(final Act object) {
        if (TypeHelper.isA(object, PatientArchetypes.CLINICAL_PROBLEM)) {
            final Act event = getLatestEvent(getContext().getPatient());
            if (event != null) {
                // there is an existing event for the patient. Prompt to add the problem to this visit, or a new one.
                final VisitSelectionDialog dialog = new VisitSelectionDialog(event.getDescription());
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        if (dialog.createVisit()) {
                            createEvent();
                        } else {
                            setEvent(event);
                        }
                        ProblemRecordCRUDWindow.super.onCreated(object);
                    }
                });
                dialog.show();
            } else {
                // there is an no event for the patient. Prompt to create a new one.
                ConfirmationDialog dialog = new ConfirmationDialog(
                        Messages.get("patient.record.problem.createVisit.title"),
                        Messages.get("patient.record.problem.createVisit.message"));
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        createEvent();
                        ProblemRecordCRUDWindow.super.onCreated(object);
                    }
                });
                dialog.show();
            }
        } else {
            super.onCreated(object);
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param act   the act
     * @param isNew determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act act, boolean isNew) {
        Act problem;
        if (!TypeHelper.isA(act, PatientArchetypes.CLINICAL_PROBLEM)) {
            problem = getProblem(act);
        } else {
            problem = act;
            act = null;
        }
        Act event = getEvent();
        PatientMedicalRecordLinker linker = createMedicalRecordLinker(event, problem, act);
        Retryer.run(linker);
        super.onSaved(act, isNew);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(Act object) {
        if (TypeHelper.isA(object, PatientArchetypes.CLINICAL_PROBLEM)) {
            setProblem(null);
        }
        super.onDeleted(object);
    }

    /**
     * Returns the latest event for a patient.
     *
     * @param patient the patient
     * @return the latest event, or {@code null} if none is found
     */
    private Act getLatestEvent(Party patient) {
        ArchetypeQuery query = new ArchetypeQuery(shortName("e", PatientArchetypes.CLINICAL_EVENT));
        query.add(join("patient").add(eq("entity", patient)));
        query.add(Constraints.sort("startTime", false));
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<Act>(query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }

    /**
     * Returns the problem associated with an act.
     * <p/>
     * If the act has an associated problem, this will be returned, otherwise the current {@link #problem} will
     * be returned.
     *
     * @param act the act
     * @return the associated event, or {@code null} if none is found and there is no current event
     */
    private Act getProblem(Act act) {
        Act result = getSource(act, PatientArchetypes.CLINICAL_PROBLEM);
        return result != null ? result : problem;
    }

    /**
     * Returns the source of an act with the specified short name.
     *
     * @param act       the act
     * @param shortName the archetype short name
     * @return the source, or {@code null} if none exists
     */
    private Act getSource(Act act, String shortName) {
        for (ActRelationship relationship : act.getTargetActRelationships()) {
            if (TypeHelper.isA(relationship.getSource(), shortName)) {
                return (Act) IMObjectHelper.getObject(relationship.getSource(), getContext());
            }
        }
        return null;
    }

    private static class ProblemActions extends ActActions<Act> {

        public static final ProblemActions INSTANCE = new ProblemActions();

        /**
         * Determines if an act can be edited.
         *
         * @param act the act to check
         * @return {@code true}
         */
        @Override
        public boolean canEdit(Act act) {
            // @todo fix when statuses are sorted out
            return true;
        }

        /**
         * Determines if an act can be deleted.
         *
         * @param act the act to check
         * @return {@code true} if the act can be deleted
         */
        @Override
        public boolean canDelete(Act act) {
            return !TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT) && super.canDelete(act)
                   && act.getSourceActRelationships().isEmpty();
        }
    }

    private static class VisitSelectionDialog extends PopupDialog {

        /**
         * If selected, indicates to create a new visit.
         */
        private final RadioButton newVisit;

        /**
         * Constructs a {@link VisitSelectionDialog}.
         *
         * @param visit the existing visit description
         */
        public VisitSelectionDialog(String visit) {
            super(Messages.get("patient.record.problem.selectVisit.title"), OK_CANCEL);
            setModal(true);
            ButtonGroup group = new ButtonGroup();
            ActionListener listener = new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                }
            };
            RadioButton existingVisit = ButtonFactory.create(null, group, listener);
            existingVisit.setText(visit);
            existingVisit.setSelected(true);
            newVisit = ButtonFactory.create("patient.record.problem.selectVisit.new", group, listener);
            existingVisit.setGroup(group);
            newVisit.setGroup(group);
            existingVisit.addActionListener(listener);
            newVisit.addActionListener(listener);
            Label label = LabelFactory.create("patient.record.problem.selectVisit.message");
            Column column = ColumnFactory.create(Styles.CELL_SPACING, label, existingVisit, newVisit);
            getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
        }

        /**
         * Determines if a new visit should be created.
         *
         * @return if {@code true}, create a new visit, otherwise use the existing one
         */
        public boolean createVisit() {
            return newVisit.isSelected();
        }
    }
}
