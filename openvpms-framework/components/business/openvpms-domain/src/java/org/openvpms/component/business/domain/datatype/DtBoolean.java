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

package org.openvpms.component.business.domain.datatype;

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A representation of a boolean type.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $Revision$
 */
public class DtBoolean extends DtValue {

    /**
     * Generated SUID.
     */
    private static final long serialVersionUID = 4546483296806891021L;

    /**
     * The value of the boolean.
     */
    private Boolean value;

    /**
     * Default constructor.
     */
    public DtBoolean() {
    }

    /**
     * Create an instance of this object.
     * 
     * @param value
     *            the value to store
     */
    public DtBoolean(final Boolean value) {
        this.value = value;
    }

    /**
     * @return Boolean The boolean value
     */
    public Boolean getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(final Boolean value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        // check for reference equality
        if (this == obj) {
            return true;
        }
        
        // ensure that types match
        if (!(obj instanceof DtBoolean)) {
            return false;
        }

        // perform a value comparison
        return new EqualsBuilder()
                .append(this.value, ((DtBoolean)obj).value)
                .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(this.value)
        .toHashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }
}
