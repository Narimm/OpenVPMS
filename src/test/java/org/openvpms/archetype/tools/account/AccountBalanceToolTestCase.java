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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.tools.account;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.AbstractCustomerAccountTest;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;


/**
 * Tests the {@link AccountBalanceTool} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration(locations = "/application-context.xml", inheritLocations = false)
public class AccountBalanceToolTestCase extends AbstractCustomerAccountTest {

    /**
     * The account balance tool.
     */
    private AccountBalanceTool tool;

    /**
     * The account rules.
     */
    private CustomerAccountRules rules;


    /**
     * Tests generation given a customer id.
     */
    @Test
    public void testGenerateForCustomerId() {
        Party customer = getCustomer();
        long id = customer.getId();
        Money amount = new Money(100);
        FinancialAct debit = createInitialBalance(amount);
        save(debit);

        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));
        assertFalse(tool.check(id));

        tool.generate(id);
        assertEquals(amount, rules.getBalance(customer));
        FinancialAct credit = createBadDebt(amount);
        save(credit);
        tool.generate(id);
        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));
        assertTrue(tool.check(id));
    }

    /**
     * Tests generation given a customer name.
     */
    @Test
    public void testGenerateForCustomerName() {
        Party customer = getCustomer();
        String name = customer.getName();
        Money amount = new Money(100);
        FinancialAct debit = createInitialBalance(amount);
        save(debit);

        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));
        assertFalse(tool.check(name));

        tool.generate(name);
        assertEquals(amount, rules.getBalance(customer));
        FinancialAct credit = createBadDebt(amount);
        save(credit);
        tool.generate(name);
        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));
        assertTrue(tool.check(name));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void onSetUp() {
        rules = new CustomerAccountRules();
        tool = new AccountBalanceTool(getArchetypeService());
    }

}
