/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.party.MergeException;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ParticipationConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_LOCATION;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_WEIGHT;
import static org.openvpms.component.system.common.query.Constraints.and;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lte;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.component.system.common.query.Constraints.sort;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;


/**
 * Patient rules.
 *
 * @author Tim Anderson
 */
public class PatientRules {

    /**
     * Allergy alert type code.
     */
    public static final String ALLERGY_ALERT_TYPE = "ALLERGY";

    /**
     * Aggression alert type code.
     */
    public static final String AGGRESSION_ALERT_TYPE = "AGGRESSION";

    /**
     * The practice rules.
     */
    private final PracticeRules rules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Patient age formatter.
     */
    private PatientAgeFormatter formatter;

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * Helper functions.
     */
    private ArchetypeServiceFunctions functions;


    /**
     * Constructs a {@link PatientRules}.
     *
     * @param rules   the practice rules
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public PatientRules(PracticeRules rules, IArchetypeService service, ILookupService lookups) {
        this(rules, service, lookups, null);
    }

    /**
     * Constructs a {@link PatientRules}.
     *
     * @param rules     the practice rules
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param formatter the patient age formatter. May be {@code null}
     */
    public PatientRules(PracticeRules rules, IArchetypeService service, ILookupService lookups,
                        PatientAgeFormatter formatter) {
        this.rules = rules;
        this.service = service;
        this.lookups = lookups;
        this.formatter = formatter;
        factory = new IMObjectBeanFactory(service);
        functions = new ArchetypeServiceFunctions(service, lookups);
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
        EntityBean bean = factory.createEntityBean(customer);
        EntityRelationship relationship = bean.addRelationship(PatientArchetypes.PATIENT_OWNER, patient);
        relationship.setActiveStartTime(new Date());
        return relationship;
    }

    /**
     * Adds a patient-location relationship between the supplied customer and patient.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return the relationship
     * @throws ArchetypeServiceException for any archetype service error
     */
    public EntityRelationship addPatientLocationRelationship(Party customer, Party patient) {
        EntityBean bean = factory.createEntityBean(customer);
        EntityRelationship relationship = bean.addRelationship(PatientArchetypes.PATIENT_LOCATION, patient);
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
     * @return the patient's owner, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Act act) {
        ActBean bean = factory.createActBean(act);
        Party patient = (Party) bean.getParticipant(PatientArchetypes.PATIENT_PARTICIPATION);
        Date startTime = act.getActivityStartTime();
        return getOwner(patient, startTime, false);
    }

    /**
     * Returns the owner of a patient.
     *
     * @param patient the patient
     * @return the patient's owner, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Party patient) {
        return getOwner(patient, new Date(), true);
    }

    /**
     * Returns the most current owner of a patient associated with an act.
     *
     * @param act the act
     * @return the patient's owner, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getCurrentOwner(Act act) {
        ActBean bean = factory.createActBean(act);
        Party patient = (Party) bean.getParticipant("participation.patient");
        return getOwner(patient, new Date(), true);
    }

    /**
     * Returns the owner of a patient for a specified date
     *
     * @param patient   the patient
     * @param startTime the date to search for the ownership
     * @param active    only check active ownerships
     * @return the patient's owner, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Party patient, Date startTime, boolean active) {
        return getSourceParty(patient, startTime, active, PatientArchetypes.PATIENT_OWNER);
    }

    /**
     * Returns a reference to the owner of a patient.
     *
     * @param patient the patient
     * @return a reference to the owner, or {@code null} if none can be found
     */
    public IMObjectReference getOwnerReference(Party patient) {
        EntityBean bean = factory.createEntityBean(patient);
        Predicate predicate = AndPredicate.getInstance(new IsA(PatientArchetypes.PATIENT_OWNER),
                                                       IsActiveRelationship.isActiveNow());
        EntityRelationship er = bean.getNodeRelationship("customers", predicate);
        return (er != null && er.isActive()) ? er.getSource() : null;
    }

    /**
     * Determines if a patient has a customer as its owner.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return {@code true} if the customer is the owner of the patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isOwner(Party customer, Party patient) {
        Party owner = getOwner(patient);
        return (owner != null && owner.equals(customer));
    }

    /**
     * Returns the location of a patient associated with an act.
     *
     * @param act the act
     * @return the patient location, or {@code null} if none is found
     */
    public Party getLocation(Act act) {
        ActBean bean = factory.createActBean(act);
        Party patient = (Party) bean.getParticipant(PatientArchetypes.PATIENT_PARTICIPATION);
        Date startTime = act.getActivityStartTime();
        return getLocation(patient, startTime, false);
    }

    /**
     * Returns the location of a patient.
     *
     * @param patient the patient
     * @return the patient's location, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getLocation(Party patient) {
        return getLocation(patient, new Date(), true);
    }

    /**
     * Returns the current location of a patient associated with an act.
     *
     * @param act the act
     * @return the patient's location, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getCurrentLocation(Act act) {
        ActBean bean = factory.createActBean(act);
        Party patient = (Party) bean.getParticipant("participation.patient");
        return getLocation(patient, new Date(), true);
    }

    /**
     * Returns the location of a patient for a specified date.
     *
     * @param patient   the patient
     * @param startTime the date to search for the location relationships
     * @param active    if {@code true}, only check active relationships
     * @return the patient's location, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getLocation(Party patient, Date startTime, boolean active) {
        return getSourceParty(patient, startTime, active, PATIENT_LOCATION);
    }

    /**
     * Returns the referral vet for a patient.
     * This is the associated party from the first matching
     * <em>entityRelationship.referredFrom</em> or
     * <em>entityrRelationship.referredTo</em> overlapping the specified time.
     *
     * @param patient the patient
     * @param time    the time
     * @return the referral vet, or {@code null} if none is founds
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getReferralVet(Party patient, Date time) {
        EntityBean bean = factory.createEntityBean(patient);
        return (Party) bean.getNodeTargetEntity("referrals", time);
    }

    /**
     * Marks a patient as being inactive.
     *
     * @param patient the patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void setInactive(Party patient) {
        IMObjectBean bean = factory.createBean(patient);
        if (bean.getBoolean("active")) {
            bean.setValue("active", false);
            bean.save();
        }
    }

    /**
     * Marks a patient as being deceased.
     *
     * @param patient the patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void setDeceased(Party patient) {
        EntityBean bean = factory.createEntityBean(patient);
        if (!bean.getBoolean("deceased")) {
            bean.setValue("deceased", true);
            bean.setValue("active", false);
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
     * @return {@code true} if the patient is deceased
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isDeceased(Party patient) {
        IMObjectBean bean = factory.createBean(patient);
        return bean.getBoolean("deceased");
    }

    /**
     * Marks a patient as being desexed.
     *
     * @param patient the patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void setDesexed(Party patient) {
        IMObjectBean bean = factory.createBean(patient);
        if (!bean.getBoolean("desexed")) {
            bean.setValue("desexed", true);
            bean.save();
        }
    }

    /**
     * Determines if a patient is desexed.
     *
     * @param patient the patient
     * @return {@code true} if the patient is desexed
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isDesexed(Party patient) {
        IMObjectBean bean = factory.createBean(patient);
        return bean.getBoolean("desexed");
    }

    /**
     * Returns the Desex status of the patient.
     *
     * @param patient the patient
     * @return the desex status in string format
     * @throws ArchetypeServiceException for any archetype service error
     *                                   todo - should be localised
     */
    public String getPatientDesexStatus(Party patient) {
        if (patient != null) {
            if (isDesexed(patient)) {
                return "Desexed";
            } else {
                return "Entire";
            }
        } else {
            return "";
        }
    }

    /**
     * Returns the Desex status of the patient associated with an act.
     *
     * @param act the act connected to the patient
     * @return the age in string format
     * @throws ArchetypeServiceException for any archetype service error
     *                                   todo - should be localised
     */
    public String getPatientDesexStatus(Act act) {
        ActBean bean = factory.createActBean(act);
        Party patient = (Party) bean.getParticipant("participation.patient");
        return getPatientDesexStatus(patient);
    }

    /**
     * Returns the patient date of birth.
     *
     * @param patient the patient
     * @return the patient's date of birth. May be {@code null}
     */
    public Date getDateOfBirth(Party patient) {
        IMObjectBean bean = factory.createBean(patient);
        return bean.getDate("dateOfBirth");
    }

    /**
     * Returns the age of the patient.
     * <p>
     * If the patient is deceased, the age of the patient when they died will be returned
     *
     * @param patient the patient
     * @return the age in string format
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPatientAge(Party patient) {
        return getPatientAge(patient, new Date());
    }

    /**
     * Returns the age of the patient as of the specified date.
     * <p>
     * If the patient is deceased, the age of the patient when they died will be returned
     *
     * @param patient the patient
     * @param date    the date to base the age upon
     * @return the age in string format
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPatientAge(Party patient, Date date) {
        String result;
        IMObjectBean bean = factory.createBean(patient);
        Date birthDate = bean.getDate("dateOfBirth");
        Date deceasedDate = bean.getDate("deceasedDate");
        synchronized (this) {
            if (formatter == null) {
                // TODO - this is a hack, but requires refactoring of rules into services to make better
                // use of dependency injection
                formatter = new PatientAgeFormatter(lookups, rules, factory);
            }
        }
        if (deceasedDate == null) {
            result = formatter.format(birthDate, date);
        } else {
            if (DateRules.compareTo(deceasedDate, date) < 0) {
                date = deceasedDate;
            }
            result = formatter.format(birthDate, date);
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
        return functions.lookup(patient, "species");
    }

    /**
     * Returns the breed of the patient.
     *
     * @param patient the patient
     * @return the species in string format
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPatientBreed(Party patient) {
        return functions.lookup(patient, "breed");
    }

    /**
     * Returns the sex of the patient.
     *
     * @param patient the patient
     * @return the sex in string format
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPatientSex(Party patient) {
        return functions.lookup(patient, "sex");
    }

    /**
     * Returns the patient's colour.
     *
     * @param patient the patient
     * @return the colour. May be {@code null}
     */
    public String getPatientColour(Party patient) {
        IMObjectBean bean = new IMObjectBean(patient, service);
        return bean.getString("colour");
    }

    /**
     * Returns the description node of the most recent
     * <em>act.patientWeight</em> for a patient.
     *
     * @param patient the patient
     * @return the description node or {@code null} if no act can be found
     */
    public String getPatientWeight(Party patient) {
        String result = null;
        ArchetypeQuery query = createWeightQuery(patient);
        query.add(new NodeSelectConstraint("act.description"));
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service, query);
        ObjectSet set = (iterator.hasNext()) ? iterator.next() : null;
        if (set != null) {
            result = (String) set.get("act.description");
        }
        return result;
    }

    /**
     * Returns the description node of the most recent
     * <em>act.patientWeight</em> for a patient associated with an Act.
     *
     * @param act the act linked to the patient
     * @return the description node or {@code null} if no act can be found
     */
    public String getPatientWeight(Act act) {
        ActBean bean = factory.createActBean(act);
        Party patient = (Party) bean.getParticipant("participation.patient");
        return getPatientWeight(patient);
    }

    /**
     * Returns the patient's weight.
     * <p>
     * This uses the most recent recorded weight for the patient.
     *
     * @param patient the patient
     * @return the patient's weight, or {@code 0} if its weight is not known
     */
    public Weight getWeight(Party patient) {
        Weight weight = Weight.ZERO;
        Act act = getWeightAct(patient);
        if (act != null) {
            weight = getWeight(act);
        } else {
            NodeDescriptor node = DescriptorHelper.getNode(PatientArchetypes.PATIENT_WEIGHT, "weightUnits", service);
            if (node != null && node.getDefaultValue() != null) {
                JXPathContext context = JXPathHelper.newContext(new Object());
                Object units = context.getValue(node.getDefaultValue());
                if (units != null) {
                    weight = new Weight(BigDecimal.ZERO, WeightUnits.valueOf(units.toString()));
                }
            }
        }
        return weight;
    }

    /**
     * Returns a patient's weight.
     *
     * @param act an <em>act.patientWeight</em>
     * @return the patient's weight, or {@code 0} if its weight is not known
     */
    public Weight getWeight(Act act) {
        IMObjectBean bean = new IMObjectBean(act, service);
        String units = bean.getString("units", WeightUnits.KILOGRAMS.toString());
        return new Weight(bean.getBigDecimal("weight", BigDecimal.ZERO), WeightUnits.valueOf(units),
                          act.getActivityStartTime());
    }

    /**
     * Returns the most recent <em>act.patientWeight</em> for a patient.
     *
     * @param patient the patient
     * @return the most recent weight act, or {@code null} if none is found
     */
    public Act getWeightAct(Party patient) {
        ArchetypeQuery query = createWeightQuery(patient);
        Iterator<Act> iterator = new IMObjectQueryIterator<>(service, query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }

    /**
     * Returns the most recent active microchip number for a patient.
     *
     * @param patient the patient
     * @return the most recent microchip number, or {@code null} if none is found
     */
    public String getMicrochipNumber(Party patient) {
        return getIdentity(patient, "entityIdentity.microchip");
    }

    /**
     * Returns the active microchip numbers for a patient, separated by commas.
     *
     * @param patient the patient
     * @return the active microchip numbers, or {@code null} if none is found
     */
    public String getMicrochipNumbers(Party patient) {
        String result = null;
        Collection<EntityIdentity> identities = getIdentities(patient, "entityIdentity.microchip");
        for (EntityIdentity identity : identities) {
            if (result == null) {
                result = identity.getIdentity();
            } else {
                result += ", " + identity.getIdentity();
            }
        }
        return result;
    }

    /**
     * Returns the most recent active microchip identity for a patient.
     *
     * @param patient the patient
     * @return the active microchip object, or {@code null} if none is found
     */
    public EntityIdentity getMicrochip(Party patient) {
        return getEntityIdentity(patient, "entityIdentity.microchip");
    }

    /**
     * Returns the most recent active pet tag for a patient.
     *
     * @param patient the patient
     * @return the most recent pet tag, or {@code null} if none is found
     */
    public String getPetTag(Party patient) {
        return getIdentity(patient, "entityIdentity.petTag");
    }

    /**
     * Returns the most recent active rabies tag for a patient.
     *
     * @param patient the patient
     * @return the most recent rabies tag, or {@code null} if none is found
     */
    public String getRabiesTag(Party patient) {
        return getIdentity(patient, "entityIdentity.rabiesTag");
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
     * Returns all IN_PROGRESS allergy alerts for a patient, active at the specified date.
     * <p>
     * An alert is considered an allergy allert if it has an alert type coded as {@link #ALLERGY_ALERT_TYPE}.
     *
     * @param patient the patient
     * @param date    the date
     * @return the patient alerts
     */
    public List<Act> getAllergies(Party patient, Date date) {
        List<Act> acts = new ArrayList<>();
        ArchetypeQuery query = createAlertQuery(patient, ALLERGY_ALERT_TYPE, date);
        IMObjectQueryIterator<Act> alerts = new IMObjectQueryIterator<>(service, query);
        while (alerts.hasNext()) {
            acts.add(alerts.next());
        }
        return acts;
    }

    /**
     * Determines if an alert is for an allergy.
     *
     * @param alert the alert
     * @return {@code true} if the alert is for an allergy
     */
    public boolean isAllergy(Act alert) {
        boolean result = false;
        ActBean bean = new ActBean(alert, service);
        Entity alertType = bean.getNodeParticipant("alertType");
        if (alertType != null) {
            IMObjectBean alertBean = new IMObjectBean(alertType, service);
            IMObject lookup = alertBean.getValue("class", new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    return (object instanceof Lookup) && ALLERGY_ALERT_TYPE.equals(((Lookup) object).getCode());
                }
            });
            result = lookup != null;
        }
        return result;
    }

    /**
     * Determines if a patient is aggressive.
     * <p>
     * A patient is aggressive if there is an IN_PROGRESS act.patientAlert active at the current time, with an
     * alert type coded as {@link #AGGRESSION_ALERT_TYPE}.
     *
     * @param patient the patient
     * @return {@code true} if a patient is aggressive
     */
    public boolean isAggressive(Party patient) {
        ArchetypeQuery query = createAlertQuery(patient, AGGRESSION_ALERT_TYPE, new Date());
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> alerts = new IMObjectQueryIterator<>(service, query);
        return alerts.hasNext();
    }

    /**
     * Returns the source of a patient relationship closest to the specified start time.
     *
     * @param patient   the patient. May be {@code null}
     * @param startTime the relationship start time. May be {@code null}
     * @param active    determines if the party must be active or not
     * @param shortName the relationship short name
     * @return the source, or {@code null} if none is found
     */
    private Party getSourceParty(Party patient, Date startTime, boolean active, String shortName) {
        Party result = null;
        if (patient != null && startTime != null) {
            EntityBean bean = factory.createEntityBean(patient);
            result = (Party) bean.getSourceEntity(shortName, startTime, false);
            if (result == null && !active) {
                // no match for the start time, so try and find a source close to the start time
                EntityRelationship match = null;
                List<EntityRelationship> relationships = bean.getRelationships(shortName, false);

                for (EntityRelationship relationship : relationships) {
                    if (match == null) {
                        result = get(relationship.getSource());
                        if (result != null) {
                            match = relationship;
                        }
                    } else {
                        if (closerTime(startTime, relationship, match)) {
                            Party party = get(relationship.getSource());
                            if (party != null) {
                                result = party;
                                match = relationship;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Creates a query for IN_PROGRESS alerts, active at the specified time.
     *
     * @param patient the patient
     * @param code    the alert code
     * @param date    the date
     * @return a new query
     */
    private ArchetypeQuery createAlertQuery(Party patient, String code, Date date) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.ALERT);
        query.add(eq("status", ActStatus.IN_PROGRESS));
        query.add(join("patient").add(eq("entity", patient)));
        query.add(join("alertType").add(join("entity").add(join("class", "clazz").add(eq("code", code)))));
        query.add(and(lte("startTime", date), or(Constraints.gt("endTime", date), isNull("endTime"))));
        query.add(sort("startTime"));
        query.add(sort("id"));
        return query;
    }

    /**
     * Helper to create a query to return the most recent <em>act.patientWeight</em> for a patient.
     *
     * @param patient the patient
     * @return the query
     */
    private ArchetypeQuery createWeightQuery(Party patient) {
        ArchetypeQuery query = new ArchetypeQuery(Constraints.shortName("act", PATIENT_WEIGHT));
        JoinConstraint participation = join("patient");
        participation.add(eq("entity", patient));
        participation.add(new ParticipationConstraint(ActShortName, PATIENT_WEIGHT));
        query.add(participation);
        query.add(sort("startTime", false));
        query.setMaxResults(1);
        return query;
    }

    /**
     * Determines if the first relationship has a closer start time than the
     * second to the specified start time.
     *
     * @param startTime the start time
     * @param r1        the first relationship
     * @param r2        the second relationship
     * @return {@code true} if the first relationship has a closer start time
     */
    private boolean closerTime(Date startTime, EntityRelationship r1,
                               EntityRelationship r2) {
        long time = getTime(startTime);
        long diff1 = Math.abs(time - getTime(r1.getActiveStartTime()));
        long diff2 = Math.abs(time - getTime(r2.getActiveStartTime()));
        return diff1 < diff2;
    }

    /**
     * Returns the time in milliseconds from a {@code Date}.
     *
     * @param date the date. May be {@code null}
     * @return the time or {@code 0} if the date is {@code null}
     */
    private long getTime(Date date) {
        return (date != null) ? date.getTime() : 0;
    }

    /**
     * Helper to return a party given its reference.
     *
     * @param ref the reference. May be {@code null}
     * @return the corresponding party or {@code null} if none can be found
     * @throws ArchetypeServiceException for any error
     */
    private Party get(IMObjectReference ref) {
        if (ref != null) {
            return (Party) service.get(ref);
        }
        return null;
    }

    /**
     * Returns the active identity with the specified short name.
     * If there are multiple identities, that with the highest id will be returned.
     *
     * @param patient   the patient
     * @param shortName the identity archetype short name
     * @return the identity, or {@code null} if none is found
     */
    private String getIdentity(Party patient, String shortName) {
        EntityIdentity result = getEntityIdentity(patient, shortName);
        return (result != null) ? result.getIdentity() : null;
    }

    /**
     * Returns the active {@link EntityIdentity} with the specified short name.
     * If there are multiple identities, that with the highest id will be returned.
     *
     * @param patient   the patient  may be {@code null}
     * @param shortName the identity archetype short name
     * @return the EntityIdentity, or {@code null} if none is found
     */
    private EntityIdentity getEntityIdentity(Party patient, String shortName) {
        EntityIdentity result = null;
        if (patient != null) {
            for (org.openvpms.component.model.entity.EntityIdentity identity : patient.getIdentities()) {
                if (identity.isActive() && identity.isA(shortName)) {
                    if (result == null || result.getId() < identity.getId()) {
                        result = (EntityIdentity) identity;
                    }
                }
            }
        }
        return result;

    }

    /**
     * Returns the active identity with the specified short name.
     * If there are multiple identities, these will be ordered with the highest id first.
     *
     * @param patient   the patient
     * @param shortName the identity archetype short name
     * @return the identities
     */
    @SuppressWarnings("unchecked")
    private Collection<EntityIdentity> getIdentities(Party patient, String shortName) {
        TreeMap<Long, EntityIdentity> result = new TreeMap<>(ComparatorUtils.reversedComparator(
                ComparatorUtils.NATURAL_COMPARATOR));
        for (org.openvpms.component.model.entity.EntityIdentity identity : patient.getIdentities()) {
            if (identity.isActive() && identity.isA(shortName)) {
                result.put(identity.getId(), (EntityIdentity) identity);
            }
        }
        return result.values();
    }

}
