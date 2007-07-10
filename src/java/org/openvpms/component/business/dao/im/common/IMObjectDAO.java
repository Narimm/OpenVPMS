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
import org.openvpms.component.system.common.query.IPage;

import java.util.Collection;
import java.util.Map;


/**
 * This interface provides data access object (DAO) support for objects of
 * type {@link IMObject}, which is the most generic type of object in the model.
 * This class should be able to save, delete and retrieve any type of object. It
 * will use the ArchetypeId and in particular the entity name to map the request
 * to the appropriate table. To achieve this there needs to be a one-to-one
 * mapping between entity name and the associated table name. (i.e if the
 * entity is address then it will look at the address table etc
 * <p/>
 * TODO Use annotation to derive this information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IMObjectDAO {
    /**
     * This method can be used to do a insert or an update of the object.
     *
     * @param object the imobject to save
     * @throws IMObjectDAOException a runtime exception if the request cannot complete
     */
    void save(IMObject object);

    /**
     * This will save an array of {@link IMObject} instances in a single
     * transaction. This is an all or nothing proposition.
     *
     * @param objects a collection of objects
     * @throws IMObjectDAOException a runtime exception if the request cannot complete
     */
    void save(Collection objects);

    /**
     * Delete the specified {@link IMObject}
     *
     * @param object the imobject to delete
     * @throws IMObjectDAOException a runtime exception if the request cannot complete
     */
    void delete(IMObject object);

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
     * Returns the result collector factory.
     *
     * @return the result collector factory
     * @throws IMObjectDAOException for any error
     */
    ResultCollectorFactory getResultCollectorFactory();

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
     * @throws IMObjectDAOException a runtime exception if the request cannot complete
     */
    IPage<IMObject> get(String shortName, String instanceName, String clazz,
                        boolean activeOnly, int firstResult, int maxResults);

    /**
     * Return an object with the specified uid for the nominated clazz and null
     * if it does not exists
     *
     * @param clazz the clazz of objects to search for
     * @param id    the uid of the object
     * @return IMObject
     * @throws IMObjectDAOException a runtime exception if the request cannot complete
     */
    public IMObject getById(String clazz, long id);

    /**
     * Return an object with the specified linkID for the nominated clazz and
     * null if the associated object does not exist
     *
     * @param clazz  the clazz of objects to search for
     * @param linkId the uid object linkId
     * @return IMObject
     * @throws IMObjectDAOException a runtime exception if the request cannot complete
     */
    public IMObject getByLinkId(String clazz, String linkId);

    /**
     * Execute a get using the specified named query, the query
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

}

