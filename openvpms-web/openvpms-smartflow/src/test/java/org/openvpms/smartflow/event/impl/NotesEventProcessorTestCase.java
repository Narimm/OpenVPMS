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

package org.openvpms.smartflow.event.impl;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.smartflow.model.Note;
import org.openvpms.smartflow.model.NotesList;
import org.openvpms.smartflow.model.event.NotesEvent;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link NotesEventProcessor}.
 *
 * @author Tim Anderson
 */
public class NotesEventProcessorTestCase extends ArchetypeServiceTest {

    private Party patient;
    private NotesEvent event;
    private Act visit;
    private Note note;
    private NotesEventProcessor processor;

    @Before
    public void setUp() {
        patient = TestHelper.createPatient();
        visit = PatientTestHelper.createEvent(new Date(), patient);
        note = new Note();
        note.setNoteGuid(UUID.randomUUID().toString());
        note.setHospitalizationId(Long.toString(visit.getId()));
        note.setStatus(Note.ADDED_STATUS);
        note.setText("a note");
        event = new NotesEvent();
        NotesList list = new NotesList();
        list.setNotes(Collections.singletonList(note));
        event.setObject(list);
        processor = new NotesEventProcessor(getArchetypeService());
    }

    /**
     * Tests {@link NotesEvent}s.
     */
    @Test
    public void testNote() {
        processor.process(event);

        List<Act> items = getItems(visit);
        assertEquals(1, items.size());
        checkNote(items.get(0), "a note");

        note.setStatus(Note.CHANGED_STATUS);
        note.setText("different note");
        processor.process(event);

        List<Act> items2 = getItems(visit);
        assertEquals(1, items2.size());
        checkNote(items2.get(0), "different note");

        note.setStatus(Note.REMOVED_STATUS);
        processor.process(event);

        List<Act> items3 = getItems(visit);
        assertEquals(0, items3.size());
    }

    /**
     * Verifies that an addendum is created for posted notes.
     */
    @Test
    public void testUpdatePostedNote() {
        processor.process(event);

        List<Act> items = getItems(visit);
        assertEquals(1, items.size());
        Act item = items.get(0);
        checkNote(item, "a note");

        item.setStatus(ActStatus.POSTED);
        save(item);

        note.setStatus(Note.CHANGED_STATUS);
        note.setText("different note");
        processor.process(note);

        item = get(item);
        assertNotNull(item);
        checkNote(item, "a note");
        checkAddendum(item, "different note");
    }

    /**
     * Verifies that an addendum is created indicating a posted note can't be removed.
     */
    @Test
    public void testRemovePostedNote() {
        processor.process(event);

        List<Act> items = getItems(visit);
        assertEquals(1, items.size());
        Act item = items.get(0);
        checkNote(item, "a note");

        item.setStatus(ActStatus.POSTED);
        save(item);

        note.setStatus(Note.REMOVED_STATUS);
        processor.process(note);

        item = get(item);
        assertNotNull(item);
        checkNote(item, "a note");
        checkAddendum(item, "This note was deleted in Smart Flow Sheet but cannot be removed as it is locked");
    }

    /**
     * Verifies a note matches that expected.
     *
     * @param act  the note act
     * @param note the expected note
     */
    private void checkNote(Act act, String note) {
        ActBean bean = new ActBean(act);
        assertTrue(bean.isA(PatientArchetypes.CLINICAL_NOTE));
        assertEquals(patient, bean.getNodeParticipant("patient"));
        assertEquals(note, bean.getString("note"));
    }

    /**
     * Reloads a visit and returns the associated items.
     *
     * @param visit the visit
     * @return the items
     */
    private List<Act> getItems(Act visit) {
        visit = get(visit);
        assertNotNull(visit);
        ActBean bean = new ActBean(visit);
        return bean.getNodeActs("items");
    }

    private void checkAddendum(Act item, String note) {
        ActBean bean = new ActBean(item);
        List<Act> acts = bean.getNodeActs("addenda");
        assertEquals(1, acts.size());
        ActBean addendum = new ActBean(acts.get(0));
        assertEquals(patient, addendum.getNodeParticipant("patient"));
        assertEquals(note, addendum.getString("note"));
        assertEquals(visit, addendum.getNodeSourceObject("event"));
    }
}
