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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeNodeConstraint;
import org.openvpms.component.system.common.query.ArchetypeProperty;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Participation editor for products.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ProductParticipationEditor extends AbstractParticipationEditor {

    /**
     * The patient, used to constrain searches to a particular species. Nay be
     * <code>null</code>.
     */
    private Property _patient;


    /**
     * Construct a new <code>PatientParticipationEditor</code>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <code>null</code>
     */
    public ProductParticipationEditor(Participation participation,
                                      Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.product")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
    }

    /**
     * Sets the patient, used to constrain product searches to a set of
     * species.
     *
     * @param patient the patient. May be <code>null</code>
     */
    public void setPatient(Property patient) {
        _patient = patient;
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
            protected Query<IMObject> createQuery(String name) {
                Query<IMObject> query = super.createQuery(name);
                return getQuery(query);
            }
        };
    }

    /**
     * Creates a query to select objects.
     *
     * @return a new query
     */
    protected Query<IMObject> getQuery(Query<IMObject> query) {
        if (_patient != null) {
            IMObjectReference ref = (IMObjectReference) _patient.getValue();
            if (ref != null) {
                IMObject patient = IMObjectHelper.getObject(ref);
                if (patient != null) {
                    String species = (String) IMObjectHelper.getValue(patient,
                                                                      "species");
                    if (species != null) {
                        IConstraint constraint = getSpeciesConstraint(species);
                        query.setConstraints(constraint);
                        query.setDistinct(true);
                    }
                }
            }
        }
        return query;
    }

    /**
     * Return a query contraint that restricts products to those that are
     * associated with a particular species, or have no species classification.
     *
     * @param species the species to resttict products to
     * @return a new constraint
     */
    private IConstraint getSpeciesConstraint(String species) {
        IConstraint noClassification = new ArchetypeNodeConstraint(
                ArchetypeProperty.ConceptName, RelationalOp.IsNULL);

        IConstraint isActive = new OrConstraint()
                .add(new NodeConstraint("active", RelationalOp.EQ, true))
                .add(new NodeConstraint("active", RelationalOp.IsNULL));

        IConstraint hasSpeciesClassification = new ArchetypeNodeConstraint(
                ArchetypeProperty.ConceptName, RelationalOp.EQ, "species");

        IConstraint isSpecies = new NodeConstraint("name", RelationalOp.EQ,
                                                   species);
        IConstraint isTargetSpecies = new AndConstraint()
                .add(hasSpeciesClassification).add(isSpecies);

        IConstraint hasNoSpeciesClassification = new ArchetypeNodeConstraint(
                ArchetypeProperty.ConceptName, RelationalOp.NE, "species");

        // @todo revisit on completion of OBF-40
        CollectionNodeConstraint constraint
                = new CollectionNodeConstraint("species", false);
        constraint.setJoinType(CollectionNodeConstraint.JoinType.LeftOuterJoin);
        constraint.add(isActive);
        constraint.add(new OrConstraint()
                .add(noClassification)
                .add(isTargetSpecies)
                .add(hasNoSpeciesClassification));
        return constraint;
    }
}
