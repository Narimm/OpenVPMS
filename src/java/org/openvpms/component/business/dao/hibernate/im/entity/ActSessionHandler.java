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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.hibernate.ObjectDeletedException;
import org.hibernate.Session;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Implementation of {@link IMObjectSessionHandler} for {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ActSessionHandler extends AbstractIMObjectSessionHandler {

    /**
     * Default session handler for updating child objects.
     */
    private final IMObjectSessionHandler defaultHandler;


    /**
     * Creates a new <tt>ActSessionHandler<tt>.
     *
     * @param dao the DAO
     */
    public ActSessionHandler(IMObjectDAO dao) {
        super(dao);
        defaultHandler = new DefaultIMObjectSessionHandler(dao);
    }

    /**
     * Saves an object.
     *
     * @param object     the object to save
     * @param session    the session to use
     * @param newObjects used to collect new objects encountered during save
     * @return the result of <tt>Session.merge(object)</tt>
     */
    @Override
    public IMObject save(IMObject object, Session session,
                         Set<IMObject> newObjects) {
        Act act = (Act) object;
        saveMerge(object, act.getActRelationships(), session, newObjects);
        saveNew(object, act.getParticipations(), session, newObjects);
        return super.save(object, session, newObjects);
    }

    /**
     * Updates the target object with the identifier and version of the source.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    @Override
    public void updateIds(IMObject target, IMObject source) {
        Act targetAct = (Act) target;
        Act sourceAct = (Act) source;
        update(targetAct.getActRelationships(), sourceAct.getActRelationships(),
               defaultHandler);
        update(targetAct.getParticipations(), sourceAct.getParticipations(),
               defaultHandler);
        super.updateIds(target, source);
    }

    /**
     * Deletes an object.
     * <p/>
     * This implementation deletes any target act where there is a parent-child
     * relationship, and deletes the relationships from related acts.
     *
     * @param object  the object to delete
     * @param session the session
     */
    @Override
    public void delete(IMObject object, Session session) {
        Act parent = (Act) object;
        List<IMObject> toDelete = getDependentChildren(parent);
        toDelete.add(parent);
        delete(toDelete, session);
        updateRelatedActs(toDelete, session);
    }

    /**
     * Returns all dependent children of the specified act. These are target
     * acts in relationships whose
     * {@link ActRelationship#isParentChildRelationship()} is <tt>true</tt>.
     *
     * @param act the act
     * @return a list of the child acts
     * @throws ArchetypeServiceException for any error
     */
    private List<IMObject> getDependentChildren(Act act) {
        Set<IMObjectReference> references = new HashSet<IMObjectReference>();
        List<IMObject> targets = new ArrayList<IMObject>();
        references.add(act.getObjectReference());
        getDependentChildren(act, references, targets);
        return targets;
    }

    /**
     * Recursively finds all dependent children of the specified act. These are
     * target acts in relationships whose
     * {@link ActRelationship#isParentChildRelationship()} is <tt>true</tt>.
     *
     * @param act        the act
     * @param references references to acts that have been retrieved/attempted
     *                   to be retrieved
     * @param acts       the acts that have been retrieved
     * @throws ArchetypeServiceException for any error
     */
    private void getDependentChildren(Act act,
                                      Set<IMObjectReference> references,
                                      List<IMObject> acts) {
        Act target;
        for (ActRelationship relationhip : act.getSourceActRelationships()) {
            if (relationhip.isParentChildRelationship()) {
                IMObjectReference targetRef = relationhip.getTarget();
                if (targetRef != null && !references.contains(targetRef)) {
                    references.add(targetRef);
                    target = (Act) get(targetRef);
                    if (target != null) {
                        getDependentChildren(target, references, acts);
                        acts.add(target);
                    }
                }
            }
        }
    }

    /**
     * Removes any relationships to deleted acts.
     *
     * @param deleted the delete acts
     * @param session the session
     */
    private void updateRelatedActs(List<IMObject> deleted, Session session) {
        Map<IMObjectReference, Act> related
                = new HashMap<IMObjectReference, Act>();
        for (IMObject act : deleted) {
            removeRelationships((Act) act, related);
        }
        Set<IMObject> newObjects = new HashSet<IMObject>();
        for (IMObject object : related.values()) {
            try {
                save(object, session, newObjects);
            } catch (ObjectDeletedException ignore) {
                // object has been deleted elsewhere in the transaction
            }
        }
    }

    /**
     * Removes relationships to the specified act from related acts.
     *
     * @param act     the deleted act
     * @param related a cache of the related acts. On completion, this will
     *                contain all related acts
     */
    private void removeRelationships(Act act,
                                     Map<IMObjectReference, Act> related) {
        for (ActRelationship relationship : act.getSourceActRelationships()) {
            if (!relationship.isParentChildRelationship()) {
                removeRelationship(relationship.getTarget(), relationship,
                                   related);
            }
        }
        for (ActRelationship relationship : act.getTargetActRelationships()) {
            removeRelationship(relationship.getSource(), relationship,
                               related);
        }
    }

    /**
     * Removes the relationship from the act identified by <tt>ref</tt>,
     * if the act exists. If the act is not present in the cache, it will be
     * retrieved and added.
     *
     * @param ref          the reference to the act. May be <tt>null</tt>
     * @param relationship the relationship to remove
     * @param relatedCache the cache of related acts
     */
    private void removeRelationship(IMObjectReference ref,
                                    ActRelationship relationship,
                                    Map<IMObjectReference, Act> relatedCache) {
        if (ref != null) {
            Act related = relatedCache.get(ref);
            if (related == null) {
                related = (Act) get(ref);
                if (related != null) {
                    relatedCache.put(ref, related);
                }
            }
            if (related != null) {
                related.removeActRelationship(relationship);
            }
        }
    }

}
