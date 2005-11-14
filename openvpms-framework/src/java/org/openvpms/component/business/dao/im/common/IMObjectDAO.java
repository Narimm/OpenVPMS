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

// java
import java.util.List;
import java.util.Map;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;

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
     * Delete the specified {@link IMOBject}
     * 
     * @param object
     *            the imobject to delete
     * @throws IMObjectDAOException
     *             a runtime exception if the request cannot complete
     */
    public void delete(IMObject object);

    /**
     * Retrieve all the objects that matches the specified search criteria.
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
     * @param return
     *            List<IMObject>
     * @throws IMObjectDAOException
     *             a runtime exception if the request cannot complete
     */
    public List<IMObject> get(String rmName, String entityName, String conceptName, 
            String instanceName, String clazz);
    
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
     * Execute the specified named query using the specified parameter
     * list.
     * 
     * @param name
     *            the name of the query
     * @param param
     *            a map of param name and param value.
     * @return List
     *            the result set
     * @throws IMObjectDAOException
     *            if there is an error processing the request                                   
     */
    public List<IMObject> getByNamedQuery(String name, Map<String, Object> params);
}
