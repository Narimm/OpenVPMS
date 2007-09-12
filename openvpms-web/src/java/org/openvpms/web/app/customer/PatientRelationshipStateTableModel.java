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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer;

import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.relationship.RelationshipStateTableModel;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Table model for entity relationships to patients that indicates if the
 * patient is deceased.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PatientRelationshipStateTableModel extends RelationshipStateTableModel {

    /**
     * Constructs a new <tt>PatientRelationshipStateTableModel</tt>.
     *
     * @param context       layout context
     * @param displayTarget if <tt>true</tt> display the relationship target,
     *                      otherwise display the source
     */
    public PatientRelationshipStateTableModel(LayoutContext context,
                                              boolean displayTarget) {
        super(context, displayTarget);
    }

    /**
     * Returns the description of the source or target entity of the
     * relationship, depending on the {@link #displayTarget} flag.
     *
     * @param state the relationship
     * @return the source or target description
     */
    @Override
    protected Object getDescription(RelationshipState state) {
        Object result;
        PatientRelationshipState p = (PatientRelationshipState) state;
        IMObjectReference ref = (displayTarget()) ? state.getTarget()
                : state.getSource();
        if (TypeHelper.isA(ref, "party.patientpet") && p.isDeceased()) {
            String desc = (displayTarget()) ? state.getTargetDescription()
                    : state.getSourceDescription();
            Label label = LabelFactory.create();
            label.setText(desc);
            Label deceased = LabelFactory.create("patient.deceased",
                                                 "Patient.Deceased");
            result = RowFactory.create("CellSpacing", label, deceased);
        } else {
            result = super.getDescription(state);
        }
        return result;
    }
}