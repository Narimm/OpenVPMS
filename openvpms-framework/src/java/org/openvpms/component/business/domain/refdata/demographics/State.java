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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A state belongs to a country
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class State implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * uniquely identify this instance
     */
    private int id;

    /**
     * The name of the name
     */
    private String name;
    
    /**
     * The country that the name belongs too
     */
    private Country country;
    
    /**
     * The list of suburbs in this state
     */
    private Set<Suburb> suburbs;
    
    /**
     * Default constructor
     */
    public State() {
    }

    /**
     * Construct a state using the name
     * 
     * @param name
     */
    public State(String state) {
        this.name = state;
        this.suburbs = new HashSet<Suburb>();
    }

    /**
     * @return Returns the country.
     */
    public Country getCountry() {
        return country;
    }

    /**
     * @param country The country to set.
     */
    public void setCountry(Country country) {
        this.country = country;
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
    public void setName(String state) {
        this.name = state;
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
     * @return Returns the suburbs.
     */
    public Set<Suburb> getSuburbs() {
        return suburbs;
    }

    /**
     * @param suburbs The suburbs to set.
     */
    public void setSuburbs(Set<Suburb> suburbs) {
        this.suburbs = suburbs;
    }

    /**
     * Add the suburb to this state
     * 
     * @param suburb
     *            the suburb to add
     */
    public void addSuburb(Suburb suburb) {
        suburbs.add(suburb);
        suburb.setState(this);
    }
    
    /**
     * Disassociate the suburb from this state
     * 
     * @param suburb
     */
    public void removeSuburb(Suburb suburb) {
        suburbs.remove(suburb);
        suburb.setState(null);
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
            !(obj instanceof State)) {
            return false;
        }
        
        State state = (State)obj;
        return new EqualsBuilder()
            .append(this.id, state.id)
            .append(this.name, state.name)
            .append(this.country, state.country)
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
            .toString();
    }
}
