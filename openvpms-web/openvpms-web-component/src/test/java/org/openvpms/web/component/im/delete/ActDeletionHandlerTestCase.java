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

package org.openvpms.web.component.im.delete;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link ActDeletionHandler}.
 *
 * @author Tim Anderson
 */
public class ActDeletionHandlerTestCase extends AbstractAppTest {

    /**
     * Tests deletion.
     */
    @Test
    public void testDelete() {
        Party customer = TestHelper.createCustomer();
        Product product = TestHelper.createProduct();
        List<FinancialAct> charge1 = FinancialTestHelper.createChargesCounter(BigDecimal.TEN, customer, product,
                                                                              ActStatus.IN_PROGRESS);
        List<FinancialAct> charge2 = FinancialTestHelper.createChargesCounter(BigDecimal.TEN, customer, product,
                                                                              ActStatus.POSTED);
        save(charge1);
        save(charge2);

        // verify non-POSTED acts can be deleted.
        ActDeletionHandler<FinancialAct> handler1 = createDeletionHandler(charge1.get(0));
        assertTrue(handler1.canDelete());
        handler1.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(charge1.get(0)));
        assertNull(get(charge1.get(1)));

        // verify POSTED acts can't be deleted
        ActDeletionHandler<FinancialAct> handler2 = createDeletionHandler(charge2.get(0));
        assertFalse(handler2.canDelete());

        // verify delete throws IllegalStateException
        try {
            handler2.delete(new LocalContext(), new HelpContext("foo", null));
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // do nothing
        }

        // verify nothing was deleted
        assertNotNull(get(charge2.get(0)));
        assertNotNull(get(charge2.get(1)));
    }

    /**
     * Verify acts can't be deactivated.
     */
    @Test
    public void testDeactivate() {
        Party customer = TestHelper.createCustomer();
        Product product = TestHelper.createProduct();
        List<FinancialAct> charge1 = FinancialTestHelper.createChargesCounter(BigDecimal.TEN, customer, product,
                                                                              ActStatus.IN_PROGRESS);
        List<FinancialAct> charge2 = FinancialTestHelper.createChargesCounter(BigDecimal.TEN, customer, product,
                                                                              ActStatus.POSTED);
        save(charge1);
        save(charge2);

        // verify non-POSTED acts can't be deactivated.
        ActDeletionHandler<FinancialAct> handler1 = createDeletionHandler(charge1.get(0));
        assertFalse(handler1.canDeactivate());

        // verify deactivate throws IllegalStateException
        try {
            handler1.deactivate();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // do nothing
        }

        // verify POSTED acts can't be deactivated.
        ActDeletionHandler<FinancialAct> handler2 = createDeletionHandler(charge2.get(0));
        assertFalse(handler2.canDeactivate());

        // verify deactivate throws IllegalStateException
        try {
            handler2.deactivate();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // do nothing
        }
    }


    /**
     * Verifies that the {@link IMObjectDeletionHandlerFactory} returns {@link ActDeletionHandler} for acts.
     */
    @Test
    public void testFactory() {
        IMObjectDeletionHandlerFactory factory = new IMObjectDeletionHandlerFactory(getArchetypeService());
        factory.setApplicationContext(applicationContext);

        Party patient = TestHelper.createPatient();
        Act act = PatientTestHelper.createNote(new Date(), patient);
        DocumentAct documentAct = PatientTestHelper.createDocumentForm(patient);

        List<FinancialAct> charge = FinancialTestHelper.createChargesCounter(
                BigDecimal.TEN, TestHelper.createCustomer(), TestHelper.createProduct(), ActStatus.IN_PROGRESS);
        save(charge);

        assertTrue(factory.create(act) instanceof ActDeletionHandler);
        assertTrue(factory.create(documentAct) instanceof ActDeletionHandler);
        assertTrue(factory.create(charge.get(0)) instanceof ActDeletionHandler);
    }

    /**
     * Creates a new deletion handler for an entity.
     *
     * @param act the entity
     * @return a new deletion handler
     */
    protected <T extends Act> ActDeletionHandler<T> createDeletionHandler(T act) {
        IMObjectEditorFactory factory = applicationContext.getBean(IMObjectEditorFactory.class);
        return new ActDeletionHandler<>(act, factory, ServiceHelper.getTransactionManager(),
                                        ServiceHelper.getArchetypeService());
    }

}
