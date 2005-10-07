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

package org.openvpms.component.business.domain.im.party;

// java core
import java.util.Set;
import java.util.Date;


// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * Defines an {@link Entity} of type animal (i.e. non-human)
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Animal extends Actor {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates the type of specie
     * 
     * TODO Change when the terminology service has been created
     */
    private String species;

    /**
     * Indicates the breed of the animal
     */
    private String breed;

    /**
     * Indicates the colour of the animal
     */
    private String colour;

    /**
     * The sex of the anumal
     */
    private String sex;

    /**
     * Indicates whether the animal has been desexed
     */
    private boolean desexed;

    /**
     * Date of birth
     */
    private Date dateOfBirth;
    

    
    /**
     * Default constructor
     */
    public Animal() {
        // do nothing
    }
    
    /**
     * Constructs an animal.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the animal's primary name            
     * @param description
     *            the description of this entity            
     * @param contacts
     *            a collection of contacts for this actor            
     * @param roles
     *            the collection of roles it belongs too
     * @param details 
     *            actor details
     */
    public Animal(ArchetypeId archetypeId, String name,
            String description, Set<Contact> contacts, Set<Role> roles, 
            DynamicAttributeMap details) {
        super(archetypeId, name, description, contacts, roles, details);
    }

    /**
     * @return Returns the breed.
     */
    public String getBreed() {
        return breed;
    }

    /**
     * @param breed
     *            The breed to set.
     */
    public void setBreed(String breed) {
        this.breed = breed;
    }

    /**
     * @return Returns the colour.
     */
    public String getColour() {
        return colour;
    }

    /**
     * @param colour
     *            The colour to set.
     */
    public void setColour(String colour) {
        this.colour = colour;
    }

    /**
     * @return Returns the dateOfBirth.
     */
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth
     *            The dateOfBirth to set.
     */
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return Returns the desexed.
     */
    public boolean getDesexed() {
        return desexed;
    }

    /**
     * @param desexed
     *            The desexed to set.
     */
    public void setDesexed(boolean desexed) {
        this.desexed = desexed;
    }

    /**
     * @return Returns the sex.
     */
    public String getSex() {
        return sex;
    }

    /**
     * @param sex
     *            The sex to set.
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * @return Returns the species.
     */
    public String getSpecies() {
        return species;
    }

    /**
     * @param species
     *            The species to set.
     */
    public void setSpecies(String species) {
        this.species = species;
    }
}
