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

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRelationshipRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.web.component.im.relationship.RelationshipStateFactory;
import org.openvpms.web.component.im.relationship.RelationshipStateQuery;

import java.util.List;


/**
 * A {@link RelationshipStateQuery} for patients that marks deceased patients inactive.
 *
 * @author Tim Anderson
 */
public class PatientRelationshipStateQuery extends RelationshipStateQuery {

    /**
     * The relationship state factory.
     */
    private static final RelationshipStateFactory FACTORY = new PatientRelationshipStateFactory();


    /**
     * Constructs a {@link PatientRelationshipStateQuery}.
     *
     * @param entity                 the parent entity
     * @param relationships          the relationships
     * @param relationshipShortNames the relationship short names
     */
    public PatientRelationshipStateQuery(Entity entity, List<IMObject> relationships, String[] relationshipShortNames) {
        this(entity, relationships, relationshipShortNames, FACTORY);
    }

    /**
     * Constructs a {@link PatientRelationshipStateQuery}.
     *
     * @param entity                 the parent entity
     * @param relationships          the relationships
     * @param relationshipShortNames the relationship short names
     * @param factory                the factory
     */
    protected PatientRelationshipStateQuery(Entity entity, List<IMObject> relationships, String[] relationshipShortNames,
                                            RelationshipStateFactory factory) {
        super(entity, relationships, relationshipShortNames, factory);
    }

    /**
     * Creates a new archetype query returning the relationship id,
     * and the name and active nodes for the secondary object.
     *
     * @return a new archetype query
     */
    @Override
    protected IArchetypeQuery createQuery() {
        if (TypeHelper.isA(getParent(), PatientArchetypes.PATIENT)) {
            return super.createQuery();
        }
        // pull in the relationship details and the patient deceased flag
        return PatientRelationshipRules.createPatientRelationshipQuery((Party) getParent(),
                                                                       getRelationshipShortNames());
    }

    /**
     * Returns the query alias for the secondary (i.e non parent) object.
     *
     * @return the alias
     */
    @Override
    protected String getSecondaryAlias() {
        return !TypeHelper.isA(getParent(), PatientArchetypes.PATIENT) ? "patient" : super.getSecondaryAlias();
    }
}
