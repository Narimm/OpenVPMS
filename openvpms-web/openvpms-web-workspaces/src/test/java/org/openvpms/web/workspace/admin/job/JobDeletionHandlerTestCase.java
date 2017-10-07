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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.job;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandler;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandlerFactory;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link JobDeletionHandler}.
 *
 * @author Tim Anderson
 */
public class JobDeletionHandlerTestCase extends AbstractAppTest {

    /**
     * Verifies that a job with relationships can be deleted.
     */
    @Test
    public void testDelete() {
        User runAs = TestHelper.createUser();
        Entity job = createJob(runAs);

        IMObjectDeletionHandler handler = createDeletionHandler(job);
        assertTrue(handler.canDelete());
        handler.delete(new LocalContext(), new HelpContext("foo", null));

        assertNull(get(job));            // verify the job has been removed
        assertEquals(runAs, get(runAs)); // verify the user hasn't been removed
    }

    /**
     * Verifies jobs can be deactivated.
     */
    @Test
    public void testDeactivate() {
        User runAs = TestHelper.createUser();
        Entity job = createJob(runAs);
        assertTrue(job.isActive());

        IMObjectDeletionHandler handler = createDeletionHandler(job);
        handler.deactivate();

        assertFalse(get(job).isActive());
    }

    /**
     * Verifies that the {@link IMObjectDeletionHandlerFactory} returns {@link JobDeletionHandler} for jobs.
     */
    @Test
    public void testFactory() {
        IMObjectDeletionHandlerFactory factory = new IMObjectDeletionHandlerFactory(getArchetypeService());
        factory.setApplicationContext(applicationContext);

        User runAs = TestHelper.createUser();
        Entity job = createJob(runAs);
        IMObjectDeletionHandler<Entity> handler = factory.create(job);
        assertTrue(handler instanceof JobDeletionHandler);
        handler.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(job));
    }

    /**
     * Creates a new deletion handler for a job.
     *
     * @param job the job
     * @return a new deletion handler
     */
    protected JobDeletionHandler createDeletionHandler(Entity job) {
        IMObjectEditorFactory factory = applicationContext.getBean(IMObjectEditorFactory.class);
        return new JobDeletionHandler(job, factory, ServiceHelper.getTransactionManager(),
                                      ServiceHelper.getArchetypeService());
    }

    /**
     * Creates a job.
     *
     * @return a new job
     */
    protected Entity createJob(User user) {
        Entity job = (Entity) create("entity.jobESCIInboxReader");
        EntityBean bean = new EntityBean(job);
        bean.addNodeRelationship("runAs", user);
        save(job, user);
        return job;
    }

}
