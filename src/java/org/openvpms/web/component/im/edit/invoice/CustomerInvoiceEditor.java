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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.invoice;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.QueryIterator;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.util.ErrorHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountChargesInvoice</em>,
 * <em>act.customerAccountChargesCredit</em>
 * or <em>act.customerAccountChargesCounter</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class CustomerInvoiceEditor extends InvoiceEditor {

    /**
     * Constructs a new <code>CustomerInvoiceEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public CustomerInvoiceEditor(Act act, IMObject parent,
                                 LayoutContext context) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    public boolean save() {
        boolean saved = true;
        if (super.save()) {
            saved = processMedication();
        }
        return saved;
    }

    /**
     * Links medication acts associated with the invoice to the current
     * IN_PROGRESS or COMPLETED visit for the associated patient.
     *
     * @return <code>true</code> if medication was processed successfully
     */
    private boolean processMedication() {
        boolean saved = false;
        try {
            ActRelationshipCollectionEditor editor = getEditor();
            MedicationProcessor processor
                    = new MedicationProcessor(editor.getActs());
            processor.process();
            saved = true;
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return saved;
    }

    /**
     * Helper to link medication acts associated with the invoice to
     * the current IN_PROGRESS or COMPLETED <em>act.patientClinicalEvent</em>
     * for the associated patient.
     */
    private static class MedicationProcessor {

        /**
         * The invoice act items.
         */
        private final List<Act> acts;

        /**
         * The set of retrieved <em>act.patientClinicalEvent</em>s.
         */
        private final Map<Party, Act> events = new HashMap<Party, Act>();

        /**
         * The set of modified <em>act.patientClinicalEvent</em>s.
         */
        private final Set<Act> processedEvents = new HashSet<Act>();


        /**
         * Constructs a new <code>MedicationProcessor</code>.
         *
         * @param acts the invoice act items
         */
        public MedicationProcessor(List<Act> acts) {
            this.acts = acts;
        }

        /**
         * Processes the act items.
         *
         * @throws OpenVPMSException for any error.
         */
        public void process() {
            for (Act act : acts) {
                if (TypeHelper.isA(act, "act.customerAccountInvoiceItem")) {
                    processInvoice(act);
                }
            }
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            for (Act event : processedEvents) {
                service.save(event);
            }
        }

        /**
         * Processes an invoice item.
         *
         * @param act an <em>act.customerAccountInvoiceItem</em>.
         */
        private void processInvoice(Act act) {
            ActBean bean = new ActBean(act);
            for (Act medication : bean.getActs("act.patientMedication")) {
                processMedication(medication);
            }
        }

        /**
         * Processes a medication act.
         *
         * @param medication an <em>act.patientMedication</em>.
         */
        private void processMedication(Act medication) {
            ActBean medBean = new ActBean(medication);
            Party patient = (Party) medBean.getParticipant(
                    "participation.patient");
            if (patient != null) {
                Act event = getClinicalEventItem(patient);
                if (event != null) {
                    ActBean eventBean = new ActBean(event);
                    if (eventBean.getRelationship(medication) == null) {
                        eventBean.addRelationship(
                                "actRelationship.patientClinicalEventItem",
                                medication);
                        processedEvents.add(event);
                    }
                }
            }
        }

        /**
         * Returns the most recent <em>act.patientClinicalEvent</em> for a
         * patient.
         *
         * @param patient the patient
         * @return the most recent event or <code>null</code> if none is found
         */
        private Act getClinicalEventItem(Party patient) {
            Act event = events.get(patient);
            if (event == null) {
                ArchetypeQuery query = new ArchetypeQuery(
                        "act.patientClinicalEvent", false, true);
                query.setFirstResult(0);
                query.setMaxResults(1);

                ParticipantConstraint participant
                        = new ParticipantConstraint("patient",
                                                    "participation.patient",
                                                    patient);
                query.add(participant);
                OrConstraint or = new OrConstraint();
                or.add(new NodeConstraint("status", ActStatus.IN_PROGRESS));
                or.add(new NodeConstraint("status", ActStatus.COMPLETED));
                query.add(or);
                query.add(new NodeSortConstraint("startTime", false));

                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                QueryIterator<Act> iterator = new IMObjectQueryIterator<Act>(
                        service, query);
                if (iterator.hasNext()) {
                    event = iterator.next();
                    events.put(patient, event);
                }
            }
            return event;
        }
    }

}