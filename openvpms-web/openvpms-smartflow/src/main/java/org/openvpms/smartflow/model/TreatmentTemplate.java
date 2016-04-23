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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Smart Flow Sheet treatment template.
 *
 * @author benjamincharlton on 21/10/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TreatmentTemplate {

    /**
     * Describes the type of the object transferred with the SFS events. Should be assigned treatmenttemplate value.
     * Optional.
     */
    private String objectType = "treatmenttemplate";

    /**
     * The name of the template.
     */
    private String name;

    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Returns the treatment template name.
     *
     * @return the treatment template name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the treatment template name.
     *
     * @param name the treatment template name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the treatment template name.
     *
     * @return the treatment template name
     */
    @Override
    public String toString() {
        return name;
    }
}
