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

package org.openvpms.component.business.service.lookup;

// openvpms-framework
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;


/**
 * This service interface, provides standard CRUD (create, retrieve, update and 
 * delete) functionality for lookup and lookup relationship
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface ILookupService {
    /**
     * Create an lookup using the specified shortName. The short name is a 
     * reference to the archetype that is used to create the object. An archetype 
     * restricts the instances of the domain class by declaring constraints on 
     * it.
     * <p>
     * The returned object is a default (but not necessarily valid) representation
     * of the archetype. The returned object is transient
     * 
     * @param shortName
     *            the short name to the archetype
     * @return Lookup
     * @throws LookupServiceException
     *             a runtime exception
     * 
     */
    public Lookup create(String shortName);

    /**
     * Insert the specified {@link Lookup}. This service is now responsible for
     * managing the lookup. The lookup is validated before it is inserted.
     * 
     * @param lookup
     *            the lookup to insert
     * @throws LookupServiceException                      
     */
    public void insert(Lookup lookup);
    
    /**
     * Remove the specified lookup and associated lookup relationships. If the
     * the lookup cannot be deleted then throw a {@link LookupServiceException}.
     * 
     * @param lookup
     *            the lookup to remove
     * @throws LookupServiceException
     *             a runtime exception
     */
    public void remove(Lookup lookup);

    /**
     * Update the specified lookup. The lookup is validated against its
     * archetype before it is updated. If the lookup is invalid or it cannot be 
     * updated the {@link LookupServiceException} exception is thrown.
     * 
     * @param lookup
     *            the lookup to update
     * @throws LookupServiceException
     *             a runtime exception
     */
    public void update(Lookup lookup);

    /**
     * Save the specified {@link Lookup} instance. This method should be used
     * instesd of the deprecated {@link #update(Lookup)} or {@link #insert(Lookup)}.
     * <p>
     * If there is a problem executing this request the runtime exception 
     * {@link LookupServiceException} is thrown.
     * 
     * @param lookup
     *            the lookup to save
     * @throws LookupServiceException            
     */
    public void save(Lookup lookup);
    
    /**
     * Add a lookup relationship between two lookup entities.
     * 
     * TODO This maybe a bit too fine grained.
     * 
     * @param relationship
     *            the relationship to add
     * @throws LookupServiceException                        
     */
    public void add(LookupRelationship relationship);
    
    /**
     * Remove the specified lookup relationship
     * 
     * @param relationship
     *            the relationship to remove
     * @throws LookupServiceException             
     */
    public void remove(LookupRelationship relationship);
    
    /**
     * Retrieve all the lookups with the specified short name
     * 
     * @param shortName
     *            an alias to an archetypeId (i.e type)
     * @return List<Lookup>            
     * @throws LookupServiceException
     *            aa runtime exception            
     */
    public List<Lookup> get(String shortName);


    /**
     * Uses the specified criteria to return zero, one or more matching . 
     * lookups. This is a very generic query which will constrain the 
     * returned set on one or more of the supplied values.
     * <p>
     * Each of the parameters can denote an exact match or a partial match. If
     * a partial match is required then the last character of the value must be
     * a '*'. In every other case the search will look for an exact match.
     * <p>
     * All the values are optional. In the case where all the values are null
     * then all the lookup instances will be returned. In the case where two or 
     * more values are specified (i.e. rmName and entityName) then only entities 
     * satisfying the specified conditions will be returned.
     * 
     * @param rmName
     *            the reference model name (partial or complete)
     * @param entityName
     *            the name of the entity
     * @param concept
     *            the concept name
     * @param instanceName
     *            the particular instance name
     * @return List  
     *            a list of Lookup values                                 
     * @throws LookupServiceException
     *            a runtime exception                         
     */
    public List get(String rmName, String entityName, 
            String conceptName, String instanceName);
    
    /**
     * Retrieve all the lookup instances of a particular type, which are the 
     * target of a lookup relationship with the specified source
     * 
     * @param relType
     *            the relationship type
     * @param source
     *            the source of the relationship
     * @return List<Lookup>            
     * @throws LookupServiceException            
     */
    public List<Lookup> getTargetLookups(String relType, Lookup source);
    
    /**
     * Retrieve all the lookup instances of a particular type, which are the
     * target of a lookup relationship with the specified source. In this 
     * case the source is a string so an initial lookup is required to retrieve
     * the source lookup object
     * 
     * @param relType
     *            the relationship type
     * @param source
     *            the source of the relationship
     * @return List<Lookup>            
     * @throws LookupServiceException            
     */
    public List<Lookup> getTargetLookups(String relType, String source);
    
    /**
     * Retrieve all the lookup instance of a particular type, which are the 
     * source of a lookup relationship with the specified target
     * 
     * @param relType
     *            the relationship type
     * @param target
     *            the target of the relationship
     * @return List<Lookup>            
     * @throws LookupServiceException            
     */
    public List<Lookup> getSourceLookups(String relType, Lookup target);
    
    /**
     * Retrieve all the lookup instance of a particular type, which are the 
     * source of a lookup relationship with the specified target. In this 
     * case the source is a string so an initial lookup is required to retrieve
     * the target lookup object
     * 
     * @param relType
     *            the relationship type
     * @param target
     *            the target of the relationship
     * @return List<Lookup>            
     * @throws LookupServiceException            
     */
    public List<Lookup> getSourceLookups(String relType, String target);
    
    /**
     * Return a list of lookups for the specified {@link NodeDescriptor} or
     * an empty list if not applicable
     * 
     * @param descriptor
     *            the node descriptor
     * @return List<Lookup>
     * @throws LookupServiceException                      
     */
    public List<Lookup> get(NodeDescriptor descriptor);
    
    /**
     * Return a list of {@link Lookup} instances given the specified 
     * {@link NodeDescriptor} and {@link IMObject}. 
     * <p>
     * This method should be used if you want to constrain a lookup
     * search based on a source or target relationship.
     * 
     * @param descriptor
     *            the node descriptor
     * @param object
     *            the object to use.                         
     * @return List<Lookup>
     * @throws LookupServiceException                      
     */
    public List<Lookup> get(NodeDescriptor descriptor, IMObject object);
}
