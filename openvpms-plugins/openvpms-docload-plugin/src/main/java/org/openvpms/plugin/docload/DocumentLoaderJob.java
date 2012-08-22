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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.plugin.docload;

import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.etl.tools.doc.Loader;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.io.File;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public abstract class DocumentLoaderJob implements StatefulJob {

    /**
     * The plugin.
     */
    private DocumentLoaderPlugin loader;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    private IMObjectBean configuration;


    @Resource
    public void setConfiguration(Entity configuration) {
        this.configuration = new IMObjectBean(configuration, service);
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
     * Called by the {@link Scheduler} when a {@link Trigger} fires that is associated with the {@code Job}.
     *
     * @param context the execution context
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (loader == null) {
            throw new JobExecutionException("DocumentLoader has not been registered");
        }
        if (service == null) {
            throw new JobExecutionException("ArchetypeService has not been registered");
        }
        initSecurityContext();
        try {
            Loader loader = createLoader();
            while (loader.hasNext()) {
                loader.loadNext();
            }
        } catch (Throwable exception) {
            throw new JobExecutionException(exception);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    protected abstract Loader createLoader();

    protected IMObjectBean getConfiguration() {
        return configuration;
    }

    protected IArchetypeService getArchetypeService() {
        return service;
    }
    protected File getSource() {
        return new File(configuration.getString("source"));
    }

    protected File getTarget() {
        return new File(configuration.getString("target"));
    }

    protected String getType() {
        return configuration.getString("type");
    }

    protected String getRunAs() {
        return configuration.getString("runAs");
    }

    /**
     * Initialises the security context.
     */
    private void initSecurityContext() {
        UserRules rules = new UserRules(service);
        String runAs = getRunAs();
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
