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

// java core

// openvpms-framework

/**
 * This is a helper class for the archetype service.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeServiceHelper {
    
    /**
     * A reference to the archetype service
     */
    private static IArchetypeService archetypeService;
    
    
    /**
     * Instantiate an instance of this class using the specified service
     * 
     * @param service
     *            a reference to the archetype service
     */
    public ArchetypeServiceHelper(IArchetypeService service) {
        archetypeService = service;
    }
    
    /**
     * Return a reference to the {@link IArchetypeService}. If one is not 
     * available then raise an exception
     * 
     * @return IArchetypeService
     * @throws ArchetypeServiceException
     *            if the value is not set
     */
    public static IArchetypeService getArchetypeService()
    throws ArchetypeServiceException {
        if (archetypeService == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.ArchetypeServiceNotSet);
        }
        
        return archetypeService;
    }
}
