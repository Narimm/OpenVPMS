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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * A helper class, wrapping frequently used queries.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class ArchetypeQueryHelper {

    /**
     * Return a list of {@link Act}s given the following constraints
     *
     * @param service       a reference ot the archetype service
     * @param ref           the reference of the entity to search for {mandatory}
     * @param pShortName    the name of the participation short name
     * @param entityName    the act entityName, which can be wildcarded (optional}
     * @param aConceptName  the act concept name, which can be wildcarded  (optional)
     * @param startTimeFrom the activity from  start time for the act(optional)
     * @param startTimeThru the activity thru from  start time for the act(optional)
     * @param endTimeFrom   the activity from end time for the act (optional)
     * @param endTimeThru   the activity thru end time for the act (optional)
     * @param status        a particular act status
     * @param activeOnly    only areturn acts that are active
     * @param firstResult   the first result to return
     * @param maxResults    the number of results to return
     * @return IPage<Act>
     * @throws ArchetypeServiceException if there is a problem executing the service request
     */
    public static IPage getActs(IArchetypeService service,
                                IMObjectReference ref,
                                String pShortName, String entityName,
                                String aConceptName,
                                Date startTimeFrom, Date startTimeThru,
                                Date endTimeFrom,
                                Date endTimeThru, String status,
                                boolean activeOnly, int firstResult,
                                int maxResults) {
        ArchetypeQuery query = new ArchetypeQuery(entityName,
                                                  aConceptName,
                                                  false, activeOnly)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .setCountResults(true);

        // process the status
        if (!StringUtils.isEmpty(status)) {
            query.add(new NodeConstraint("status", RelationalOp.EQ, status));
        }

        // process the start time
        if (startTimeFrom != null || startTimeThru != null) {
            query.add(new NodeConstraint("startTime", RelationalOp.BTW,
                                         startTimeFrom, startTimeThru));
        }

        // process the end time
        if (endTimeFrom != null || endTimeThru != null) {
            query.add(new NodeConstraint("endTime", RelationalOp.BTW,
                                         endTimeFrom, endTimeThru));
        }

        CollectionNodeConstraint participations = new CollectionNodeConstraint(
                "participations", pShortName, false, activeOnly)
                .add(new ObjectRefNodeConstraint("entity", ref));
        query.add(participations);

        return service.get(query);
    }

    /**
     * Return a list of {@link Participation} given the following constraints.
     *
     * @param service       a reference ot the archetype service
     * @param ref           the ref of the entity to search for {mandatory}
     * @param shortName     the participation short name, which can be wildcarded  (optional)
     * @param startTimeFrom the participation from start time for the act(optional)
     * @param startTimeThru the participation thru start time for the act(optional)
     * @param endTimeFrom   the participation from end time for the act (optional)
     * @param endTimeThru   the participation thru end time for the act (optional)
     * @param activeOnly    only return participations that are active
     * @param firstResult   the first result to return
     * @param maxResults    the number of results to return
     * @return IPage<Participation>
     * @throws ArchetypeServiceException if there is a problem executing the service request
     */
    public static IPage getParticipations(IArchetypeService service,
                                          IMObjectReference ref,
                                          String shortName, Date startTimeFrom,
                                          Date startTimeThru, Date endTimeFrom,
                                          Date endTimeThru,
                                          boolean activeOnly, int firstResult,
                                          int maxResults) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, activeOnly)
                .add(new ObjectRefNodeConstraint("entity", ref))
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .setCountResults(true);

        // process the start time
        if (startTimeFrom != null || startTimeThru != null) {
            query.add(new NodeConstraint("startTime", RelationalOp.BTW,
                                         startTimeFrom, startTimeThru));
        }

        // process the end time
        if (endTimeFrom != null || endTimeThru != null) {
            query.add(new NodeConstraint("endTime", RelationalOp.BTW,
                                         endTimeFrom, endTimeThru));
        }

        return service.get(query);
    }

    /**
     * Return a list of {@link Act} instances for the specified constraints.
     *
     * @param service       a reference to the archetype service
     * @param entityName    the act entityName, which can be wildcarded (optional}
     * @param conceptName   the act concept name, which can be wildcarded  (optional)
     * @param startTimeFrom the activity from  start time for the act(optional)
     * @param startTimeThru the activity thru from  start time for the act(optional)
     * @param endTimeFrom   the activity from end time for the act (optional)
     * @param endTimeThru   the activity thru end time for the act (optional)
     * @param status        a particular act status
     * @param activeOnly    only areturn acts that are active
     * @param firstResult   the first result to retrieve
     * @param maxResults    the no. of results to retrieve
     * @return IPage<Act>
     * @throws ArchetypeServiceException if there is a problem executing the service request
     */
    public static IPage getActs(IArchetypeService service, String entityName,
                                String conceptName, Date startTimeFrom,
                                Date startTimeThru,
                                Date endTimeFrom, Date endTimeThru,
                                String status, boolean activeOnly,
                                int firstResult, int maxResults) {
        ArchetypeQuery query = new ArchetypeQuery(entityName, conceptName,
                                                  false, activeOnly)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .setCountResults(true);

        // process the status
        if (!StringUtils.isEmpty(status)) {
            query.add(new NodeConstraint("status", RelationalOp.EQ, status));
        }

        // process the start time
        if (startTimeFrom != null || startTimeThru != null) {
            query.add(new NodeConstraint("startTime", RelationalOp.BTW,
                                         startTimeFrom, startTimeThru));
        }

        // process the end time
        if (endTimeFrom != null || endTimeThru != null) {
            query.add(new NodeConstraint("endTime", RelationalOp.BTW,
                                         endTimeFrom, endTimeThru));
        }


        return service.get(query);
    }

    /**
     * Uses the specified criteria to return zero, one or more matching .
     * entities. This is a very generic query which will constrain the
     * result set to one or more of the supplied values.
     * <p>
     * Each of the parameters can denote an exact match or a partial match. If
     * a partial match is required then the last character of the value must be
     * a '*'. In every other case the search will look for an exact match.
     * <p>
     * All the values are optional. In the case where all the values are null
     * then all the entities will be returned. In the case where two or more
     * values are specified (i.e. rmName and entityName) then only entities
     * satisfying both conditions will be returned.
     *
     * @param service      a reference to the archetype service
     * @param entityName   the name of the entity (partial or complete)
     * @param conceptName  the concept name (partial or complete)
     * @param instanceName the particular instance name
     * @param activeOnly   whether to retrieve only the active objects
     * @param firstResult  the first result to retrieve
     * @param maxResults   the no. of results to retrieve
     * @return IPage<IMObject>
     * @throws ArchetypeServiceException if there is a problem executing the
     *                                   service request
     */
    public static IPage<IMObject> get(IArchetypeService service,
                                      String entityName, String conceptName,
                                      String instanceName,
                                      boolean activeOnly, int firstResult,
                                      int maxResults) {
        ArchetypeQuery query = new ArchetypeQuery(entityName, conceptName,
                                                  false, activeOnly)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .setCountResults(true);

        // add the instance name constraint, if specified
        if (!StringUtils.isEmpty(instanceName)) {
            query.add(new NodeConstraint("name", instanceName));
        }

        return service.get(query);
    }


    /**
     * Retrieve a list of IMObjects that match one or more of the supplied
     * short names. The short names are specified as an array of strings.
     * <p>
     * The short names must be valid short names without wild card characters.
     *
     * @param service     a reference to the archetype service
     * @param shortNames  an array of short names
     * @param firstResult the first result to retrieve
     * @param maxResults  the no. of results to retrieve
     * @return IPage<IMObject>
     * @throws ArchetypeServiceException a runtime exception
     */
    public static IPage<IMObject> get(IArchetypeService service,
                                      String[] shortNames,
                                      boolean activeOnly, int firstResult,
                                      int maxResults) {
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, activeOnly)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .setCountResults(true);
        return service.get(query);
    }

    /**
     * Returns a list of candidates for a node. This applies to
     * collection nodes that aren't a parent/child relationship
     * (i.e {@link NodeDescriptor#isParentChild()} returns <tt>false</tt>).
     *
     * @param service    the archetype service
     * @param descriptor the node descriptor
     * @return a list of candidates, or an empty list if the node doesn't have
     * a parent/child relationship
     */
    public static List<IMObject> getCandidates(IArchetypeService service,
                                               NodeDescriptor descriptor) {
        if (!descriptor.isParentChild()) {
            String[] shortNames = descriptor.getArchetypeRange();
            if (shortNames.length > 0) {
                IPage<IMObject> page = get(service, shortNames, true, 0,
                                           ArchetypeQuery.ALL_RESULTS);
                return page.getResults();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns the name of an object, given its reference.
     *
     * @param reference the object reference. May be {@code null}
     * @return the name or {@code null} if none exists
     */
    public static String getName(Reference reference, IArchetypeService service) {
        String result = null;
        if (reference != null) {
            ObjectRefConstraint constraint = new ObjectRefConstraint("o", reference);
            ArchetypeQuery query = new ArchetypeQuery(constraint);
            query.add(new NodeSelectConstraint("o.name"));
            query.setMaxResults(1);
            Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service, query);
            if (iter.hasNext()) {
                ObjectSet set = iter.next();
                result = set.getString("o.name");
            }
        }
        return result;
    }

}
