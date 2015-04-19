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

package org.openvpms.component.business.dao.im.common;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collection;
import java.util.Map;


/**
 * This interface provides data access object (DAO) support for objects of
 * type {@link IMObject}, which is the most generic type of object in the model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IMObjectDAO {

    /**
     * Saves an object.
     *
     * @param object the object to save
     * @throws IMObjectDAOException if the request cannot complete
     */
    void save(IMObject object);

    /**
     * Saves a collection of objects in a single transaction.
     *
     * @param objects the objects to save
     * @throws IMObjectDAOException if the request cannot complete
     */
    void save(Collection<? extends IMObject> objects);

    /**
     * Deletes an {@link IMObject}.
     *
     * @param object the object to delete
     * @throws IMObjectDAOException if the request cannot complete
     */
    void delete(IMObject object);

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws IMObjectDAOException for any error
     */
    public IPage<IMObject> get(IArchetypeQuery query);

    /**
     * Retrieves partially populated objects that match the query.
     * This may be used to selectively load parts of object graphs to improve
     * performance.
     * <p/>
     * All simple properties of the returned objects are populated - the
     * <code>nodes</code> argument is used to specify which collection nodes to
     * populate. If empty, no collections will be loaded, and the behaviour of
     * accessing them is undefined.
     *
     * @param query the archetype query
     * @param nodes the collection node names
     * @return a page of objects that match the query criteria
     * @throws IMObjectDAOException for any error
     */
    IPage<IMObject> get(IArchetypeQuery query, Collection<String> nodes);

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws IMObjectDAOException for any error
     */
    IPage<ObjectSet> getObjects(IArchetypeQuery query);

    /**
     * Retrieves the nodes from the objects that match the query criteria.
     *
     * @param query the archetype query
     * @param nodes the node names
     * @return the nodes for each object that matches the query criteria
     * @throws IMObjectDAOException for any error
     */
    IPage<NodeSet> getNodes(IArchetypeQuery query, Collection<String> nodes);

    /**
     * Execute a get using the specified query string, the query
     * parameters and the result collector. The first result and the number of
     * results is used to control the paging of the result set.
     *
     * @param queryString the query string
     * @param parameters  the query parameters
     * @param collector   the result collector
     * @param firstResult the first result to retrieve
     * @param maxResults  the maximum number of results to return
     * @param count       if <code>true</code> counts the total no. of results,
     *                    returning it in {@link IPage#getTotalResults()}
     * @throws IMObjectDAOException for any error
     */
    void get(String queryString, Map<String, Object> parameters,
             ResultCollector collector, int firstResult, int maxResults,
             boolean count);

    /**
     * Retrieves the objects that match the specified search criteria.
     * This is a very generic method that provides a mechanism to return
     * objects based on, one or more criteria.
     * <p/>
     * All parameters are optional and can either denote an exact or partial
     * match semantics. If a parameter has a '*' at the start or end of the
     * value then it will perform a wildcard match.  If not '*' is specified in
     * the value then it will only return objects with the exact value.
     * <p/>
     * If two or more parameters are specified then it will return entities
     * that matching all criteria.
     * <p/>
     * The results will be returned in a {@link Page} object, which may contain
     * a subset of the total result set. The caller can then use the context
     * information in the {@link Page} object to make subsequent calls.
     *
     * @param rmName       the reference model name
     * @param entityName   the entity name
     * @param conceptName  the concept name
     * @param instanceName the instance name
     * @param clazz        the fully qualified name of the class to search for
     * @param activeOnly   indicates whether to return active objects.
     * @param firstResult  the first result to retrieve
     * @param maxResults   the maximum number of results to return
     * @return IPage<IMObject>
     *         the results and associated context information
     * @throws IMObjectDAOException a runtime exception if the request cannot
     *                              complete
     * @deprecated replaced by {@link #get(String, String, String, boolean,
     *             int, int)}
     */
    @Deprecated
    IPage<IMObject> get(String rmName, String entityName, String conceptName,
                        String instanceName, String clazz, boolean activeOnly,
                        int firstResult, int maxResults);

    /**
     * Retrieve the objects that matches the specified search criteria.
     * This is a very generic method that provides a mechanism to return
     * objects based on, one or more criteria.
     * <p/>
     * All parameters are optional and can either denote an exact or partial
     * match semantics. If a parameter has a '*' at the start or end of the
     * value then it will perform a wildcard match.  If not '*' is specified in
     * the value then it will only return objects with the exact value.
     * <p/>
     * If two or more parameters are specified then it will return entities
     * that matching all criteria.
     * <p/>
     * The results will be returned in a {@link Page} object, which may contain
     * a subset of the total result set. The caller can then use the context
     * information in the {@link Page} object to make subsequent calls.
     *
     * @param shortName    the archetype short name
     * @param instanceName the instance name
     * @param clazz        the fully qualified name of the class to search for
     * @param activeOnly   indicates whether to return active objects.
     * @param firstResult  the first result to retrieve
     * @param maxResults   the maximum number of results to return
     * @return a page of the results
     * @throws IMObjectDAOException if the request cannot complete
     */
    IPage<IMObject> get(String shortName, String instanceName, String clazz,
                        boolean activeOnly, int firstResult, int maxResults);

    /**
     * Returns an object with the specified reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none exists
     * @throws IMObjectDAOException for any error
     */
    IMObject get(IMObjectReference reference);

    /**
     * Executes a get using the specified named query, the query
     * parameters and the result collector. The first result and the number of
     * results is used to control the paging of the result set.
     *
     * @param query       the query name
     * @param parameters  the query parameters
     * @param collector   the result collector
     * @param firstResult the first result to retrieve
     * @param maxResults  the maximum number of results to return
     * @param count       if <code>true</code> counts the total no. of results,
     *                    returning it in {@link IPage#getTotalResults()}
     * @throws IMObjectDAOException for any error
     */
    void getByNamedQuery(String query, Map<String, Object> parameters,
                         ResultCollector collector, int firstResult,
                         int maxResults, boolean count);

    /**
     * Replaces the uses of one lookup with another.
     *
     * @param source the lookup to replace
     * @param target the lookup to replace <tt>source</tt> it with
     */
    void replace(Lookup source, Lookup target);
}

