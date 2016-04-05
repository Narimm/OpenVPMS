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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.recordlocking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.component.system.common.event.Listener;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.resource.i18n.Messages;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PreDestroy;

/**
 * Schedules the {@link MedicalRecordLockingJob}, based on the practice record lock period.
 *
 * @author Tim Anderson
 */
public class MedicalRecordLockingScheduler {

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
    private final Listener<Party> listener;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MedicalRecordLockingScheduler.class);

    /**
     * The job archetype short name.
     */
    private static final String SHORT_NAME = "entity.jobMedicalRecordLocker";

    /**
     * Constructs an {@link MedicalRecordLockingScheduler}.
     *
     * @param service            the archetype service
     * @param practiceService    the practice service
     * @param rules              the practice rules
     * @param transactionManager the transaction manager
     */
    public MedicalRecordLockingScheduler(IArchetypeService service, PracticeService practiceService,
                                         PracticeRules rules, PlatformTransactionManager transactionManager) {
        this.service = service;
        this.practiceService = practiceService;
        this.rules = rules;
        this.transactionManager = transactionManager;

        Party practice = practiceService.getPractice();
        if (practice != null) {
            init(practice);
        } else {
            log.error("Medical record locking cannot be enabled until a Practice is configured");
        }
        listener = new Listener<Party>() {
            @Override
            public void onEvent(Party practice) {
                init(practice);
            }
        };
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
    private void init(final Party practice) {
        final User user = practiceService.getServiceUser();
        if (user == null) {
            log.error("Medical record locking cannot be enabled until a Practice Service User is configured");
        } else {
            RunAs.run(user, new Runnable() {
                @Override
                public void run() {
                    init(practice, user);
                }
            });
        }
    }

    /**
     * Initialises the scheduler.
     *
     * @param practice the practice
     * @param user     the user to send audit messages to
     */
    private void init(final Party practice, final User user) {
        final Period period = rules.getRecordLockPeriod(practice);
        if (period == null) {
            disable();
            audit(Messages.get("recordlocking.disabled.subject"), Messages.get("recordlocking.disabled.message"), user);
        } else {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    enable(user);
                    String periodStr = PeriodFormat.getDefault().print(period);
                    audit(Messages.get("recordlocking.enabled.subject"),
                          Messages.format("recordlocking.enabled.message", periodStr), user);
                }
            });
        }
    }

    /**
     * Sends an audit message.
     *
     * @param subject the audit message subject
     * @param message the audit message text
     * @param user    the user to send the message to
     */
    private void audit(String subject, String message, User user) {
        log.info(subject);
        log.info(message);
        Act act = (Act) service.create(MessageArchetypes.AUDIT);
        ActBean bean = new ActBean(act, service);
        bean.setNodeParticipant("to", user);
        bean.setValue("reason", "MEDICAL_RECORD_LOCKING");
        bean.setValue("description", subject);
        bean.setValue("message", message);
        bean.save();
    }

    /**
     * Enables locking.
     * <p/>
     * This will activate an existing job if one is available. If not, it will create a new job.
     *
     * @param user the user the job should be run as
     */
    private void enable(User user) {
        IMObjectQueryIterator<IMObject> active = getJobs(true);
        if (!active.hasNext()) {
            // there is currently no active job. Try and activate one
            IMObjectQueryIterator<IMObject> inactive = getJobs(false);
            if (inactive.hasNext()) {
                IMObject object = inactive.next();
                setActive(object, true);
            } else {
                IMObject config = service.create(SHORT_NAME);
                IMObjectBean bean = new IMObjectBean(config, service);
                bean.addNodeTarget("runAs", user);
                bean.save();
            }
        }
    }

    /**
     * Disables all active locking jobs.
     */
    private void disable() {
        IMObjectQueryIterator<IMObject> iterator = getJobs(true);
        try {
            while (iterator.hasNext()) {
                IMObject object = iterator.next();
                setActive(object, false);
            }
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
    private void setActive(final IMObject object, boolean active) {
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
        ArchetypeQuery query = new ArchetypeQuery(SHORT_NAME, active);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        query.add(Constraints.sort("id"));
        return new IMObjectQueryIterator<>(service, query);
    }

}
