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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Schedules jobs configured via <em>entity.job*</em> archetypes.
 *
 * @author Tim Anderson
 */
public class JobScheduler implements ApplicationContextAware, InitializingBean {

    /**
     * The Quartz scheduler.
     */
    private final Scheduler scheduler;

    /**
     * The application context.
     */
    private ApplicationContext context;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(JobScheduler.class);

    /**
     * The set of configurations pending removal. These are used to ensure that previous jobs are unscheduled if
     * their name changes.
     */
    private Map<Long, IMObject> pending = Collections.synchronizedMap(new HashMap<Long, IMObject>());

    /**
     * The job archetype short name prefix.
     */
    private static final String JOB_SHORT_NAME = "entity.job*";


    /**
     * Constructs a {@link JobScheduler}.
     *
     * @param scheduler the Quartz scheduler
     * @param service   the archetype service
     */
    public JobScheduler(Scheduler scheduler, IArchetypeService service) {
        this.scheduler = scheduler;
        this.service = service;
    }

    /**
     * Set the ApplicationContext that this object runs in.
     * Normally this call will be used to initialize the object.
     *
     * @param applicationContext the ApplicationContext object to be used by this object
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied.
     */
    @Override
    public void afterPropertiesSet() {
        scheduleJobs();
        String[] shortNames = DescriptorHelper.getShortNames(JOB_SHORT_NAME);
        UpdateListener listener = new UpdateListener();
        for (String shortName : shortNames) {
            service.addListener(shortName, listener);
        }
    }

    /**
     * Schedules a job.
     *
     * @param configuration the job configuration
     * @throws SchedulerException for any error
     */
    public void schedule(IMObject configuration) {
        JobDetail job = createJobDetail(configuration);
        Trigger trigger = createTrigger(configuration, job);
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (OpenVPMSException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new SchedulerException(exception);
        }
    }

    /**
     * Schedules all active configured jobs.
     */
    protected void scheduleJobs() {
        ArchetypeQuery query = new ArchetypeQuery(JOB_SHORT_NAME, true);
        Iterator<IMObject> iterator = new IMObjectQueryIterator<IMObject>(query);
        while (iterator.hasNext()) {
            try {
                schedule(iterator.next());
            } catch (Throwable exception) {
                log.error(exception, exception);
            }
        }
    }

    /**
     * Creates a {@code JobDetail} from a job configuration.
     *
     * @param configuration the job configuration
     * @return a new {@code JobDetail}
     * @throws SchedulerException for any error
     */
    protected JobDetail createJobDetail(IMObject configuration) {
        IMObjectBean bean = new IMObjectBean(configuration, service);
        JobDetail job = new JobDetail();
        String name = bean.getString("name");
        job.setName(name);
        job.setGroup(Scheduler.DEFAULT_GROUP);
        Class<?> type;
        try {
            type = Class.forName(bean.getString("class"));
        } catch (ClassNotFoundException exception) {
            throw new SchedulerException(exception);
        }
        Class runner = type.isAssignableFrom(StatefulJob.class) ? StatefulJobRunner.class : JobRunner.class;
        job.setJobClass(runner);
        job.getJobDataMap().put("Configuration", configuration);
        job.getJobDataMap().put("ApplicationContext", context);
        job.getJobDataMap().put("ArchetypeService", service);
        return job;
    }

    /**
     * Creates a trigger for a job.
     *
     * @param configuration the job configuration
     * @param job           the job
     * @return a new trigger
     * @throws SchedulerException for any error
     */
    protected Trigger createTrigger(IMObject configuration, JobDetail job) {
        IMObjectBean bean = new IMObjectBean(configuration, service);
        String name = bean.getString("name");
        CronTrigger trigger = new CronTrigger(name, job.getGroup());
        try {
            trigger.setJobName(name);
            trigger.setCronExpression(bean.getString("expression"));
        } catch (Throwable exception) {
            throw new SchedulerException(exception);
        }
        return trigger;
    }

    /**
     * Unschedules a job.
     *
     * @param configuration the job configuration
     */
    private void unschedule(IMObject configuration) {
        IMObject existing = pending.get(configuration.getId());
        String name = (existing != null) ? existing.getName() : configuration.getName();
        try {
            scheduler.unscheduleJob(name, null);
        } catch (org.quartz.SchedulerException exception) {
            log.error(exception, exception);
        }
        pending.remove(configuration.getId());
    }

    /**
     * Invoked when a configuration is saved. This unschedules any existing job with the same name. If the
     * configuration is active, it schedules a new job.
     *
     * @param configuration the configuration
     */
    private void onSaved(IMObject configuration) {
        unschedule(configuration);
        if (configuration.isActive()) {
            schedule(configuration);
        }
    }

    /**
     * Invoked prior to an event being added or removed from the cache.
     * <p/>
     * If the event is already persistent, the persistent instance will be
     * added to the map of acts that need to be removed prior to any new
     * instance being cached.
     *
     * @param configuration the job configuration
     */
    private void addPending(IMObject configuration) {
        if (!configuration.isNew() && !pending.containsKey(configuration.getId())) {
            IMObject original = service.get(configuration.getObjectReference());
            if (original != null) {
                pending.put(configuration.getId(), original);
            }
        }
    }

    /**
     * Invoked on transaction rollback.
     * <p/>
     * This removes the associated configuration from the map of configurations pending removal.
     *
     * @param configuration the rolled back configuration
     */
    private void removePending(IMObject configuration) {
        pending.remove(configuration.getId());
    }

    private class UpdateListener extends AbstractArchetypeServiceListener {

        @Override
        public void save(IMObject object) {
            addPending(object);
        }

        @Override
        public void saved(IMObject object) {
            onSaved(object);
        }

        @Override
        public void remove(IMObject object) {
            addPending(object);
        }

        @Override
        public void removed(IMObject object) {
            unschedule(object);
        }

        @Override
        public void rollback(IMObject object) {
            removePending(object);
        }
    }
}