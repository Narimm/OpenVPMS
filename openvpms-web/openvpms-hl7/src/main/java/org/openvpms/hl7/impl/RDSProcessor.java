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

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.group.RDS_O13_ORDER;
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.RXD;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.order.CustomerOrder;
import org.openvpms.archetype.rules.finance.order.CustomerPharmacyOrder;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.math.BigDecimal;
import java.util.List;

import static org.openvpms.archetype.rules.finance.order.CustomerOrder.addNote;

/**
 * Processes RDS messages.
 * <p/>
 * This generates <em>act.customerPharmacyOrder</em> messages for each RDS message received.
 * <p/>
 * Note that an RDS message may relate to a specific <em>act.customerAccountInvoiceItem</em>, and could be
 * be used to update its receivedQuantity and cancelledQuantity nodes. This is not done as it would lead to
 * version conflicts if users are editing invoices when the message is received.
 *
 * @author Tim Anderson
 */
public class RDSProcessor extends OrderMessageProcessor {

    /**
     * Constructs a {@link RDSProcessor}.
     *
     * @param service   the archetype service
     * @param rules     the patient rules
     * @param userRules the user rules
     */
    public RDSProcessor(IArchetypeService service, PatientRules rules, UserRules userRules) {
        super(service, rules, userRules);
    }

    /**
     * Processes a dispense message.
     *
     * @param message  the message
     * @param location the practice location reference
     * @return the customer order and/or return
     * @throws HL7Exception for any HL7 error
     */
    public List<Act> process(RDS_O13 message, IMObjectReference location) throws HL7Exception {
        if (message.getORDERReps() < 1) {
            throw new HL7Exception("RDS O13 message contains no order group");
        }
        PID pid = message.getPATIENT().getPID();
        CustomerOrder state = createState(pid, location);
        for (int i = 0; i < message.getORDERReps(); ++i) {
            RDS_O13_ORDER group = message.getORDER(i);
            addItem(group, state);
        }
        return state.getActs();
    }

    /**
     * Creates state for an order.
     *
     * @param patient  the patient. May be {@code null}
     * @param customer the customer. May be {@code null}
     * @param note     the note. May be {@code null}
     * @param location the practice location. May be {@code null}
     * @param service  the archetype service
     * @return a new {@link CustomerOrder}
     */
    @Override
    protected CustomerPharmacyOrder createState(Party patient, Party customer, String note, IMObjectReference location,
                                IArchetypeService service) {
        return new CustomerPharmacyOrder(patient, customer, note, location, service);
    }

    /**
     * Adds an order item.
     *
     * @param group the order group
     * @param state the state
     */
    private void addItem(RDS_O13_ORDER group, CustomerOrder state) {
        BigDecimal quantity = getQuantity(group);
        ActBean bean;
        ActBean itemBean;
        if (quantity.signum() >= 0) {
            bean = state.getOrder();
            itemBean = state.createOrderItem();
        } else {
            bean = state.getReturn();
            itemBean = state.createReturnItem();
            quantity = quantity.abs();
        }

        String fillerOrderNumber = group.getORC().getFillerOrderNumber().getEntityIdentifier().getValue();
        if (fillerOrderNumber != null) {
            itemBean.setValue("reference", fillerOrderNumber);
        }
        FinancialAct invoiceItem = addInvoiceItem(group.getORC(), bean, itemBean, state);
        addClinician(group, bean, itemBean, invoiceItem);
        Product product = addProduct(group, bean, itemBean);
        if (product != null) {
            checkSellingUnits(group, bean, product);
        }
        itemBean.setValue("quantity", quantity);
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
     * @return the product or {@code null} if none is found
     */
    private Product addProduct(RDS_O13_ORDER group, ActBean bean, ActBean itemBean) {
        RXD rxd = group.getRXD();
        CE code = rxd.getDispenseGiveCode();
        long id = HL7MessageHelper.getId(code);
        Product result = null;
        if (id != -1) {
            ArchetypeQuery query = new ArchetypeQuery("product.*");
            query.getArchetypeConstraint().setAlias("p");
            query.add(Constraints.eq("id", id));
            IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<>(getService(), query);
            result = (iterator.hasNext()) ? iterator.next() : null;
        }
        if (result != null) {
            itemBean.addNodeParticipation("product", result);
        } else {
            addNote(bean, "Unknown Dispense Give Code, Id='" + code.getIdentifier().getValue()
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
    private void addClinician(RDS_O13_ORDER group, ActBean bean, ActBean itemBean, Act invoiceItem) {
        XCN dispensingProvider = group.getRXD().getDispensingProvider(0);
        IMObjectReference clinician = null;
        long id = HL7MessageHelper.getId(dispensingProvider.getIDNumber().getValue());
        if (id != -1) {
            User user = getClinician(id);
            if (user != null) {
                clinician = user.getObjectReference();
            }
        } else if (invoiceItem != null) {
            ActBean invoiceBean = new ActBean(invoiceItem, getService());
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
     * Adds a reference to the original invoice item, if any.
     *
     * @param orc      the order segment
     * @param bean     the act
     * @param itemBean the item
     * @param state    the state
     */
    private FinancialAct addInvoiceItem(ORC orc, ActBean bean, ActBean itemBean, CustomerOrder state) {
        FinancialAct invoiceItem = (FinancialAct) getOrder(CustomerAccountArchetypes.INVOICE_ITEM, orc, bean, state);
        if (invoiceItem != null) {
            itemBean.setValue("sourceInvoiceItem", invoiceItem.getObjectReference());
        }
        return invoiceItem;
    }

    /**
     * Adds a note if the dispense units are set, but don't match the product selling units.
     *
     * @param group   the order group
     * @param bean    the order bean
     * @param product the product
     */
    private void checkSellingUnits(RDS_O13_ORDER group, ActBean bean, Product product) {
        RXD rxd = group.getRXD();
        CE dispenseUnits = rxd.getActualDispenseUnits();
        String units = dispenseUnits.getIdentifier().getValue();
        IMObjectBean productBean = new IMObjectBean(product, getService());
        String sellingUnits = productBean.getString("sellingUnits");
        if (!StringUtils.isEmpty(units) && !StringUtils.isEmpty(sellingUnits) && !units.equals(sellingUnits)) {
            String name = dispenseUnits.getText().getValue();
            if (name == null) {
                name = "";
            }
            addNote(bean, "Dispense Units (Id='" + units + "', name='" + name + "')"
                          + " do not match selling units (" + sellingUnits + ")");
        }
    }

}
