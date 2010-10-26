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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.service;

import static org.junit.Assert.*;
import org.junit.Test;
import org.oasis.ubl.InvoiceType;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.FutureValue;
import org.openvpms.esci.adapter.map.invoice.AbstractInvoiceTest;
import org.openvpms.esci.exception.ESCIException;


/**
 * Tests the {@link org.openvpms.esci.adapter.service.InvoiceServiceAdapter} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceServiceAdapterTestCase extends AbstractInvoiceTest {

    /**
     * Tests submitting an invoice.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSubmitInvoice() throws Exception {
        // create an author for invoices, and to receive system messages
        User author = initDefaultAuthor();

        // set up the ESCI user
        User user = createESCIUser(getSupplier());
        initSecurityContext(user);

        InvoiceServiceAdapter service = createInvoiceServiceAdapter();

        // set up the invoice listener
        final FutureValue<FinancialAct> future = new FutureValue<FinancialAct>();
        SystemMessageInvoiceListener listener = new SystemMessageInvoiceListener() {
            public void receivedInvoice(FinancialAct delivery) {
                super.receivedInvoice(delivery);
                future.set(delivery);
            }
        };
        listener.setBeanFactory(new IMObjectBeanFactory(getArchetypeService()));
        service.setInvoiceListener(listener);

        // submit an invoice
        InvoiceType invoice = createInvoice();
        service.submitInvoice(invoice);

        // verify the delivery was created
        FinancialAct delivery = future.get(1000);
        assertNotNull(delivery);

        // verify a system message was sent to the author
        checkSystemMessage(author, delivery, SystemMessageReason.ORDER_INVOICED);
    }

    /**
     * Verifies that an {@link ESCIException} is raised if there is no ESCI user registered.
     */
    @Test
    public void testInvoiceNoESCIUser() {
        InvoiceType invoice = createInvoice();
        InvoiceServiceAdapter service = createInvoiceServiceAdapter();
        String expected = "ESCIA-0200: No ESCI user";
        checkSubmitException(invoice, service, expected);

        User user = TestHelper.createUser(); // not an ESCI user
        initSecurityContext(user);

        checkSubmitException(invoice, service, expected);
    }

    /**
     * Verifies that an {@link ESCIException} is raised if {@link InvoiceServiceAdapter#submitInvoice} encounters
     * an unexpected exception.
     */
    @Test
    public void testFailedToSubmitInvoice() {
        InvoiceType invoice = createInvoice();
        InvoiceServiceAdapter service = new InvoiceServiceAdapter() {
            @Override
            protected User getUser() {
                throw new NullPointerException("Foo");
            }
        };
        service.setArchetypeService(getArchetypeService());
        service.setInvoiceMapper(createMapper());
        service.setUserRules(new UserRules());

        checkSubmitException(invoice, service, "ESCIA-0700: Failed to submit Invoice: Foo");
    }

    /**
     * Verifies that {@link InvoiceServiceAdapter#submitInvoice} fails with the expected message.
     *
     * @param invoice  the invoice
     * @param service  the service
     * @param expected the expected message
     */
    private void checkSubmitException(InvoiceType invoice, InvoiceServiceAdapter service, String expected) {
        try {
            service.submitInvoice(invoice);
            fail("Expected submitInvoice() to fail");
        } catch (ESCIException exception) {
            assertEquals(expected, exception.getMessage());
        }
    }

    /**
     * Creates a new invoice service adapter
     *
     * @return a new invoice service adapter
     */
    private InvoiceServiceAdapter createInvoiceServiceAdapter() {
        InvoiceServiceAdapter service = new InvoiceServiceAdapter();
        service.setArchetypeService(getArchetypeService());
        service.setInvoiceMapper(createMapper());
        service.setUserRules(new UserRules());
        return service;
    }

}
