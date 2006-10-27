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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper.lookup;

import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;


/**
 * Lookup assertion helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupAssertionHelper {

    /**
     * Returns the value of the named property from an assertion descriptor.
     *
     * @param assertion the assertion descriptor
     * @param name      the property name
     * @return the property value, or <code>null</code> if it doesn't exist
     */
    public static String getValue(AssertionDescriptor assertion, String name) {
        NamedProperty property = assertion.getProperty(name);
        return (property != null) ? (String) property.getValue() : null;
    }

}
