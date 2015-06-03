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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Configures message population.
 *
 * @author Tim Anderson
 */
public class HL7Mapping {

    /**
     * Determines if PID-3 should be populated with the patient identifier.
     */
    private boolean populatePID3 = true;

    /**
     * Determines if PID-2 should be populated with the patient identifier.
     */
    private boolean populatePID2;

    /**
     * Male code.
     */
    private String male = "M";

    /**
     * Desexed male code.
     */
    private String maleDesexed = "C";

    /**
     * Female code.
     */
    private String female = "F";

    /**
     * Desexed female code.
     */
    private String femaleDesexed = "S";

    /**
     * Unknown sex code.
     */
    private String unknownSex = "U";

    /**
     * The species lookup.
     */
    private String speciesLookup;

    /**
     * Determines if date/times should include milliseconds.
     */
    private boolean includeMillis = true;

    /**
     * Determines if date/times should include timezone offsets.
     */
    private boolean includeTimeZone = true;

    /**
     * Constructs an {@link HL7Mapping}.
     */
    public HL7Mapping() {

    }

    /**
     * Constructs an {@link HL7Mapping}.
     *
     * @param populatePID3 if {@code true} populate PID-3 with the patient identifier
     * @param populatePID2 if {@code true} populate PID-2 with the patient identifier
     */
    public HL7Mapping(boolean populatePID3, boolean populatePID2, String male, String maleDesexed, String female,
                      String femaleDesexed, String unknownSex, String speciesLookup, boolean includeMillis,
                      boolean includeTimeZone) {
        this.populatePID3 = populatePID3;
        this.populatePID2 = populatePID2;
        this.male = male;
        this.maleDesexed = maleDesexed;
        this.female = female;
        this.femaleDesexed = femaleDesexed;
        this.unknownSex = unknownSex;
        this.speciesLookup = speciesLookup;
        this.includeMillis = includeMillis;
        this.includeTimeZone = includeTimeZone;
    }

    /**
     * Determines if PID-3 should be populated with the patient identifier.
     *
     * @return {@code true} if PID-3 should be populated
     */
    public boolean getPopulatePID3() {
        return populatePID3;
    }

    /**
     * Determines if PID-3 should be populated with the patient identifier.
     *
     * @param populatePID3 if {@code true} populate PID-3
     */
    public void setPopulatePID3(boolean populatePID3) {
        this.populatePID3 = populatePID3;
    }

    /**
     * Determines if PID-2 should be populated with the patient identifier.
     *
     * @return {@code true} if PID-2 should be populated
     */
    public boolean getPopulatePID2() {
        return populatePID2;
    }

    /**
     * Determines if PID-2 should be populated with the patient identifier.
     *
     * @param populatePID2 if {@code true} populate PID-2
     */
    public void setPopulatePID2(boolean populatePID2) {
        this.populatePID2 = populatePID2;
    }

    /**
     * Returns the administrative sex code for a male patient.
     *
     * @return the administrative sex code. May be {@code null}
     */
    public String getMale() {
        return male;
    }

    /**
     * Returns the administrative sex code for a desexed male patient.
     *
     * @return the administrative sex code. May be {@code null}
     */
    public String getMaleDesexed() {
        return maleDesexed;
    }

    /**
     * Returns the administrative sex code for a female patient.
     *
     * @return the administrative sex code. May be {@code null}
     */
    public String getFemale() {
        return female;
    }

    /**
     * Returns the administrative sex code for a female desexed patient.
     *
     * @return the administrative sex code. May be {@code null}
     */
    public String getFemaleDesexed() {
        return femaleDesexed;
    }

    /**
     * Returns the administrative sex code for a patient of unknown sex.
     *
     * @return the administrative sex code. May be {@code null}
     */
    public String getUnknownSex() {
        return unknownSex;
    }

    /**
     * Determines if date/times should include milliseconds.
     *
     * @return {@code true} if date/times should include milliseconds
     */
    public boolean isIncludeMillis() {
        return includeMillis;
    }

    /**
     * Determines if date/times should include milliseconds.
     *
     * @param includeMillis if {@code true}, date/times should include milliseconds
     */
    public void setIncludeMillis(boolean includeMillis) {
        this.includeMillis = includeMillis;
    }

    /**
     * Determines if date/times should include time zones.
     *
     * @return {@code true} if date/times should include time zones
     */
    public boolean isIncludeTimeZone() {
        return includeTimeZone;
    }

    /**
     * Determines if date/times should include time zones.
     *
     * @param includeTimeZone if {@code true}, date/times should include time zones
     */
    public void setIncludeTimeZone(boolean includeTimeZone) {
        this.includeTimeZone = includeTimeZone;
    }

    /**
     * Returns the species mapping lookup archetype.
     *
     * @return the species mapping lookup. May be {@code null}
     */
    public String getSpeciesLookup() {
        return speciesLookup;
    }

    /**
     * Creates a mapping from an <em>entity.HL7Mapping*</em> entity.
     *
     * @param mapping the mapping
     * @param service the archetype service
     * @return a new mapping
     */
    public static HL7Mapping create(Entity mapping, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(mapping, service);
        return new HL7Mapping(bean.getBoolean("populatePID3"), bean.getBoolean("populatePID2"), bean.getString("male"),
                              bean.getString("maleDesexed"), bean.getString("female"),
                              bean.getString("femaleDesexed"), bean.getString("unknownSex"),
                              bean.getString("speciesMapping"), bean.getBoolean("includeMillis"),
                              bean.getBoolean("includeTimeZone"));
    }
}
