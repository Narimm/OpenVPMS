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


package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A helper class, wrapping frequently used queries.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeQueryHelper {

    /**
     * Return the object with the specified archId and uid.
     *
     * @param service the archetype service
     * @param archId  the archetype id of the object to retrieve
     * @param uid     the uid of the object
     * @return the object of null if one does not exist
     */
    public static IMObject getByUid(IArchetypeService service,
                                    ArchetypeId archId, long uid) {
        ArchetypeQuery query = new ArchetypeQuery(archId)
                .add(new NodeConstraint("uid", RelationalOp.EQ, uid));
        List<IMObject> results = service.get(query).getResults();
        return (!results.isEmpty()) ? results.get(0) : null;
    }

    /**
     * Return the object with the specified {@link IMObjectReference}.
     *
     * @param service   the archetype service
     * @param reference the object reference
     * @return the matching object or null
     */
    public static IMObject getByObjectReference(IArchetypeService service,
                                                IMObjectReference reference) {
        ArchetypeQuery query = new ArchetypeQuery(reference);
        List<IMObject> results = service.get(query).getResults();
        return (!results.isEmpty()) ? results.get(0) : null;
    }

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
        ArchetypeQuery query = new ArchetypeQuery(null, entityName,
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
        ArchetypeQuery query = new ArchetypeQuery(null, entityName, conceptName,
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
     * <p/>
     * Each of the parameters can denote an exact match or a partial match. If
     * a partial match is required then the last character of the value must be
     * a '*'. In every other case the search will look for an exact match.
     * <p/>
     * All the values are optional. In the case where all the values are null
     * then all the entities will be returned. In the case where two or more
     * values are specified (i.e. rmName and entityName) then only entities
     * satisfying both conditions will be returned.
     *
     * @param service      a reference to the archetype service
     * @param rmName       the reference model name (must be complete name)
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
    public static IPage<IMObject> get(IArchetypeService service, String rmName,
                                      String entityName, String conceptName,
                                      String instanceName,
                                      boolean activeOnly, int firstResult,
                                      int maxResults) {
        ArchetypeQuery query = new ArchetypeQuery(rmName, entityName,
                                                  conceptName,
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
     * <p/>
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
     * This is a static method that will return a list of candidate children
     * given an reference to the archetype service, the node descriptor for
     * the node in question and the context object.
     *
     * @param service the archetype service
     * @param ndesc   the node descriptor
     * @param context the context object
     * @return List<IMObject>
     * @deprecated use get(service, ndesc.getArchetypeRange(), true, 0, ArchetypeQuery.ALL_RESULTS)
     */
    @Deprecated
    public static List<IMObject> getCandidateChildren(IArchetypeService service,
                                                      NodeDescriptor ndesc,
                                                      IMObject context) {

        // find the node descriptor
        if (ndesc == null) {
            return null;
        }

        // check that the node is a collection and that the parentChild
        // attribute is set to false
        if (!(ndesc.isCollection()) ||
                (ndesc.isParentChild())) {
            return null;
        }

        // now there are two ways that candidate children cna be specified
        // Firstly they can be specified using the candidateChildren assertion
        // and secondly they can be specified using the archetypeRange
        // assertion
        List<IMObject> children = new ArrayList<IMObject>();
        if (ndesc.containsAssertionType("candidateChildren")) {
            children = ndesc.getCandidateChildren(context);
        } else if (ndesc.containsAssertionType("archetypeRange")) {
            children = get(service,
                           ndesc.getArchetypeRange(), true, 0,
                           ArchetypeQuery.ALL_RESULTS)
                    .getResults();
        }

        return children;
    }
}
