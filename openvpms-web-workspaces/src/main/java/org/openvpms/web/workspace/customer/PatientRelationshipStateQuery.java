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

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.NodeSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.relationship.RelationshipStateFactory;
import org.openvpms.web.component.im.relationship.RelationshipStateQuery;
import org.openvpms.web.system.ServiceHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
    public PatientRelationshipStateQuery(Entity entity, List<IMObject> relationships, String[] relationshipShortNames,
                                         RelationshipStateFactory factory) {
        super(entity, relationships, relationshipShortNames, factory);
    }

    /**
     * Queries all those {@link RelationshipState} instances corresponding to the relationships supplied at
     * construction.
     *
     * @return the matching {@link RelationshipState} instances, keyed on their associated relationships
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Map<IMObjectRelationship, RelationshipState> query() {
        Map<IMObjectRelationship, RelationshipState> result = super.query();
        checkDeceased(result);
        return result;
    }

    /**
     * For each relationship, checks if the patient is deceased. If so, marks
     * them deceased and inactive.
     *
     * @param states the set of relationship states
     */
    private void checkDeceased(Map<IMObjectRelationship, RelationshipState> states) {
        if (TypeHelper.isA(getParent(), "party.patientpet")) {
            // if the entity is the patient, update the states using the its deceased state
            PatientRules rules = ServiceHelper.getBean(PatientRules.class);
            boolean deceased = rules.isDeceased((Party) getParent());
            if (deceased) {
                for (RelationshipState state : states.values()) {
                    PatientRelationshipState pState = (PatientRelationshipState) state;
                    pState.setDeceased(true);
                }
            }
        } else if (!states.isEmpty()) {
            // build a map of patient ids to their corresponding states
            Map<Long, PatientRelationshipState> patients = new HashMap<Long, PatientRelationshipState>();
            for (RelationshipState state : states.values()) {
                long id = (parentIsSource()) ? state.getTargetId() : state.getSourceId();
                patients.put(id, (PatientRelationshipState) state);
            }

            // query each matching patient on id
            ArchetypeQuery query = new ArchetypeQuery(getShortNames(), false, false);
            query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
            List<String> nodes = Arrays.asList("id", "deceased");
            query.add(new NodeConstraint("id", RelationalOp.IN, patients.keySet().toArray()));
            Iterator<NodeSet> iter = new NodeSetQueryIterator(query, nodes);
            while (iter.hasNext()) {
                NodeSet set = iter.next();
                boolean deceased = set.getBoolean("deceased");
                if (deceased) {
                    long id = set.getLong("id");
                    PatientRelationshipState state = patients.get(id);
                    state.setDeceased(true);
                }
            }
        }
    }

}
