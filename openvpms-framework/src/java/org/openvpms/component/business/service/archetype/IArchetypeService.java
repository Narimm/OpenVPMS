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
import java.util.Map;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

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
     * Go and generate the derived values for the specified {@link IMOjbect}, 
     * based on the corresponding {@link ArchetypeDescriptor}
     * 
     * @param object
     *            generate the derived values for this object
     * @throws ArchetypeServiceException
     *            if it cannot derive the values                        
     */
    public void deriveValues(IMObject object);

    /**
     * Derive the value for the {@link NodeDescriptor} with the specified 
     * name
     * 
     * @param object
     *            the object to operate on.
     * @param node
     *            the name of the {@link NodeDescriptor}, which will be used
     *            to derive the value            
     * @param FailedToDeriveValueException
     *            if it cannot derive the value            
     */
    public void deriveValue(IMObject object, String node);
    /**
     * Return all the {@link ArchetypeDescriptor} managed by this service
     * 
     * @return List<ArchetypeDescriptor>
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
     * Retrieve the objects specified by the following {@link ArchetypeQuery}
     * request.
     * 
     * @param query
     *        the archetype query
     * @return IPage<IMObject>
     *            the list of objects meeting request constraints
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public IPage<IMObject> get(ArchetypeQuery query);
    
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
     * @param firstRow
     *            the firstRow to retrieve
     * @param numOfRows
     *            the maximum number of rows to retrieve             
     * @return IPage<IMObject>
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public IPage<IMObject> getByNamedQuery(String name, Map<String, Object>params,
            int firstRow, int numOfRows);
    
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
     * @return List<String>
     *            a list of short names                                                               
     * @throws ArchetypeServiceException
     *            a runtime exception                         
     */
    public List<String> getArchetypeShortNames(String rmName, String entityName,
            String conceptName, boolean primaryOnly);
    
    /**
     * Return all the archetype short names
     * 
     * @return List<String>
     *            a list of short names                                                               
     * @throws ArchetypeServiceException
     *            a runtime exception if it cannot complete the call                        
     */
    public List<String> getArchetypeShortNames();
    
    /**
     * Return all the archetypes which match the specified short name
     * 
     * @param shortName
     *            the short name, which may contain a wildcard character
     * @param primaryOnly
     *            return only the primary archetypes
     * @return List<String>                              
     * @throws ArchetypeServiceException
     *            a runtime exception if it cannot complete the call                        
     */
    public List<String> getArchetypeShortNames(String shortName, boolean primaryOnly);
    
    /**
     * Execute the rule specified by the uri and using the passed in 
     * properties and facts. 
     * 
     * @param ruleUri
     *            the rule uri
     * @param properties
     *            a set of properties that can be used by the rule engine
     * @param facts
     *            a list of facts that are asserted in to the working memory
     * @return List<Object>
     *            a list objects, which maybe an empty list.
     * @throws ArchetypeServiceException
     *            if it cannot execute the specified rule                                                
     */
    public List<Object> executeRule(String ruleUri, Map<String, Object> props,
            List<Object> facts);
}
