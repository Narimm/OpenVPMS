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

package org.openvpms.smartflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Smart Flow Sheet note.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Note {

    /**
     * Note added status.
     */
    public static final String ADDED_STATUS = "added";

    /**
     * Note changed status.
     */
    public static final String CHANGED_STATUS = "changed";

    /**
     * Note removed status.
     */
    public static final String REMOVED_STATUS = "removed";

    /**
     * Describes the type of the object transferred with the SFS events (e.g. notes.entered).
     * Should be assigned note value.
     */
    private String objectType = "note";

    /**
     * A unique internal identifier of the note item.
     */
    private String noteGuid;

    /**
     * Hospitalization external id (which was provided with hospitalization creation).
     */
    private String hospitalizationId;

    /**
     * Note creation time (UTC time that corresponds to an hour on a flowsheet).
     */
    private Date time;

    /**
     * The string value that was entered during note creation.
     */
    private String text;

    /**
     * This field describes what have happened to the note. Can be one of the following: added, changed,
     * removed.
     */
    private String status;

    /**
     * The type of note. Should be 0 - Flowsheet note or 1 - Anesthetic note.
     */
    private int type;

    /**
     * A unique internal identifier of the anesthetic sheet, required if type = 1, otherwise is equal to null.
     */
    private String anestheticGuid;

    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Sets the object type.
     *
     * @param objectType the object type
     */
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * Returns a unique internal identifier of the note item.
     *
     * @return a unique internal identifier of the note item
     */
    public String getNoteGuid() {
        return noteGuid;
    }

    /**
     * Sets the unique internal identifier of the note item.
     *
     * @param noteGuid a unique internal identifier of the note item
     */
    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    /**
     * Returns the hospitalization identifier.
     *
     * @return the hospitalization identifier
     */
    public String getHospitalizationId() {
        return hospitalizationId;
    }

    /**
     * Sets the hospitalization identifier.
     *
     * @param hospitalizationId the hospitalization identifier
     */
    public void setHospitalizationId(String hospitalizationId) {
        this.hospitalizationId = hospitalizationId;
    }

    /**
     * Returns the note creation time (UTC time that corresponds to an hour on a flowsheet).
     *
     * @return the note creation time
     */
    public Date getTime() {
        return time;
    }

    /**
     * Sets the note creation time.
     *
     * @param time the note creation time
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * Returns the note text.
     *
     * @return the note text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the note text.
     *
     * @param text the note text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the note status.
     * Can be one of the following: 1. added, 2. changed, 3. removed.
     *
     * @return the note status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the note status.
     *
     * @param status the note status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the type of note. Should be 0 - Flowsheet note or 1 - Anesthetic note.
     *
     * @return the type of note
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type of note.
     *
     * @param type the type of note
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Returns the unique internal identifier of the anesthetic sheet, required if type = 1, otherwise is equal to null.
     *
     * @return the anesthetic sheet identifier. May be {@code null}
     */
    public String getAnestheticGuid() {
        return anestheticGuid;
    }

    /**
     * Sets the unique internal identifier of the anesthetic sheet
     *
     * @param anestheticGuid the anesthetic sheet identifier. May be {@code null}
     */
    public void setAnestheticGuid(String anestheticGuid) {
        this.anestheticGuid = anestheticGuid;
    }
}
