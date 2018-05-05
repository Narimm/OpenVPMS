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

package org.openvpms.smartflow.event.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.smartflow.model.Note;
import org.openvpms.smartflow.model.Notes;
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

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * The test visit.
     */
    private Act visit;

    /**
     * The event processor.
     */
    private NotesEventProcessor processor;

    /**
     * The SmartFlow Sheet configuration.
     */
    private Entity config;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        patient = TestHelper.createPatient();
        visit = PatientTestHelper.createEvent(new Date(), patient);

        Party practice = TestHelper.getPractice();
        PracticeService practiceService = Mockito.mock(PracticeService.class);
        Mockito.when(practiceService.getPractice()).thenReturn(practice);

        IArchetypeService service = getArchetypeService();
        config = (Entity) service.create("entity.smartflowConfigurationType");
        setMinimumWordCount(0); // include all notes

        IMObjectBean practiceBean = service.getBean(practice);
        practiceBean.setTarget("smartflowConfiguration", config);

        FlowSheetConfigService configService = new FlowSheetConfigService(service, practiceService);
        processor = new NotesEventProcessor(getArchetypeService(), configService);
    }

    /**
     * Tests {@link NotesEvent}s.
     */
    @Test
    public void testNote() {
        Note note = createNote("a note");
        NotesEvent event = createEvent(note);
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
        Note note = createNote("a note");
        NotesEvent event = createEvent(note);
        processor.process(event);

        List<Act> items = getItems(visit);
        assertEquals(1, items.size());
        Act item = items.get(0);
        checkNote(item, "a note");

        item.setStatus(ActStatus.POSTED);
        save(item);

        note.setStatus(Note.CHANGED_STATUS);
        note.setText("different note");
        processor.process(event);

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
        Note note = createNote("a note");
        NotesEvent event = createEvent(note);
        processor.process(event);

        List<Act> items = getItems(visit);
        assertEquals(1, items.size());
        Act item = items.get(0);
        checkNote(item, "a note");

        item.setStatus(ActStatus.POSTED);
        save(item);

        note.setStatus(Note.REMOVED_STATUS);
        processor.process(event);

        item = get(item);
        assertNotNull(item);
        checkNote(item, "a note");
        checkAddendum(item, "This note was deleted in Smart Flow Sheet but cannot be removed as it is locked");
    }

    /**
     * Verifies that notes that are too short are excluded. This only applies to notes being added for the first
     * time.
     */
    @Test
    public void testExcludeShortNotes() {
        setMinimumWordCount(5);

        // create a note that is too short to be included
        Note note = createNote("a note");
        NotesEvent event = createEvent(note);
        processor.process(event);

        List<Act> item1 = getItems(visit);
        assertEquals(0, item1.size());

        // now amended it, so it will be included
        note.setStatus(Note.CHANGED_STATUS);
        note.setText("amended note to be longer");
        processor.process(event);

        List<Act> items2 = getItems(visit);
        assertEquals(1, items2.size());
        checkNote(items2.get(0), "amended note to be longer");

        // make the note too short. This will be updated as the record exists
        note.setText("amended note shorter");
        processor.process(event);

        List<Act> items3 = getItems(visit);
        assertEquals(1, items3.size());
        checkNote(items3.get(0), "amended note shorter");

        // now verify it is removed
        note.setStatus(Note.REMOVED_STATUS);
        processor.process(event);
        List<Act> items4 = getItems(visit);
        assertEquals(0, items4.size());
    }

    /**
     * Verify that notes aren't processed if {@code synchroniseNotes} is {@code false}.
     */
    @Test
    public void testDisableSynchroniseNotes() {
        setSynchroniseNotes(false);

        Note note = createNote("a note");
        NotesEvent event = createEvent(note);
        processor.process(event);

        List<Act> item1 = getItems(visit);
        assertEquals(0, item1.size());

        note.setStatus(Note.CHANGED_STATUS);
        note.setText("different note");
        processor.process(event);

        List<Act> item2 = getItems(visit);
        assertEquals(0, item2.size());

        note.setStatus(Note.REMOVED_STATUS);
        processor.process(event);

        List<Act> item3 = getItems(visit);
        assertEquals(0, item3.size());
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

    /**
     * Verifies an addendum is present and matches that expected.
     *
     * @param act          the clinical note act
     * @param expectedNote the expected text
     */
    private void checkAddendum(Act act, String expectedNote) {
        ActBean bean = new ActBean(act);
        List<Act> acts = bean.getNodeActs("addenda");
        assertEquals(1, acts.size());
        ActBean addendum = new ActBean(acts.get(0));
        assertEquals(patient, addendum.getNodeParticipant("patient"));
        assertEquals(expectedNote, addendum.getString("note"));
        assertEquals(visit, addendum.getNodeSourceObject("event"));
    }

    /**
     * Set the {@code synchroniseNotes} configuration option.
     *
     * @param synchroniseNotes if {@code true}, synchronise notes, otherwise ignore them
     */
    private void setSynchroniseNotes(boolean synchroniseNotes) {
        IMObjectBean bean = getArchetypeService().getBean(config);
        bean.setValue("synchroniseNotes", synchroniseNotes);
        bean.save();
    }

    /**
     * Sets the {@code minimumWordCount} configuration option.
     *
     * @param count exclude notes with words less than that specified
     */
    private void setMinimumWordCount(int count) {
        IMObjectBean bean = getArchetypeService().getBean(config);
        bean.setValue("minimumWordCount", count);
        bean.save();
    }

    /**
     * Creates a new note.
     *
     * @param text the note text
     * @return a new note
     */
    private Note createNote(String text) {
        Note note = new Note();
        note.setNoteGuid(UUID.randomUUID().toString());
        note.setHospitalizationId(Long.toString(visit.getId()));
        note.setStatus(Note.ADDED_STATUS);
        note.setText(text);
        return note;
    }

    /**
     * Creates a new notes event, with a single note.
     *
     * @param note the note
     * @return a new event
     */
    private NotesEvent createEvent(Note note) {
        NotesEvent event = new NotesEvent();
        Notes list = new Notes();
        list.setNotes(Collections.singletonList(note));
        event.setObject(list);
        return event;
    }

}
