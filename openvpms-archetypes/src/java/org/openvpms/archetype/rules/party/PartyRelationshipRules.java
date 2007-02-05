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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.party;

import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanException;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Party Relationship Rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PartyRelationshipRules {

    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(PartyRelationshipRules.class);


    /**
     * Check the relationship records for a Patient and make sure they are valid
     * and only one is active for each relationship type.
     *
     * @param service the archetype service
     * @param party   the party
     * @throws RuleEngineException
     */

    public static void checkPatientRelationships(IArchetypeService service,
                                                 Party party) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Executing PartyRelationship.checkPatientRelationships");
        }
        Map<String, EntityRelationship> currentActives = new HashMap<String, EntityRelationship>();
        EntityRelationship currentActive;

        // Loop through all the patient relationships.
        // If one is new then assume it is the active and set the ActiveEndTime on any others.
        // If more than 1 is new then set active to youngest activeStartTime. 
        // If no new relationship and more than one active relationship of same type then set the one with the youngest 
        // ActiveStartTime as Active. 
        for (EntityRelationship rel : party.getEntityRelationships()) {
            if (rel.getActiveEndTime() == null) {
                String shortname = rel.getArchetypeId().getShortName();
                currentActive = currentActives.get(shortname);
                if (rel.isNew()) {
                    if (currentActive == null)
                        currentActive = rel;
                    else if (currentActive.isNew()) {
                        if (rel.getActiveStartTime().after(
                                currentActive.getActiveStartTime())) {
                            currentActive.setActiveEndTime(new Date(
                                    System.currentTimeMillis() - 1000));
                            currentActive = rel;
                        }
                    } else {
                        currentActive.setActiveEndTime(
                                new Date(System.currentTimeMillis() - 1000));
                        currentActive = rel;
                    }
                } else {
                    if (currentActive == null)
                        currentActive = rel;
                    else if (currentActive.isNew())
                        rel.setActiveEndTime(
                                new Date(System.currentTimeMillis() - 1000));
                    else if (rel.getActiveStartTime().after(
                            currentActive.getActiveStartTime())) {
                        currentActive.setActiveEndTime(
                                new Date(System.currentTimeMillis() - 1000));
                        currentActive = rel;
                    } else if (currentActive.getActiveStartTime().after(
                            rel.getActiveStartTime())) {
                        rel.setActiveEndTime(
                                new Date(System.currentTimeMillis() - 1000));
                    }
                }
                currentActives.put(shortname, currentActive);
            }
        }
    }

    /**
     * Returns the target entity in the first entity relationship for the
     * specified node that has a start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param service the archetype service
     * @param entity  the entity
     * @param node    the entity relationship node
     * @param time    the time
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Entity getTargetEntity(
            IArchetypeService service, Entity entity, String node, Date time) {
        return getEntity(service, entity, node, time,
                         new EntityAccessor(false));
    }

    /**
     * Returns the source entity in the first entity relationship for the
     * specified node that has a start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param service the archetype service
     * @param entity  the entity
     * @param node    the entity relationship node
     * @param time    the time
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Entity getSourceEntity(
            IArchetypeService service, Entity entity, String node, Date time) {
        return getEntity(service, entity, node, time, new EntityAccessor(true));
    }

    /**
     * Returns the entity in the first entity relationship for the
     * specified node that has a start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param service  the archetype service
     * @param entity   the entity
     * @param node     the entity relationship node
     * @param time     the time
     * @param accessor the entity accessor
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    private static Entity getEntity(
            IArchetypeService service, Entity entity, String node, Date time,
            EntityAccessor accessor) {
        IMObjectBean bean = new IMObjectBean(entity, service);
        for (IMObject object : bean.getValues(node)) {
            EntityRelationship relationship = (EntityRelationship) object;
            Date start = relationship.getActiveStartTime();
            if (start instanceof Timestamp) {
                start = new Date(start.getTime());
            }
            if (start != null && start.compareTo(time) <= 0
                    && accessor.getRef(relationship) != null) {
                Date end = relationship.getActiveEndTime();
                if (end == null || end.compareTo(time) >= 0) {
                    return accessor.get(relationship, service);
                }
            }
        }
        return null;
    }

    private static class EntityAccessor {

        private final boolean source;

        public EntityAccessor(boolean source) {
            this.source = source;
        }

        public IMObjectReference getRef(EntityRelationship relationship) {
            return source ? relationship.getSource() : relationship.getTarget();
        }

        public Entity get(EntityRelationship relationship,
                          IArchetypeService service) {
            return (Entity) ArchetypeQueryHelper.getByObjectReference(
                    service, getRef(relationship));
        }
    }
}