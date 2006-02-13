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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;

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
     * Retrieve the {@link ArchetypeDescriptor} with the specified short name. 
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
     * @preturn IMObject
     * @thorws ArchetypeServiceException
     *            if there is a problem creating the object.                        
     */
    public IMObject create(String name);
    
    /**
     * Create a default domain object given an {@link ArchetypeId}. If the 
     * archetype id is not defined then return null.
     * 
     * @param id
     *            the archetype id
     * @return IMObject
     * @throws ArchetypeServiceException
     *            if there is a problem creating the object.            
     */
    public IMObject create(ArchetypeId id);
    
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
    public List<ArchetypeDescriptor> getArchetypeDescriptors(); 

    /**
     * Return all the {@link ArchetypeDescriptor} instances that match the
     * specified shortName. The short name can be a regular expression.
     * 
     * @param shortName
     *            the short name, which can be a regular expression
     * @return ArchetypeDescriptor[]
     * @throws ArchetypeServiceException            
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName);
    
    /**
     * Return all the {@link ArchetypeDescriptor} instance with the specified 
     * reference model name. 
     * 
     * @param rmName
     *            the reference model name
     * @return List<ArchetypeDescriptor>
     * @throws ArchetypeServiceException            
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(String rmName);
    
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
     * @return List<AssertionTypeDescriptor>
     */
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors();
    
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
     *            if the service cannot save the specified entity
     * @throws ValidationException
     *            if the specified entity cannot be validated                        
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
     * @param activeOnly
     *            whether to retrieve only the active objects            
     * @return List<IMObject>                                   
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public List<IMObject> get(String rmName, String entityName, 
            String conceptName, String instanceName, boolean activeOnly);
    
    
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
     * @param activeOnly
     *            whether to retrieve only the active objects            
     * @return List<IMObject>                                   
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public List<IMObject> get(String rmName, String entityName, 
            String conceptName, String instanceName, boolean primaryOnly,
            boolean activeOnly);
    
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
    public List<IMObject> get(String[] shortNames, boolean activeOnly);
    
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
     * Retrun a list of {@link IMObject} instances that satisfy the specified
     * named query. The query name must map to a valid query in the target 
     * database. That params are key-value pairs that are required for the 
     * query to execute correctly
     * 
     * @param name
     *            the query name
     * @param params
     *            a map holding key value pairs.
     * @return List<IMObject>
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public List<IMObject> getByNamedQuery(String name, Map<String, Object>params);
    
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
    
    /**
     * Retrieve the {@link IMObject} given an {@link IMObjectReference}
     * 
     * @param reference
     *            a valid reference
     * @return IMObject
     *            the object or null if it cannot be resolved.
     * @throws ArchetypeServiceException            
     */
    public IMObject get(IMObjectReference reference);
    
    /**
     * Retrieve all the {@link IMObject} that satisfiy the following search
     * criteria. 
     * 
     * @param shortNames
     *            an array of archetype short names
     * @param instanceName
     *            the instance name, which may contain wildcard 
     * @param primaryOnly
     *            constrain the search to primary archetypes
     * @param activeOnly
     *            constrain the search to active only.
     * @throws ArchetypeServiceException                                                                     
     */
    public List<IMObject> get(String[] shortNames, String instanceName,
            boolean primaryOnly, boolean activeOnly);

    /**
     * Return a list of {@link Act} for the specfied {@link Entity}. The list
     * will be further filtered by entityName, conceptName, startTime, 
     * endTime and status.
     * <p>
     * For some parameters you can specifiy the '*' wildcard character, which 
     * can appear at the start or end of the parameter value
     * 
     * @param entityUid
     *            the id of the entity to search for {mandatory}
     * @param pConceptName
     *            the participaton concept name (optional)            
     * @param entityName
     *            the act entityName, which can be wildcarded (optional}
     * @param aConceptName
     *            the act concept name, which can be wildcarded  (optional)
     * @param startTimeFrom
     *            the activity from  start time for the act(optional)
     * @param startTimeThru
     *            the activity thru from  start time for the act(optional)
     * @param endTimeFrom
     *            the activity from end time for the act (optional)
     * @param endTimeThru
     *            the activity thru end time for the act (optional)
     * @param status
     *            a particular act status
     * @param activeOnly 
     *            only areturn acts that are active
     * @param List<Act>            
     * @param EntityServiceException
     *            if there is a problem executing the service request                                                                                  
     */
    public List<Act> getActs(long entityUid, String pConceptName, String entityName, 
            String aConceptName, Date startTimeFrom, Date startTimeThru, Date endTimeFrom, 
            Date endTimeThru, String status, boolean activeOnly);
    
    /**
     * Return a list of {@link Participation} instances for the specified 
     * entity. The list will be constrained by psrticipation concept name,
     * start time and end time, as specfied below.
     * <p>
     * For some parameters you can specifiy the '*' wildcard character, which 
     * can appear at the start or end of the parameter value
     * 
     * @param entityUid
     *            the id of the entity to search for {mandatory}
     * @param conceptName
     *            the participation concept name, which can be wildcarded  (optional)
     * @param startTimeFrom 
     *            the participation from start time for the act(optional)
     * @param startTimeThru 
     *            the participation thru start time for the act(optional)
     * @param endTimeFrom
     *            the participation from end time for the act (optional)
     * @param endTimeThru
     *            the participation thru end time for the act (optional)
     * @param activeOnly 
     *            only return participations that are active
     * @param List<Participation>            
     * @param EntityServiceException
     *            if there is a problem executing the service request                                                                                  
     */
    public List<Participation> getParticipations(long entityUid, String conceptName, 
            Date startTimeFrom, Date startTimeThru, Date endTimeFrom, 
            Date endTimeThru, boolean activeOnly);
    
    /**
     * Return a list of {@link Act} instances} filtered by entityName,
     * conceptName, startTime, endTime and status as described below.
     * <p>
     * For some parameters you can specifiy the '*' wildcard character, which 
     * can appear at the start or end of the parameter value
     * 
     * @param entityName
     *            the act entityName, which can be wildcarded (optional}
     * @param conceptName
     *            the act concept name, which can be wildcarded  (optional)
     * @param startTimeFrom
     *            the activity from  start time for the act(optional)
     * @param startTimeThru
     *            the activity thru from  start time for the act(optional)
     * @param endTimeFrom
     *            the activity from end time for the act (optional)
     * @param endTimeThru
     *            the activity thru end time for the act (optional)
     * @param status
     *            a particular act status
     * @param activeOnly 
     *            only areturn acts that are active
     * @param List<Act>            
     * @param EntityServiceException
     *            if there is a problem executing the service request                                                                                  
     */
    public List<Act> getActs(String entityName, String conceptName, Date startTimeFrom, 
            Date startTimeThru, Date endTimeFrom, Date endTimeThru, 
            String status, boolean activeOnly);
}
