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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.math;

/**
 * Weight units.
 *
 * @author Tim Anderson
 */
public enum WeightUnits {
    KILOGRAMS,
    GRAMS,
    POUNDS;

    /**
     * Converts a string to a weight unit, ignoring nulls.
     *
     * @param value the string value. May be {@code null}
     * @return the corresponding unit, or {@code null} if {@code value} is {@code null}
     */
    public static WeightUnits fromString(String value) {
        return value != null ? valueOf(value) : null;
    }

    /**
     * Converts a string to a weight unit, ignoring nulls.
     *
     * @param value        the string value. May be {@code null}
     * @param defaultValue the default value if {@code value} is {@code null}
     * @return the corresponding unit, or {@code defaultValue} if {@code value} is {@code null}
     */
    public static WeightUnits fromString(String value, WeightUnits defaultValue) {
        return (value != null) ? fromString(value) : defaultValue;
    }
}
