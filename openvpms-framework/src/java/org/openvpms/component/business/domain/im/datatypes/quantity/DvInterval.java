/*
 * component:   "openEHR Reference Implementation"
 * description: "Class DvInterval"
 * keywords:    "datatypes"
 *
 * author:      "Rong Chen <rong@acode.se>"
 * support:     "Acode HB <support@acode.se>"
 * copyright:   "Copyright (c) 2004 Acode HB, Sweden"
 * license:     "See notice at bottom of class"
 *
 * file:        "$URL: http://svn.openehr.org/ref_kernel_java/TRUNK/src/java/org/openehr/rm/datatypes/quantity/DvInterval.java $"
 * revision:    "$LastChangedRevision$"
 * last_change: "$LastChangedDate$"
 * 
 * $Id$
 */
package org.openvpms.component.business.domain.im.datatypes.quantity;

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


//openvpms-framework
import org.openvpms.component.business.domain.im.datatypes.basic.ComparableDataValue;
import org.openvpms.component.business.domain.im.datatypes.basic.DataValue;

/**
 * Generic class defining an interval (ie range) of a comparable type. An
 * interval is a contiguous subrange of a comparable base type. Instancese of
 * this class are immutable.
 * 
 * @author Rong Chen
 * @version 1.0
 * 
 * This file has been imported in to OpenVPMS from the OpenEHR project.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public final class DvInterval<T extends ComparableDataValue> extends DataValue {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * lower part of the interval
     */
    private T lower;
    
    /**
     * upper part of the interval
     */
    private T upper;
    

    /**
     * Default constructor
     *
     */
    private DvInterval() {
    }
    
    /**
     * Constructs an Interval. If the upper bound is 
     * 
     * @param lower
     *            null if unbounded
     * @param upper
     *            null if unbounded
     * @throws IllegalArgumentException
     *             if lower > upper
     */
    @SuppressWarnings("unchecked")
    public DvInterval(T lower, T upper) {
        if (lower != null && upper != null && upper.compareTo(lower) < 0) {
            throw new IllegalArgumentException("lower > upper");
        }
        
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * Returns lower boundary
     * 
     * @return null if not specified
     */
    public T getLower() {
        return this.lower;
    }

    /**
     * Returns upper boundary
     * 
     * @return null if not specified
     */
    public T getUpper() {
        return this.upper;
    }

    /**
     * Returns true if lower boundary open
     * 
     * @return true is unbounded
     */
    public boolean isLowerUnbounded() {
        return this.lower == null;
    }

    /**
     * Returns true if upper boundary open
     * 
     * @return true is unbounded
     */
    public boolean isUpperUnbounded() {
        return this.upper == null;
    }

    /**
     * Returns true if lower >= value and value <= upper
     * 
     * @param value
     *            not null
     * @return true if given value is within this interval
     * @throws IllegalArgumentException
     *             if value is null
     */
    @SuppressWarnings("unchecked")
    public boolean has(T value) {
        if (value == null) {
            throw new IllegalArgumentException("null value");
        }

        return (((this.isLowerUnbounded()) || 
                 (value.compareTo(this.getLower()) >= 0)) &&
                ((this.isUpperUnbounded()) || 
                 (value.compareTo(this.getUpper()) <= 0)));
    }

    /**
     * Equals if both has same value for lower and upper boundaries
     * 
     * @param o
     * @return true if equals
     */
    public boolean equals(Object o) {
        // check it is a reference to this object
        if (this == o)
            return true;
        
        // check if the types are the same
        if (!(o instanceof DvInterval))
            return false;

        DvInterval interval = (DvInterval)o;

        return new EqualsBuilder()
            .append(this.lower, interval.lower)
            .append(this.upper, interval.upper)
            .isEquals();
    }

    /**
     * Return a hash code of this interval
     * 
     * @return hash code
     */
    public int hashCode() {
        return new HashCodeBuilder()
            .append(lower)
            .append(upper)
            .hashCode();
    }


    // POJO end
}

/*
 * ***** BEGIN LICENSE BLOCK ***** Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the 'License'); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is DvInterval.java
 * 
 * The Initial Developer of the Original Code is Rong Chen. Portions created by
 * the Initial Developer are Copyright (C) 2003-2004 the Initial Developer. All
 * Rights Reserved.
 * 
 * Contributor(s):
 * 
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * ***** END LICENSE BLOCK *****
 */