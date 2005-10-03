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
    public Object createDefaultObject(String name);
    
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
    public Object createDefaultObject(ArchetypeId id);
    
    /**
     * Validate the specified {@link IMObject}. To validate the object it will
     * retrieve the archetype and iterate through the assertions
     * 
     * @param object
     *            the object to validate
     * @return boolean
     *            true if the object is valid; false otherwise
     * @throws ArchetypeServiceException
     *            runtime exception, which is thrown when there is a problem                        
     */
    public boolean validateObject(IMObject object);
    
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
}
