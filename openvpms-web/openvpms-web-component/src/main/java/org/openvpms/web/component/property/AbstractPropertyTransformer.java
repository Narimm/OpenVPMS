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
 *  $Id:AbstractPropertyTransformer.java 2147 2007-06-21 04:16:11Z tanderson $
 */

package org.openvpms.web.component.property;

/**
 * PropertyTransformer is responsible for processing user input prior to it
 * being set on {@link Property}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2007-06-21 04:16:11Z $
 */
public abstract class AbstractPropertyTransformer
    implements PropertyTransformer {

    /**
     * The property.
     */
    private final Property property;


    /**
     * Construct a new <tt>PropertyTransformer</tt>.
     *
     * @param property the property
     */
    public AbstractPropertyTransformer(Property property) {
        this.property = property;
    }

    /**
     * Returns the property.
     *
     * @return the property
     */
    public Property getProperty() {
        return property;
    }
}
