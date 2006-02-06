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

package org.openvpms.component.business.dao.im.lookup;

// openvpms-domain
import java.util.List;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;

/**
 * This interface provides data access object (DAO) support for objects of 
 * type {@link Lookup} and {@link LookupRelationship}. The class includes the 
 * capability to perform insert, delete, update and remove data.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface ILookupDAO {
    /**
     * Insert the specified {@link Lookup}.
     * 
     * @param lookup
     *            the lookup to insert
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public void insert(Lookup lookup);

    /**
     * Update the specified {@link Lookup}
     * 
     * @param lookup
     *            the lookup to update
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public void update(Lookup lookup);

    /**
     * Save the specified {@link Lookup}. This should be used in preference to
     * {@link #insert(Lookup)} and {@link #update(Lookup)}.
     * 
     * @param lookup
     *            the lookup to save
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public void save(Lookup lookup);
    
    /**
     * Delete the specified {@link Lookup}
     * 
     * @param lookup
     *            the lookup to delete
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public void delete(Lookup lookup);

    /**
     * Inseert the specified {@link LookupRelationship}
     * 
     * @param relationship
     *            the lookup relatioship to create
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public void insert(LookupRelationship relationship);
    
    /**
     * Delete the specified {@link LookupRelationship}
     * 
     * @param relationship
     *            the lookup relatioship to delete
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public void delete(LookupRelationship relationship);
    
    /**
     * Retrieve all tjhe {@link Lookup} of the specified concept
     * 
     * @param concept
     *            the lookup concept
     * @return List<Lookup>
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public List<Lookup> getLookupsByConcept(String concept);
    
    /**
     * Retrieve all the lookup instances that matches the specified search 
     * criteria. This is a very generic method that provides a mechanism to 
     * return entities based on, one or more criteria.
     * <p>
     * All parameters are optional and can either denote an exact or partial
     * match semantics. If a parameter ends with '*' then it will do a partial
     * match and return all lookyps that start with the specified value. 
     * Alternatively, if a parameter does not end with a '*' then it will only
     * return entities with the exact value. 
     * <p>
     * If two or more parameters are specified then it will return lookups
     * that match all criteria.
     * 
     * @param rmName
     *            the reference model name
     * @param entityName
     *            the entity name
     * @param conceptName
     *            the concept name
     * @param instanceName
     *            the instance name                                    
     * @return List
     *            a list of Lookup instances
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public List get(String rmName, String entityName, String conceptName, 
            String instanceName);
    
    /**
     * Retrieve all the {@link Lookup} instances that are of the specified type
     * (i.e. archetypeId} and are the target of the a relationship with the 
     * nominated source
     * 
     * @param type
     *            the relationship type
     * @param source
     *            the source lookup
     * @return List<Lookup>            
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public List<Lookup> getTargetLookups(String type, Lookup source);
    
    /**
     * Retrieve all the {@link Lookup} instances that are of the specified type
     * (i.e. archetypeId} and are the target of the a relationship with the 
     * nominated source. In this case the source is a String rather than a 
     * {@link Lookup} object.
     * 
     * @param type
     *            the relationship type
     * @param source
     *            the source value
     * @return List<Lookup>            
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public List<Lookup> getTargetLookups(String type, String source);
    
    /**
     * Retrieve all the {@link Lookup} instances that are of the specified type
     * (i.e. archetypeId} and are the source of the a relationship with the 
     * nominated target
     * 
     * @param type
     *            the relationship type
     * @param target
     *            the target lookup
     * @return List<Lookup>            
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public List<Lookup> getSourceLookups(String type, Lookup target);
    
    /**
     * Retrieve all the {@link Lookup} instances that are of the specified type
     * (i.e. archetypeId} and are the source of the a relationship with the 
     * nominated target. In this case the source is a String rather than a 
     * {@link Lookup} object.
     * 
     * @param type
     *            the relationship type
     * @param target
     *            the target value
     * @return List<Lookup>            
     * @throws LookupDAOException
     *             a runtime exception if the request cannot complete
     */
    public List<Lookup> getSourceLookups(String type, String target);
}
