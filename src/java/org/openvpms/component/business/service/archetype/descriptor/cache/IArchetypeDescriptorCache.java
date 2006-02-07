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


package org.openvpms.component.business.service.archetype.descriptor.cache;

// java core
import java.util.List;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;

/**
 * This interface is used for accessing {@link ArchetypeDescriptor}.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public interface IArchetypeDescriptorCache {
    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified  short name. 
     * If there are multiple archetype descriptors with the same name then it 
     * will retrieve the first descriptor marked with latest=true.
     * 
     * @param name
     *            the short name
     * @return ArchetypeDescriptor
     *            the matching archetype descriptor or null.
     * @throws ArchetypeDescriptorCacheException
     *            if there is a problem completing the request.                        
     */
    public ArchetypeDescriptor getArchetypeDescriptor(String name);
    
    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified 
     * {@link ArchetypeId}.
     * 
     * @param id
     *            the archetype id
     * @return ArchetypeDescriptor    
     *            the matching archetype descriptor or null.
     * @throws ArchetypeDescriptorCacheException
     *            if there is a problem completing the request.                        
     */
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id);

    /**
     * Return all the {@link ArchetypeDescriptor} instances managed by this 
     * cache.
     * 
     * @return List<ArchetypeDescriptor>
     * @throws ArchetypeDescriptorCacheException
     *            if there is a problem completing the request.                        
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors(); 

    /**
     * Return all the {@link ArchetypeDescriptor} instances that match the
     * specified shortName. The short name can be a regular expression.
     * 
     * @param shortName
     *            the short name, which can be a regular expression
     * @return List<ArchetypeDescriptor>
     * @throws ArchetypeDescriptorCacheException
     *            if there is a problem completing the request.                        
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName);
    
    /**
     * Return all the {@link ArchetypeDescriptor} instance with the specified 
     * reference model name. 
     * 
     * @param rmName
     *            the reference model name
     * @return List<ArchetypeDescriptor>
     * @throws ArchetypeDescriptorCacheException
     *            if there is a problem completing the request.                        
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(String rmName);
    
    /**
     * Return the {@link AssertionTypeDescriptor} with the specified name.
     * 
     * @param name
     *            the name of the assertion type
     * @return AssertionTypeDescriptor
     * @throws ArchetypeDescriptorCacheException
     *            if there is a problem completing the request.                        
     */
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name);
    
    /**
     * Return all the {@link AssertionTypeDescriptor} instances supported by 
     * this cache
     * 
     * @return List<AssertionTypeDescriptor>
     * @throws ArchetypeDescriptorCacheException
     *            if there is a problem completing the request.                        
     */
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors();
    
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
     * @throws ArchetypeDescriptorCacheException
     *            if there is a problem completing the request.                        
     */
    public List<String> getArchetypeShortNames(String rmName, String entityName,
            String conceptName, boolean primaryOnly);

    /**
     * Add the specified archetype descriptor to the cache. If the replace
     * flag is specified and the archetype descriptor exists, then replace the
     * existing archetype descriptor with the new one.
     * 
     * @param adesc
     *            the archetype descriptor to add
     * @param replace
     *            indicates whether it should replace and existing version.
     * 
     */
    public void addArchetypeDescriptor(ArchetypeDescriptor adesc, boolean b);
    
    /**
     * Return all the archetype short names
     * 
     * @return List<String>
     */
    public List<String> getArchetypeShortNames();
}
