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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Random;


/**
 * Financial test helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FinancialTestHelper extends TestHelper {

    /**
     * Helper to create a POSTED <em>act.customerAccountChargesInvoice</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param patient  the patient
     * @param product  the product
     * @return a new act
     */
    public static FinancialAct createChargesInvoice(Money amount,
                                                    Party customer,
                                                    Party patient,
                                                    Product product) {
        return createCharges("act.customerAccountChargesInvoice",
                             "act.customerAccountInvoiceItem",
                             "actRelationship.customerAccountInvoiceItem",
                             amount, customer, patient, product);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountChargesCounter</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param product  the product
     * @return a new act
     */
    public static FinancialAct createChargesCounter(Money amount,
                                                    Party customer,
                                                    Product product) {
        return createCharges("act.customerAccountChargesCounter",
                             "act.customerAccountCounterItem",
                             "actRelationship.customerAccountCounterItem",
                             amount, customer, null, product);
    }

    /**
     * Helper to create an <em>act.customerAccountChargesCredit</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param patient  the patient. May be <tt>null</tt>
     * @param product  the product
     * @return a new act
     */
    public static FinancialAct createChargesCredit(Money amount, Party customer,
                                                   Party patient,
                                                   Product product) {
        return createCharges("act.customerAccountChargesCredit",
                             "act.customerAccountCreditItem",
                             "actRelationship.customerAccountCreditItem",
                             amount, customer, patient, product);
    }

    /**
     * Helper to create and save a new <em>lookup.customerAccountType</em>
     * classification.
     *
     * @param paymentTerms     the payment terms
     * @param paymentUom       the payment units
     * @param accountFeeAmount the account fee
     * @return a new classification
     */
    public static Lookup createAccountType(int paymentTerms,
                                           DateUnits paymentUom,
                                           BigDecimal accountFeeAmount) {
        Lookup lookup = (Lookup) create("lookup.customerAccountType");
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("code", "XCUSTOMER_ACCOUNT_TYPE"
                + Math.abs(new Random().nextInt()));
        bean.setValue("paymentTerms", paymentTerms);
        bean.setValue("paymentUom", paymentUom.toString());
        bean.setValue("accountFeeAmount", accountFeeAmount);
        save(lookup);
        return lookup;
    }

    /**
     * Helper to create a charges act.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @param customer  the customer
     * @param patient   the patient. May be <tt>null</tt>
     * @param product   the product. May be <tt>null</tt>
     * @return a new act
     */
    private static FinancialAct createCharges(String shortName,
                                              String itemShortName,
                                              String relationshipShortName,
                                              Money amount, Party customer,
                                              Party patient, Product product) {
        FinancialAct act = createAct(shortName, amount, customer);
        ActBean bean = new ActBean(act);
        FinancialAct item = (FinancialAct) create(itemShortName);
        item.setTotal(amount);
        ActBean itemBean = new ActBean(item);
        if (patient != null) {
            itemBean.addParticipation("participation.patient", patient);
        }
        if (product != null) {
            itemBean.addParticipation("participation.product", product);
        }
        itemBean.save();
        bean.addRelationship(relationshipShortName, item);
        return act;
    }

    /**
     * Helper to create a new act, setting the total, adding a customer
     * participation and setting the status to 'POSTED'.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @param customer  the customer
     * @return a new act
     */
    private static FinancialAct createAct(String shortName, Money amount,
                                          Party customer) {
        FinancialAct act = (FinancialAct) create(shortName);
        act.setStatus(FinancialActStatus.POSTED);
        act.setTotal(amount);
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", customer);
        return act;
    }
}
