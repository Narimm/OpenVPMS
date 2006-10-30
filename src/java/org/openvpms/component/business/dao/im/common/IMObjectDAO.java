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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.query.NodeSet;
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
 * <p>
 * TODO Use annotation to derive this information.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IMObjectDAO {
    /**
     * This method can be used to do a insert or an update of the object. 
     * 
     * @param object
     *            the imobject to save
     * @throws IMObjectDAOException
     *             a runtime exception if the request cannot complete
     */
    public void save(IMObject object);

    /**
     * This will save an array of {@link IMObject} instances in a single 
     * transaction. This is an all or nothing proposition.
     * 
     * @param objects
     *            a collection of objects
     * @throws IMObjectDAOException
     *            a runtime exception if the request cannot complete            
     */
    public void save(Collection objects);

    /**
     * Delete the specified {@link IMObject}
     * 
     * @param object
     *            the imobject to delete
     * @throws IMObjectDAOException
     *             a runtime exception if the request cannot complete
     */
    public void delete(IMObject object);

    /**
     * Execute a get using the specified query string and a map of the
     * values. The first row and ther number of rows is used to control
     * the paging of the result set 
     * 
     * @param queryString
     *            the query string
     * @param valueMap
     *            the values applied to the query
     * @param firstRow
     *            the first row to retrieve
     * @param numOfRows
     *            the maximum number of rows to return            
     * @return IPage<IMObject>
     * @throws IMObjectDAOException
     *            a runtime exception, raised if the request cannot complete.            
     */
    public IPage<IMObject> get(String queryString, Map<String, Object> valueMap,
            int firstRow, int numOfRows);

    /**
     * Execute a get using the specified query string and a map of the values.
     * This retrieves partially populated objects and may be used to selectively
     * load parts of object graphs to improve performance.
     * <p/>
     * All simple properties of the returned objects are populated - the
     * <code>nodes</code> argument is used to specify which collection nodes to
     * populate. If empty, no collections will be loaded, and the behaviour of
     * accessing them is undefined.
     *
     * @param archetypes  the archetype descriptors of the objects being
     *                    queried, keyed on short name
     * @param nodes       the nodes to load
     * @param queryString the query string
     * @param valueMap    the values applied to the query
     * @param firstRow    the first row to retrieve
     * @param numOfRows   the maximum number of rows to return
     * @return the nodes for each object that matches the query criteria
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    IPage<IMObject> get(Map<String, ArchetypeDescriptor> archetypes,
                        Collection<String> nodes, String queryString,
                        Map<String, Object> valueMap, int firstRow,
                        int numOfRows);

    /**
     * Execute a get using the specified query string and a map of the values.
     * The first row and the number of rows is used to control the paging of the
     * result set.
     *
     * @param archetypes  the archetype descriptors of the objects being
     *                    queried, keyed on short name
     * @param queryString the query string
     * @param valueMap    the values applied to the query
     * @param firstRow    the first row to retrieve
     * @param numOfRows   the maximum number of rows to return
     * @return the nodes for each object that matches the query criteria
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    IPage<NodeSet> getNodes(Map<String, ArchetypeDescriptor> archetypes,
                            Collection<String> nodes, String queryString,
                            Map<String, Object> valueMap, int firstRow,
                            int numOfRows);

    /**
     * Retrieve the objects that matches the specified search criteria.
     * This is a very generic method that provides a mechanism to return 
     * objects based on, one or more criteria.
     * <p>
     * All parameters are optional and can either denote an exact or partial
     * match semantics. If a parameter has a '*' at the start or end of the 
     * value then it will perform a wildcard match.  If not '*' is specified in
     * the value then it will only return objects with the exact value.
     * <p>
     * If two or more parameters are specified then it will return entities
     * that matching all criteria.
     * <p>
     * The results will be returned in a {@link Page} object, which may contain
     * a subset of the total result set. The caller can then use the context 
     * information in the {@link Page} object to make subsequent calls.
     * 
     * @param rmName
     *            the reference model name
     * @param entityName
     *            the entity name
     * @param conceptName
     *            the concept name
     * @param instanceName
     *            the instance name   
     * @param clazz
     *            the fully qualified name of the class to search for  
     * @param activeOnly
     *            indicates whether to return active objects.
     * @param firstRow
     *            the first row to retrieve
     * @param numOfRows
     *            the maximum number of rows to return            
     * @return IPage<IMObject>
     *            the results and associated context information
     * @throws IMObjectDAOException
     *             a runtime exception if the request cannot complete
     */
    public IPage<IMObject> get(String rmName, String entityName, 
            String conceptName, String instanceName, String clazz, 
            boolean activeOnly, int firstRow, int numOfRows);
    
    /**
     * Return an object with the specified uid for the nominated clazz and null 
     * if it does not exists
     * 
     * @param clazz
     *            the clazz of objects to search for
     * @param id
     *            the uid of the object
     * @return IMObject           
     * @throws IMObjectDAOException
     *             a runtime exception if the request cannot complete
     */
    public IMObject getById(String clazz, long id);

    /**
     * Return an object with the specified linkID for the nominated clazz and 
     * null if the associated object does not exist
     * 
     * @param clazz
     *            the clazz of objects to search for
     * @param linkId
     *            the uid object linkId
     * @return IMObject           
     * @throws IMObjectDAOException
     *             a runtime exception if the request cannot complete
     */
    public IMObject getByLinkId(String clazz, String linkId);

    /**
     * Execute the specified named query using the specified parameter
     * list.
     * 
     * @param name
     *            the name of the query
     * @param params
     *            a map of param name and param value.
     * @param firstRow
     *            the first row to retrieve
     * @param numOfRows
     *            the maximum number of rows to return            
     * @return IPage<IMObject>
     *            the results and associated context information
     * @throws IMObjectDAOException
     *            if there is an error processing the request                                   
     */
    public IPage<IMObject> getByNamedQuery(String name, Map<String, Object> params,
            int firstRow, int numOfRows);
}

