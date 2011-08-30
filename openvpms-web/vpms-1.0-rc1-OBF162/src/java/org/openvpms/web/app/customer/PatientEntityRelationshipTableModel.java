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
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipTableModel;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Table model for entity relationships to patients that indicates if the
 * patient is deceased.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientEntityRelationshipTableModel
        extends EntityRelationshipTableModel {

    /**
     * The patient rules.
     */
    private final PatientRules rules;


    /**
     * Constructs a new <tt>PatientEntityRelationshipTableModel</tt>.
     *
     * @param context layout context
     */
    public PatientEntityRelationshipTableModel(LayoutContext context) {
        super(context);
        rules = new PatientRules();
    }

    /**
     * Returns the description of an entity.
     *
     * @param entity the entity. May be <tt>null</tt>
     * @return the entity description
     */
    @Override
    protected Object getDescription(IMObject entity) {
        Object result;
        if (TypeHelper.isA(entity, "party.patientpet")
                && rules.isDeceased((Party) entity)) {
            String description = entity.getDescription();
            Label label = LabelFactory.create();
            label.setText(description);
            Label deceased = LabelFactory.create("patient.deceased",
                                                 "Patient.Deceased");
            result = RowFactory.create("CellSpacing", label, deceased);
        } else {
            result = super.getDescription(entity);
        }
        return result;
    }
}