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

package org.openvpms.domain.internal.patient;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.archetype.NodeDescriptor;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.business.domain.im.party.PartyDecorator;
import org.openvpms.domain.patient.Microchip;
import org.openvpms.domain.patient.Patient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Default implementation of {@link Patient}.
 *
 * @author Tim Anderson
 */
public class PatientImpl extends PartyDecorator implements Patient {

    /**
     * The bean.
     */
    private final IMObjectBean bean;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The species node name.
     */
    private static final String SPECIES = "species";

    /**
     * The breed node name.
     */
    private static final String BREED = "breed";

    /**
     * The colour node name.
     */
    private static final String COLOUR = "colour";

    /**
     * Constructs a {@link PatientImpl}.
     *
     * @param peer the peer to delegate to
     */
    public PatientImpl(Party peer, IArchetypeService service, PatientRules rules) {
        super(peer);
        bean = service.getBean(peer);
        this.rules = rules;
    }

    /**
     * Returns the patient's date of birth.
     *
     * @return the patient's date of birth. May be {@code null}
     */
    @Override
    public LocalDate getDateOfBirth() {
        Date date = rules.getDateOfBirth(getPeer());
        return DateRules.toLocalDate(date);
    }

    /**
     * Returns the species name.
     *
     * @return the species name
     */
    @Override
    public String getSpeciesName() {
        Lookup lookup = getSpeciesLookup();
        return (lookup != null) ? lookup.getName() : null;
    }

    /**
     * Returns the species code.
     *
     * @return the species code. May be {@code null}
     */
    @Override
    public String getSpeciesCode() {
        return bean.getString(SPECIES);
    }

    /**
     * Returns the species.
     *
     * @return the species.
     */
    @Override
    public Lookup getSpeciesLookup() {
        return bean.getLookup(SPECIES);
    }

    /**
     * Returns the breed name.
     *
     * @return the breed name. May be {@code null}
     */
    @Override
    public String getBreedName() {
        Lookup lookup = getBreedLookup();
        return (lookup != null) ? lookup.getName() : null;
    }

    /**
     * Returns the breed code.
     *
     * @return the breed code. May be {@code null}
     */
    @Override
    public String getBreedCode() {
        return bean.getString(BREED);
    }

    /**
     * Returns the breed lookup.
     *
     * @return the breed lookup. May be {@code null}
     */
    @Override
    public Lookup getBreedLookup() {
        return bean.getLookup(BREED);
    }

    /**
     * Returns the patient's sex.
     *
     * @return the sex
     */
    @Override
    public Sex getSex() {
        String code = bean.getString("sex");
        return (code != null) ? Sex.valueOf(code) : Sex.UNSPECIFIED;
    }

    /**
     * Determines if the patient is desexed.
     *
     * @return {@code true} if the patient is desexed
     */
    @Override
    public boolean isDesexed() {
        return bean.getBoolean("desexed");
    }

    /**
     * Returns the patient's colour.
     * <p/>
     * Some implementations define this as a lookup.
     *
     * @return the patient's colour. May be {@code null}
     */
    @Override
    public String getColourName() {
        Lookup lookup = getColourLookup();
        return (lookup != null) ? lookup.getName() : getColourCode();
    }

    /**
     * Returns the colour code.
     *
     * @return the colour code. May be {@code null}
     */
    @Override
    public String getColourCode() {
        return bean.getString(COLOUR);
    }

    /**
     * Returns the colour lookup.
     *
     * @return the colour lookup. May be {@code null}
     */
    @Override
    public Lookup getColourLookup() {
        NodeDescriptor node = bean.getNode(COLOUR);
        return node.isLookup() ? bean.getLookup(COLOUR) : null;
    }

    /**
     * Returns the patient's microchip.
     *
     * @return the patient's microchip. May be {@code null}
     */
    @Override
    public Microchip getMicrochip() {
        EntityIdentity identity = rules.getMicrochip(getPeer());
        return identity != null ? new MicrochipImpl(identity) : null;
    }

    /**
     * Returns the date when the patient was created in OpenVPMS.
     *
     * @return the date. May be {@code null}
     */
    @Override
    public OffsetDateTime getCreated() {
        Date date = bean.getDate("createdDate");
        return DateRules.toOffsetDateTime(date);
    }
}
