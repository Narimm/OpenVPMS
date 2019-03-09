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

package org.openvpms.web.workspace.admin.user;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandler;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandlerFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link UserDeletionHandler}.
 *
 * @author Tim Anderson
 */
public class UserDeletionHandlerTestCase extends AbstractAppTest {

    /**
     * The deletion handler factory.
     */
    @Autowired
    IMObjectDeletionHandlerFactory factory;


    /**
     * Verifies that the {@link IMObjectDeletionHandlerFactory} returns {@link UserDeletionHandler} for users.
     */
    @Test
    public void testFactory() {
        User user = TestHelper.createClinician(false);
        assertTrue(factory.create(user) instanceof UserDeletionHandler);
    }

    /**
     * Tests deletion of a user with participation relationships.
     */
    @Test
    public void testDeleteUserWithParticipations() {
        User user = TestHelper.createClinician();

        IMObjectDeletionHandler<User> handler = factory.create(user);
        assertTrue(handler.canDelete());
        assertTrue(handler.canDeactivate());

        // create an act that refers to the user via a participation.clinician relationship.
        // The user should no long be able to be deleted
        PatientTestHelper.createNote(new Date(), TestHelper.createPatient(), user);
        assertFalse(handler.canDelete());
        assertTrue(handler.canDeactivate());
    }

    /**
     * Verifies that when a user is the source of an entity relationship, it cannot be deleted.
     */
    @Test
    public void testDeleteUserWhoIsEntityRelationshipSource() {
        User user = TestHelper.createUser();
        Party location = TestHelper.createLocation();
        IMObjectBean bean = getBean(user);
        bean.addTarget("locations", location); // TODO - probably a bad example. No good reason to disallow this?
        bean.save();

        IMObjectDeletionHandler<User> handler = factory.create(user);
        assertFalse(handler.canDelete());
        assertTrue(handler.canDeactivate());
    }

    /**
     * Verifies that when a user is the target of an entity relationship, it can be deleted.
     */
    @Test
    public void testDeleteUserWhoIsEntityRelationshipTarget() {
        User user = TestHelper.createUser();
        Entity group = (Entity) create(UserArchetypes.GROUP);
        group.setName(TestHelper.randomName("ZGroup"));
        IMObjectBean bean = getBean(group);
        bean.addTarget("users", user, "groups");
        bean.save(user);

        IMObjectDeletionHandler<User> handler = factory.create(user);
        assertTrue(handler.canDelete());
        assertTrue(handler.canDeactivate());

        // now delete the user
        handler.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(user));
    }

    /**
     * Verifies that when a user is the source of an entity link, it can be deleted.
     */
    @Test
    public void testDeleteUserWhoIsEntityLinkSource() {
        User user = TestHelper.createUser();
        Entity worklist = ScheduleTestHelper.createWorkList();
        IMObjectBean bean = getBean(user);
        bean.addTarget("followupWorkLists", worklist);
        bean.save();

        IMObjectDeletionHandler<User> handler = factory.create(user);
        assertTrue(handler.canDelete());
        assertTrue(handler.canDeactivate());

        // now delete the user
        handler.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(user));
        assertNotNull(get(worklist)); // deletion doesn't propagate
    }

    /**
     * Verifies that when a user is the target of an entity link, it cannot be deleted.
     */
    @Test
    public void testDeleteUserWhoIsEntityLinkTarget() {
        User user = TestHelper.createUser();
        Entity preferences = (Entity) create(PreferenceArchetypes.PREFERENCES);
        IMObjectBean bean = getBean(preferences);
        bean.setTarget("user", user);
        bean.save(user);

        IMObjectDeletionHandler<User> handler = factory.create(user);
        assertFalse(handler.canDelete());
        assertTrue(handler.canDeactivate());
    }

    /**
     * Verifies that a user associated with an active <em>entity.job*</em> cannot be deleted or deactivated.
     */
    @Test
    public void testDeleteUserAssociatedWithJob() {
        User user = TestHelper.createUser();
        IMObjectDeletionHandler<User> handler = factory.create(user);
        assertTrue(handler.canDelete());
        assertTrue(handler.canDeactivate());

        // now link the user to a job and verify it cannot be deleted or deactivated
        IMObject job = create("entity.jobPharmacyOrderDiscontinuation");
        IMObjectBean bean = getBean(job);
        bean.addTarget("runAs", user);
        bean.save();

        assertFalse(handler.canDelete());
        assertFalse(handler.canDeactivate());

        // deactivate the job. The user can now be deleted
        job.setActive(false);
        save(job);

        assertTrue(handler.canDelete());
        assertTrue(handler.canDeactivate());

        // now delete the user
        handler.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(user));
        assertNotNull(get(job));  // deletion doesn't propagate
    }

}
