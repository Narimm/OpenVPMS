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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;


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
     * Patient owner relationship short name.
     */
    private static final String PATIENT_OWNER
            = "entityRelationship.patientOwner";


    /**
     * Constructs a new <code>PatientRules</code>.
     *
     * @throws ArchetypeServiceException if the archetype service is not
     *                                   configured
     */
    public PatientRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Construct a new <code>PatientRules/code>.
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
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addPatientOwnerRelationship(Party customer, Party patient) {
        EntityRelationship relationship
                = (EntityRelationship) service.create(PATIENT_OWNER);
        relationship.setActiveStartTime(new Date());
        relationship.setSequence(1);
        relationship.setSource(new IMObjectReference(customer));
        relationship.setTarget(new IMObjectReference(patient));
        customer.addEntityRelationship(relationship);
        patient.addEntityRelationship(relationship);
    }

    /**
     * Returns the owner of a patient associated with an act.
     *
     * @param act the act
     * @return the patient's owner, or <code>null</code> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Act act) {
        ActBean bean = new ActBean(act, service);
        Party patient = (Party) bean.getParticipant("participation.patient");
        Date startTime = act.getActivityStartTime();
        if (patient != null && startTime != null) {
            EntityBean patientBean = new EntityBean(patient, service);
            return (Party) patientBean.getSourceEntity(PATIENT_OWNER,
                                                       startTime);
        }
        return null;
    }

    /**
     * Returns the owner of a patient.
     *
     * @param patient the patient
     * @return the patient's owner, or <code>null</code> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Party patient) {
        EntityBean bean = new EntityBean(patient, service);
        return (Party) bean.getSourceEntity(PATIENT_OWNER);
    }

    /**
     * Determines if a patient has a customer as its owner.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return <code>true</code> if the customer is the owner of the patient
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
     * @return the referral vet, or <code>null</code> if none is founds
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
            bean.save();
        }
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
            long diffdays = diffMs / DateUtils.MILLIS_IN_DAY;
            if (diffdays < 90) {
                long weeks = diffdays / 7;
                if (weeks == 0) {
                    result = diffdays + " Days";
                } else {
                    result = weeks + " Weeks";
                }
            } else if (diffdays < (365 * 2)) {
                result = (diffdays / 31) + " Months";
            } else {
                result = (diffdays / 365) + " Years";
            }
        } else {
            result = "No Birthdate";
        }
        return result;
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
                = new ShortNameConstraint("act", "act.patientWeight");
        ArchetypeQuery query = new ArchetypeQuery(shortName);
        query.add(new NodeSelectConstraint("act.description"));
        CollectionNodeConstraint participation
                = new CollectionNodeConstraint("patient",
                                               "participation.patient", true,
                                               true);
        ObjectRefNodeConstraint patientRef = new ObjectRefNodeConstraint(
                "entity", patient.getObjectReference());
        participation.add(patientRef);
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

}