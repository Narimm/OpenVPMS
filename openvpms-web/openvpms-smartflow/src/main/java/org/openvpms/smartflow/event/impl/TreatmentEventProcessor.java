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

package org.openvpms.smartflow.event.impl;

import org.openvpms.archetype.rules.finance.order.CustomerPharmacyOrder;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.smartflow.model.Treatment;
import org.openvpms.smartflow.model.event.TreatmentEvent;

import java.math.BigDecimal;

import static org.openvpms.archetype.rules.finance.order.CustomerOrder.addNote;

/**
 * .
 *
 * @author Tim Anderson
 */
public class TreatmentEventProcessor extends EventProcessor<TreatmentEvent> {

    /**
     * The location.
     */
    private final Party location;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * Constructs a {@link TreatmentEventProcessor}.
     *
     * @param location the practice location. May be {@code null}
     * @param service  the archetype service
     * @param rules    the patient rules
     */
    public TreatmentEventProcessor(Party location, IArchetypeService service, PatientRules rules) {
        super(service);
        this.location = location;
        this.rules = rules;
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(TreatmentEvent event) {
        for (Treatment treatment : event.getObject()) {
            treated(treatment);
        }
    }


    /**
     * Invoked when a patient is treated.
     *
     * @param treatment the treatment
     */
    protected void treated(Treatment treatment) {
        Act visit = getVisit(treatment.getHospitalizationId(), null);
        Party patient = getPatient(visit);
        Party customer = rules.getOwner(patient);
        Product product = getProduct(treatment);
        IArchetypeService service = getService();
        CustomerPharmacyOrder order = new CustomerPharmacyOrder(
                patient, customer, null, location != null ? location.getObjectReference() : null, service);
        if (visit == null) {
            addNote(order.getOrder(), "Unknown visit, Id='" + treatment.getHospitalizationId()
                                      + "'. The customer and patient cannot be determined");
        } else if (patient == null) {
            addNote(order.getOrder(), "Cannot determine patient for visit");
        } else if (customer == null) {
            addNote(order.getOrder(), "Cannot determine customer for patient");
        }
        ActBean item = order.createOrderItem();
        item.setValue("quantity", MathRules.round(BigDecimal.valueOf(treatment.getQty()), 2));
        if (product != null) {
            item.setNodeParticipant("product", product);
        } else {
            addNote(order.getOrder(), "Unknown Treatment, Id='" + treatment.getInventoryId()
                                      + "', name='" + treatment.getName() + "'");
        }
        service.save(order.getActs());
    }

    private Product getProduct(Treatment treatment) {
        Product result = null;
        long id = getId(treatment.getInventoryId());
        if (id != -1) {
            ArchetypeQuery query = new ArchetypeQuery("product.*");
            query.getArchetypeConstraint().setAlias("p");
            query.add(Constraints.eq("id", id));
            IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<>(getService(), query);
            result = (iterator.hasNext()) ? iterator.next() : null;
        }
        return result;
    }

}
