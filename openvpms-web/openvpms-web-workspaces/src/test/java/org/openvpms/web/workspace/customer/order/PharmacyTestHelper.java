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

package org.openvpms.web.workspace.customer.order;

import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Pharmacy test helper methods.
 *
 * @author Tim Anderson
 */
public class PharmacyTestHelper {

    /**
     * Creates a pharmacy order.
     *
     * @param customer    the customer. May be {@code null}
     * @param patient     the patient. May be {@code null}
     * @param product     the product. May be {@code null}
     * @param quantity    the order quantity
     * @param invoiceItem the related invoice item. May be {@code null}
     * @return a new order
     */
    public static FinancialAct createOrder(Party customer, Party patient, Product product, BigDecimal quantity,
                                           FinancialAct invoiceItem) {
        return createOrder(new Date(), customer, patient, product, quantity, invoiceItem);
    }

    /**
     * Creates a pharmacy order.
     *
     * @param startTime   the start time
     * @param customer    the customer. May be {@code null}
     * @param patient     the patient. May be {@code null}
     * @param product     the product. May be {@code null}
     * @param quantity    the order quantity
     * @param invoiceItem the related invoice item. May be {@code null}
     * @return a new order
     */
    public static FinancialAct createOrder(Date startTime, Party customer, Party patient, Product product,
                                           BigDecimal quantity, FinancialAct invoiceItem) {
        return createOrderReturn(true, startTime, customer, patient, product, quantity, invoiceItem);
    }

    /**
     * Creates a pharmacy return.
     *
     * @param customer    the customer. May be {@code null}
     * @param patient     the patient. May be {@code null}
     * @param product     the product. May be {@code null}
     * @param quantity    the order quantity
     * @param invoiceItem the related invoice item. May be {@code null}
     * @return a new return
     */
    public static FinancialAct createReturn(Party customer, Party patient, Product product, BigDecimal quantity,
                                            FinancialAct invoiceItem) {
        return createReturn(new Date(), customer, patient, product, quantity, invoiceItem);
    }

    /**
     * Creates a pharmacy return.
     *
     * @param startTime   the start time
     * @param customer    the customer. May be {@code null}
     * @param patient     the patient. May be {@code null}
     * @param product     the product. May be {@code null}
     * @param quantity    the order quantity
     * @param invoiceItem the related invoice item. May be {@code null}
     * @return a new return
     */
    public static FinancialAct createReturn(Date startTime, Party customer, Party patient, Product product,
                                            BigDecimal quantity, FinancialAct invoiceItem) {
        return createOrderReturn(false, startTime, customer, patient, product, quantity, invoiceItem);
    }

    /**
     * Creates a pharmacy order/return.
     *
     * @param isOrder     if {@code true}, create an order, else create a return
     * @param startTime   the start time
     * @param customer    the customer. May be {@code null}
     * @param patient     the patient. May be {@code null}
     * @param product     the product. May be {@code null}
     * @param quantity    the order quantity
     * @param invoiceItem the related invoice item. May be {@code null}      @return a new order
     */
    private static FinancialAct createOrderReturn(boolean isOrder, Date startTime, Party customer, Party patient,
                                                  Product product, BigDecimal quantity, FinancialAct invoiceItem) {
        FinancialAct act = (FinancialAct) TestHelper.create(isOrder ? OrderArchetypes.PHARMACY_ORDER
                                                                    : OrderArchetypes.PHARMACY_RETURN);
        FinancialAct item = (FinancialAct) TestHelper.create(isOrder ? OrderArchetypes.PHARMACY_ORDER_ITEM
                                                                     : OrderArchetypes.PHARMACY_RETURN_ITEM);
        act.setActivityStartTime(startTime);
        item.setActivityStartTime(startTime);
        ActBean bean = new ActBean(act);
        if (customer != null) {
            bean.addNodeParticipation("customer", customer);
        }
        bean.addNodeRelationship("items", item);

        ActBean itemBean = new ActBean(item);
        if (patient != null) {
            itemBean.addNodeParticipation("patient", patient);
        }
        if (product != null) {
            itemBean.addNodeParticipation("product", product);
        }
        if (invoiceItem != null) {
            itemBean.setValue("sourceInvoiceItem", invoiceItem.getObjectReference());
        }
        item.setQuantity(quantity);
        TestHelper.save(act, item);
        return act;
    }

}
