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

package org.openvpms.web.component.property;


/**
 * Factory for {@link AbstractPropertyTransformer} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PropertyTransformerFactory {

    /**
     * The default property transformer.
     */
    private static DefaultPropertyTransformer DEFAULT
            = new DefaultPropertyTransformer();

    /**
     * Creates a new property transformer.
     *
     * @param property the property
     */
    public static PropertyTransformer create(Property property) {
        PropertyTransformer result;
        if (property.isLookup()) {
            result = DEFAULT;
        } else if (property.isString()) {
            result = new StringPropertyTransformer(property);
        } else if (property.isMoney()) {
            result = new MoneyPropertyTransformer(property);
        } else if (property.isNumeric()) {
            result = new NumericPropertyTransformer(property);
        } else {
            result = DEFAULT;
        }
        return result;
    }
}
