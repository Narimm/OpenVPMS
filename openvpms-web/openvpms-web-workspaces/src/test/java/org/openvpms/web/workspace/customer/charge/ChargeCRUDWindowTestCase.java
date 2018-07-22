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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ChargeCRUDWindow}.
 *
 * @author Tim Anderson
 */
public class ChargeCRUDWindowTestCase extends AbstractAppTest {

    /**
     * The context.
     */
    private Context context;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        context = new LocalContext();
        context.setPractice(TestHelper.getPractice());
        context.setLocation(TestHelper.createLocation());
    }

    /**
     * Verifies that if an invoice has been finalised by another user, it cannot be edited.
     */
    @Test
    public void testEditPostedObject() {
        ArrayList<String> errors = new ArrayList<>();
        initErrorHandler(errors);

        // create an invoice
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(BigDecimal.TEN, customer, patient,
                                                                           product, ActStatus.IN_PROGRESS);
        save(acts);
        Archetypes<FinancialAct> archetypes = Archetypes.create("act.customerAccountCharges*", FinancialAct.class);

        ChargeCRUDWindow window = new ChargeCRUDWindow(archetypes, context, new HelpContext("goo", null));
        FinancialAct invoice = acts.get(0);
        window.setObject(invoice);
        assertTrue(window.canEdit());

        // simulate finalising the invoice by another user
        FinancialAct copy = get(invoice);
        copy.setStatus(ActStatus.POSTED);
        save(copy);

        // verify it can't be edited
        assertTrue(window.canEdit());  // works on the cached version
        window.edit();

        assertEquals(1, errors.size());
        assertEquals("Can't edit Invoice", errors.get(0));
    }

    /**
     * Verifies that if an invoice has been deleted by another user, it cannot be edited.
     */
    @Test
    public void testEditDeletedObject() {
        ArrayList<String> errors = new ArrayList<>();
        initErrorHandler(errors);

        // create an invoice
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(BigDecimal.TEN, customer, patient,
                                                                           product, ActStatus.IN_PROGRESS);
        save(acts);
        Archetypes<FinancialAct> archetypes = Archetypes.create("act.customerAccountCharges*", FinancialAct.class);

        ChargeCRUDWindow window = new ChargeCRUDWindow(archetypes, context, new HelpContext("goo", null));
        FinancialAct invoice = acts.get(0);
        window.setObject(invoice);
        assertTrue(window.canEdit());

        // simulate deleting the invoice by another user
        remove(invoice);

        // verify it can't be edited
        assertTrue(window.canEdit());  // works on the cached version
        window.edit();

        assertEquals(1, errors.size());
        assertEquals("Invoice no longer exists", errors.get(0));
    }

}
