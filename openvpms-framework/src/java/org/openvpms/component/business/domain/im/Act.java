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
import java.util.HashSet;
import java.util.Set;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;
import org.openvpms.component.business.domain.im.datatypes.quantity.DvInterval;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;

/**
 * A class representing an activity that is being done, has been done, 
 * can be done, or is intended or requested to be done.  An Act instance 
 * is a record of an intentional business action.  

 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Act extends IMObject {

    /**
     * Generated SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Text that defines the modality of the Act i.e Definition, Intent, 
     * Event, Goal. The mood of an Act does not change. To describe the 
     * progression of an Act from defined through to executed you create 
     * different Acts connected via ActRelationships.
     * 
     * TODO Change to use terminology service
     */
    private String mood;
    
    /**
     * Represents the title of the act.
     * 
     * TODO Change to use terminology service
     */
    private String title;
    
    /**
     * Description of the Act.
     */
    private String descritpion;
    
    /**
     * Time interval representing the operative time of the Act.
     */
    private DvInterval<DvDateTime> effectiveTime;
    
    /**
     * A time expression specifying when an Act occurs, is supposed to occur, 
     * is scheduled to occur etc.  For example an event may have occurred 3 
     * hours ago {@link effectiveTime} but we only recorded it now.
     */
    private DvInterval<DvDateTime> activityTime;
    
    /**
     * The urgency under which the Act happened, can happen, is intended to 
     * happen.
     */
    private String priority;
    
    /**
     * Text representing the reason for the Act. Often this is beter 
     * represented by a realtionship to another Act of type "has reason".
     */
    private String reason;
    
    /**
     * An indicator specifiying that the Act statement is a negation of the 
     * Act as described by it's properties.  i.e Animal has NO hind limp.
     */
    private boolean negationInd;
    
    /**
     * An interval of integers stating the minimal and maximum nymber of Act 
     * repetitions. 
     */
    private int repeatNumber;

    /**
     * A String representing the status or state of the Act. (i.e  Normal, 
     * Aborted, Completed, Suspended, Cancelled etc
     */
    private String status;
    
    /**
     * Describes the specific details of the act, whether it is clinical,
     * financial or other.
     */
    private DynamicAttributeMap details;
    
    /**
     * The {@link Participations} for this act.
     */
    private Set<Participation> participations;
    
    /**
     * Holds all the {@link ActRelationship}s that this act is a source off.
     */
    private Set<ActRelationship> sourceActRelationships;
    
    /**
     * Holds all the {@link ActRelationship}s that this act is a target off.
     */
    private Set<ActRelationship> targetActRelationships;
    

    /**
     * Default constructor
     */
    protected Act() {
        // do nothing
    }
    
    /**
     * Constructs an instance of an act.
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name 
     * @param details
     *            dynamic details of the act.
     */
    public Act(String uid, ArchetypeId archetypeId, String name, 
            DynamicAttributeMap details) {
        super(uid, archetypeId, name);
        this.details = details;
        this.participations = new HashSet<Participation>();
        this.sourceActRelationships = new HashSet<ActRelationship>();
        this.targetActRelationships = new HashSet<ActRelationship>();
    }

    /**
     * @return Returns the activityTime.
     */
    public DvInterval<DvDateTime> getActivityTime() {
        return activityTime;
    }

    /**
     * @param activityTime The activityTime to set.
     */
    public void setActivityTime(DvInterval<DvDateTime> activityTime) {
        this.activityTime = activityTime;
    }

    /**
     * @return Returns the descritpion.
     */
    public String getDescritpion() {
        return descritpion;
    }

    /**
     * @param descritpion The descritpion to set.
     */
    public void setDescritpion(String descritpion) {
        this.descritpion = descritpion;
    }

    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }

    /**
     * @return Returns the effectiveTime.
     */
    public DvInterval<DvDateTime> getEffectiveTime() {
        return effectiveTime;
    }

    /**
     * @param effectiveTime The effectiveTime to set.
     */
    public void setEffectiveTime(DvInterval<DvDateTime> effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    /**
     * @return Returns the mood.
     */
    public String getMood() {
        return mood;
    }

    /**
     * @param mood The mood to set.
     */
    public void setMood(String mood) {
        this.mood = mood;
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
     * @return Returns the priority.
     */
    public String getPriority() {
        return priority;
    }

    /**
     * @param priority The priority to set.
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * @return Returns the reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason The reason to set.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return Returns the repeatNumber.
     */
    public int getRepeatNumber() {
        return repeatNumber;
    }

    /**
     * @param repeatNumber The repeatNumber to set.
     */
    public void setRepeatNumber(int repeatNumber) {
        this.repeatNumber = repeatNumber;
    }

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Returns the sourceActRelationships.
     */
    public ActRelationship[] getSourceActRelationships() {
        return (ActRelationship[])sourceActRelationships.toArray();
    }

    /**
     * @param sourceActRelationships The sourceActRelationships to set.
     */
    public void setSourceActRelationships(
            Set<ActRelationship> sourceActRelationships) {
        this.sourceActRelationships = sourceActRelationships;
    }

    /**
     * Add a source {@link ActRelationship}.
     * 
     * @param source 
     */
    public void addSourceActRelationship(ActRelationship source) {
        this.sourceActRelationships.add(source);
    }

    /**
     * Remove a source {@link ActRelationship}.
     * 
     * @param source
     */
    public void removeSourceActRelationship(ActRelationship source) {
        this.sourceActRelationships.remove(source);
    }

    /**
     * @return Returns the targetActRelationships.
     */
    public ActRelationship[] getTargetActRelationships() {
        return (ActRelationship[])targetActRelationships.toArray();
    }

    /**
     * Set this act to be a targt of an {@link ActRelationship}.
     * 
     * @param targetActRelationships The targetActRelationships to set.
     */
    public void setTargetActRelationships(
            Set<ActRelationship> targetActRelationships) {
        this.targetActRelationships = targetActRelationships;
    }
    
    /**
     * Add a target {@link ActRelationship}.
     * 
     * @param target 
     *            add a new target.
     */
    public void addTargetActRelationship(ActRelationship target) {
        this.targetActRelationships.add(target);
    }

    /**
     * Remove a target {@link ActRelationship}.
     * 
     * @param target
     */
    public void removeTargetActRelationship(ActRelationship target) {
        this.targetActRelationships.remove(target);
    }

    /**
     * Return the associated {@link Participantion} instances.
     * 
     * @return Participation
     */
    public Participation[] getParticipations() {
        return (Participation[])this.participations.toArray();
    }

    /**
     * @param participations The participations to set.
     */
    public void setParticipations(Set<Participation> participations) {
        this.participations = participations;
    }
    
    /**
     * Add a {@link Participation}.
     * 
     * @param participation 
     */
    public void addParticipation(Participation participation) {
        this.participations.add(participation);
    }

    /**
     * Remove a {@link Participation}.
     * 
     * @param source
     */
    public void removeParticipation(Participation participation) {
        this.participations.remove(participation);
    }
}
