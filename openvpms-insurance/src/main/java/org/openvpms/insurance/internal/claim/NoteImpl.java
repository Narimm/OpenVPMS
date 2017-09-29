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

package org.openvpms.insurance.internal.claim;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.insurance.claim.Note;

import java.util.Date;

/**
 * Default implementation of the {@link Note} interface.
 *
 * @author Tim Anderson
 */
public class NoteImpl implements Note {

    /**
     * The note.
     */
    private final ActBean note;

    /**
     * Constructs a {@link NoteImpl}.
     *
     * @param note    the note
     * @param service the archetype service.
     */
    public NoteImpl(Act note, IArchetypeService service) {
        this.note = new ActBean(note, service);
    }

    /**
     * Returns the date/time the note was entered.
     *
     * @return the date/time
     */
    @Override
    public Date getDate() {
        return note.getAct().getActivityStartTime();
    }

    /**
     * Returns the author of the note.
     *
     * @return the author of the note. May be {@code null}
     */
    @Override
    public User getAuthor() {
        return (User) note.getNodeParticipant("author");
    }

    /**
     * Returns the clinician associated with the note.
     *
     * @return the clinician. May be {@code null}
     */
    @Override
    public User getClinician() {
        return (User) note.getNodeParticipant("clinician");
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
}
