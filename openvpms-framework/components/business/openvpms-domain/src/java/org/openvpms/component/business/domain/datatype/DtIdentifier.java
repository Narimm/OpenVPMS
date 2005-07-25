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

// jug-uuid
import org.safehaus.uuid.UUIDGenerator;

/**
 * A system unique id
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastDateChanged$
 */
public class DtIdentifier extends DtValue {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = -4324536492010770198L;
    
    /**
     * UUIDGeenrate
     */
    private static UUIDGenerator generator = UUIDGenerator.getInstance();

    /**
     * A string is used to represent the identity
     */
    private String value;

    /**
     * Default constructor
     */
    public DtIdentifier() {
        value = generator.generateTimeBasedUUID().toString();
    }

    /**
     * Construct a UUID with the specific prefix
     * 
     * @param prefix
     *            prepend the uuid with the prefix
     */
    public DtIdentifier(String prefix) {
        this.value = new StringBuffer(prefix)
            .append(generator.generateTimeBasedUUID().toString())
            .toString();
    }

    /**
     * @return Returns the id.
     */
    public String getValue() {
        return value;
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
        if ((obj == null) ||
            !(obj instanceof DtIdentifier)) {
            return false;
        }

        // perform a value comparison
        return new EqualsBuilder()
                .append(this.value, ((DtIdentifier)obj).value)
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
}
