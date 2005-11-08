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

package org.openvpms.component.business.service.archetype;

// openvpms-framework
import java.util.List;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.AssertionTypeDescriptor;

/**
 * This interface defines the services that are provided by the archetype
 * service. The client is able to return a archetype by name or by archetype
 * idenity.
 * <p>
 * This class depends on the acode implementation of the Java kernel.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IArchetypeService {
    /**
     * Retrieve the {@link ArchetypeDescriptor} for the specified name. The 
     * name is a short name.
     * 
     * @param name
     *            the short name
     * @return ArchetypeDescriptor
     * @thorws ArchetypeServiceException
     *            if there is a problem creating the object.                        
     */
    public ArchetypeDescriptor getArchetypeDescriptor(String name);
    
    /**
     * Retrieve the {@link ArchetypeDescriptor} for the specified 
     * {@link ArchetypeId}.
     * 
     * @param id
     *            the archetype id
     * @return ArchetypeDescriptor           
     * @thorws ArchetypeServiceException
     *            if there is a problem creating the object.                        
     */
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id);
    
    /**
     * Create a default domain object given a short name. The short name is
     * a reference to an {@link ArchetypeRecord}. If the short name is not 
     * known then return a null object.
     * 
     * @param name
     *            the short name
     * @preturn Object
     * @thorws ArchetypeServiceException
     *            if there is a problem creating the object.                        
     */
    public Object create(String name);
    
    /**
     * Create a default domain object given an {@link ArchetypeId}. If the 
     * archetype id is not defined then return null.
     * 
     * @param id
     *            the archetype id
     * @return Object
     * @throws ArchetypeServiceException
     *            if there is a problem creating the object.            
     */
    public Object create(ArchetypeId id);
    
    /**
     * Validate the specified {@link IMObject}. To validate the object it will
     * retrieve the archetype and iterate through the assertions
     * 
     * @param object
     *            the object to validate
     * @throws ValidationException
     *            if there are validation errors                        
     */
    public void validateObject(IMObject object);
    
    /**
     * Return all the {@link ArchetypeDescriptor} managed by this service
     * 
     * @return ArchetypeDescriptor[]
     * @throws ArchetypeServiceException
     *            runtime error, which is thrown if the request cannot be completed
     */
    public ArchetypeDescriptor[] getArchetypeDescriptors(); 

    /**
     * Return the {@link ArchetypeDescriptor} with the specified shortName. If 
     * the short name is a regular expression then it will return all the 
     * matching records.
     * 
     * @param shortName
     *            the short name, which can be a regular expression
     * @return ArchetypeDescriptor[]
     * @throws ArchetypeServiceException            
     */
    public ArchetypeDescriptor[] getArchetypeDescriptors(String shortName);
    
    /**
     * Return all the {@link ArchetypeDescriptor} instance with the specified 
     * reference model name. 
     * 
     * @param rmName
     *            the reference model name
     * @return ArchetypeDescriptor[]
     * @throws ArchetypeServiceException            
     */
    public ArchetypeDescriptor[] getArchetypeDescriptorsByRmName(String rmName);
    
    /**
     * Return the {@link AssertionTypeDescriptor} with the specified name.
     * 
     * @param name
     *            the name of the assertion type
     * @return AssertionTypeDescriptor
     * @throws ArchetypeServiceException            
     */
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name);
    
    /**
     * Return all the {@link AssertionTypeDescriptor} instances supported by this
     * service
     * 
     * @return AssertionTypeDescriptor[]
     */
    public AssertionTypeDescriptor[] getAssertionTypeDescriptors();
    
    /**
     * Remove the specified entity. If the entity cannot be removed for whatever
     * reason then raise a {@link ArchetypeServiceException}.
     * 
     * @param entity
     *            the entity to remove
     * @throws ArchetypeServiceException
     *             a runtime exception
     */

    public void remove(IMObject entity);

    /**
     * Save or upadate the specified enity
     * 
     * @param entity
     *            the entity to insert or update
     * @throws ArchetypeServiceException
     *             a runtime exception
     */
    public void save(IMObject entity);
    
    /**
     * Uses the specified criteria to return zero, one or more matching . 
     * entities. This is a very generic query which will constrain the 
     * returned set on one or more of the supplied values.
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
     * @param rmName
     *            the reference model name (must be complete name)
     * @param entityName
     *            the name of the entity (partial or complete)
     * @param concept
     *            the concept name (partial or complete)
     * @param instanceName
     *            the particular instance name
     * @return List<IMObject>                                   
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public List<IMObject> get(String rmName, String entityName, 
            String conceptName, String instanceName);
    
    
    /**
     * Uses the specified criteria to return zero, one or more matching . 
     * entities. This is a very generic query which will constrain the 
     * returned set on one or more of the supplied values.
     * <p>
     * Each of the parameters can denote an exact match or a partial match. If
     * a partial match is required then the last character of the value must be
     * a '*'. In every other case the search will look for an exact match.
     * <p>
     * All the values are optional. In the case where all the values are null
     * then all the entities will be returned. In the case where two or more 
     * values are specified (i.e. rmName and entityName) then only entities 
     * satisfying both conditions will be returned.
     * <p>
     * If the caller specified primaryOnly flag then it will only process
     * archetypes that are marked as primary
     * 
     * @param rmName
     *            the reference model name (must be complete name)
     * @param entityName
     *            the name of the entity (partial or complete)
     * @param concept
     *            the concept name (partial or complete)
     * @param instanceName
     *            the particular instance name
     * @param primaryOnly
     *            determines whether to restrict processing to archetypes 
     *            that are marked as primary only.            
     * @return List<IMObject>                                   
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public List<IMObject> get(String rmName, String entityName, 
            String conceptName, String instanceName, boolean primaryOnly);
    
    /**
     * Retrieve a list of IMObjects that match one or more of the supplied
     * short names. The short names are specified as an array of strings.
     * <p>
     * The short names must be valid short names without wild card characters.
     * 
     * @param shortNames
     *            an array of short names
     * @return List<IMObject>                                   
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public List<IMObject> get(String[] shortNames);
    
    /**
     * Return the entity with the specified identity or null if it does not
     * exist
     * 
     * @param archId
     *            the archetype id
     * @param id
     *            the entities identity
     * @return IMObject
     *            the entity object
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public IMObject getById(ArchetypeId archId, long id);
    
    /**
     * Return a list of archtype short names (i.e strings) given the 
     * nominated criteria
     * 
     * @param rmName
     *            the reference model name
     * @param entityName
     *            the entity name
     * @param conceptName
     *            the concept name
     * @param primaryOnly
     *            indicates whether to return primary objects only.                                                     
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public List<String> getArchetypeShortNames(String rmName, String entityName,
            String conceptName, boolean primaryOnly);
}
