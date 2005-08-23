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

// openehr java kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;

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
    private static final long serialVersionUID = 9163075594646412781L;

    /**
     * Indicates the type of specie
     */
    private DvCodedText species;

    /**
     * Indicates the breed of the animal
     */
    private DvCodedText breed;

    /**
     * Indicates the colour of the animal
     */
    private DvCodedText colour;

    /**
     * The sex of the anumal
     */
    private DvCodedText sex;

    /**
     * Indicates whether the animal has been desexed
     */
    private DvBoolean desexed;

    /**
     * Date of birth
     */
    private DvDate dateOfBirth;

    /**
     * Constructs an animal.
     * 
     * @param uid
     *            a unique object identity
     * @param archetypeNodeId
     *            the node id for this archetype
     * @param name
     *            the name of this archetype
     * @param archetypeDetails
     *            descriptive meta data for the achetype
     * @param contacts
     *            a collection of contacts for this actor            
     * @param roles
     *            the collection of roles it belongs too
     * @param details 
     *            actor details
     * @throws IllegalArgumentException
     *             thrown if the preconditions are not met.
     */
    @FullConstructor
    public Animal(
            @Attribute(name = "uid", required = true) ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails,
            @Attribute(name = "contacts") Set<Contact> contacts,
            @Attribute(name = "roles") Set<Role> roles,
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, contacts, roles, details);
    }

    /**
     * @return Returns the breed.
     */
    public DvCodedText getBreed() {
        return breed;
    }

    /**
     * @param breed
     *            The breed to set.
     */
    public void setBreed(DvCodedText breed) {
        this.breed = breed;
    }

    /**
     * @return Returns the colour.
     */
    public DvCodedText getColour() {
        return colour;
    }

    /**
     * @param colour
     *            The colour to set.
     */
    public void setColour(DvCodedText colour) {
        this.colour = colour;
    }

    /**
     * @return Returns the dateOfBirth.
     */
    public DvDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth
     *            The dateOfBirth to set.
     */
    public void setDateOfBirth(DvDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return Returns the desexed.
     */
    public DvBoolean getDesexed() {
        return desexed;
    }

    /**
     * @param desexed
     *            The desexed to set.
     */
    public void setDesexed(DvBoolean desexed) {
        this.desexed = desexed;
    }

    /**
     * @return Returns the sex.
     */
    public DvCodedText getSex() {
        return sex;
    }

    /**
     * @param sex
     *            The sex to set.
     */
    public void setSex(DvCodedText sex) {
        this.sex = sex;
    }

    /**
     * @return Returns the species.
     */
    public DvCodedText getSpecies() {
        return species;
    }

    /**
     * @param species
     *            The species to set.
     */
    public void setSpecies(DvCodedText species) {
        this.species = species;
    }
}
