/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.archetype.rules.finance.invoice;

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Helper to links charge item dispensing, investigation and document acts to patient clinical evemts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
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
     * Constructs a <tt>ChargeItemEventLinker</tt>.
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
     * @param item the charge item
     */
    public void link(FinancialAct item) {
        link(Arrays.asList(item));
    }

    /**
     * Links multiple charge item's dispensing, investigation and document acts to the associated patient's clinical
     * events.
     *
     * @param items the charge items
     */
    public void link(List<FinancialAct> items) {
        Map<IMObjectReference, List<Act>> events         // cache of patient clinical events keyed on patient reference
                = new HashMap<IMObjectReference, List<Act>>();
        Set<Act> toSave = new HashSet<Act>();            // the acts to save

        for (FinancialAct item : items) {
            List<Act> acts = new ArrayList<Act>();
            ActBean bean = new ActBean(item, service);
            acts.addAll(bean.getNodeActs("dispensing"));
            acts.addAll(bean.getNodeActs("investigations"));
            acts.addAll(bean.getNodeActs("documents"));

            Date startTime = item.getActivityStartTime();
            if (startTime == null) {
                startTime = new Date();
            }
            Set<Act> changed = rules.addToEvents(acts, startTime, events);
            toSave.addAll(changed);
        }
        if (!toSave.isEmpty()) {
            service.save(toSave);
        }
    }

    /**
     * Helper to make the clumsy but relatively efficient addToEvents() method accessible.
     */
    private class Rules extends MedicalRecordRules {

        /**
         * Constructs a <tt>Rules</tt>.
         *
         * @param service the archetype service
         */
        public Rules(IArchetypeService service) {
            super(service);
        }

        /**
         * Adds a list of <em>act.patientMedication</em>,
         * <em>act.patientInvestigation*</em> and <em>act.patientDocument*</em> acts
         * to an <em>act.patientClinicalEvent</em> associated with each act's
         * patient.
         *
         * @param acts      the acts to add
         * @param startTime the startTime used to select the event
         * @param events    the cache of events keyed on patient reference
         * @return the changed events
         */
        @Override
        public Set<Act> addToEvents(List<Act> acts, Date startTime, Map<IMObjectReference, List<Act>> events) {
            return super.addToEvents(acts, startTime, events);
        }
    }
}
