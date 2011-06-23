/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.dispatcher.quartz;

import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.dispatcher.ESCIDispatcher;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import javax.annotation.Resource;


/**
 * Integrates {@link ESCIDispatcher} with Quartz so that it may be scheduled.
 * <p/>
 * This implemements <tt>StatefulJob</tt> so that ESCIDispatcher won't be scheduled concurrently.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ESCIDispatcherJob implements InterruptableJob, StatefulJob {

    /**
     * The dispatcher.
     */
    private ESCIDispatcher dispatcher;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The user to run the job as.
     */
    private String runAs;


    /**
     * Registers the dispatcher.
     *
     * @param dispatcher the dispatcher
     */
    @Resource
    public void setESCIDispatcher(ESCIDispatcher dispatcher) {
        this.dispatcher = dispatcher;
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
     * Sets the user login name to run the job under.
     *
     * @param login the login name
     */
    @Resource
    public void setRunAs(String login) {
        this.runAs = login;
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     *
     * @param context the execution context
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (dispatcher == null) {
            throw new JobExecutionException("ESCIDispatcher has not been registered");
        }
        if (service == null) {
            throw new JobExecutionException("ArchetypeService has not been registered");
        }
        if (runAs == null) {
            throw new JobExecutionException("runAs has not been set");
        }
        initSecurityContext();
        try {
            dispatcher.dispatch();
        } catch (Throwable exception) {
            throw new JobExecutionException(exception);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a user interrupts the <code>Job</code>.
     */
    public void interrupt() {
        dispatcher.stop();
    }

    /**
     * Initialises the security context.
     */
    private void initSecurityContext() {
        UserRules rules = new UserRules(service);
        User user = rules.getUser(runAs);
        if (user == null) {
            throw new IllegalArgumentException("User '" + runAs + "' does not correspond to a valid user");
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication
                = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}

