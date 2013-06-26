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

package org.openvpms.web.component.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;
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
     * Constructs an {@link JobScheduler}.
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
     * Schedules all active configured jobs.
     */
    private void scheduleJobs() {
        ArchetypeQuery query = new ArchetypeQuery(JOB_SHORT_NAME, true);
        Iterator<IMObject> iterator = new IMObjectQueryIterator<IMObject>(query);
        while (iterator.hasNext()) {
            schedule(iterator.next());
        }
    }

    /**
     * Schedules a job.
     *
     * @param configuration the job configuration
     */
    private void schedule(IMObject configuration) {
        try {
            IMObjectBean bean = new IMObjectBean(configuration, service);
            JobDetail job = new JobDetail();
            String name = bean.getString("name");
            job.setName(name);
            job.setGroup(Scheduler.DEFAULT_GROUP);
            Class<?> type = Class.forName(bean.getString("class"));
            Class runner = type.isAssignableFrom(StatefulJob.class) ? StatefulJobRunner.class : JobRunner.class;
            job.setJobClass(runner);
            job.getJobDataMap().put("Configuration", configuration);
            job.getJobDataMap().put("ApplicationContext", context);
            job.getJobDataMap().put("ArchetypeService", service);
            CronTrigger trigger = new CronTrigger(name, job.getGroup());
            trigger.setJobName(name);
            trigger.setCronExpression(bean.getString("expression"));
            scheduler.scheduleJob(job, trigger);
        } catch (Throwable exception) {
            log.error(exception, exception);
        }
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
        } catch (SchedulerException exception) {
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