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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.invoice;

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Helper to links charge item dispensing, investigation and document acts to patient clinical events.
 *
 * @author Tim Anderson
 */
public class ChargeItemEventLinker {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The rules.
     */
    private final Rules rules;


    /**
     * Constructs a {@link ChargeItemEventLinker}.
     *
     * @param service the archetype service
     */
    public ChargeItemEventLinker(IArchetypeService service) {
        this.service = service;
        rules = new Rules(service);
    }

    /**
     * Links a charge item's dispensing, investigation and document acts to the associated patient's clinical events.
     *
     * @param item    the charge item
     * @param changes the patient history changes
     */
    public void link(FinancialAct item, PatientHistoryChanges changes) {
        link(Arrays.asList(item), changes);
    }

    /**
     * Links an item to an event.
     * <p/>
     * The item must be linked to the same patient as the event.
     *
     * @param event   the event to link to
     * @param item    the charge item
     * @param changes the patient history changes
     */
    public void link(Act event, FinancialAct item, PatientHistoryChanges changes) {
        link(event, Arrays.asList(item), changes);
    }

    /**
     * Links items to an event.
     * <p/>
     * The items must be linked to the same patient as the event.
     *
     * @param event   the event to link to
     * @param items   the charge items
     * @param changes the patient history changes
     */
    public void link(Act event, List<FinancialAct> items, PatientHistoryChanges changes) {
        prepare(event, items, changes);
        changes.save();
    }

    /**
     * Links items to an event, recording the modifications in the supplied {@code changes}.
     * <p/>
     * Invoke {@link PatientHistoryChanges#save()} to commit the changes.
     *
     * @param event   the event
     * @param items   the charge items
     * @param changes the patient history changes
     */
    public void prepare(Act event, List<FinancialAct> items, PatientHistoryChanges changes) {
        for (FinancialAct item : items) {
            List<Act> acts = getActs(item, changes);
            rules.addToEvent(event, acts, changes);
        }
    }

    /**
     * Links multiple charge item's dispensing, investigation and document acts to the associated patient's clinical
     * events.
     *
     * @param items   the charge items
     * @param changes the patient history changes
     */
    public void link(List<FinancialAct> items, PatientHistoryChanges changes) {
        prepare(items, changes);
        changes.save();
    }

    /**
     * Links multiple charge item's dispensing, investigation and document acts to the associated patient's clinical
     * events. The modifications are recorded in the supplied {@code changes}.
     * <p/>
     * Invoke {@link PatientHistoryChanges#save()} to commit the changes.
     *
     * @param items   the charge items
     * @param changes the patient history changes
     */
    public void prepare(List<FinancialAct> items, PatientHistoryChanges changes) {
        for (FinancialAct item : items) {
            List<Act> acts = getActs(item, changes);
            Date startTime = item.getActivityStartTime();
            if (startTime == null) {
                startTime = new Date();
            }
            rules.addToEvents(acts, startTime, changes);
        }
    }

    /**
     * Returns the dispensing, investigations, and documents acts linked to a charge item.
     *
     * @param item    the charge item
     * @param changes the patient history changes
     * @return the acts
     */
    private List<Act> getActs(FinancialAct item, PatientHistoryChanges changes) {
        List<Act> acts = new ArrayList<Act>();
        ActBean bean = new ActBean(item, service);
        acts.add(item);
        acts.addAll(getActs(bean, "dispensing", changes));
        acts.addAll(getActs(bean, "investigations", changes));
        acts.addAll(getActs(bean, "documents", changes));
        return acts;
    }

    private List<Act> getActs(ActBean bean, String node, PatientHistoryChanges changes) {
        List<Act> result = new ArrayList<Act>();
        for (IMObjectReference ref : bean.getNodeTargetObjectRefs(node)) {
            Act act = (Act) changes.getObject(ref);
            if (act != null) {
                result.add(act);
            }
        }
        return result;
    }


    /**
     * Helper to make the clumsy but relatively efficient addToEvents() method accessible.
     */
    private class Rules extends MedicalRecordRules {

        /**
         * Constructs a {@code Rules}.
         *
         * @param service the archetype service
         */
        public Rules(IArchetypeService service) {
            super(service);
        }

        /**
         * Adds a list of <em>act.patientMedication</em>, <em>act.patientInvestigation*</em>,
         * <em>act.patientDocument*</em>, and <em>act.customerAccountInvoiceItem</em> acts to an
         * <em>act.patientClinicalEvent</em> associated with each act's patient.
         *
         * @param acts      the acts to add
         * @param startTime the startTime used to select the event
         * @param changes   the cache of events keyed on patient reference
         */
        @Override
        public void addToEvents(List<Act> acts, Date startTime, PatientHistoryChanges changes) {
            super.addToEvents(acts, startTime, changes);
        }

        /**
         * Adds acts to an event, where no relationship exists.
         *
         * @param event   the event
         * @param acts    the acts to add
         * @param changes tracks changes to the patient history
         */
        @Override
        public void addToEvent(Act event, List<Act> acts, PatientHistoryChanges changes) {
            super.addToEvent(event, acts, changes);
        }
    }
}
