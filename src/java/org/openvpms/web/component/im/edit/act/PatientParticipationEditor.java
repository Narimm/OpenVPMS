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

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Participation editor for patients. This updates {@link Context#setPatient}
 * when a patient is selected.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientParticipationEditor extends AbstractParticipationEditor {

    /**
     * Construct a new <code>PatientParticipationEditor</code>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <code>null</code>
     */
    protected PatientParticipationEditor(Participation participation,
                                         Act parent,
                                         LayoutContext context) {
        super(participation, parent, context);

        if (participation.isNew() && participation.getEntity() == null) {
            IMObject patient = Context.getInstance().getPatient();
            getObjectReferenceEditor().setObject(patient);
        }
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static PatientParticipationEditor create(IMObject object,
                                                    IMObject parent,
                                                    LayoutContext context) {
        PatientParticipationEditor result = null;
        if (object instanceof Participation
            && parent instanceof Act) {
            Participation participation = (Participation) object;
            if (IMObjectHelper.isA(participation, "participation.patient")) {
                result = new PatientParticipationEditor(
                        participation, (Act) parent, context);
            }
        }
        return result;
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
            protected void onSelected(IMObject object) {
                super.onSelected(object);
                Party patient = (Party) object;
                Context.getInstance().setPatient(patient);
            }
        };
    }
}
