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
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
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
        String[] shortNames = {"act.customerAccountCharges*",
                               "act.customerAccountPayment"};
        return getAccountBalance(customer.getObjectReference(), "customer",
                                 "participation.customer", shortNames);
    }

    /**
     * Returns an account balance for a supplier.
     *
     * @param supplier the supplier
     * @return the account balance for <code>supplier</code>
     */
    public static BigDecimal getSupplierAccountBalance(Party supplier) {
        String[] shortNames = {"act.supplierAccountCharges*",
                               "act.supplierAccountPayment"};
        return getAccountBalance(supplier.getObjectReference(), "supplier",
                                 "participation.supplier", shortNames);
    }

    /**
     * Returns an account balance for any entity.
     *
     * @param entity        the entity
     * @param participant   the participant node name
     * @param participation the participation short name
     * @param shortNames    the act short names
     */
    public static BigDecimal getAccountBalance(IMObjectReference entity,
                                               String participant,
                                               String participation,
                                               String[] shortNames) {
        String[] statuses = {"Posted"};
        BaseArchetypeConstraint archetypes = new ArchetypeShortNameConstraint(
                shortNames, true, true);
        ActResultSet set = new ActResultSet(entity, participant,  participation,
                                            archetypes, null, null, statuses,
                                            50, null);
        BigDecimal balance = BigDecimal.ZERO;
        while (set.hasNext()) {
            IPage<Act> acts = set.next();
            balance = sum(balance, acts.getRows(), "amount");
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
            BigDecimal amount = getAmount(act, node, service);
            result = result.add(amount);
        }
        return result;
    }

    /**
     * Returns an amount, taking into account any credit node.
     *
     * @param act  the act
     * @param node the amount node
     * @return the amount corresponding to <code>node</code>
     */
    public static BigDecimal getAmount(Act act, String node) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return getAmount(act, node, service);
    }

    /**
     * Returns an amount, taking into account any credit node.
     *
     * @param act     the act
     * @param node    the amount node
     * @param service the archetype service
     * @return the amount corresponding to <code>node</code>
     */
    private static BigDecimal getAmount(Act act, String node,
                                        IArchetypeService service) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(act, service);
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal value = IMObjectHelper.getNumber(act, archetype, node);
        if (value != null) {
            Boolean credit = (Boolean) IMObjectHelper.getValue(
                    act, archetype, "credit");
            if (credit == null) {
                credit = Boolean.FALSE;
            }
            if (Boolean.TRUE.equals(credit)) {
                result = result.subtract(value);
            } else {
                result = result.add(value);
            }
        }

        return result;
    }
}
