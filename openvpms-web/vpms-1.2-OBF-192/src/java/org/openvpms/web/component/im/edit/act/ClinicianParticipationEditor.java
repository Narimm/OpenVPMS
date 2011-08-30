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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for clinicians.
 * This updates the context with the selected clinician.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-24 01:44:28Z $
 */
public class ClinicianParticipationEditor
        extends AbstractParticipationEditor<User> {

    /**
     * Constructs a new <tt>ClinicianParticipationEditor</tt>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <tt>null</tt>
     */
    public ClinicianParticipationEditor(Participation participation,
                                        Act parent,
                                        LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.clinician")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
        if (participation.getEntity() == null && parent.isNew()) {
            User clinician = getLayoutContext().getContext().getClinician();
            getEditor().setObject(clinician);
        }
    }

    /*
    * Creates a new object reference editor.
    *
    * @param property the reference property
    * @return a new object reference editor
    */
    @Override
    protected IMObjectReferenceEditor<User> createObjectReferenceEditor(
            Property property) {
        return new AbstractIMObjectReferenceEditor<User>(
                property, getParent(), getLayoutContext()) {

            @Override
            protected Query<User> createQuery(String name) {
                Query<User> query = super.createQuery(name);
                addConstraints(query);
                return query;
            }

            @Override
            public void setObject(User object) {
                super.setObject(object);
                getLayoutContext().getContext().setClinician(object);
            }
        };
    }

    /**
     * Adds contraints to the query to restrict it to return users with
     * a 'Clinician' classification.
     *
     * @param query the query
     */
    private void addConstraints(Query query) {
        IConstraint hasClinicianClassification = new ArchetypeNodeConstraint(
                RelationalOp.EQ, "lookup.userType");

        IConstraint isClinician = new NodeConstraint("code", RelationalOp.EQ,
                                                     "CLINICIAN");
        CollectionNodeConstraint constraint
                = new CollectionNodeConstraint("classifications", true);
        constraint.add(hasClinicianClassification);
        constraint.add(isClinician);
        query.setConstraints(constraint);
    }
}

