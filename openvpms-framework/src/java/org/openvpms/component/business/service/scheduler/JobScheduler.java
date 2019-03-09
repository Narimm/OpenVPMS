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

package org.openvpms.component.business.service.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.query.criteria.CriteriaBuilder;
import org.openvpms.component.query.criteria.CriteriaQuery;
import org.openvpms.component.query.criteria.Root;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schedules jobs configured via <em>entity.job*</em> archetypes.
 * <p/>
 * By default, all jobs may run concurrently. To force a job to run sequentially either:
 * <ul>
 * <li>annotate the implementation class with {@link DisallowConcurrentExecution}.<br/>
 * This prevents the same <em>entity.job*</em> being launched concurrently</li>
 * <li>implement the {@link SingletonJob} interface.<br/>
 * This prevents multiple <em>entity.job*</em> that use the class being launched concurrently</li>
 * </ul>
 * NOTE: only one <em>entity.job*</em> can use a {@link SingletonJob} implementation, as the last one to save
 * unschedules any existing instance. On startup, the first implementation of a {@link SingletonJob} is scheduled.<br/>
 * To avoid unexpected behaviour, prevent multiple configurations of a {@link SingletonJob} from being created.
 *
 * @author Tim Anderson
 */
public class JobScheduler implements ApplicationContextAware, InitializingBean, DisposableBean {

    /**
     * The Quartz scheduler.
     */
    private final Scheduler scheduler;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The update listener.
     */
    private final UpdateListener listener;

    /**
     * The application context.
     */
    private ApplicationContext context;

    /**
     * The set of configurations pending removal. These are used to ensure that previous jobs are unscheduled if
     * their name changes.
     */
    private Map<Long, Entity> pending = Collections.synchronizedMap(new HashMap<>());

    /**
     * The job archetype short name prefix.
     */
    static final String JOB_ARCHETYPE = "entity.job*";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(JobScheduler.class);


    /**
     * Constructs a {@link JobScheduler}.
     *
     * @param scheduler the Quartz scheduler
     * @param service   the archetype service
     */
    public JobScheduler(Scheduler scheduler, IArchetypeService service) {
        this.scheduler = scheduler;
        this.service = service;
        listener = new UpdateListener();
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
        // NOTE: this will not pick up jobs whose archetype was loaded after the scheduler started
        service.addListener(JOB_ARCHETYPE, listener);
    }

    /**
     * Destroys this.
     */
    @Override
    public void destroy() {
        service.removeListener(JOB_ARCHETYPE, listener);
    }

    /**
     * Schedules a job.
     *
     * @param configuration the job configuration
     * @throws SchedulerException for any error
     */
    public void schedule(Entity configuration) {
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
    public void run(Entity configuration) {
        String name = getJobName(configuration);
        if (log.isInfoEnabled()) {
            log.info("Running " + configuration.getName() + " (" + configuration.getId() + ") with job name " + name);
        }

        try {
            scheduler.triggerJob(new JobKey(name, Scheduler.DEFAULT_GROUP));
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
    public Date getNextRunTime(Entity configuration) {
        Date result = null;
        try {
            String name = getJobName(configuration);
            Trigger trigger = scheduler.getTrigger(new TriggerKey(name, Scheduler.DEFAULT_GROUP));
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
    public List<Entity> getJobs(String type) {
        List<Entity> result;
        if (TypeHelper.matches(type, JOB_ARCHETYPE)) {
            CriteriaBuilder builder = service.getCriteriaBuilder();
            CriteriaQuery<Entity> query = builder.createQuery(Entity.class);
            Root<Entity> root = query.from(Entity.class, type);
            query.where(builder.equal(root.get("active"), true));
            query.orderBy(builder.asc(root.get("id")));
            result = service.createQuery(query).getResultList();
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
    public String getJobName(Entity configuration) {
        return getJobConfig(configuration).getJobName();
    }

    /**
     * Schedules all active configured jobs.
     */
    protected void scheduleJobs() {
        List<Entity> jobs = getJobs(JOB_ARCHETYPE);
        for (Entity job : jobs) {
            try {
                schedule(job);
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
        Entity existing = pending.get(configuration.getId());
        String name = (existing != null) ? getJobName(existing) : getJobName((Entity) configuration);
        if (log.isInfoEnabled()) {
            log.info("Unscheduling " + name + " (" + configuration.getId() + ")");
        }
        try {
            scheduler.unscheduleJob(new TriggerKey(name));
        } catch (Throwable exception) {
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
            try {
                schedule((Entity) configuration);
            } catch (Throwable exception) {
                log.error("Failed to schedule job " + configuration.getName() + " (" + configuration.getId() + "): "
                          + exception.getMessage(), exception);
            }
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
            Entity original = (Entity) service.get(configuration.getObjectReference());
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

    private class UpdateListener extends AbstractArchetypeServiceListener {

        @Override
        public void save(org.openvpms.component.business.domain.im.common.IMObject object) {
            addPending(object);
        }

        @Override
        public void saved(org.openvpms.component.business.domain.im.common.IMObject object) {
            onSaved(object);
        }

        @Override
        public void remove(org.openvpms.component.business.domain.im.common.IMObject object) {
            addPending(object);
        }

        @Override
        public void removed(org.openvpms.component.business.domain.im.common.IMObject object) {
            unschedule(object);
        }

        @Override
        public void rollback(org.openvpms.component.business.domain.im.common.IMObject object) {
            removePending(object);
        }
    }

    private static final class JobConfig {

        private final IMObjectBean config;

        private Class<?> jobClass;

        JobConfig(IMObject config, IArchetypeService service) {
            this(service.getBean(config));
        }

        JobConfig(IMObjectBean config) {
            this.config = config;
        }

        /**
         * Returns the job name.
         * <p>
         * If the job class implements {@link SingletonJob}, the returned name will be the job class name prefixed with
         * "singleton:".<br/>
         * For all other job classes, the returned name will be the configuration id and name prefixed with
         * "job:".<br/>
         * This ensures that only a single instance of a {@link SingletonJob} can ever be scheduled.
         * <br/>
         * The job: name includes the configuration identifier to allow two configurations with the
         * same name to be scheduled.
         *
         * @return the job name
         */
        String getJobName() {
            String name;
            Class type = getJobClass();
            if (SingletonJob.class.isAssignableFrom(type)) {
                name = "singleton:" + type.getName();
            } else {
                IMObject object = config.getObject();
                name = "job:" + object.getId() + ":" + object.getName();
            }
            return name;
        }

        /**
         * Returns the job class.
         *
         * @return the job class
         * @throws SchedulerException for any error
         */
        Class<?> getJobClass() {
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
        Class<? extends Job> getRunner() {
            return disallowConcurrentExecution(getJobClass()) ? StatefulJobRunner.class : JobRunner.class;
        }

        /**
         * Determines if concurrent execution should be disallowed for a particular job.
         *
         * @param jobClass the job class
         * @return {@code true} if concurrent execution should be disallowed
         */
        boolean disallowConcurrentExecution(Class<?> jobClass) {
            return StatefulJob.class.isAssignableFrom(jobClass)
                   || SingletonJob.class.isAssignableFrom(jobClass)
                   || jobClass.getAnnotation(DisallowConcurrentExecution.class) != null;
        }

        /**
         * Creates a {@code JobDetail} from a job configuration.
         *
         * @return a new {@code JobDetail}
         * @throws SchedulerException for any error
         */
        JobDetail createJobDetail(ApplicationContext context, IArchetypeService service) {
            JobDataMap map = new JobDataMap();
            map.put("Configuration", config.getObject());
            map.put("ApplicationContext", context);
            map.put("ArchetypeService", service);
            JobBuilder builder = JobBuilder.newJob(getRunner());
            builder.withIdentity(getJobName(), Scheduler.DEFAULT_GROUP).setJobData(map);
            return builder.build();
        }

        /**
         * Creates a trigger for the job.
         *
         * @return a new trigger
         */
        Trigger createTrigger() {
            String jobName = getJobName();
            CronScheduleBuilder expression = CronScheduleBuilder.cronSchedule(config.getString("expression"));
            Trigger trigger;
            try {
                TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger();
                trigger = builder.withIdentity(jobName, Scheduler.DEFAULT_GROUP).withSchedule(expression)
                        .forJob(jobName).build();
            } catch (Throwable exception) {
                throw new SchedulerException(exception);
            }
            return trigger;
        }
    }
}