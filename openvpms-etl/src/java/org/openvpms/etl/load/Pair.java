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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.apache.commons.lang.ObjectUtils;


/**
 * A pair of values.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Pair {

    /**
     * The first value.
     */
    private String value1;

    /**
     * The second value.
     */
    private String value2;

    /**
     * Constructs a new <tt>Pair</tt>.
     */
    public Pair() {
        this(null, null);
    }

    /**
     * Constructs a new <tt>Pair</tt>.
     *
     * @param value1 the first value
     * @param value2 the second value
     */
    public Pair(String value1, String value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * Returns the first value.
     *
     * @return the first value
     */
    public String getValue1() {
        return value1;
    }

    /**
     * Sets the first value.
     *
     * @param value1 the first value
     */
    public void setValue1(String value1) {
        this.value1 = value1;
    }

    /**
     * Returns the second value.
     *
     * @return the second value
     */
    public String getValue2() {
        return value2;
    }

    /**
     * Sets the second value.
     *
     * @param value2 the second value
     */
    public void setValue2(String value2) {
        this.value2 = value2;
    }

    /**
     * Determines if this equals another object.
     *
     * @param other the other object
     * @return <tt>true</tt> if this equals <tt>other</tt>,
     *         otherwise <tt>false</tt>
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof Pair)) {
            return false;
        }
        Pair pair = (Pair) other;
        return ObjectUtils.equals(value1, pair.value1)
                && ObjectUtils.equals(value2, pair.value2);
    }

    /**
     * Returns the hash code.
     *
     * @return the hash code
     */
    public int hashCode() {
        int hash1 = value1 != null ? value1.hashCode() : 0;
        int hash2 = value2 != null ? value2.hashCode() : 0;
        return hash1 + hash2;
    }

}
