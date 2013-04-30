/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.patient.PatientMedicalRecordLinker;
import org.openvpms.web.app.patient.PatientRecordCRUDWindow;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD Window for patient record acts in 'problem' view.
 *
 * @author Tim Anderson
 */
public class ProblemRecordCRUDWindow extends ActCRUDWindow<Act>
    implements PatientRecordCRUDWindow {

    /**
     * The current act.patientClinicalEvent.
     */
    private Act event;


    /**
     * Constructs a {@code ProblemRecordCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public ProblemRecordCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(PatientArchetypes.CLINICAL_PROBLEM, Act.class,
                                Messages.get("patient.record.createtype")), ProblemActions.INSTANCE, context, help);
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
     * Invoked when the object has been saved.
     *
     * @param act   the act
     * @param isNew determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act act, boolean isNew) {
        Act event = getEvent(act);
        if (event != null) {
            // link the act to the event
            PatientMedicalRecordLinker linker = new PatientMedicalRecordLinker(event, act);
            Retryer.run(linker);
            super.onSaved(act, isNew);
        }
    }

    /**
     * Returns the event associated with an act.
     * <p/>
     * If the act has an associated event, this will be returned, otherwise the {@link #getEvent current event} will
     * be returned.
     *
     * @param act the act
     * @return the associated event, or {@code null} if none is found and there is no current event
     */
    private Act getEvent(Act act) {
        for (ActRelationship relationship : act.getTargetActRelationships()) {
            if (TypeHelper.isA(relationship.getSource(), PatientArchetypes.CLINICAL_EVENT)) {
                return (Act) IMObjectHelper.getObject(relationship.getSource(), getContext());
            }
        }
        return getEvent();
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
