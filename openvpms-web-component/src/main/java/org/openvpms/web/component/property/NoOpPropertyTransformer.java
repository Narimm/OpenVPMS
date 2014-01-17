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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

/**
 * A {@link PropertyTransformer} that does no transformation.
 *
 * @author Tim Anderson
 */
public class NoOpPropertyTransformer implements PropertyTransformer {

    /**
     * Singleton instance.
     */
    public static final PropertyTransformer INSTANCE = new NoOpPropertyTransformer();

    /**
     * Default constructor.
     */
    private NoOpPropertyTransformer() {
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <code>object</code> if no
     *         transformation is required
     * @throws PropertyException if the object is invalid
     */
    @Override
    public Object apply(Object object) {
        return object;
    }
}
