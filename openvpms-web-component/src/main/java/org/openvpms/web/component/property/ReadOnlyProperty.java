/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

/**
 * Helper {@link Property} that marks the underlying property read-only.
 * <p/>
 * The property may still be updated; this is primarily used to alter the presentation of properties.
 *
 * @author Tim Anderson
 */
public class ReadOnlyProperty extends DelegatingProperty {

    /**
     * Constructs a {@link ReadOnlyProperty}
     *
     * @param property the property to delegate to
     */
    public ReadOnlyProperty(Property property) {
        super(property);
    }

    /**
     * Determines if the property is read-only.
     *
     * @return {@code true} if the property is read-only
     */
    @Override
    public boolean isReadOnly() {
        return true;
    }
}
