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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.CageType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gte;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lte;

/**
 * A collection of {@link Visit}s for a customer's pets.
 *
 * @author Tim Anderson
 */
class Visits implements Iterable<Visit> {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The appointment rules.
     */
    private final AppointmentRules appointmentRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The visits.
     */
    private List<Visit> visits = new ArrayList<>();

    /**
     * Constructs a {@link Visits}.
     *
     * @param customer         the customer
     * @param appointmentRules the appointment rules
     * @param patientRules     the patient rules
     * @param service          the archetype service
     */
    public Visits(Party customer, AppointmentRules appointmentRules, PatientRules patientRules,
                  IArchetypeService service) {
        this.customer = customer;
        this.appointmentRules = appointmentRules;
        this.patientRules = patientRules;
        this.service = service;
    }

    /**
     * Creates a new visit.
     *
     * @param event       the <em>act.patientClinicalEvent</em>
     * @param appointment the appointment. May be {@code null}
     * @return a new visit
     */
    public Visit create(Act event, Act appointment) {
        return new Visit(event, appointment, appointmentRules, patientRules, service);
    }

    /**
     * Adds a visit.
     *
     * @param event       the <em>act.patientClinicalEvent</em>
     * @param appointment the appointment. May be {@code null}
     */
    public void add(Act event, Act appointment) {
        visits.add(create(event, appointment));
    }

    /**
     * Adds a visit.
     *
     * @param visit the visit
     */
    public void add(Visit visit) {
        visits.add(visit);
    }

    /**
     * Returns an iterator over the visits.
     *
     * @return a new iterator
     */
    @Override
    public Iterator<Visit> iterator() {
        return visits.iterator();
    }

    /**
     * Adds a collection of visits.
     *
     * @param visits the visits to add
     */
    public void addAll(List<Visit> visits) {
        this.visits.addAll(visits);
    }

    /**
     * Returns the patients associated with the events.
     *
     * @return the patients associated with the events
     */
    public Set<Reference> getPatients() {
        Set<Reference> patients = new LinkedHashSet<>(); // TODO - should guarantee no duplicate patients
        for (Visit visit : visits) {
            IMObjectBean bean = service.getBean(visit.getEvent());
            Reference patient = bean.getTargetRef("patient");
            if (patient != null) {
                patients.add(patient);
            }
        }
        return patients;
    }

    /**
     * Determines if there are no visits.
     *
     * @return {@code true} if there are no visits
     */
    public boolean isEmpty() {
        return visits.isEmpty();
    }

    /**
     * Reloads the appointments to pick up any changes.
     */
    public void reload() {
        List<Visit> reloaded = new ArrayList<>();
        for (Visit visit : visits) {
            boolean isFirstPet = visit.isFirstPet();
            Act event = visit.getEvent();
            Act appointment = IMObjectHelper.reload(visit.getAppointment());

            visit = create(event, appointment);
            visit.setFirstPet(isFirstPet);
            reloaded.add(visit);
        }
        visits = reloaded;
    }

    /**
     * Determines the rates (i.e. first pet or second pet) to charge each visit.
     * <p/>
     * This takes into account visits that may have already been billed or completed.
     *
     * @param visits  the visits
     * @param endTime the end time to use, if a visit hasn't been completed
     */
    public void rate(List<Visit> visits, Date endTime) {
        Map<VisitKey, List<Visit>> map = new HashMap<>();
        for (Visit visit : visits) {
            VisitKey key = VisitKey.create(visit, endTime);
            if (key != null) {
                List<Visit> list = map.computeIfAbsent(key, k -> new ArrayList<>());
                list.add(visit);
            }
        }
        for (Map.Entry<VisitKey, List<Visit>> entry : map.entrySet()) {
            List<Visit> incomplete = entry.getValue();
            if (firstPetRateCharged(entry.getKey(), incomplete)) {
                for (Visit visit : incomplete) {
                    visit.setFirstPet(false);
                }
            } else {
                sortOnWeight(incomplete);
                boolean first = true;
                for (Visit visit : incomplete) {
                    visit.setFirstPet(first);
                    first = false;
                }
            }
        }
    }

    /**
     * Saves the visits.
     */
    public void save() {
        for (Visit visit : visits) {
            visit.save();
        }
    }

    /**
     * Sorts visits on patient weight.
     * <p/>
     * If a patient doesn't have a weight recorded, or weights are equal, the patient id will be used.
     *
     * @param visits the visits to sort
     */
    private void sortOnWeight(List<Visit> visits) {
        visits.sort((o1, o2) -> {
            BigDecimal weight1 = o1.getWeight().toKilograms();
            BigDecimal weight2 = o2.getWeight().toKilograms();
            int result = weight1.compareTo(weight2);
            if (result == 0) {
                result = Long.compare(o1.getPatient().getId(), o2.getPatient().getId());
            } else {
                // sort on descending weight
                result = -result;
            }
            return result;
        });
    }

    /**
     * Determines if there is another appointment that has already been charged for the same dates at the first pet
     * rate.
     *
     * @param key     the visit key
     * @param exclude visits to exclude from comparison
     * @return {@code true} if another appointment has been charged
     */
    private boolean firstPetRateCharged(VisitKey key, List<Visit> exclude) {
        boolean result = false;
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT);
        query.add(join("patient").add(join("entity").add(join("customers").add(eq("source", customer)))));
        JoinConstraint appointmentJoin = join("source");
        appointmentJoin.add(Constraints.ne("status", ActStatus.CANCELLED));
        appointmentJoin.add(join("schedule").add(eq("entity", key.schedule)));
        query.add(join("appointment").add(appointmentJoin));
        query.add(gte("startTime", key.start));
        query.add(Constraints.lt("startTime", DateRules.getNextDate(key.start)));
        query.add(gte("endTime", key.end));
        query.add(lte("endTime", DateRules.getNextDate(key.end)));
        // NOTE: can end at midnight and still be treated as same day
        List<Long> ids = new ArrayList<>();
        for (Visit visit : exclude) {
            ids.add(visit.getEvent().getId());
        }
        query.add(Constraints.not(Constraints.in("id", ids.toArray())));

        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            Act event = iterator.next();
            IMObjectBean bean = service.getBean(event);
            Act appointment = bean.getSource("appointment", Act.class);
            if (appointment != null) {
                IMObjectBean appointmentBean = service.getBean(appointment);
                if (appointmentBean.getBoolean(Visit.BOARDING_CHARGED)
                    && appointmentBean.getBoolean(Visit.FIRST_PET_RATE)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private static class VisitKey {

        private final Reference schedule;

        private final Date start;

        private final Date end;

        VisitKey(Reference schedule, Date startTime, Date endTime) {
            this.schedule = schedule;
            this.start = DateRules.getDate(startTime);
            Date date = DateRules.getDate(endTime);
            if (DateRules.compareTo(date, endTime) == 0) { // midnight
                date = DateRules.getPreviousDate(date);
            }
            this.end = date;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof VisitKey) {
                VisitKey other = (VisitKey) obj;
                return schedule.equals(other.schedule) && start.equals(other.start) && end.equals(other.end);
            }
            return false;
        }

        /**
         * Returns a hash code value for the object. This method is
         * supported for the benefit of hash tables such as those provided by
         * {@link HashMap}.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(schedule).append(start).append(end).toHashCode();
        }

        public static VisitKey create(Visit visit, Date endTime) {
            Reference schedule = visit.getScheduleRef();
            if (visit.getEndTime() != null) {
                endTime = visit.getEndTime();
            }
            if (schedule != null && endTime != null && visit.getCageType() != null) {
                CageType cageType = visit.getCageType();
                if (cageType.hasSecondPetProducts()) {
                    return new VisitKey(schedule, visit.getStartTime(), endTime);
                }
            }
            return null;
        }
    }

}
