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


package org.openvpms.component.business.domain.im;


// openehr-java-kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.DataStructure;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.text.DvText;

/**
 * A class representing an {@link Entity}'s participantion in an {@link Act}.
 * 
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
/**
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Participation extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * An integer representing the relative order of the participantion in
     * relation to other participations of the same act.
     */
    private int sequence;
    
    /**
     * Description of the participation
     */
    private String description;
    
    /**
     * Indicates that a particular participation did not occur
     */
    private boolean negationInd;
    
    /**
     * Indicates the mode that the {@link Entity} is participating in the
     * {@link Act}
     * 
     * TODO Look at incorporating with the terminology service
     */
    private String mode;
    
    /**
     * Indicates the time interval that the {@link Entity} was participating
     * in the {@link Act}.
     */
    private DvInterval<DvDate> timeInterval;
    
    /**
     * The percentage of participation in the specified {@link Act}.
     */
    private int percentage;
    
    /**
     * Reference to the associated entity
     */
    private Entity entity;
    
    /**
     * Reference to the associated act
     */
    private Act act;
    
    /**
     * Holds details about the participation
     */
    private DataStructure details;

    
    /**
     * Default constructor
     */
    protected Participation() {
        // do nothing
    }

    /**
     * Constructs a participantion between an {@link Entity} and an {@link Act}.
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archietype that is constraining this object
     * @param imVersion
     *            the version of the reference model
     * @param archetypeNodeId
     *            the id of this node                        
     * @param name
     *            the name 
     * @param details
     *            holds details about the participation.
     * @throws IllegalArgumentException
     *            thrown if the preconditions are not met.
     */
    @FullConstructor
    public Participation(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "entity") Entity entity,
            @Attribute(name = "act") Act act,
            @Attribute(name = "details") DataStructure details) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name);
        
        this.act = act;
        this.entity = entity;
        this.details = details;
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the details.
     */
    public DataStructure getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DataStructure details) {
        this.details = details;
    }

    /**
     * @return Returns the mode.
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode The mode to set.
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * @return Returns the negationInd.
     */
    public boolean getNegationInd() {
        return negationInd;
    }

    /**
     * @param negationInd The negationInd to set.
     */
    public void setNegationInd(boolean negationInd) {
        this.negationInd = negationInd;
    }

    /**
     * @return Returns the sequence.
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence The sequence to set.
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * @return Returns the timeInterval.
     */
    public DvInterval<DvDate> getTimeInterval() {
        return timeInterval;
    }

    /**
     * @param timeInterval The timeInterval to set.
     */
    public void setTimeInterval(DvInterval<DvDate> timeInterval) {
        this.timeInterval = timeInterval;
    }

    /**
     * @return Returns the act.
     */
    public Act getAct() {
        return act;
    }

    /**
     * @return Returns the entity.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * @return Returns the percentage.
     */
    public int getPercentage() {
        return percentage;
    }

    /**
     * @param percentage The percentage to set.
     */
    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    /* (non-Javadoc)
     * @see org.openehr.rm.common.archetyped.Locatable#pathOfItem(org.openehr.rm.common.archetyped.Locatable)
     */
    @Override
    public String pathOfItem(Locatable item) {
        // TODO implement this method
        return null;
    }

}
