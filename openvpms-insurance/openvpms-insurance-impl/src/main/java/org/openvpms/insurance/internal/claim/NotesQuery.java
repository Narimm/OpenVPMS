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

package org.openvpms.insurance.internal.claim;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.insurance.claim.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gte;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lt;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * Queries {@link Note}s for a patient.
 *
 * @author Tim Anderson
 */
class NotesQuery {

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * Constructs a {@link NotesQuery}.
     *
     * @param service the archetype service
     */
    public NotesQuery(IArchetypeRuleService service) {
        this.service = service;
    }

    /**
     * Queries all notes linked to <em>act.patientClinicalEvents</em> dated between the specified date range.
     *
     * @param patient the patient
     * @param from    the start date (inclusive). May be {@code null}
     * @param to      the end date (exclusive). May be {@code null}
     * @return the notes
     */
    public List<Note> query(Party patient, Date from, Date to) {
        List<Note> result = new ArrayList<>();
        ArchetypeQuery query = new ArchetypeQuery(Constraints.shortName("note", PatientArchetypes.CLINICAL_NOTE));
        JoinConstraint event = join("source", "event")
                .add(join("patient").add(eq("entity", patient)));
        if (from != null) {
            event.add(gte("event.startTime", from));
        }
        query.add(join("event", "e").add(event));
        if (to != null) {
            query.add(or(isNull("event.endTime"), lt("event.endTime", to)));
        }
        query.add(sort("event.startTime"));
        query.add(sort("event.id"));
        query.add(sort("note.startTime"));
        query.add(sort("note.id"));
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            Act note = iterator.next();
            result.add(new NoteImpl(note, service));
        }
        return result;
    }
}
