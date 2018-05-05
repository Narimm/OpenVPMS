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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.reminder;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ReminderEditor}.
 *
 * @author Tim Anderson
 */
public class ReminderEditorTestCase extends AbstractAppTest {

    /**
     * The reminder rules.
     */
    @Autowired
    private ReminderRules rules;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The author.
     */
    private User author;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        patient = TestHelper.createPatient();
        author = TestHelper.createUser();
    }

    /**
     * Verifies that matching reminders are marked COMPLETED on save by default.
     */
    @Test
    public void testMarkMatchingRemindersCompleted() {
        Entity reminderType = ReminderTestHelper.createReminderType(1, DateUnits.MONTHS);

        Act existing = ReminderTestHelper.createReminder(patient, reminderType);
        assertEquals(ActStatus.IN_PROGRESS, existing.getStatus());
        save(existing);

        Act reminder = (Act) create(ReminderArchetypes.REMINDER);

        ReminderEditor editor = createEditor(reminder, reminderType);
        SaveHelper.save(editor);

        existing = get(existing);
        assertEquals(ActStatus.COMPLETED, existing.getStatus());

        reminder = get(reminder);
        assertEquals(ActStatus.IN_PROGRESS, reminder.getStatus());
    }

    /**
     * Verifies that matching reminders are COMPLETED can be disabled.
     */
    @Test
    public void testDisableMarkCompleted() {
        Entity reminderType = ReminderTestHelper.createReminderType(1, DateUnits.MONTHS);

        Act existing = ReminderTestHelper.createReminder(patient, reminderType);
        assertEquals(ActStatus.IN_PROGRESS, existing.getStatus());
        save(existing);

        Act reminder = (Act) create(ReminderArchetypes.REMINDER);

        ReminderEditor editor = createEditor(reminder, reminderType);
        editor.setMarkMatchingRemindersCompleted(false);
        SaveHelper.save(editor);

        existing = get(existing);
        assertEquals(ActStatus.IN_PROGRESS, existing.getStatus());

        reminder = get(reminder);
        assertEquals(ActStatus.IN_PROGRESS, reminder.getStatus());
    }

    /**
     * Verifies that reminders are marked COMPLETED if they are IN_PROGRESS on save and
     * {@link ReminderRules#shouldCancel(Act, Date)} is {@code true}.
     * <p/>
     * Note that COMPLETED is used an not CANCELLED, which is only used when processing reminders.
     * This behaviour is to support reminders that complete other reminders, but should also be marked COMPLETED rather
     * than left IN_PROGRESS.
     */
    @Test
    public void testCompleteOnSave() {
        Entity reminderType = ReminderTestHelper.createReminderType(0, DateUnits.MONTHS, 0, DateUnits.MONTHS);
        Act reminder = (Act) create(ReminderArchetypes.REMINDER);

        ReminderEditor editor = createEditor(reminder, reminderType);
        editor.setEndTime(new Date());

        assertEquals(ActStatus.IN_PROGRESS, editor.getStatus());
        assertTrue(rules.shouldCancel(reminder, new Date()));

        SaveHelper.save(editor);
        assertEquals(ActStatus.COMPLETED, reminder.getStatus());
    }

    /**
     * Creates a new reminder editor.
     *
     * @param reminder     the reminder to edit
     * @param reminderType the reminder type
     * @return a new reminder editor
     */
    protected ReminderEditor createEditor(Act reminder, Entity reminderType) {
        DefaultLayoutContext layout = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        ReminderEditor editor = new ReminderEditor(reminder, null, layout);
        editor.getComponent();
        editor.setPatient(patient);
        editor.setReminderType(reminderType);
        editor.setAuthor(author);
        return editor;
    }

}
