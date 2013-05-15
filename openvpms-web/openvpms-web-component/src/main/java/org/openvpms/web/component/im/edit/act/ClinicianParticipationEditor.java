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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

/**
 * Clinician participation editor.
 *
 * @author Tim Anderson
 */
package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeNodeConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for clinicians.
 * This updates the context with the selected clinician.
 *
 * @author Tim Anderson
 */
public class ClinicianParticipationEditor extends ParticipationEditor<User> {

    /**
     * Constructs a {@code ClinicianParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be {@code null}
     */
    public ClinicianParticipationEditor(Participation participation,
                                        Act parent,
                                        LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.clinician")) {
            throw new IllegalArgumentException("Invalid participation type:"
                                               + participation.getArchetypeId().getShortName());
        }
        if (participation.getEntity() == null && parent.isNew()) {
            User clinician = getLayoutContext().getContext().getClinician();
            setEntity(clinician);
        }
    }

    /*
    * Creates a new object reference editor.
    *
    * @param property the reference property
    * @return a new object reference editor
    */
    @Override
    protected IMObjectReferenceEditor<User> createEntityEditor(Property property) {
        LayoutContext context = getLayoutContext();
        LayoutContext subContext = new DefaultLayoutContext(context, context.getHelpContext().topic("clinician"));
        return new AbstractIMObjectReferenceEditor<User>(property, getParent(), subContext) {

            @Override
            protected Query<User> createQuery(String name) {
                Query<User> query = super.createQuery(name);
                addConstraints(query);
                return query;
            }

            @Override
            public boolean setObject(User object) {
                getLayoutContext().getContext().setClinician(object);
                return super.setObject(object);
            }
        };
    }

    /**
     * Adds constraints to the query to restrict it to return users with a 'Clinician' classification.
     *
     * @param query the query
     */
    private void addConstraints(Query query) {
        IConstraint hasClinicianClassification = new ArchetypeNodeConstraint(RelationalOp.EQ, "lookup.userType");

        IConstraint isClinician = new NodeConstraint("code", RelationalOp.EQ, "CLINICIAN");
        CollectionNodeConstraint constraint = new CollectionNodeConstraint("classifications", true);
        constraint.add(hasClinicianClassification);
        constraint.add(isClinician);
        query.setConstraints(constraint);
    }
}

