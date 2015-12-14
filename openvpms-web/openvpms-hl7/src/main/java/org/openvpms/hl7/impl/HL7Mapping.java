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
     * Determines if admit/discharge/transfer messages should be sent.
     */
    private boolean sendADT = true;

    /**
     * Determines if ADT A08 (Update Patient Information) should be sent.
     */
    private boolean sendUpdatePatient = true;

    /**
     * Determines if ADT A11 (Cancel Admit) should be sent.
     */
    private boolean sendCancelAdmit = true;

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
     * The unmapped species code.
     */
    private String unmappedSpecies;

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
     * Determines if ADT messages should be sent.
     *
     * @return {@code true} if ADT messages should be sent
     */
    public boolean sendADT() {
        return sendADT;
    }

    /**
     * Determines if ADT messages should be sent.
     *
     * @param sendADT if {@code true} send ADT messages
     */
    public void setSendADT(boolean sendADT) {
        this.sendADT = sendADT;
    }

    /**
     * Determines if Update Patient Information (ADT A08) should be sent.
     *
     * @return {@code true} if ADT A08 messages should be sent
     */
    public boolean sendUpdatePatient() {
        return sendUpdatePatient;
    }

    /**
     * Determines if Update Patient Information (ADT A08) should be sent.
     *
     * @param sendUpdatePatient if {@code true} send ADT A08 messages
     */
    public void setSendUpdatePatient(boolean sendUpdatePatient) {
        this.sendUpdatePatient = sendUpdatePatient;
    }

    /**
     * Determines if Cancel Admit (ADT A11) should be sent.
     *
     * @return {@code true} if ADT A11 messages should be sent
     */
    public boolean sendCancelAdmit() {
        return sendCancelAdmit;
    }

    /**
     * Determines if Cancel Admit (ADT A11) should be sent.
     *
     * @param sendCancelAdmit if {@code true} send ADT A11 messages
     */
    public void setSendCancelAdmit(boolean sendCancelAdmit) {
        this.sendCancelAdmit = sendCancelAdmit;
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
     * Sets the administrative sex code for a male patient.
     *
     * @param male the administrative sex code. May be {@code null}
     */
    public void setMale(String male) {
        this.male = male;
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
     * Sets the administrative sex code for a desexed male patient.
     *
     * @param maleDesexed the administrative sex code. May be {@code null}
     */
    public void setMaleDesexed(String maleDesexed) {
        this.maleDesexed = maleDesexed;
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
     * Sets the administrative sex code for a female patient.
     *
     * @param female the administrative sex code. May be {@code null}
     */
    public void setFemale(String female) {
        this.female = female;
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
     * Sets the administrative sex code for a female desexed patient.
     *
     * @param femaleDesexed the administrative sex code. May be {@code null}
     */
    public void setFemaleDesexed(String femaleDesexed) {
        this.femaleDesexed = femaleDesexed;
    }

    /**
     * Sets the administrative sex code for a patient of unknown sex.
     *
     * @param unknownSex the administrative sex code. May be {@code null}
     */
    public void setUnknownSex(String unknownSex) {
        this.unknownSex = unknownSex;
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
     * Returns the species mapping lookup archetype.
     *
     * @return the species mapping lookup. May be {@code null}
     */
    public String getSpeciesLookup() {
        return speciesLookup;
    }

    /**
     * Sets the species mapping lookup archetype.
     *
     * @param speciesLookup the species mapping lookup. May be {@code null}
     */
    public void setSpeciesLookup(String speciesLookup) {
        this.speciesLookup = speciesLookup;
    }

    /**
     * Returns the code to use if no mapping exists for a species.
     *
     * @return the unmapped species code. May be {@code null}
     */
    public String getUnmappedSpecies() {
        return unmappedSpecies;
    }

    /**
     * Sets the code to use if no mapping exists for a species.
     *
     * @param unmappedSpecies the unmapped species code. May be {@code null}
     */
    public void setUnmappedSpecies(String unmappedSpecies) {
        this.unmappedSpecies = unmappedSpecies;
    }

    /**
     * Determines if date/times should include milliseconds.
     *
     * @return {@code true} if date/times should include milliseconds
     */
    public boolean includeMillis() {
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
    public boolean includeTimeZone() {
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
     * Creates a mapping from an <em>entity.HL7Mapping*</em> entity.
     *
     * @param mapping the mapping
     * @param service the archetype service
     * @return a new mapping
     */
    public static HL7Mapping create(Entity mapping, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(mapping, service);
        HL7Mapping result = new HL7Mapping();
        result.setSendADT(bean.getBoolean("sendADT", true));
        result.setSendUpdatePatient(bean.getBoolean("sendUpdatePatient", true));
        result.setSendCancelAdmit(bean.getBoolean("sendCancelAdmit", true));

        // This is deprecated, but included as Cubex uses it.
        result.setPopulatePID2(bean.getBoolean("setPID2", true));

        // PID-3 should be used by default, but in the absence of any configuration defaults to off.
        result.setPopulatePID3(bean.getBoolean("setPID3", false));

        result.setMale(bean.getString("male"));
        result.setMaleDesexed(bean.getString("maleDesexed"));
        result.setFemale(bean.getString("female"));
        result.setFemaleDesexed(bean.getString("femaleDesexed"));
        result.setUnknownSex(bean.getString("unknownSex"));

        result.setSpeciesLookup(bean.getString("speciesMapping"));
        result.setUnmappedSpecies(bean.getString("unmappedSpecies"));

        result.setIncludeMillis(bean.getBoolean("includeMillis"));
        result.setIncludeTimeZone(bean.getBoolean("includeTimeZone"));
        return result;
    }
}
