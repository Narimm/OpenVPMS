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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map;

// log4j
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyCollection;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;

/**
 * These assertions are applied to archetype and parts of archetypes. These are
 * all static functions that take an object and property map as arguments and
 * return a boolean as a result.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeAssertions {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeAssertions.class);

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
        String[] atypes = getArchetypeRange(assertion);

        // no children to match
        if (target == null) {
            return true;
        }

        // no children to match
        if (atypes.length == 0) {
            return true;
        }

        // first check to see if the object is of a collection type
        Collection entries = null;
        if (target instanceof Set) {
            entries = (Collection) target;
        } else if (target instanceof Map) {
            entries = (Collection) ((Map) target).values();
        } else if (target instanceof List) {
            entries = (Collection) target;
        } else if (target instanceof PropertyCollection) {
            entries = ((PropertyCollection) target).values();
        }
        
        if (entries != null) {
            // if it is a collection type then process all the objects in
            // the collection
            if (entries.size() > 0) {
                for (Object entry : entries) {
                    if (entry instanceof IMObject) {
                        boolean match = false;
                        IMObject imobj = (IMObject) entry;
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
            }
            return true;
        } else if (target instanceof IMObjectReference) {
            // the target is an IMObjectReference so check the reference.
            IMObjectReference imref = (IMObjectReference) target;
            for (String type : atypes) {
                if (imref.getArchetypeId().getShortName().matches(type)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Retrieve the archetype range given the specified
     * {@link AssertionDescriptor}
     * 
     * @param desc
     *            the assertion descriptor
     * @return String[] an array of archetype short namwa
     */
    private static String[] getArchetypeRange(AssertionDescriptor desc) {
        ArrayList<String> range = new ArrayList<String>();
        PropertyList archetypes = (PropertyList) desc.getPropertyMap()
                .getProperties().get("archetypes");
        for (NamedProperty archetype : archetypes.getProperties()) {
            AssertionProperty shortName = (AssertionProperty) ((PropertyMap) archetype)
                    .getProperties().get("shortName");
            range.add(shortName.getValue());
        }

        return (String[]) range.toArray(new String[range.size()]);
    }
}
