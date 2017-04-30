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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * List of hospitalizations.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hospitalizations {

    /**
     * Describes the type of the object transferred with the SFS events. Should be assigned hospitalizations value.
     */
    private String objectType = "hospitalizations";

    /**
     * Identifier of the object. Will be transferred to EMR with the SFS events.
     */
    private String id;

    /**
     * The hospitalizations.
     */
    @JsonDeserialize(contentAs = Hospitalization.class)
    private List<Hospitalization> hospitalizations;

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
     * Returns the object identifier.
     *
     * @return the object identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the object identifier.
     *
     * @param id the object identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the hospitalizations.
     *
     * @return the hospitalizations
     */
    public List<Hospitalization> getHospitalizations() {
        return hospitalizations;
    }

    /**
     * Sets the hospitalizations
     *
     * @param hospitalizations the hospitalizations
     */
    public void setHospitalizations(List<Hospitalization> hospitalizations) {
        this.hospitalizations = hospitalizations;
    }
}
