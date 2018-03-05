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

package org.openvpms.insurance.claim;

import org.openvpms.component.model.user.User;

import java.util.Date;
import java.util.List;

/**
 * A note from the animal's clinical history.
 *
 * @author Tim Anderson
 */
public interface Note {

    /**
     * Returns the date/time the note was entered.
     *
     * @return the date/time
     */
    Date getDate();

    /**
     * Returns the author of the note.
     *
     * @return the author of the note. May be {@code null}
     */
    User getAuthor();

    /**
     * Returns the clinician associated with the note.
     *
     * @return the clinician. May be {@code null}
     */
    User getClinician();

    /**
     * Returns the text of the note.
     *
     * @return the text of the note
     */
    String getText();

    /**
     * Returns additional notes associated with this note.
     *
     * @return additional notes, if any
     */
    List<Note> getNotes();
}
