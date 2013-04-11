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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.workflow.worklist;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link TaskActEditor} class.
 *
 * @author Tim Anderson
 */
public class TaskActEditorTestCase extends AbstractAppTest {

    /**
     * The work list.
     */
    private Party worklist;

    /**
     * The task type.
     */
    private Entity taskType;

    /**
     * The uaer to populate the author node with.
     */
    private User user;


    /**
     * Verifies that a task editor can be created and saved when mandatory fields are populated.
     */
    @Test
    public void testSave() {
        Context context = new LocalContext();
        LayoutContext layout = new DefaultLayoutContext(new HelpContext("foo", null));
        layout.setContext(context);
        Act act = (Act) create(ScheduleArchetypes.TASK);

        TaskActEditor editor = new TaskActEditor(act, null, layout);
        editor.getComponent();
        assertFalse(editor.isValid());
        editor.setCustomer(TestHelper.createCustomer());
        editor.setWorkList(worklist);
        assertFalse(editor.isValid());
        editor.setTaskType(taskType);
        assertFalse(editor.isValid());
        editor.setAuthor(user);
        assertFalse(editor.isValid());

        editor.setStartTime(new Date());
        assertTrue(editor.isValid());  // should now be valid

        assertTrue(SaveHelper.save(editor));
    }


    /**
     * Verifies that the end time cannot be set prior to the start time.
     */
    @Test
    public void testTimes() {
        TaskActEditor editor = createEditor();
        editor.getComponent();

        Date start = new Date();
        editor.setStartTime(start);
        Date end = DateRules.getDate(start, -1, DateUnits.MINUTES);
        editor.setEndTime(end);
        assertTrue(editor.getEndTime().equals(start));
        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        worklist = ScheduleTestHelper.createWorkList();
        taskType = ScheduleTestHelper.createTaskType();
        user = TestHelper.createClinician();

        EntityBean bean = new EntityBean(worklist);
        EntityRelationship relationship = bean.addNodeRelationship("taskTypes", taskType);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("noSlots", 100);
        save(taskType, worklist);
    }

    /**
     * Creates a new editor, pre-populating the customer, worklist, and user.
     *
     * @return a new editor
     */
    private TaskActEditor createEditor() {
        Context context = new LocalContext();
        Party customer = TestHelper.createCustomer();

        // populate the context. These will be used to initialise the task
        context.setCustomer(customer);
        context.setWorkList(worklist);
        context.setUser(user);

        LayoutContext layout = new DefaultLayoutContext(new HelpContext("foo", null));
        layout.setContext(context);
        Act act = (Act) create(ScheduleArchetypes.TASK);

        return new TaskActEditor(act, null, layout);
    }


}
