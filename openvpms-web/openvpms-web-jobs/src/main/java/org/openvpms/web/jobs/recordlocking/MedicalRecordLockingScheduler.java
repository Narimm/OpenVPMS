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

import org.apache.commons.lang.ObjectUtils;
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
 * Schedules the {@link MedicalRecordLockerJob}, based on the practice record lock period.
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
    private final Listener<PracticeService.Update> listener;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The record locking period, or {@code null} if locking is disabled.
     */
    private Period period;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MedicalRecordLockingScheduler.class);

    /**
     * The job archetype short name.
     */
    protected static final String JOB_SHORT_NAME = "entity.jobMedicalRecordLocker";

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
            init(practice, null, true);
        } else {
            log.error("Medical record locking cannot be enabled until a Practice is configured");
        }
        listener = new Listener<PracticeService.Update>() {
            @Override
            public void onEvent(PracticeService.Update update) {
                init(update.getPractice(), update.getUser(), false);
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
     * @param practice  the practice
     * @param updatedBy the user that updated the practice. May be {@code null}
     * @param audit     if {@code true}, send an audit message, even if nothing has changed
     */
    private void init(final Party practice, final String updatedBy, final boolean audit) {
        final User user = practiceService.getServiceUser();
        if (user == null) {
            log.error("Medical record locking cannot be enabled until a Practice Service User is configured");
        } else {
            RunAs.run(user, new Runnable() {
                @Override
                public void run() {
                    init(practice, updatedBy, user, audit);
                }
            });
        }
    }

    /**
     * Initialises the scheduler.
     *
     * @param practice  the practice
     * @param updatedBy the user that updated the practice. May be {@code null}
     * @param user      the user to send audit messages to
     * @param audit     if {@code true}, send an audit message, even if nothing has changed
     */
    private void init(final Party practice, final String updatedBy, final User user, final boolean audit) {
        final Period newPeriod = rules.getRecordLockPeriod(practice);
        if (newPeriod == null) {
            boolean enabled = disable();
            if (audit || enabled) {
                String subject = updatedBy != null
                                 ? Messages.format("recordlocking.disabled.subjectby", updatedBy)
                                 : Messages.get("recordlocking.disabled.subject");
                audit(subject, Messages.get("recordlocking.disabled.message"), user);
            }
        } else {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    boolean disabled = enable(user);
                    Period current;
                    synchronized (this) {
                        current = period;
                    }
                    if (audit || disabled || !ObjectUtils.equals(current, newPeriod)) {
                        String subject = updatedBy != null && (disabled || !ObjectUtils.equals(current, newPeriod))
                                         ? Messages.format("recordlocking.enabled.subjectby", updatedBy)
                                         : Messages.get("recordlocking.enabled.subject");
                        String periodStr = PeriodFormat.getDefault().print(newPeriod);
                        audit(subject, Messages.format("recordlocking.enabled.message", periodStr), user);
                    }
                    synchronized (this) {
                        period = newPeriod;
                    }
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
     * @return {@code true} if locking was previously disabled, {@code false} if it wasn't
     */
    private boolean enable(User user) {
        boolean result = false;
        IMObjectQueryIterator<IMObject> active = getJobs(true);
        if (!active.hasNext()) {
            // there is currently no active job. Try and activate one
            IMObjectQueryIterator<IMObject> inactive = getJobs(false);
            if (inactive.hasNext()) {
                IMObject object = inactive.next();
                setActive(object, true);
            } else {
                IMObject config = service.create(JOB_SHORT_NAME);
                IMObjectBean bean = new IMObjectBean(config, service);
                bean.addNodeTarget("runAs", user);
                bean.save();
            }
            result = true;
        }
        return result;
    }

    /**
     * Disables all active locking jobs.
     *
     * @return {@code true} if locking was previously enabled, {@code false} if it wasn't
     */
    private boolean disable() {
        int count = 0;
        IMObjectQueryIterator<IMObject> iterator = getJobs(true);
        try {
            while (iterator.hasNext()) {
                IMObject object = iterator.next();
                if (object.isActive()) {
                    setActive(object, false);
                    ++count;
                }
            }
        } catch (Throwable exception) {
            log.error("Failed to disable job", exception);
        }
        return count > 0;
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
        ArchetypeQuery query = new ArchetypeQuery(JOB_SHORT_NAME, active);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        query.add(Constraints.sort("id"));
        return new IMObjectQueryIterator<>(service, query);
    }

}
