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

package org.openvpms.web.jobs.pharmacy;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Period;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.system.common.event.Listener;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import javax.annotation.PreDestroy;

/**
 * Schedules the {@link PharmacyOrderDiscontinuationJob}, based on the practice Pharmacy Order Discontinue Period.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderDiscontinuationScheduler {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The practice rules.
     */
    private final PracticeRules rules;

    /**
     * The listener for practice updates.
     */
    private final Listener<PracticeService.Update> listener;

    /**
     * The discontinuation period, or {@code null} if scheduled discontinuation is disabled.
     */
    private Period period;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PharmacyOrderDiscontinuationScheduler.class);

    /**
     * Constructs an {@link PharmacyOrderDiscontinuationScheduler}.
     *
     * @param service         the archetype service
     * @param practiceService the practice service
     * @param rules           the practice rules
     */
    public PharmacyOrderDiscontinuationScheduler(IArchetypeService service, PracticeService practiceService,
                                                 PracticeRules rules) {
        this.service = service;
        this.practiceService = practiceService;
        this.rules = rules;

        Party practice = practiceService.getPractice();
        if (practice != null) {
            init(practice);
        } else {
            log.error("Pharmacy Order discontinuation cannot be enabled until a Practice is configured");
        }
        listener = update -> init(update.getPractice());
        practiceService.addListener(listener);
    }

    /**
     * Disposes of the service.
     */
    @PreDestroy
    public void dispose() {
        practiceService.removeListener(listener);
    }

    /**
     * Initialises the scheduler.
     *
     * @param practice the practice
     */
    private void init(Party practice) {
        User user = practiceService.getServiceUser();
        if (user == null) {
            log.error("Pharmacy Order discontinuation cannot be enabled until a Practice Service User is configured");
        } else {
            RunAs.run(user, () -> init(practice, user));
        }
    }

    /**
     * Initialises the scheduler.
     *
     * @param practice the practice
     * @param user     the user to run the job as
     */
    private void init(Party practice, User user) {
        Period newPeriod = rules.getPharmacyOrderDiscontinuePeriod(practice);
        if (newPeriod == null) {
            disable();
        } else {
            Period current;
            synchronized (this) {
                current = period;
            }
            if (!ObjectUtils.equals(current, newPeriod)) {
                enable(user, newPeriod);
            }
            synchronized (this) {
                period = newPeriod;
            }
        }
    }

    /**
     * Enables pharmacy order discontinuation.
     * <p/>
     * This will activate an existing job if one is available. If not, it will create a new job.
     *
     * @param user   the user the job should be run as
     * @param period the new period
     */
    private void enable(User user, Period period) {
        IMObjectQueryIterator<IMObject> active = getJobs(true);
        IMObject config;
        if (active.hasNext()) {
            config = active.next();
        } else {
            // there is currently no active job. Try and activate one
            IMObjectQueryIterator<IMObject> inactive = getJobs(false);
            if (inactive.hasNext()) {
                config = inactive.next();
                config.setActive(true);
            } else {
                // create a new one
                config = service.create(PharmacyOrderDiscontinuationJob.JOB_ARCHETYPE);
            }
        }

        // schedule twice as often as the discontinuation period, with a minimum of 5 minutes
        int total = period.toStandardMinutes().getMinutes();
        int totalDiv2 = total / 2;
        if (totalDiv2 < 5) {
            totalDiv2 = 5;
        }
        int hours = totalDiv2 / 60;
        int minutes = totalDiv2 - (hours * 60);
        IMObjectBean bean = service.getBean(config);
        if (config.isNew()) {
            bean.setTarget("runAs", user);
        }
        if (minutes != 0) {
            bean.setValue("minutes", "*/" + minutes);
        } else {
            bean.setValue("minutes", "0");
        }
        if (hours == 0) {
            bean.setValue("hours", "*");
        } else {
            bean.setValue("hours", "*/" + hours);
        }
        service.save(config);
        log.info("Pharmacy order discontinuation is scheduled to occur every " + totalDiv2
                 + " minutes for invoices that have been finalised for " + total + " minutes");
    }

    /**
     * Disables all active discontinuation jobs.
     */
    private void disable() {
        IMObjectQueryIterator<IMObject> iterator = getJobs(true);
        try {
            while (iterator.hasNext()) {
                IMObject object = iterator.next();
                if (object.isActive()) {
                    setActive(object, false);
                }
            }
            log.info("Pharmacy order discontinuation is set to occur at invoice finalisation");
        } catch (Throwable exception) {
            log.error("Failed to disable job", exception);
        }
    }

    /**
     * Activates/deactivates a job.
     *
     * @param object the job configuration
     * @param active if {@code true}, activate the job, otherwise deactivate it
     */
    private void setActive(IMObject object, boolean active) {
        object.setActive(active);
        service.save(object);
    }

    /**
     * Returns an iterator of locking jobs.
     *
     * @param active if {@code true}, the jobs must be active
     * @return a new iterator
     */
    private IMObjectQueryIterator<IMObject> getJobs(boolean active) {
        ArchetypeQuery query = new ArchetypeQuery(PharmacyOrderDiscontinuationJob.JOB_ARCHETYPE, active);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        query.add(Constraints.sort("id"));
        return new IMObjectQueryIterator<>(service, query);
    }

}
