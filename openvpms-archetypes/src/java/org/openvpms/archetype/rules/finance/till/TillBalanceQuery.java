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

package org.openvpms.archetype.rules.finance.till;

import org.apache.commons.collections.comparators.NullComparator;
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
import java.util.Date;
import java.util.List;


/**
 * Queries an <em>act.tillBalance</em>, flattening out the act heirarchy
 * for reporting purposes.
 * Each object set in the returned query contains:
 * <ul>
 * <li><em>tillBalance</em> - the till balance act</li>
 * <li><em>act</em> - an item of <em>tillBalance</em></li>
 * <li><em>item</em> - an item of <em>act</em>. Not present if <em>act</em>
 * is an <em>act.tillBalanceAdjustment</em></li>
 * <li><em>amount</em> - the <em>act</em> or <em>item</em> amount, taking
 * into account any credit flag.</li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TillBalanceQuery {

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
    private final FinancialAct tillBalance;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The amount calculator.
     */
    private final ActCalculator calculator;


    /**
     * Constructs a new <code>TillBalanceQuery</code>.
     */
    public TillBalanceQuery(FinancialAct tillBalance) {
        this(tillBalance, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <code>TillBalanceQuery</code>.
     *
     * @param service the archetype service
     */
    public TillBalanceQuery(FinancialAct tillBalance,
                            IArchetypeService service) {
        this.tillBalance = tillBalance;
        this.service = service;
        calculator = new ActCalculator(service);
    }

    /**
     * Executes the query.
     * Returns an empty page if any of the schedule, from or to dates are null.
     *
     * @return the query results.
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<ObjectSet> query() {
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        ActBean bean = new ActBean(tillBalance, service);
        for (Act tillBalanceItem : bean.getActs()) {
            ActBean tillBalanceItemBean = new ActBean(tillBalanceItem, service);
            if (tillBalanceItemBean.isA("act.tillBalanceAdjustment")) {
                ObjectSet set = new ObjectSet();
                set.set(TILL_BALANCE, tillBalance);
                set.set(ACT, tillBalanceItem);
                set.set(ACT_ITEM, null);
                set.set(AMOUNT, getAmount(tillBalanceItem).negate());
                result.add(set);
            } else {
                for (Act item : tillBalanceItemBean.getActs()) {
                    ActBean itemBean = new ActBean(item);
                    if (!itemBean.isA("act.customerAccountPayment")) {
                        ObjectSet set = new ObjectSet();
                        set.set(TILL_BALANCE, tillBalance);
                        set.set(ACT, tillBalanceItem);
                        set.set(ACT_ITEM, item);
                        set.set(AMOUNT, getAmount(item).negate());
                        result.add(set);
                    }
                }
            }
        }

        // sort on act start time
        Collections.sort(result, new Comparator<ObjectSet>() {
            public int compare(ObjectSet o1, ObjectSet o2) {
                Act a1 = (Act) o1.get(ACT);
                Act a2 = (Act) o2.get(ACT);
                Comparator<Date> compator = new NullComparator();
                return compator.compare(a1.getActivityStartTime(),
                                        a2.getActivityStartTime());
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

}
