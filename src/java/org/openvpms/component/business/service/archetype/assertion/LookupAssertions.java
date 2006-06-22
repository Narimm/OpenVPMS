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

//openvpms-framework
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;

/**
 * This class has static methods for local reference data assertions. All
 * the static methods return boolean and take an object and a property
 * map as parameters.        
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class LookupAssertions {

    /**
     * Default constructor
     */
    public LookupAssertions() {
    }
    
    /**
     * Check that the target object is in the specified list.
     * 
     * @param target
     *            the target object
     * @param node
     *            the node descriptor for this assertion
     * @param assertion
     *            the particular assertion                        
     */
    public static boolean isStringValueInList(Object target, 
            NodeDescriptor node, AssertionDescriptor assertion) {
        PropertyList entries = (PropertyList)assertion.getPropertyMap()
            .getProperties().get("entries");
        if (entries == null) {
            return false;
        }
        
        for (NamedProperty property : entries.getProperties()) {
            AssertionProperty prop = (AssertionProperty)property;
            if (prop.getValue().equals((String)target)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * This  method will be inspect this lookup assertion and determine 
     * whether it should set node value of the supplied target object to 
     * the default lookup type.
     * 
     * @param target
     *            the target object
     * @param node
     *            the node descriptor for this assertion
     * @param assertion
     *            the particular assertion                              
     */ 
    public static void setDefaultValue(Object target, NodeDescriptor node, 
            AssertionDescriptor assertion) {
        
        // only process if it is a lookup and there is no defeault value
        if ((assertion.getName().equals("lookup")) &&
            (StringUtils.isEmpty(node.getDefaultValue()))) {
            String type = (String)assertion.getProperty("type").getValue();
            if ((!StringUtils.isEmpty(type)) &&
                (type.equals("lookup")) &&
                (assertion.getProperty("source") != null)){
                String source = (String)assertion.getProperty("source").getValue();
                if (!StringUtils.isEmpty(source)) {
                    Lookup defaultLookup = LookupHelper.getDefaultLookup(
                            ArchetypeServiceHelper.getArchetypeService(), source);
                    
                    // if a default lookup has identified then set it through the 
                    // node descriptor.
                    if (defaultLookup != null) {
                        node.setValue((IMObject)target, defaultLookup.getValue());
                    }
                }
            }
        }
    }
    
    /**
     * Always return true
     * 
     * @param target
     *            the target object
     * @param node
     *            the node descriptor for this assertion
     * @param assertion
     *            the particular assertion    
     * @return boolean                                
     */
    public static boolean alwaysTrue(Object target, 
            NodeDescriptor node, AssertionDescriptor assertion) {
        return true;
    }
}
