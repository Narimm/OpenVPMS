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

package org.openvpms.web.component.im.edit.act;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Act helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActHelper {

    /**
     * Returns an account balance for a customer.
     *
     * @param customer the customer
     * @return the account balance for <code>customer</code>
     */
    public static BigDecimal getCustomerAccountBalance(Party customer) {
        String[] statuses = {"Posted"};
        String[] shortNames = {"act.customerAccountCharges*",
                               "act.customerAccountPayment",
                               "act.customerAccountRefund"};
        BaseArchetypeConstraint archetypes = new ArchetypeShortNameConstraint(
                shortNames, true, true);
        ActResultSet set = new ActResultSet(customer.getObjectReference(),
                                            archetypes, null, null, statuses,
                                            50, null);
        BigDecimal balance = BigDecimal.ZERO;
        while (set.hasNext()) {
            IPage<Act> acts = set.next();
            balance = ActHelper.sum(balance, acts.getRows(), "amount");
        }
        return balance;
    }

    /**
     * Returns an account balance for a supplier.
     *
     * @param customer the supplier
     * @return the account balance for <code>supplier</code>
     */
    public static BigDecimal getSupplierAccountBalance(Party supplier) {
        String[] statuses = {"Posted"};
        String[] shortNames = {"act.supplierAccountCharges*",
                               "act.supplierAccountPayment",
                               "act.supplierAccountRefund"};
        BaseArchetypeConstraint archetypes = new ArchetypeShortNameConstraint(
                shortNames, true, true);
        ActResultSet set = new ActResultSet(supplier.getObjectReference(),
                                            archetypes, null, null, statuses,
                                            50, null);
        BigDecimal balance = BigDecimal.ZERO;
        while (set.hasNext()) {
            IPage<Act> acts = set.next();
            balance = ActHelper.sum(balance, acts.getRows(), "amount");
        }
        return balance;
    }

    /**
     * Suma a node in a list of act items.
     *
     * @param act  the parent act
     * @param node the node to sum
     * @return the summed total
     */
    public static BigDecimal sum(Act act, String node) {
        List<Act> acts = new ArrayList<Act>();
        for (ActRelationship relationship : act.getSourceActRelationships()) {
            Act item = (Act) IMObjectHelper.getObject(relationship.getTarget());
            acts.add(item);
        }
        return sum(acts, node);
    }

    /**
     * Sums a node in a list of acts.
     *
     * @param acts the acts
     * @param node the node to sum
     * @return the summed total
     */
    public static BigDecimal sum(Collection<Act> acts, String node) {
        return sum(BigDecimal.ZERO, acts, node);
    }

    /**
     * Sums a node in a list of acts.
     *
     * @param initial the initial value
     * @param acts    the acts
     * @param node    the node to sum
     * @return the summed total
     */
    public static BigDecimal sum(BigDecimal initial, Collection<Act> acts,
                                 String node) {
        IArchetypeService service = ServiceHelper.getArchetypeService();

        BigDecimal result = initial;
        for (Act act : acts) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(act, service);
            NodeDescriptor decscriptor = archetype.getNodeDescriptor(node);
            if (decscriptor != null) {
                NodeDescriptor creditDesc
                        = archetype.getNodeDescriptor("credit");
                Boolean credit = Boolean.FALSE;
                if (creditDesc != null) {
                    credit = (Boolean) creditDesc.getValue(act);
                }
                Number number = (Number) decscriptor.getValue(act);
                if (number != null) {
                    BigDecimal value;
                    if (number instanceof BigDecimal) {
                        value = (BigDecimal) number;
                    } else if (number instanceof Double
                               || number instanceof Float) {
                        value = new BigDecimal(number.doubleValue());
                    } else {
                        value = new BigDecimal(number.longValue());
                    }
                    if (Boolean.TRUE.equals(credit)) {
                        result = result.subtract(value);
                    } else {
                        result = result.add(value);
                    }
                }
            }
        }
        return result;
    }
}
