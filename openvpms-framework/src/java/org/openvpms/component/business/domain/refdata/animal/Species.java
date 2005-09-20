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


package org.openvpms.component.business.domain.refdata.animal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Defines the species of the animal
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Species implements Serializable {

    /**
     * The default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * uniquely identify this instance
     */
    private int id;
    
    /**
     * The common name
     */
    private String name;
    
    /**
     * The alternate name
     */
    private String alternateName;
    
    /**
     * The list of breeds associated with this specie
     */
    private Set<Breed> breeds;
    
    /**
     * Default constructor
     */
    public Species() {
        super();
    }

    /**
     * Create a specie using an name and an alternate name
     * 
     * @param name
     * @param alternateName
     */
    public Species(String name, String alternateName) {
        this.name = name;
        this.alternateName = alternateName;
        this.breeds = new HashSet<Breed>();
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
     * @return Returns the alternateName.
     */
    public String getAlternateName() {
        return alternateName;
    }

    /**
     * @param alternateName The alternateName to set.
     */
    public void setAlternateName(String alternateName) {
        this.alternateName = alternateName;
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
     * @return Return the breeds.
     */
    public Set<Breed> getBreeds() {
        return breeds;
    }

    /**
     * @param breeds The breeds to set.
     */
    public void setBreeds(Set<Breed> breeds) {
        this.breeds = breeds;
    }
    
    /**
     * Add this breed to the species.
     * 
     * @param breed
     *            the breed to add
     */
    public void addBreed(Breed breed) {
        breed.setSpecies(this);
        breeds.add(breed);
    }
    
    /**
     * Remove this breed from the species
     * 
     * @param breed
     *            the breed to remove
     */
    public void removeBreed(Breed breed) {
        breed.setSpecies(null);
        breeds.remove(breed);
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
            !(obj instanceof Species)) {
            return false;
        }
        
        Species species = (Species)obj;
        return new EqualsBuilder()
            .append(this.id, species.id)
            .append(this.name, species.name)
            .append(this.alternateName, species.alternateName)
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
            .append(alternateName)
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
            .append(" alternateName=").append(alternateName)
            .toString();
    }
}
