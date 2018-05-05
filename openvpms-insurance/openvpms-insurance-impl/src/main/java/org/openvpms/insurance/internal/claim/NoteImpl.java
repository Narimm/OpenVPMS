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
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.functor.ActComparator;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.insurance.claim.Note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of the {@link Note} interface.
 *
 * @author Tim Anderson
 */
public class NoteImpl implements Note {

    /**
     * The note.
     */
    private final IMObjectBean note;

    /**
     * The underlying act.
     */
    private final Act act;

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * Addenda.
     */
    private List<Note> addenda;

    /**
     * Constructs a {@link NoteImpl}.
     *
     * @param note    the note or addenda
     * @param service the archetype service.
     */
    public NoteImpl(Act note, IArchetypeRuleService service) {
        this.note = service.getBean(note);
        this.act = note;
        this.service = service;
        if (note.isA(PatientArchetypes.CLINICAL_ADDENDUM)) {
            // addendum acts can't have addenda
            addenda = Collections.emptyList();
        }
    }

    /**
     * Returns the date/time the note was entered.
     *
     * @return the date/time
     */
    @Override
    public Date getDate() {
        return act.getActivityStartTime();
    }

    /**
     * Returns the author of the note.
     *
     * @return the author of the note. May be {@code null}
     */
    @Override
    public User getAuthor() {
        return note.getTarget("author", User.class);
    }

    /**
     * Returns the clinician associated with the note.
     *
     * @return the clinician. May be {@code null}
     */
    @Override
    public User getClinician() {
        return note.getTarget("clinician", User.class);
    }

    /**
     * Returns the text of the note.
     *
     * @return the text of the note
     */
    @Override
    public String getText() {
        return note.getString("note");
    }

    /**
     * Returns additional notes associated with this note.
     *
     * @return additional notes, if any
     */
    @Override
    public List<Note> getNotes() {
        if (addenda == null) {
            addenda = collectAddenda();
        }
        return addenda;
    }

    /**
     * Collects addenda associated with the note.
     *
     * @return the addenda
     */
    protected List<Note> collectAddenda() {
        List<Note> result = new ArrayList<>();
        List<Act> acts = note.getTargets("addenda", Act.class);
        Collections.sort(acts, ActComparator.ascending());
        for (Act act : acts) {
            result.add(new NoteImpl(act, service));
        }
        return result;
    }
}
