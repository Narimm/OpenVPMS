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

package org.openvpms.insurance.policy;

import java.util.Date;

/**
 * Represents an insured animal.
 *
 * @author Tim Anderson
 */
public interface Animal {

    enum Sex {
        MALE,
        FEMALE,
        UNSPECIFIED
    }

    /**
     * Returns the OpenVPMS identifier for the animal.
     *
     * @return the OpenVPMS identifier for the animal
     */
    long getId();

    /**
     * Returns the animal's name.
     *
     * @return the animal's name
     */
    String getName();

    /**
     * Returns the animal's date of birth.
     *
     * @return the animal's date of birth. May be {@code null}
     */
    Date getDateOfBirth();

    /**
     * Returns the species name.
     *
     * @return the species name. May be {@code null}
     */
    String getSpecies();

    /**
     * Returns the breed name.
     *
     * @return the breed name. May be {@code null}
     */
    String getBreed();

    /**
     * Returns the animal's sex.
     *
     * @return the sex
     */
    Sex getSex();

    /**
     * Returns the animal's colour.
     *
     * @return the animal's colour. May be {@code null}
     */
    String getColour();

    /**
     * Returns the animal's microchip number.
     *
     * @return the animal's microchip number. May be {@code null}
     */
    String getMicrochip();

    /**
     * Returns the date when the animal was created in OpenVPMS.
     *
     * @return the date. May be {@code null}
     */
    Date getCreatedDate();

}
