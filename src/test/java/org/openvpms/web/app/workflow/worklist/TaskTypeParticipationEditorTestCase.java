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

package org.openvpms.web.app.workflow.worklist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.test.AbstractAppTest;


/**
 * Tests the {@link TaskTypeParticipationEditor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class TaskTypeParticipationEditorTestCase extends AbstractAppTest {

    /**
     * Tests validation.
     */
    @Test
    public void testValidate() {
        Act task = (Act) create(ScheduleArchetypes.TASK);
        Participation type = (Participation) create(ScheduleArchetypes.TASK_TYPE_PARTICIPATION);
        LayoutContext context = new DefaultLayoutContext();
        TaskTypeParticipationEditor editor = new TaskTypeParticipationEditor(type, task, context);
        assertFalse(editor.isValid());

        Entity taskType1 = ScheduleTestHelper.createTaskType();
        Entity taskType2 = ScheduleTestHelper.createTaskType();
        Party worklist1 = ScheduleTestHelper.createWorkList(2, taskType1);
        Party worklist2 = ScheduleTestHelper.createWorkList(2, taskType2);
        Party worklist3 = ScheduleTestHelper.createWorkList();

        editor.setWorkList(worklist1);
        assertEquals(taskType1, editor.getEntity());
        assertTrue(editor.isValid());
        
        editor.setWorkList(worklist2);
        assertEquals(taskType2, editor.getEntity());
        assertTrue(editor.isValid());

        editor.setWorkList(worklist3);
        assertNull(editor.getEntity());
        assertFalse(editor.isValid());      // need the task type to be valid

        editor.setEntity(taskType2); // taskType2 not associated with worklist1
        assertFalse(editor.isValid());

        editor.setWorkList(worklist2);
        assertEquals(taskType2, editor.getEntity());
        assertTrue(editor.isValid());

        editor.setEntity(taskType1); // taskType1 not associated with worklist2
        assertFalse(editor.isValid());

        // set the worklist to one with no task type
        editor.setWorkList(worklist3);
        assertNull(editor.getEntity());
        assertFalse(editor.isValid());
    }
}