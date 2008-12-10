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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient;

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.party.MergeException;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_PARTICIPATION;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_WEIGHT;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ParticipationConstraint;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;


/**
 * Patient rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a new <tt>PatientRules</tt>.
     *
     * @throws ArchetypeServiceException if the archetype service is not
     *                                   configured
     */
    public PatientRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Construct a new <tt>PatientRules</tt>.
     *
     * @param service the archetype service
     */
    public PatientRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Adds a patient-owner relationship between the supplied customer and
     * patient.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return the relationship
     * @throws ArchetypeServiceException for any archetype service error
     */
    public EntityRelationship addPatientOwnerRelationship(Party customer,
                                                          Party patient) {
        EntityBean bean = new EntityBean(customer, service);
        EntityRelationship relationship = bean.addRelationship(
                PatientArchetypes.PATIENT_OWNER, patient);
        relationship.setActiveStartTime(new Date());
        return relationship;
    }

    /**
     * Returns the owner of a patient associated with an act.
     * If a patient has had multiple owners, then the returned owner will be
     * that whose ownership period encompasses the act start time. If there is
     * no such owner, the returned owner will be that whose ownership began
     * closest to the act start time.
     *
     * @param act the act
     * @return the patient's owner, or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Act act) {
        Party owner = null;
        ActBean bean = new ActBean(act, service);
        Party patient = (Party) bean.getParticipant("participation.patient");
        Date startTime = act.getActivityStartTime();
        if (patient != null && startTime != null) {
            EntityBean patientBean = new EntityBean(patient, service);
            owner = (Party) patientBean.getSourceEntity(
                    PatientArchetypes.PATIENT_OWNER, startTime, false);
            if (owner == null) {
                // no match for the start time, so try and find an owner close
                // to the start time
                EntityRelationship match = null;
                List<EntityRelationship> relationships
                        = patientBean.getRelationships(PatientArchetypes.PATIENT_OWNER, false);

                for (EntityRelationship relationship : relationships) {
                    if (match == null) {
                        owner = get(relationship.getSource());
                        if (owner != null) {
                            match = relationship;
                        }
                    } else {
                        if (closerTime(startTime, relationship, match)) {
                            Party party = get(relationship.getSource());
                            if (party != null) {
                                owner = party;
                                match = relationship;
                            }
                        }
                    }
                }
            }
        }
        return owner;
    }

    /**
     * Returns the owner of a patient.
     *
     * @param patient the patient
     * @return the patient's owner, or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Party patient) {
        EntityBean bean = new EntityBean(patient, service);
        return (Party) bean.getNodeSourceEntity("customers", new Date());
    }

    /**
     * Returns a reference to the owner of a patient.
     *
     * @param patient the patient
     * @return a reference to the owner, or <tt>null</tt> if none can be found
     */
    public IMObjectReference getOwnerReference(Party patient) {
        EntityBean bean = new EntityBean(patient, service);
        List<IMObjectReference> refs
                = bean.getNodeSourceEntityRefs("customers", new Date());
        return refs.isEmpty() ? null : refs.get(0);
    }

    /**
     * Determines if a patient has a customer as its owner.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return <tt>true</tt> if the customer is the owner of the patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isOwner(Party customer, Party patient) {
        Party owner = getOwner(patient);
        return (owner != null && owner.equals(customer));
    }

    /**
     * Returns the referral vet for a patient.
     * This is the associated party from the first matching
     * <em>entityRelationship.referredFrom</em> or
     * <em>entityrRelationship.referredTo</em> overlapping the specified time.
     *
     * @param patient the patient
     * @param time    the time
     * @return the referral vet, or <tt>null</tt> if none is founds
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getReferralVet(Party patient, Date time) {
        EntityBean bean = new EntityBean(patient, service);
        return (Party) bean.getNodeTargetEntity("referrals", time);
    }

    /**
     * Marks a patient as being deceased.
     *
     * @param patient the patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void setDeceased(Party patient) {
        EntityBean bean = new EntityBean(patient, service);
        if (!bean.getBoolean("deceased")) {
            bean.setValue("deceased", true);
            if (bean.hasNode("deceasedDate")) {
            	bean.setValue("deceasedDate", new Date());
            }
            bean.save();
        }
    }

    /**
     * Determines if a patient is deceased.
     *
     * @param patient the patient
     * @return <tt>true</tt> if the patient is deceased
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isDeceased(Party patient) {
        EntityBean bean = new EntityBean(patient, service);
        return bean.getBoolean("deceased");
    }

    /**
     * Marks a patient as being desexed.
     *
     * @param patient the patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void setDesexed(Party patient) {
        EntityBean bean = new EntityBean(patient, service);
        if (!bean.getBoolean("desexed")) {
            bean.setValue("desexed", true);
            bean.save();
        }
    }

    /**
     * Determines if a patient is desexed.
     *
     * @param patient the patient
     * @return <tt>true</tt> if the patient is desexed
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isDesexed(Party patient) {
        EntityBean bean = new EntityBean(patient, service);
        return bean.getBoolean("desexed");
    }

    /**
     * Returns the age of the patient.
     *
     * @param patient the patient
     * @return the age in string format
     * @throws ArchetypeServiceException for any archetype service error
     *                                   todo - should be localised
     */
    public String getPatientAge(Party patient) {
        EntityBean bean = new EntityBean(patient, service);
        Date birthDate = bean.getDate("dateOfBirth");
        String result;
        if (birthDate != null) {
            Date currentDate = new Date();
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(birthDate);
            long diffMs = currentDate.getTime() - calendar.getTimeInMillis();
            long diffdays = diffMs / DateUtils.MILLIS_PER_DAY;
            if (diffdays < 90) {
                long weeks = diffdays / 7;
                if (weeks == 0) {
                    result = diffdays + " Days";
                } else {
                    result = weeks + " Weeks";
                }
            } else if (diffdays < (365 * 2)) {
                result = (diffdays / 30) + " Months";
            } else {
                result = (diffdays / 365) + " Years";
            }
        } else {
            result = "No Birthdate";
        }
        return result;
    }

    /**
     * Returns the species of the patient.
     *
     * @param patient the patient
     * @return the species in string format
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPatientSpecies(Party patient) {
        return ArchetypeServiceFunctions.lookup(patient, "species");
    }

    /**
     * Returns the breed of the patient.
     *
     * @param patient the patient
     * @return the species in string format
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPatientBreed(Party patient) {
        return ArchetypeServiceFunctions.lookup(patient, "breed");
    }

    /**
     * Returns the sex of the patient.
     *
     * @param patient the patient
     * @return the sex in string format
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPatientSex(Party patient) {
        return ArchetypeServiceFunctions.lookup(patient, "sex");
    }

    /**
     * Returns the description node of the most recent
     * <em>act.patientWeight</em> for a patient.
     *
     * @param patient the patient
     * @return the description node or <tt>null</tt> if no act can be found
     */
    public String getPatientWeight(Party patient) {
        String result = null;
        ShortNameConstraint shortName
                = new ShortNameConstraint("act", PATIENT_WEIGHT);
        ArchetypeQuery query = new ArchetypeQuery(shortName);
        query.add(new NodeSelectConstraint("act.description"));
        CollectionNodeConstraint participation
                = new CollectionNodeConstraint("patient",
                                               PATIENT_PARTICIPATION, true,
                                               true);
        participation.add(new ObjectRefNodeConstraint(
                "entity", patient.getObjectReference()));
        participation.add(new ParticipationConstraint(ActShortName,
                                                      PATIENT_WEIGHT));
        query.add(participation);
        query.add(new NodeSortConstraint("startTime", false));
        query.setMaxResults(1);
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(query);
        ObjectSet set = (iterator.hasNext()) ? iterator.next() : null;
        if (set != null) {
            result = (String) set.get("act.description");
        }
        return result;
    }

    /**
     * Returns the description node of the most recent
     * <em>act.patientWeight</em> for a patient asscoaited with an Act.
     *
     * @param act the act linked to the patient
     * @return the description node or <tt>null</tt> if no act can be found
     */
    public String getPatientWeight(Act act) {
        ActBean bean = new ActBean(act, service);
        Party patient = (Party) bean.getParticipant("participation.patient");
        return getPatientWeight(patient);
    }

    /**
     * Returns the most recent microchip number for a patient.
     *
     * @param patient the patient
     * @return the most recent microchip number, or <tt>null<tt> if none is
     *         found patient
     */
    public String getMicrochip(Party patient) {
        for (EntityIdentity identity : patient.getIdentities()) {
            if (TypeHelper.isA(identity, "entityIdentity.microchip")) {
                return identity.getIdentity();
            }
        }
        return null;
    }

    /**
     * Returns the most recent pet tag for a patient.
     *
     * @param patient the patient
     * @return the most recent pet Tag, or <tt>null<tt> if none is
     *         found patient
     */
    public String getPetTag(Party patient) {
        for (EntityIdentity identity : patient.getIdentities()) {
            if (TypeHelper.isA(identity, "entityIdentity.petTag")) {
                return identity.getIdentity();
            }
        }
        return null;
    }

    /**
     * Merges two patients.
     *
     * @param from the patient to merge
     * @param to   the patient to merge to
     * @throws MergeException            if the patients cannot be merged
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void mergePatients(Party from, Party to) {
        PatientMerger merger = new PatientMerger(service);
        merger.merge(from, to);
    }

    /**
     * Determines if the first relationship has a closer start time than the
     * second to the specified start time.
     *
     * @param startTime the start time
     * @param r1        the first relationship
     * @param r2        the second relationship
     * @return <tt>true</tt> if the first relationship has a closer start time
     */
    private boolean closerTime(Date startTime, EntityRelationship r1,
                               EntityRelationship r2) {
        long time = getTime(startTime);
        long diff1 = Math.abs(time - getTime(r1.getActiveStartTime()));
        long diff2 = Math.abs(time - getTime(r2.getActiveStartTime()));
        return diff1 < diff2;
    }

    /**
     * Returns the time in milliseconds from a <tt>Date</tt>.
     *
     * @param date the date. May be <tt>null</tt>
     * @return the time or <tt>0</tt> if the date is <tt>null</tt>
     */
    private long getTime(Date date) {
        return (date != null) ? date.getTime() : 0;
    }

    /**
     * Helper to return a party given its reference.
     *
     * @param ref the reference. May be <tt>null</tt>
     * @return the corresponding party or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any error
     */
    private Party get(IMObjectReference ref) {
        if (ref != null) {
            return (Party) service.get(ref);
        }
        return null;
    }
}