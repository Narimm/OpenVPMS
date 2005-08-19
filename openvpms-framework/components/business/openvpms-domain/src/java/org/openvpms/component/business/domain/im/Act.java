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

// java core
import java.util.Set;

// openehr java kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.DvQuantity;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;

/**
 * A class representing an activity that is being done, has been done, 
 * can be done, or is intended or requested to be done.  An Act instance 
 * is a record of an intentional business action.  

 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Act extends Locatable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 9007260593488278053L;

    /**
     * Text that defines the modality of the Act i.e Definition, Intent, 
     * Event, Goal. The mood of an Act does not change. To describe the 
     * progression of an Act from defined through to executed you create 
     * different Acts connected via ActRelationships.
     */
    private DvText mood;
    
    /**
     * Represents the title of the act
     */
    private DvText title;
    
    /**
     * Description of the Act
     */
    private DvText descritpion;
    
    /**
     * Time interval representing the operative time of the Act
     */
    private DvInterval effectiveTime;
    
    /**
     * A time expression specifying when an Act occurs, is supposed to occur, 
     * is scheduled to occur etc.  For example an event may have occurred 3 
     * hours ago {@link effectiveTime} but we only recorded it now.
     */
    private DvInterval activityTime;
    
    /**
     * The urgency under which the Act happened, can happen, is intended to 
     * happen.
     */
    private DvText priority;
    
    /**
     * Text representing the reason for the Act. Often this is beter 
     * represented by a realtionship to another Act of type "has reason".
     */
    private DvText reason;
    
    /**
     * An indicator specifiying that the Act statement is a negation of the 
     * Act as described by it's properties.  i.e Animal has NO hind limp.
     */
    private DvBoolean negationInd;
    
    /**
     * An interval of integers stating the minimal and maximum nymber of Act 
     * repetitions. 
     */
    private DvQuantity repeatNumber;

    /**
     * A String representing the status or state of the Act. (i.e  Normal, 
     * Aborted, Completed, Suspended, Cancelled etc
     */
    private DvText status;
    
    /**
     * Describes the specific details of the act, whether it is clinical,
     * financial or other.
     */
    private ItemStructure details;
    
    /**
     * The {@link Participations} for this act
     */
    private Set<Participation> participations;
    
    /**
     * Holds all the {@link ActRelationship}s that this act is a source off
     */
    private Set<ActRelationship> sourceActRelationships;
    
    /**
     * Holds all the {@link ActRelationship}s that this act is a target off.
     */
    private Set<ActRelationship> targetActRelationships;
    

    /**
     * Constructs an instance of an act.
     * TODO Need to determine what constitutes a valid construction of this
     * objct.
     * 
     * @param uid
     *            a unique object identity
     * @param archetypeNodeId
     *            the node id for this archetype
     * @param name
     *            the name of this archetype
     * @param archetypeDetails
     *            descriptive meta data for the achetype
     * @param links
     *            null if not specified
     * @param details
     *            a compound item that describes the details of this
     *            archetype.
     * @throws IllegalArgumentException
     *            thrown if the preconditions are not met.
     */
    @FullConstructor
    public Act(
            @Attribute(name = "uid", required = true) ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true)DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails, 
            @Attribute(name = "links") Set<Link> links, 
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, null, links);
        this.details = details;
    }
    

    /**
     * @return Returns the activityTime.
     */
    public DvInterval getActivityTime() {
        return activityTime;
    }


    /**
     * @param activityTime The activityTime to set.
     */
    public void setActivityTime(DvInterval activityTime) {
        this.activityTime = activityTime;
    }


    /**
     * @return Returns the descritpion.
     */
    public DvText getDescritpion() {
        return descritpion;
    }


    /**
     * @param descritpion The descritpion to set.
     */
    public void setDescritpion(DvText descritpion) {
        this.descritpion = descritpion;
    }


    /**
     * @return Returns the details.
     */
    public ItemStructure getDetails() {
        return details;
    }


    /**
     * @param details The details to set.
     */
    public void setDetails(ItemStructure details) {
        this.details = details;
    }


    /**
     * @return Returns the effectiveTime.
     */
    public DvInterval getEffectiveTime() {
        return effectiveTime;
    }


    /**
     * @param effectiveTime The effectiveTime to set.
     */
    public void setEffectiveTime(DvInterval effectiveTime) {
        this.effectiveTime = effectiveTime;
    }


    /**
     * @return Returns the mood.
     */
    public DvText getMood() {
        return mood;
    }


    /**
     * @param mood The mood to set.
     */
    public void setMood(DvText mood) {
        this.mood = mood;
    }


    /**
     * @return Returns the negationInd.
     */
    public DvBoolean getNegationInd() {
        return negationInd;
    }


    /**
     * @param negationInd The negationInd to set.
     */
    public void setNegationInd(DvBoolean negationInd) {
        this.negationInd = negationInd;
    }


    /**
     * @return Returns the participations.
     */
    public Set<Participation> getParticipations() {
        return participations;
    }


    /**
     * @param participations The participations to set.
     */
    public void setParticipations(Set<Participation> participations) {
        this.participations = participations;
    }
    
    /**
     * Add a {@link Participation}
     * 
     * @param participation 
     */
    public void addParticipation(Participation participation) {
        this.participations.add(participation);
    }

    /**
     * Remove a {@link Participation}
     * 
     * @param source
     */
    public void removeParticipation(Participation participation) {
        this.participations.remove(participation);
    }

    /**
     * @return Returns the priority.
     */
    public DvText getPriority() {
        return priority;
    }


    /**
     * @param priority The priority to set.
     */
    public void setPriority(DvText priority) {
        this.priority = priority;
    }


    /**
     * @return Returns the reason.
     */
    public DvText getReason() {
        return reason;
    }


    /**
     * @param reason The reason to set.
     */
    public void setReason(DvText reason) {
        this.reason = reason;
    }


    /**
     * @return Returns the repeatNumber.
     */
    public DvQuantity getRepeatNumber() {
        return repeatNumber;
    }


    /**
     * @param repeatNumber The repeatNumber to set.
     */
    public void setRepeatNumber(DvQuantity repeatNumber) {
        this.repeatNumber = repeatNumber;
    }


    /**
     * @return Returns the sourceActRelationships.
     */
    public Set<ActRelationship> getSourceActRelationships() {
        return sourceActRelationships;
    }


    /**
     * @param sourceActRelationships The sourceActRelationships to set.
     */
    public void setSourceActRelationships(
            Set<ActRelationship> sourceActRelationships) {
        this.sourceActRelationships = sourceActRelationships;
    }

    /**
     * Add a source {@link ActRelationship}
     * 
     * @param source 
     */
    public void addSourceActRelationship(ActRelationship source) {
        this.sourceActRelationships.add(source);
    }

    /**
     * Remove a source {@link ActRelationship}
     * 
     * @param source
     */
    public void removeSourceActRelationship(ActRelationship source) {
        this.sourceActRelationships.remove(source);
    }

    /**
     * @return Returns the status.
     */
    public DvText getStatus() {
        return status;
    }


    /**
     * @param status The status to set.
     */
    public void setStatus(DvText status) {
        this.status = status;
    }


    /**
     * @return Returns the targetActRelationships.
     */
    public Set<ActRelationship> getTargetActRelationships() {
        return targetActRelationships;
    }


    /**
     * @param targetActRelationships The targetActRelationships to set.
     */
    public void setTargetActRelationships(
            Set<ActRelationship> targetActRelationships) {
        this.targetActRelationships = targetActRelationships;
    }
    
    /**
     * Add a target {@link ActRelationship}
     * 
     * @param target 
     */
    public void addTargetActRelationship(ActRelationship target) {
        this.targetActRelationships.add(target);
    }

    /**
     * Remove a target {@link ActRelationship}
     * 
     * @param target
     */
    public void removeTargetActRelationship(ActRelationship target) {
        this.targetActRelationships.remove(target);
    }

    /**
     * @return Returns the title.
     */
    public DvText getTitle() {
        return title;
    }


    /**
     * @param title The title to set.
     */
    public void setTitle(DvText title) {
        this.title = title;
    }


    /* (non-Javadoc)
     * @see org.openehr.rm.common.archetyped.Locatable#pathOfItem(org.openehr.rm.common.archetyped.Locatable)
     */
    @Override
    public String pathOfItem(Locatable item) {
        // TODO Auto-generated method stub
        return null;
    }

}
