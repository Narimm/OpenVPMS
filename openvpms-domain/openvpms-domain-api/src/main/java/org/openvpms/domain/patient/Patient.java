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

package org.openvpms.domain.patient;

import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.party.Party;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * An animal who is a patient.
 *
 * @author Tim Anderson
 */
public interface Patient extends Party {

    enum Sex {
        MALE,
        FEMALE,
        UNSPECIFIED
    }

    /**
     * Returns the patient's date of birth.
     *
     * @return the patient's date of birth. May be {@code null}
     */
    LocalDate getDateOfBirth();

    /**
     * Returns the species name.
     *
     * @return the species name. May be {@code null}
     */
    String getSpeciesName();

    /**
     * Returns the species code.
     *
     * @return the species code. May be {@code null}
     */
    String getSpeciesCode();

    /**
     * Returns the species lookup.
     *
     * @return the species lookup. May be {@code null}
     */
    Lookup getSpeciesLookup();

    /**
     * Returns the breed name.
     *
     * @return the breed name. May be {@code null}
     */
    String getBreedName();

    /**
     * Returns the breed code.
     *
     * @return the breed code. May be {@code null}
     */
    String getBreedCode();

    /**
     * Returns the breed lookup.
     *
     * @return the breed lookup. May be {@code null}
     */
    Lookup getBreedLookup();

    /**
     * Returns the patient's sex.
     *
     * @return the sex
     */
    Sex getSex();

    /**
     * Determines if the patient is desexed.
     *
     * @return {@code true} if the patient is desexed
     */
    boolean isDesexed();

    /**
     * Returns the colour name.
     *
     * @return the colour name. May be {@code null}
     */
    String getColourName();

    /**
     * Returns the colour code.
     *
     * @return the colour code. May be {@code null}
     */
    String getColourCode();

    /**
     * Returns the colour lookup.
     *
     * @return the colour lookup. May be {@code null}
     */
    Lookup getColourLookup();

    /**
     * Returns the patient's microchip.
     *
     * @return the patient's microchip. May be {@code null}
     */
    Microchip getMicrochip();

    /**
     * Returns the date when the patient was created in OpenVPMS.
     *
     * @return the date. May be {@code null}
     */
    OffsetDateTime getCreated();

}