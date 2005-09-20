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
 * A breed belongs to a particular specie.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Breed implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * uniquely identify this instance
     */
    private int id;
    
    /**
     * The name of the breed
     */
    private String name;
    
    /**
     * The species that the breed belongs too
     */
    private Species species;
    
    /**
     * The default breed colour
     */
    private Set<BreedColour> colours;
    
     /**
      * Indicates whether it is a cross breed
      */
    private boolean crossBreed;
    
    /**
     * Default constructor 
     */
    public Breed() {
    }

    /**
     * Full constructor for the breed
     * 
     * @param name
     *            the name of the breed
     * @param species
     *            the species it belongs too
     * @param colour
     *            the default colour for the breed
     * @param breed
     *            whether it is a crossBreed
     */
    public Breed(String name, boolean crossBreed) {
        this.name = name;
        this.crossBreed = crossBreed;
        this.colours = new HashSet<BreedColour>();
    }

    /**
     * @return Returns the breedColour.
     */
    public Set<BreedColour> getBreedColour() {
        return colours;
    }

    /**
     * @param breedColour The breedColour to set.
     */
    public void setBreedColour(Set<BreedColour> colours) {
        this.colours = colours;
    }
    
    /**
     * Add a breed colour for this breed.
     * 
     * @param colour
     *            the colour to add
     */
    public void addBreedColour(BreedColour colour) {
        colours.add(colour);
    }
    
    /**
     * Remove the breed colour for this breed.
     * 
     * @param colour
     *            the colour to remove
     */
    public void removeBreedColour(BreedColour colour) {
        colours.remove(colour);
    }

    /**
     * @return Returns the crossBreed.
     */
    public boolean isCrossBreed() {
        return crossBreed;
    }

    /**
     * @param crossBreed The crossBreed to set.
     */
    public void setCrossBreed(boolean crossBreed) {
        this.crossBreed = crossBreed;
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
     * @return Returns the species.
     */
    public Species getSpecies() {
        return species;
    }

    /**
     * @param species The species to set.
     */
    public void setSpecies(Species species) {
        this.species = species;
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

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if ((obj == null) ||
            !(obj instanceof Breed)) {
            return false;
        }
        
        Breed breed = (Breed)obj;
        return new EqualsBuilder()
            .append(this.id, breed.id)
            .append(this.name, breed.name)
            .append(this.crossBreed, breed.crossBreed)
            .append(this.species, breed.species)
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
