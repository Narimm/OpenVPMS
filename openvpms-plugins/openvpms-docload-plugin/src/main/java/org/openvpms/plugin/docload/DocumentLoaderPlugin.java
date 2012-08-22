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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.plugin.runas.RunAsService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DocumentLoaderPlugin {

    private final IArchetypeService service;

    public static final String GROUP = "Document Loader Plugin";

    private final PlatformTransactionManager transactionManager;

    private final Scheduler scheduler;

    private final RunAsService runAs;

    private Map<IMObjectReference, Entity> config = new HashMap<IMObjectReference, Entity>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DocumentLoaderPlugin.class);

    private static final String CONFIG_BY_ID = "entity.pluginDocumentLoaderById";
    private static final String CONFIG_BY_NAME = "entity.pluginDocumentLoaderByName";

    public DocumentLoaderPlugin(IArchetypeService service, PlatformTransactionManager transactionManager,
                                Scheduler scheduler, RunAsService runAs) throws IOException {
        this.service = service;
        this.transactionManager = transactionManager;
        this.scheduler = scheduler;
        this.runAs = runAs;
        initialise(service);
    }

    private void initialise(IArchetypeService service) {
        runAs.runAs(runAs.getDefaultUser(), new Runnable() {
            @Override
            public void run() {
                try {
                    loadArchetypes();
                } catch (IOException exception) {
                    throw new DocumentLoaderException("Failed to load archetypes", exception);
                }
                loadConfig();
            }
        });
        service.addListener("entity.pluginDocumentLoader*", new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                updateConfig((Entity) object);
            }
        });
    }

    private void loadConfig() {
        ArchetypeQuery query = new ArchetypeQuery(new String[]{CONFIG_BY_ID, CONFIG_BY_NAME}, false, true);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        IPage<IMObject> page = service.get(query);
        for (IMObject object : page.getResults()) {
            config.put(object.getObjectReference(), (Entity) object);
        }
    }

    private synchronized void updateConfig(Entity entity) {
        if (!entity.isActive()) {
            entity = config.remove(entity.getObjectReference());
            if (entity != null) {
                try {
                    scheduler.interrupt(entity.getName(), GROUP);
                    scheduler.deleteJob(entity.getName(), GROUP);
                } catch (SchedulerException exception) {
                    log.error("Failed to delete job: " + entity.getName(), exception);
                }
            }
        } else {
            config.put(entity.getObjectReference(), entity);
            try {
                scheduler.addJob(createJobDetail(entity), true);
            } catch (SchedulerException exception) {
                log.error("Failed to add job: " + entity.getName(), exception);
            }
        }
    }

    private JobDetail createJobDetail(Entity entity) {
        IMObjectBean bean = new IMObjectBean(entity, service);
        JobDetail detail;
        if (bean.isA(CONFIG_BY_ID)) {
            detail = new JobDetail(entity.getName(), GROUP, IdLoaderJob.class);
        } else {
            detail = new JobDetail(entity.getName(), GROUP, NameLoaderJob.class);
        }
        JobDataMap map = new JobDataMap();
        map.put("PlatformTransactionManager", transactionManager);
        map.put("Configuration", entity);
        map.put("ArchetypeService", service);
        detail.setJobDataMap(map);
        return detail;
    }

    private void loadArchetypes() throws IOException {
        ArchetypeInstaller installer = new ArchetypeInstaller(service, transactionManager, null);
        installer.install();
    }
}
