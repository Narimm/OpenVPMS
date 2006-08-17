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

package org.openvpms.web.app.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.AbstractParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;


/**
 * Participation editor for appointment types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-15 06:42:15Z $
 */
public class AppointmentTypeParticipationEditor
        extends AbstractParticipationEditor {

    /**
     * The schedule, used to constrain appointment types. Nay be
     * <code>null</code>.
     */
    private Party _schedule;


    /**
     * Construct a new <code>PatientParticipationEditor</code>.
     *
     * @param participation the object to edit
     * @param parent        the parent act
     * @param context       the layout context. May be <code>null</code>
     */
    public AppointmentTypeParticipationEditor(Participation participation,
                                              Act parent,
                                              LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.appointmentType")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
    }

    /**
     * Sets the schedule, used to constrain appointment types.
     *
     * @param schedule the patient. May be <code>null</code>
     */
    public void setSchedule(Party schedule) {
        _schedule = schedule;
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor createObjectReferenceEditor(
            Property property) {
        return new IMObjectReferenceEditor(property, getLayoutContext()) {

            @Override
            protected Query<IMObject> createQuery() {
                return new AppointmentTypeQuery(_schedule);
            }
        };
    }

}
