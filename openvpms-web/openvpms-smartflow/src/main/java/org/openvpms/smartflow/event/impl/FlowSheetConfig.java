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

package org.openvpms.smartflow.event.impl;

import org.openvpms.component.model.bean.IMObjectBean;

/**
 * Smart Flow Sheet configuration.
 *
 * @author Tim Anderson
 */
public class FlowSheetConfig {

    /**
     * Determines if the flow sheet report should be saved on discharge.
     */
    private final boolean saveFlowSheetReportOnDischarge;

    /**
     * Determines if the medical records report should be saved on discharge.
     */
    private final boolean saveMedicalRecordsReportOnDischarge;

    /**
     * Determines if the billing report should be saved on discharge.
     */
    private final boolean saveBillingReportOnDischarge;

    /**
     * Determines if the notes report should be saved on discharge.
     */
    private final boolean saveNotesReportOnDischarge;

    /**
     * Determines if the forms should be saved on discharge.
     */
    private final boolean saveFormsReportsOnDischarge;

    /**
     * Determines if the anesthetics reports should be saved on discharge.
     */
    private final boolean saveAnestheticsReportsOnDischarge;

    /**
     * Determines if notes should be synchronised.
     */
    private final boolean synchroniseNotes;

    /**
     * Determines the minimum word count for new notes to be synchronised.
     */
    private final int minimumWordCount;

    /**
     * Constructs a default {@link FlowSheetConfig}.
     */
    public FlowSheetConfig() {
        this(true, true, true, true, true, true, true, 5);
    }

    /**
     * Constructs a default {@link FlowSheetConfig} from an <em>entity.smartflowConfiguration</em>.
     */
    public FlowSheetConfig(IMObjectBean bean) {
        this(bean.getBoolean("saveFlowSheetReportOnDischarge"), bean.getBoolean("saveMedicalRecordsReportOnDischarge"),
             bean.getBoolean("saveBillingReportOnDischarge"), bean.getBoolean("saveNotesReportOnDischarge"),
             bean.getBoolean("saveFormsReportOnDischarge"), bean.getBoolean("saveAnestheticsReportsOnDischarge"),
             bean.getBoolean("synchroniseNotes"), bean.getInt("minimumWordCount"));
    }

    /**
     * Constructs a {@link FlowSheetConfig}.
     *
     * @param saveFlowSheetReportOnDischarge      determines if the flow sheet report should be saved on discharge
     * @param saveMedicalRecordsReportOnDischarge determines if the medical records report should be saved on discharge
     * @param saveBillingReportOnDischarge        determines if the billing report should be saved on discharge
     * @param saveNotesReportOnDischarge          determines if the notes report should be saved on discharge
     * @param saveFormsReportsOnDischarge         determines if the forms should be saved on discharge
     * @param saveAnestheticsReportsOnDischarge   determines if the anesthetics reports should be saved on discharge
     * @param synchroniseNotes                    determines if notes should be synchronised
     * @param minimumWordCount                    determines the minimum word count for new notes to be synchronised
     */
    public FlowSheetConfig(boolean saveFlowSheetReportOnDischarge,
                           boolean saveMedicalRecordsReportOnDischarge,
                           boolean saveBillingReportOnDischarge,
                           boolean saveNotesReportOnDischarge,
                           boolean saveFormsReportsOnDischarge,
                           boolean saveAnestheticsReportsOnDischarge,
                           boolean synchroniseNotes, int minimumWordCount) {
        this.saveFlowSheetReportOnDischarge = saveFlowSheetReportOnDischarge;
        this.saveMedicalRecordsReportOnDischarge = saveMedicalRecordsReportOnDischarge;
        this.saveBillingReportOnDischarge = saveBillingReportOnDischarge;
        this.saveNotesReportOnDischarge = saveNotesReportOnDischarge;
        this.saveFormsReportsOnDischarge = saveFormsReportsOnDischarge;
        this.saveAnestheticsReportsOnDischarge = saveAnestheticsReportsOnDischarge;
        this.synchroniseNotes = synchroniseNotes;
        this.minimumWordCount = minimumWordCount;
    }

    /**
     * Determines if the flow sheet report should be saved on discharge.
     *
     * @return {@code true} the flow sheet report should be saved on discharge
     */
    public boolean isSaveFlowSheetReportOnDischarge() {
        return saveFlowSheetReportOnDischarge;
    }

    /**
     * Determines if the medical records report should be saved on discharge.
     *
     * @return {@code true} the medical records report should be saved on discharge
     */
    public boolean isSaveMedicalRecordsReportOnDischarge() {
        return saveMedicalRecordsReportOnDischarge;
    }

    /**
     * Determines if the billing report should be saved on discharge.
     *
     * @return {@code true} the billing report should be saved on discharge
     */
    public boolean isSaveBillingReportOnDischarge() {
        return saveBillingReportOnDischarge;
    }

    /**
     * Determines if the notes report should be saved on discharge.
     *
     * @return {@code true} the notes report should be saved on discharge
     */
    public boolean isSaveNotesReportOnDischarge() {
        return saveNotesReportOnDischarge;
    }

    /**
     * Determines if the forms reports should be saved on discharge.
     *
     * @return {@code true} the forms reports should be saved on discharge
     */
    public boolean isSaveFormsReportsOnDischarge() {
        return saveFormsReportsOnDischarge;
    }

    /**
     * Determines if the anesthetics reports should be saved on discharge.
     *
     * @return {@code true} the anesthetics reports should be saved on discharge
     */
    public boolean isSaveAnestheticsReportsOnDischarge() {
        return saveAnestheticsReportsOnDischarge;
    }

    /**
     * Determines if note synchronisation is enabled.
     *
     * @return {@code true} if note synchronisation is enabled, {@code false} if it is disabled
     */
    public boolean isSynchroniseNotes() {
        return synchroniseNotes;
    }

    /**
     * Determines the minimum word count for new notes.
     * <p>
     * Notes with fewer words will be excluded.
     *
     * @return the minimum word count
     */
    public int getMinimumWordCount() {
        return minimumWordCount;
    }

}
