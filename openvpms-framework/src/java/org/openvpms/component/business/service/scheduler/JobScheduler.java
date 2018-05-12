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

package org.openvpms.component.business.service.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The application context.
     */
    private ApplicationContext context;

    /**
     * The set of configurations pending removal. These are used to ensure that previous jobs are unscheduled if
     * their name changes.
     */
    private Map<Long, IMObject> pending = Collections.synchronizedMap(new HashMap<Long, IMObject>());

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(JobScheduler.class);

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
    public void setApplicationContext(ApplicationContext applicationContext) {
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
        JobConfig config = getJobConfig(configuration);
        JobDetail job = config.createJobDetail(context, service);
        Trigger trigger = config.createTrigger();
        try {
            Date date = scheduler.scheduleJob(job, trigger);
            if (log.isInfoEnabled()) {
                log.info("Job " + configuration.getName() + " (" + configuration.getId() + ") set to trigger at "
                         + date);
            }
        } catch (OpenVPMSException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new SchedulerException(exception);
        }
    }

    /**
     * Runs a job now.
     *
     * @param configuration the job configuration
     * @throws SchedulerException for any error
     */
    public void run(IMObject configuration) {
        String name = getJobName(configuration);
        if (log.isInfoEnabled()) {
            log.info("Running " + configuration.getName() + " (" + configuration.getId() + ") with job name " + name);
        }

        try {
            scheduler.triggerJob(name, Scheduler.DEFAULT_GROUP);
        } catch (Throwable exception) {
            throw new SchedulerException(exception);
        }
    }

    /**
     * Returns the time when a job is next scheduled to run.
     *
     * @param configuration the job configuration
     * @return the next scheduled time, or {@code null} if it is not schedule to run
     * @throws SchedulerException for any error
     */
    public Date getNextRunTime(IMObject configuration) {
        Date result = null;
        try {
            String name = getJobName(configuration);
            Trigger trigger = scheduler.getTrigger(name, Scheduler.DEFAULT_GROUP);
            if (trigger != null) {
                result = trigger.getNextFireTime();
            }
        } catch (Throwable exception) {
            throw new SchedulerException(exception);
        }
        return result;
    }

    /**
     * Returns all jobs of the specified type.
     *
     * @param type the job archetype
     * @return the job configurations
     */
    public List<IMObject> getJobs(String type) {
        List<IMObject> result;
        if (TypeHelper.matches(type, JOB_SHORT_NAME)) {
            ArchetypeQuery query = new ArchetypeQuery(type, true);
            query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
            result = service.get(query).getResults();
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns the job name for a configuration.
     *
     * @param configuration the configuration
     * @return the job name
     */
    public String getJobName(IMObject configuration) {
        return getJobConfig(configuration).getJobName();
    }

    /**
     * Schedules all active configured jobs.
     */
    protected void scheduleJobs() {
        ArchetypeQuery query = new ArchetypeQuery(JOB_SHORT_NAME, true);
        Iterator<IMObject> iterator = new IMObjectQueryIterator<>(query);
        while (iterator.hasNext()) {
            try {
                schedule(iterator.next());
            } catch (Throwable exception) {
                log.error(exception, exception);
            }
        }
    }

    /**
     * Unschedules a job.
     *
     * @param configuration the job configuration
     */
    private void unschedule(IMObject configuration) {
        IMObject existing = pending.get(configuration.getId());
        String name = (existing != null) ? getJobName(existing) : getJobName(configuration);
        if (log.isInfoEnabled()) {
            log.info("Unscheduling " + name + " (" + configuration.getId() + ")");
        }
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
     * <p>
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
     * <p>
     * This removes the associated configuration from the map of configurations pending removal.
     *
     * @param configuration the rolled back configuration
     */
    private void removePending(IMObject configuration) {
        pending.remove(configuration.getId());
    }

    /**
     * Returns the job configuration for an <em>entity.job*</em>.
     *
     * @param configuration the job configuration
     * @return the job configuration
     */
    private JobConfig getJobConfig(IMObject configuration) {
        return new JobConfig(configuration, service);
    }

    private static final class JobConfig {

        private final IMObjectBean config;

        private Class<?> jobClass;

        public JobConfig(IMObject config, IArchetypeService service) {
            this(new IMObjectBean(config, service));
        }

        public JobConfig(IMObjectBean config) {
            this.config = config;

        }

        /**
         * Returns the job name.
         * <p>
         * If the job class implements {@link SingletonJob}, the returned name will be the job class name prefixed with
         * "singleton:".<br/>
         * For all other job classes, the returned name will be the configuration name prefixed with
         * "job:".<br/>
         * This ensures that only a single instance of a {@link SingletonJob} can ever be scheduled.
         *
         * @return the job name
         */
        public String getJobName() {
            String name;
            Class type = getJobClass();
            if (SingletonJob.class.isAssignableFrom(type)) {
                name = "singleton:" + type.getName();
            } else {
                name = "job:" + config.getObject().getName();
            }
            return name;
        }

        /**
         * Returns the job class.
         *
         * @return the job class
         * @throws SchedulerException for any error
         */
        public Class<?> getJobClass() {
            if (jobClass == null) {
                try {
                    jobClass = Class.forName(config.getString("class"));
                } catch (ClassNotFoundException exception) {
                    throw new SchedulerException(exception);
                }
            }
            return jobClass;
        }

        /**
         * Returns the class that will be used to run the {@link #getJobClass()}.
         *
         * @return the runner class
         */
        public Class getRunner() {
            return StatefulJob.class.isAssignableFrom(getJobClass()) ? StatefulJobRunner.class : JobRunner.class;
        }

        /**
         * Creates a {@code JobDetail} from a job configuration.
         *
         * @return a new {@code JobDetail}
         * @throws SchedulerException for any error
         */
        public JobDetail createJobDetail(ApplicationContext context, IArchetypeService service) {
            JobDetail job = new JobDetail();
            job.setName(getJobName());
            job.setGroup(Scheduler.DEFAULT_GROUP);
            job.setJobClass(getRunner());
            job.getJobDataMap().put("Configuration", config.getObject());
            job.getJobDataMap().put("ApplicationContext", context);
            job.getJobDataMap().put("ArchetypeService", service);
            return job;
        }

        /**
         * Creates a trigger for the job.
         *
         * @return a new trigger
         */
        public Trigger createTrigger() {
            String jobName = getJobName();
            CronTrigger trigger = new CronTrigger(jobName, Scheduler.DEFAULT_GROUP);
            try {
                trigger.setJobName(jobName);
                trigger.setCronExpression(config.getString("expression"));
            } catch (Throwable exception) {
                throw new SchedulerException(exception);
            }
            return trigger;
        }
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