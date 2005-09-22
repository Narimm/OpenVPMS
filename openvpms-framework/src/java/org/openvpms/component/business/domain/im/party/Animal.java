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


// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;

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
    private DvDateTime dateOfBirth;
    
    /**
     * The primary name of the animal
     */
    private String primaryName;

    
    /**
     * Default constructor
     */
    protected Animal() {
        // do nothing
    }
    
    /**
     * Constructs an animal.
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archetype id constraining this object
     * @param description
     *            the description of this entity            
     * @param contacts
     *            a collection of contacts for this actor            
     * @param roles
     *            the collection of roles it belongs too
     * @param details 
     *            actor details
     */
    public Animal(String uid, ArchetypeId archetypeId,  
            String description, Set<Contact> contacts, Set<Role> roles, 
            DynamicAttributeMap details) {
        super(uid, archetypeId, description, contacts, roles, details);
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
    public DvDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth
     *            The dateOfBirth to set.
     */
    public void setDateOfBirth(DvDateTime dateOfBirth) {
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

    /**
     * @return Returns the primaryName.
     */
    public String getPrimaryName() {
        return primaryName;
    }

    /**
     * @param primaryName The primaryName to set.
     */
    public void setPrimaryName(String primaryName) {
        this.primaryName = primaryName;
    }
}
