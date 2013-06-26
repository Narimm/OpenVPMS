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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.DelegatingJob;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;

/**
 * Runs a job configured using an <em>entity.job*</em> archetype.
 * <p/>
 * This runs the job with the authorities of the jobs's runAs user, if one has been specified.
 * <p/>
 * The job class specified by the configuration must implement the {@code org.quartz.Job} or the {@code Runnable}
 * interface.
 *
 * @author Tim Anderson
 */
public class JobRunner implements InterruptableJob {

    /**
     * The job configuration.
     */
    private IMObject configuration;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The application context.
     */
    private ApplicationContext context;

    /**
     * The job.
     */
    private Job job;

    /**
     * Sets the job configuration.
     *
     * @param configuration the configuration. An instance of <em>entity.job*</em>
     */
    @Resource
    public void setConfiguration(IMObject configuration) {
        this.configuration = configuration;
    }

    /**
     * Registers the archetype service.
     *
     * @param service the archetype service
     */
    @Resource
    public void setArchetypeService(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Sets the application context.
     *
     * @param context the application context
     */
    @Resource
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }


    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     *
     * @param context the execution context
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (configuration == null) {
            throw new JobExecutionException("Configuration has not been registered");
        }
        if (service == null) {
            throw new JobExecutionException("ArchetypeService has not been registered");
        }

        initSecurityContext();
        try {
            job = createJob();
            job.execute(context);
        } catch (Throwable exception) {
            throw new JobExecutionException(exception);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a user interrupts the <code>Job</code>.
     */
    public void interrupt() throws UnableToInterruptJobException {
        if (job instanceof InterruptableJob) {
            ((InterruptableJob) job).interrupt();
        }
    }

    /**
     * Creates the job.
     *
     * @return a new job
     * @throws ClassNotFoundException if the job class cannot be found
     */
    private Job createJob() throws ClassNotFoundException {
        Job result;
        IMObjectBean bean = new IMObjectBean(configuration, service);
        Class type = Class.forName(bean.getString("class"));
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory(context);
        factory.registerSingleton("jobConfiguration", configuration);
        Object job = factory.createBean(type, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);
        if (job instanceof Runnable) {
            result = new DelegatingJob((Runnable) job);
        } else {
            result = (Job) job;
        }
        return result;
    }

    /**
     * Initialises the security context.
     */
    private void initSecurityContext() {
        IMObjectBean bean = new IMObjectBean(configuration);
        User user = (User) bean.getNodeTargetObject("runAs");
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication
                = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}