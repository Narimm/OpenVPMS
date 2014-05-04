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

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryCRUDWindow;


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
        buttons.setEnabled(NEW_ID, getEvent() != null);
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
        Act event = getEvent(problem);
        PatientMedicalRecordLinker linker = new PatientMedicalRecordLinker(event, problem, act);
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
     * Returns the event associated with an act.
     * <p/>
     * If the act has an associated event, this will be returned, otherwise the current event will be returned.
     *
     * @param act the act
     * @return the associated event, or {@code null} if none is found and there is no current event
     */
    private Act getEvent(Act act) {
        Act result = getSource(act, PatientArchetypes.CLINICAL_EVENT);
        return result != null ? result : getEvent();
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
    }
}
