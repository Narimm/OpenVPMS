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

package org.openvpms.archetype.rules.finance.deposit;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author tony
 */
public class DepositQuery {

    /**
     * The bank deposit act.
     */
    public static final String BANK_DEPOSIT = "bankDeposit";

    /**
     * The till balance act.
     */
    public static final String TILL_BALANCE = "tillBalance";

    /**
     * The till balance act item.
     */
    public static final String ACT = "act";

    /**
     * The act item.
     */
    public static final String ACT_ITEM = "item";

    /**
     * The act amount.
     */
    public static final String AMOUNT = "amount";

    /**
     * The till balance.
     */
    private final FinancialAct bankDeposit;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The amount calculator.
     */
    private final ActCalculator calculator;


    /**
     * Constructs a new <code>DepositQuery</code>.
     */
    public DepositQuery(FinancialAct bankDeposit) {
        this(bankDeposit, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <code>DepositQuery</code>.
     *
     * @param service the archetype service
     */
    public DepositQuery(FinancialAct bankDeposit,
                        IArchetypeService service) {
        this.bankDeposit = bankDeposit;
        this.service = service;
        calculator = new ActCalculator(service);
    }

    /**
     * Executes the query.
     *
     * @return the query results.
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<ObjectSet> query() {
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        ActBean bean = new ActBean(bankDeposit, service); //The bankDeposit Act
        for (Act tillBalance : bean.getActs()) { // Deposit Till Balances loop
            ActBean tillBalanceBean = new ActBean(tillBalance);
            for (Act tillBalanceItem : tillBalanceBean.getActs()) { // Till Balance Item loop
                ActBean tillBalanceItemBean = new ActBean(tillBalanceItem);
                if (tillBalanceItemBean.isA("act.tillBalanceAdjustment")) {
                    ObjectSet set = new ObjectSet();
                    set.set(BANK_DEPOSIT, bankDeposit);
                    set.set(ACT, tillBalanceItem);
                    Act item;
                    if (tillBalanceItemBean.getBoolean("credit")) {
                        item = (FinancialAct) service.create(
                                "act.customerAccountPaymentCash");
                    } else {
                        item = (FinancialAct) service.create(
                                "act.customerAccountRefundCash");
                    }
                    ActBean itemBean = new ActBean(item);
                    BigDecimal actAmount = getAmount(tillBalanceItem);

                    itemBean.setValue("amount", actAmount);
                    item.setDescription(tillBalanceItem.getDescription());
                    set.set(ACT_ITEM, item);
                    set.set(AMOUNT, actAmount.negate());
                    result.add(set);
                } else {
                    for (Act item : tillBalanceItemBean.getNodeActs("items")) { //Till Balance Item Items loop
                        // Skip any discounts
                        String shortName = item.getArchetypeId().getShortName();
                        if (shortName.endsWith("Discount")) {
                            continue;
                        }
                        ObjectSet set = new ObjectSet();
                        set.set(BANK_DEPOSIT, bankDeposit);
                        set.set(ACT, tillBalanceItem);
                        set.set(ACT_ITEM, item);
                        set.set(AMOUNT, getAmount(item).negate());
                        result.add(set);

                    }
                }
            }
        }
        // sort on item payment type
        Collections.sort(result, new Comparator<ObjectSet>() {
            public int compare(ObjectSet o1, ObjectSet o2) {
                Act a1 = (Act) o1.get(ACT_ITEM);
                Act a2 = (Act) o2.get(ACT_ITEM);
                ActBean ab1 = new ActBean(a1);
                ActBean ab2 = new ActBean(a2);
                return getPaymentTypeNumber(
                        ab1.getObject().getArchetypeId().getShortName()) -
                       getPaymentTypeNumber(
                               ab2.getObject().getArchetypeId().getShortName());
            }
        });
        return new Page<ObjectSet>(result, 0, result.size(), result.size());
    }

    /**
     * Returns an amount, taking into account any credit node.
     *
     * @param act the act
     * @return the amount corresponding to <code>node</code>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private BigDecimal getAmount(Act act) {
        return calculator.getAmount(act, "amount");
    }

    /**
     * Returns a number based on the payment type extracted from the shortname
     *
     * @param shortName the archetype shortname
     * @return a number corresponding to the payment type
     */

    private Integer getPaymentTypeNumber(String shortName) {
        if (shortName.endsWith("Cheque")) {
            return 0;
        } else if (shortName.endsWith("Credit")) {
            return 1;
        } else if (shortName.endsWith("EFT")) {
            return 2;
        } else if (shortName.endsWith("Cash")) {
            return 3;
        } else {
            return 4;
        }
    }

}
