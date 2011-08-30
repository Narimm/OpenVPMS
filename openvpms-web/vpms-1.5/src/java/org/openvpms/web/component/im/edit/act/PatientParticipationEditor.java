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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for patients. This defaults the patient to that
 * contained in the context if the none is selected, and the parent object
 * is new.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientParticipationEditor extends ParticipationEditor<Party> {

    /**
     * Construct a new <tt>PatientParticipationEditor</tt>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param layout        the layout context. May be <tt>null</tt>
     */
    public PatientParticipationEditor(Participation participation,
                                      Act parent,
                                      LayoutContext layout) {
        super(participation, parent, layout);
        if (!TypeHelper.isA(participation, "participation.patient")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
        Context context = getLayoutContext().getContext();
        IMObjectReference patientRef = participation.getEntity();
        if (patientRef == null && parent.isNew()) {
            setEntity(context.getPatient());
        } else {
            // add the existing patient to the context
            Party patient = (Party) IMObjectHelper.getObject(patientRef);
            if (patient != null && patient != context.getPatient()) {
                ContextHelper.setPatient(context, patient);
            }
        }
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Party> createObjectReferenceEditor(
            Property property) {
        return new AbstractIMObjectReferenceEditor<Party>(
                property, getParent(), getLayoutContext(), true) {

            @Override
            public void setObject(Party object) {
                super.setObject(object);
                ContextHelper.setPatient(getLayoutContext().getContext(),
                                         object);
            }
        };
    }

}
