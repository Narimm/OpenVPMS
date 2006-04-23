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
import java.util.ArrayList;
import java.util.List;

// openvpms-framework
import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * This class provides a list of helper functions for using the 
 * archetype service.
 * <p>
 * A reference to the {@link IArchetypeService} is used during 
 * construction. The reference is cached in a class attribute.
 * <p>
 * These functions can then be used as JXPath extension functions.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeServiceFunctions {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceFunctions.class);
    
    /**
     * A reference to the archetype service
     */
    private static IArchetypeService archetypeService;
    
    
    /**
     * Construct an instance give a reference to the archetype service
     * 
     * @param service
     *            the archetype service
     */
    public ArchetypeServiceFunctions(IArchetypeService service) {
        archetypeService = service;
    }
    
    /**
     * This will take a list of {@link IMObjectReference} instances and return
     * a list of the corresponding {@link IMObject} instances
     * 
     * @param references
     *            a list of references
     * @return List<IMObject>
     */
    public static List<IMObject> resolveRefs(List<IMObjectReference> references) {
        List<IMObject> objects = new ArrayList<IMObject>();
        for (IMObjectReference ref : references) {
            objects.add(ArchetypeQueryHelper.getByObjectReference(
                    archetypeService, ref));
        }

        return objects;
    }
    
    /**
     * Return the {@link IMObject} given a reference
     * 
     * @param reference
     *            the reference
     * @return IMObject            
     */
    public static IMObject resolve(IMObjectReference reference) {
        return ArchetypeQueryHelper.getByObjectReference(archetypeService, reference);
    }
}
