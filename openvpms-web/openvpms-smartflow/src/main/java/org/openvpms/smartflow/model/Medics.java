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

import java.util.List;

/**
 * Smart Flow Sheet medics.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Medics {

    /**
     * Describes the type of the object transferred with the SFS events (e.g. medics.imported).
     * Should be assigned medics value. Optional.
     */
    private String objectType = "medics";

    /**
     * Identifier of the object. Will be transferred to EMR with the SFS events (e.g. medics.imported). Optional.
     */
    private String id;

    /**
     * The medics.
     */
    private List<Medic> medics;

    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Returns the identifier of the object. Will be transferred to EMR with the SFS events (e.g. medics.imported).
     *
     * @return the identifier. May beb {@code null}
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier of the object. Will be transferred to EMR with the SFS events (e.g. inventory.imported).
     *
     * @param id the identifier. May beb {@code null}
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the medics.
     *
     * @return the medics
     */
    public List<Medic> getMedics() {
        return medics;
    }

    /**
     * Sets the medics.
     *
     * @param medics the medics
     */
    public void setMedics(List<Medic> medics) {
        this.medics = medics;
    }

}
