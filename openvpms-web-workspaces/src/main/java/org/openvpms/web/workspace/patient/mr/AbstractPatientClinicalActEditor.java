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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;

import java.util.Date;

/**
 * An editor for <em>act.patientClinicalEvent</em> and <em>act.patientClinicalProblem</em> acts.
 * <p/>
 * This disables editing of "items" nodes.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPatientClinicalActEditor extends ActEditor {

    /**
     * The completed status.
     */
    private final String completedStatus;

    /**
     * Constructs an {@link AbstractPatientClinicalActEditor}.
     *
     * @param act             the act
     * @param parent          the parent. May be {@code null}
     * @param completedStatus the 'completed' status of the act, used to determine the end time when the status changes
     * @param context         the layout context
     */
    public AbstractPatientClinicalActEditor(Act act, IMObject parent, String completedStatus, LayoutContext context) {
        super(act, parent, false, context);
        this.completedStatus = completedStatus;
        initParticipant("patient", context.getContext().getPatient());
        initParticipant("clinician", context.getContext().getClinician());

        addStartEndTimeListeners();

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
    }

    /**
     * Invoked when the status changes. Sets the end time to today if the act is completed, or {@code null} otherwise.
     */
    protected void onStatusChanged() {
        Property status = getProperty("status");
        String value = status.getString();
        Date time = completedStatus.equals(value) ? new Date() : null;
        setEndTime(time, false);
    }
}
