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


package org.openvpms.component.business.domain.refdata.demographics;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A suburb belongs to a state
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Suburb implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * uniquely identify this instance
     */
    private int id;

    /**
     * The name of the suburn
     */
    private String name;
    
    /**
     * The postcode
     */
    private String postCode;
    
    /**
     * The state that it belongs too
     */
    private State state;
    
    
    /**
     * Default constructor
     */
    public Suburb() {
    }

    /**
     * Create a suburb given the name and postcode.
     * 
     * @param name
     * @param postCode
     * @param state
     */
    public Suburb(String name, String postCode) {
        this.name = name;
        this.postCode = postCode;
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the postCode.
     */
    public String getPostCode() {
        return postCode;
    }

    /**
     * @param postCode The postCode to set.
     */
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    /**
     * @return Returns the state.
     */
    public State getState() {
        return state;
    }

    /**
     * @param state The state to set.
     */
    public void setState(State state) {
        this.state = state;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if ((obj == null) ||
            !(obj instanceof Suburb)) {
            return false;
        }
        
        Suburb suburb = (Suburb)obj;
        return new EqualsBuilder()
            .append(this.id, suburb.id)
            .append(this.name, suburb.name)
            .append(this.postCode, suburb.postCode)
            .append(this.state, suburb.state)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(name)
            .append(postCode)
            .hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return  new StringBuilder()
            .append(" id=").append(id)
            .append(" name=").append(name)
            .append(" postCode=").append(postCode)
            .toString();
    }
}
