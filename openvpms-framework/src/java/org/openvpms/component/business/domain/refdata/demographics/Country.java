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

// java core
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Defines a country and its country code
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Country implements Serializable {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * uniquely identify this instance
     */
    private int id;

    /**
     * The name of the country
     */
    private String name;
    
    /**
     * The country code
     */
    private String code;
    
    /**
     * The country currency
     * 
     * TODO Will probably need to define a class for currency
     */
    private String currency;
    
    /**
     * A set of states that are part th country
     */
    private Set<State> states;
    
    
    /**
     * Default constructor
     */
    public Country() {
        states = new HashSet<State>();
    }


    /**
     * Create a country from the specified values
     * 
     * @param name
     *            the name of the currency
     * @param code
     *            the well known currency code
     * @param currency
     *            the primary currency for the country 
     */
    public Country(String name, String code, String currency) {
        this.code = code;
        this.currency = currency;
        this.name = name;
        states = new HashSet<State>();
    }


    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return Returns the currency.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency The currency to set.
     */
    public void setCurrency(String currency) {
        this.currency = currency;
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
     * @return Returns the states.
     */
    public Set<State> getStates() {
        return states;
    }

    /**
     * @param states The states to set.
     */
    public void setStates(Set<State> states) {
        this.states = states;
    }
    
    /**
     * Add a state for the country
     * 
     * @param state
     *            the state to add
     */
    public void addState(State state) {
        state.setCountry(this);
        states.add(state);
    }
    
    /**
     * Dissassociate the state from the country
     * 
     * @param state
     *            the state to remove
     */
    public void removeState(State state) {
        state.setCountry(null);
        states.remove(state);
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
            !(obj instanceof Country)) {
            return false;
        }
        
        Country country = (Country)obj;
        return new EqualsBuilder()
            .append(this.id, country.id)
            .append(this.name, country.name)
            .append(this.code, country.code)
            .append(this.currency, country.currency)
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
            .append(code)
            .append(currency)
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
            .append(" code=").append(code)
            .append(" currency=").append(currency)
            .toString();
    }
}
