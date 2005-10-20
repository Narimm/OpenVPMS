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


package org.openvpms.component.business.service.archetype.assertion;

// java core
import java.util.Set;

//openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;

/**
 * These assertions are applied to archetype and parts of archetypes. These
 * are all static functions that take an object and property map as arguments
 * and return a boolean as a result.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeAssertions {

    /**
     * Default constructor
     */
    public ArchetypeAssertions() {
    }

    /**
     * This will iterate through the collection for the specified node
     * descriptor and check that each element is in the archetype range.
     * 
     * @param target
     *            the target object
     * @param node
     *            the node descriptor for this assertion
     * @param assertion
     *            the particular assertion                        
     */
    public static boolean objectsMatchArchetypeRange(Object target, 
            NodeDescriptor node, AssertionDescriptor assertion) {
        String[] atypes = node.getArchetypeRange();
        
        if (atypes.length == 0) {
            return true;
        }
        
        if (target instanceof Set) {
            Set entries = (Set)target;
            for (Object entry : entries) {
                if (entry instanceof IMObject) {
                    boolean match = false;
                    IMObject imobj = (IMObject)entry;
                    for (String type : atypes) {
                        if (imobj.getArchetypeId().getShortName().matches(type)) {
                            match = true;
                            break;
                        }
                    }
                    
                    // if there is no match then break
                    if (!match) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
