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
import ca.uhn.hl7v2.model.v25.datatype.XCN;
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
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
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
     * The user rules.
     */
    private final UserRules userRules;

    /**
     * Constructs a {@link RDSProcessor}.
     *
     * @param service the archetype service
     * @param rules   the patient rules
     */
    public RDSProcessor(IArchetypeService service, PatientRules rules, UserRules userRules) {
        this.service = service;
        this.rules = rules;
        this.userRules = userRules;
    }

    /**
     * Processes a dispense message.
     *
     * @param message the message
     * @return the customer order
     * @throws HL7Exception for any HL7 error
     */
    public List<Act> process(RDS_O13 message) throws HL7Exception {
        List<Act> result = new ArrayList<Act>();
        if (message.getORDERReps() < 1) {
            throw new HL7Exception("RDS O13 message contains no order group");
        }
        PID pid = message.getPATIENT().getPID();
        Act order = (Act) service.create("act.customerOrderPharmacy");
        ActBean bean = new ActBean(order, service);
        Party patient = getPatient(pid, bean);
        if (patient != null) {
            addCustomer(bean, patient);
        }
        result.add(order);
        boolean match = true;
        for (int i = 0; i < message.getORDERReps(); ++i) {
            RDS_O13_ORDER group = message.getORDER(i);
            match &= addItem(group, patient, bean, result);
        }
        if (match) {
            order.setStatus("INVOICED");
        }
        return result;
    }

    /**
     * Adds the customer to the order, determined by the current patient-owner relationship.
     *
     * @param bean    the order bean
     * @param patient the patient
     */
    private void addCustomer(ActBean bean, Party patient) {
        Party customer = rules.getOwner(patient);
        if (customer != null) {
            bean.setNodeParticipant("customer", customer);
        }
    }

    /**
     * Adds an order item.
     *
     * @param group   the order group
     * @param patient the patient. May be {@code null}
     * @param bean    the order bean
     * @param acts    the order acts
     * @return {@code true} if the item was invoiced
     */
    private boolean addItem(RDS_O13_ORDER group, Party patient, ActBean bean, List<Act> acts) {
        boolean match = false;
        Act item = (Act) service.create("act.customerOrderItemPharmacy");
        ActBean itemBean = new ActBean(item, service);
        if (patient != null) {
            itemBean.addNodeParticipation("patient", patient);
        }
        String fillerOrderNumber = group.getORC().getFillerOrderNumber().getEntityIdentifier().getValue();
        if (fillerOrderNumber != null) {
            itemBean.setValue("reference", fillerOrderNumber);
        }
        FinancialAct invoiceItem = addInvoiceItem(group.getORC(), bean, itemBean);
        IMObjectReference invoicedProduct = (invoiceItem != null) ? getInvoicedProduct(invoiceItem) : null;
        addClinician(group, bean, itemBean, invoiceItem);
        IMObjectReference dispensedProduct = addProduct(group, bean, itemBean);
        BigDecimal quantity = getQuantity(group);
        itemBean.setValue("quantity", quantity);

        if (invoiceItem != null && dispensedProduct != null && invoicedProduct != null) {
            BigDecimal invoicedQty = invoiceItem.getQuantity();
            if (invoicedQty != null && invoicedQty.compareTo(quantity) == 0
                && dispensedProduct.equals(invoicedProduct)) {
                match = true;
            }
        }
        bean.addNodeRelationship("items", item);
        acts.add(item);
        return match;
    }

    /**
     * Returns the reference to the product that was invoiced.
     *
     * @param invoiceItem the invoice item
     * @return the corresponding product. May be {@code null}
     */
    private IMObjectReference getInvoicedProduct(FinancialAct invoiceItem) {
        ActBean bean = new ActBean(invoiceItem, service);
        return bean.getNodeParticipantRef("product");
    }

    /**
     * Returns the dispensed quantity.
     *
     * @param group the order group
     * @return the dispensed quantity
     */
    private BigDecimal getQuantity(RDS_O13_ORDER group) {
        String quantity = group.getRXD().getActualDispenseAmount().getValue();
        return StringUtils.isEmpty(quantity) ? BigDecimal.ZERO : new BigDecimal(quantity);
    }

    /**
     * Adds the product to the item.
     *
     * @param group    the order group
     * @param bean     the order bean
     * @param itemBean the order item bean
     * @return the product reference, or {@code null} if none is found
     */
    private IMObjectReference addProduct(RDS_O13_ORDER group, ActBean bean, ActBean itemBean) {
        RXD rxd = group.getRXD();
        CE code = rxd.getDispenseGiveCode();
        long id = getId(code);
        IMObjectReference result = null;
        if (id != -1) {
            ArchetypeQuery query = new ArchetypeQuery("product.*");
            query.getArchetypeConstraint().setAlias("p");
            query.add(new ObjectRefSelectConstraint("p"));
            query.add(Constraints.eq("id", id));
            ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
            if (iterator.hasNext()) {
                ObjectSet set = iterator.next();
                result = set.getReference("p.reference");
            }
        }
        if (result != null) {
            itemBean.addNodeParticipation("product", result);
        } else {
            addNote(bean, "Unknown Dispense Give Code, id='" + code.getIdentifier().getValue()
                          + "', name='" + code.getText().getValue() + "'");
        }
        return result;
    }

    /**
     * Populates the clinician from an order group.
     * <p/>
     * If the RXD Dispensing Provider field includes a clinician, this will be used to populate the order item.
     *
     * @param group       the order group
     * @param bean        the order bean
     * @param itemBean    the order item bean
     * @param invoiceItem the original invoice item. May be {@code null}
     */
    private void addClinician(RDS_O13_ORDER group, ActBean bean, ActBean itemBean, FinancialAct invoiceItem) {
        XCN dispensingProvider = group.getRXD().getDispensingProvider(0);
        IMObjectReference clinician = null;
        long id = getId(dispensingProvider.getIDNumber().getValue());
        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(UserArchetypes.USER, id);
            User user = (User) service.get(reference);
            if (user != null && userRules.isClinician(user)) {
                clinician = user.getObjectReference();
            }
        } else if (invoiceItem != null) {
            ActBean invoiceBean = new ActBean(invoiceItem);
            // propagate the clinician from original invoice item
            clinician = invoiceBean.getNodeParticipantRef("clinician");
            if (clinician == null) {
                // else get it from the parent
                clinician = bean.getNodeParticipantRef("clinician");
            }
        }
        if (clinician != null) {
            itemBean.addNodeParticipation("clinician", clinician);
            if (bean.getNodeParticipantRef("clinician") == null) {
                bean.addNodeParticipation("clinician", clinician);
            }
        }
    }

    /**
     * Adds a reference to the original invoice item, if any and determine the status.
     *
     * @param orc      the order segment
     * @param bean     the act
     * @param itemBean the item
     */
    private FinancialAct addInvoiceItem(ORC orc, ActBean bean, ActBean itemBean) {
        FinancialAct result = null;
        long id = getId(orc.getPlacerOrderNumber());

        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(CustomerAccountArchetypes.INVOICE_ITEM, id);
            result = (FinancialAct) service.get(reference);
            itemBean.setValue("sourceInvoiceItem", reference);
        } else {
            addNote(bean, "Unknown Placer Order Number: '" + orc.getPlacerOrderNumber().getEntityIdentifier() + "'");
        }
        return result;
    }

    /**
     * Returns the patient associated with a PID segment.
     *
     * @param pid  the pid
     * @param bean the order act
     * @return the corresponding patient, or {@code null} if none is found
     * @throws HL7Exception if the patient does not exist
     */
    private Party getPatient(PID pid, ActBean bean) throws HL7Exception {
        Party patient = null;
        long id = getId(pid.getPatientID());
        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(PatientArchetypes.PATIENT, id);
            patient = getPatient(reference);
        }
        if (patient == null) {
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

    /**
     * Adds a note to the order.
     *
     * @param bean  the order bean
     * @param value the note to add
     */
    private void addNote(ActBean bean, String value) {
        String notes = bean.getString("notes");
        if (!StringUtils.isEmpty(notes)) {
            notes += "\n" + value;
        } else {
            notes = value;
        }
        bean.setValue("notes", notes);
    }

    /**
     * Returns a patient given its reference.
     *
     * @param reference the patient reference
     * @return the corresponding patient or {@code null} if one is found
     */
    protected Party getPatient(IMObjectReference reference) {
        return (Party) service.get(reference);
    }

    /**
     * Helper to parse an id from an extended composite id.
     *
     * @param value the value
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    private long getId(CX value) {
        return getId(value.getIDNumber().getValue());
    }

    /**
     * Helper to parse an id from an entity identifier.
     *
     * @param value the value
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    private long getId(EI value) {
        return getId(value.getEntityIdentifier().getValue());
    }

    /**
     * Helper to parse an id from a coded element.
     *
     * @param value the value
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    private long getId(CE value) {
        return getId(value.getIdentifier().getValue());
    }

    /**
     * Helper to parse an id from a string.
     *
     * @param value the value to parse
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
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
