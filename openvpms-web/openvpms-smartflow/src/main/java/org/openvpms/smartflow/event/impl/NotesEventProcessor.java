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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Note;
import org.openvpms.smartflow.model.Notes;
import org.openvpms.smartflow.model.event.NotesEvent;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;

/**
 * Processes {@link NotesEvent} events.
 *
 * @author Tim Anderson
 */
public class NotesEventProcessor extends EventProcessor<NotesEvent> {

    /**
     * The configuration service.
     */
    private final FlowSheetConfigService configService;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(NotesEventProcessor.class);

    /**
     * Constructs a {@link NotesEventProcessor}.
     *
     * @param service       the archetype service
     * @param configService the configuration service
     */
    public NotesEventProcessor(IArchetypeService service, FlowSheetConfigService configService) {
        super(service);
        this.configService = configService;
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(NotesEvent event) {
        FlowSheetConfig config = configService.getConfig();
        if (config.isSynchroniseNotes()) {
            Notes notes = event.getObject();
            if (notes != null && notes.getNotes() != null) {
                for (Note note : notes.getNotes()) {
                    process(note, config);
                }
            }
        } else {
            log.debug("Note synchronisation is disabled - notes will be ignored");
        }
    }

    /**
     * Processes a note.
     *
     * @param note   the note
     * @param config the configuration
     */
    protected void process(Note note, FlowSheetConfig config) {
        Act visit = getVisit(note.getHospitalizationId());
        if (visit != null) {
            process(note, visit, config);
        } else {
            log.error("No visit for hospitalization: " + note.getHospitalizationId());
        }
    }

    /**
     * Processes a note.
     *
     * @param note   the note
     * @param visit  the visit the note relates to
     * @param config the notes configuration
     */
    private void process(Note note, Act visit, FlowSheetConfig config) {
        IArchetypeService service = getService();
        Act act = getNote(note, visit);
        String status = note.getStatus();
        List<IMObject> toSave = new ArrayList<>();
        if (Note.REMOVED_STATUS.equals(status)) {
            if (act != null) {
                if (!ActStatus.POSTED.equals(act.getStatus())) {
                    service.remove(act);
                } else {
                    Party patient = getPatient(visit);
                    String text = FlowSheetMessages.cannotDeleteFinalisedNote();
                    addAddendum(visit, act, patient, text, toSave);
                }
            }
        } else {
            ActBean bean = null;
            Party patient = getPatient(visit);
            if (act == null) {
                if (!exclude(note, config)) {
                    act = (Act) service.create(PatientArchetypes.CLINICAL_NOTE);

                    bean = new ActBean(act, service);
                    bean.addNodeParticipation("patient", patient);
                    ActIdentity identity = createIdentity(note.getNoteGuid());
                    act.addIdentity(identity);

                    ActBean visitBean = new ActBean(visit, service);
                    visitBean.addNodeRelationship("items", act);
                    toSave.add(visit);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Excluding note=" + note.getText());
                    }
                }
            } else {
                bean = new ActBean(act, service);
            }
            if (bean != null) {
                if (ActStatus.POSTED.equals(act.getStatus())) {
                    addAddendum(visit, act, patient, note.getText(), toSave);
                } else {
                    bean.setValue("note", note.getText());
                    toSave.add(act);
                }
            }
        }
        if (!toSave.isEmpty()) {
            service.save(toSave);
        }
    }

    /**
     * Determines if a note is excluded based on the number of words present.
     *
     * @param note   the note
     * @param config the configuration
     * @return {@code true} if the note should be excluded
     */
    private boolean exclude(Note note, FlowSheetConfig config) {
        boolean result = false;
        int minimumWordCount = config.getMinimumWordCount();
        if (minimumWordCount > 1) {
            int count = countWords(note);
            if (count < minimumWordCount) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Counts the number of words in a note.
     *
     * @param note the note
     * @return the number of words
     */
    private int countWords(Note note) {
        String text = note.getText();
        return (text != null) ? text.trim().split("\\s+").length : 0;
    }

    /**
     * Adds an addendum to a note and links it to a visit.
     *
     * @param visit   the visit
     * @param note    the clinical note
     * @param patient the patient
     * @param text    the addendum text
     * @param toSave  the objects to save
     */
    private void addAddendum(Act visit, Act note, Party patient, String text, List<IMObject> toSave) {
        IArchetypeService service = getService();
        ActBean bean = new ActBean(note, service);
        Act addendum = createAddendum(patient, text);

        bean.addNodeRelationship("addenda", addendum);
        ActBean visitBean = new ActBean(visit, service);
        visitBean.addNodeRelationship("items", addendum);

        toSave.add(note);
        toSave.add(addendum);
        toSave.add(visit);
    }

    /**
     * Creates an addendum.
     *
     * @param patient the patient
     * @param note    the note
     * @return a new addendum
     */
    private Act createAddendum(Party patient, String note) {
        IArchetypeService service = getService();
        Act addendum = (Act) service.create(PatientArchetypes.CLINICAL_ADDENDUM);
        ActBean addendumBean = new ActBean(addendum, service);
        addendumBean.setNodeParticipant("patient", patient);
        addendumBean.setValue("note", note);
        return addendum;
    }

    /**
     * Returns a clinical note associated with an SFS note and visit.
     *
     * @param note  the note
     * @param visit the visit
     * @return the clinical note, or {@code null} if none is found
     */
    private Act getNote(Note note, Act visit) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_NOTE);
        query.add(join("event").add(eq("source", visit.getObjectReference())));
        query.add(join("identities", shortName(SFS_IDENTITY)).add(eq("identity", note.getNoteGuid())));
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(getService(), query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }


}
