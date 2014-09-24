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

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.EI;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.group.RDS_O13_ORDER;
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.RXD;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Processes RDS messages.
 *
 * @author Tim Anderson
 */
public class RDSProcessor {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * Constructs a {@link RDSProcessor}.
     *
     * @param service the archetype service
     * @param rules   the patient rules
     */
    public RDSProcessor(IArchetypeService service, PatientRules rules) {
        this.service = service;
        this.rules = rules;
    }

    /**
     * Processes a dispense message.
     *
     * @param message the message
     * @return the customer order
     * @throws HL7Exception for any HL7 error
     */
    public List<Act> process(RDS_O13 message) throws HL7Exception {
        PID pid = message.getPATIENT().getPID();
        Act order = (Act) service.create("act.customerOrderPharmacy");
        Act item = (Act) service.create("act.customerOrderItemPharmacy");
        ActBean bean = new ActBean(order, service);
        bean.addNodeRelationship("items", item);
        ActBean itemBean = new ActBean(item, service);
        Party patient = addPatient(pid, bean, itemBean);
        if (patient != null) {
            addCustomer(bean, patient);
        }
        String fillerOrderNumber = message.getORDER().getORC().getFillerOrderNumber().getEntityIdentifier().getValue();
        if (fillerOrderNumber != null) {
            itemBean.setValue("reference", fillerOrderNumber);
        }
        addInvoiceItem(message.getORDER().getORC(), bean, itemBean);
        addProduct(message.getORDER(), bean, itemBean);
        return Arrays.asList(order, item);
    }

    private void addCustomer(ActBean bean, Party patient) {
        Party customer = rules.getOwner(patient);
        if (customer != null) {
            bean.setNodeParticipant("customer", customer);
        }
    }

    private void addProduct(RDS_O13_ORDER order, ActBean bean, ActBean itemBean) {
        RXD rxd = order.getRXD();
        CE code = rxd.getDispenseGiveCode();
        long id = getId(code);
        IMObjectReference product = null;
        if (id != -1) {
            ArchetypeQuery query = new ArchetypeQuery("product.*");
            query.getArchetypeConstraint().setAlias("p");
            query.add(new ObjectRefSelectConstraint("p"));
            query.add(Constraints.eq("id", id));
            ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
            if (iterator.hasNext()) {
                ObjectSet set = iterator.next();
                product = set.getReference("p.reference");
            }
        }
        if (product != null) {
            itemBean.addNodeParticipation("product", product);
        } else {
            addNote(bean, "Unknown Dispense Give Code, id='" + code.getIdentifier().getValue()
                          + "', name='" + code.getText().getValue() + "'");
        }
        itemBean.setValue("quantity", new BigDecimal(rxd.getActualDispenseAmount().getValue()));
    }

    /**
     * Adds a reference to the original invoice item, if any.
     *
     * @param orc      the order segment
     * @param bean     the act
     * @param itemBean the item
     */
    private void addInvoiceItem(ORC orc, ActBean bean, ActBean itemBean) {
        long id = getId(orc.getPlacerOrderNumber());
        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(CustomerAccountArchetypes.INVOICE_ITEM, id);
            itemBean.setValue("sourceInvoiceItem", reference);
        } else {
            addNote(bean, "Unknown Placer Order Number: '" + orc.getPlacerOrderNumber().getEntityIdentifier() + "'");
        }
    }

    /**
     * Returns the patient associated with a PID segment.
     *
     * @param pid      the pid
     * @param bean     the act
     * @param itemBean the item
     * @return the corresponding patient, or {@code null} if none is found
     * @throws HL7Exception if the patient does not exist
     */
    private Party addPatient(PID pid, ActBean bean, ActBean itemBean) throws HL7Exception {
        Party patient = null;
        long id = getId(pid.getPatientID());
        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(PatientArchetypes.PATIENT, id);
            patient = getPatient(reference);
        }
        if (patient != null) {
            itemBean.addNodeParticipation("patient", patient);
        } else {
            XPN xpn = pid.getPatientName(0);
            String firstName = xpn.getGivenName().getValue();
            String lastName = xpn.getFamilyName().getSurname().getValue();
            String name;
            if (!StringUtils.isEmpty(lastName) && !StringUtils.isEmpty(firstName)) {
                name = firstName + " " + lastName;
            } else if (!StringUtils.isEmpty(lastName)) {
                name = lastName;
            } else {
                name = firstName;
            }
            addNote(bean, "Unknown patient, Id='" + pid.getPatientID().getIDNumber() + "', name='" + name + "'");
        }
        return patient;
    }

    private void addNote(ActBean bean, String value) {
        String notes = bean.getString("notes");
        if (!StringUtils.isEmpty(notes)) {
            notes += "\n" + value;
        } else {
            notes = value;
        }
        bean.setValue("notes", notes);
    }

    protected Party getPatient(IMObjectReference reference) {
        return (Party) service.get(reference);
    }

    private long getId(CX cx) {
        return getId(cx.getIDNumber().getValue());
    }

    private long getId(EI ei) {
        return getId(ei.getEntityIdentifier().getValue());
    }

    private long getId(CE ce) {
        return getId(ce.getIdentifier().getValue());
    }

    private long getId(String value) {
        long id = -1;
        if (!StringUtils.isEmpty(value)) {
            try {
                id = Long.valueOf(value);
            } catch (NumberFormatException ignore) {
                // do nothing
            }
        }
        return id;
    }

}
