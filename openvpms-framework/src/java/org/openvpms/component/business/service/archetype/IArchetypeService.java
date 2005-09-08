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
import org.openvpms.component.business.domain.im.IMObject;

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
     * Retrieve the {@link ArchetypeRecord} for the specified name. The name is 
     * a short name.
     * 
     * @param name
     *            the short name
     * @return ArchetypeRecord
     * @thorws ArchetypeServiceException
     *            if there is a problem creating the object.                        
     */
    public ArchetypeRecord getArchetypeRecord(String name);
    
    /**
     * Retrieve the {@link ArchetypeRecord} for the specified {@link ArchetypeId}.
     * 
     * @param id
     *            the archetype id
     * @return ArchetypeRecord           
     * @thorws ArchetypeServiceException
     *            if there is a problem creating the object.                        
     */
    public ArchetypeRecord getArchetypeRecord(ArchetypeId id);
    
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
}
