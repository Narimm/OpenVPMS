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
package org.openvpms.esci.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.oasis.ubl.InvoiceType;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.esci.adapter.map.invoice.AbstractInvoiceTest;
import org.openvpms.esci.adapter.service.InvoiceServiceAdapter;
import org.openvpms.esci.exception.ESCIException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Tests the {@link org.openvpms.esci.adapter.service.InvoiceServiceAdapter} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceServiceAdapterTestCase extends AbstractInvoiceTest {

    /**
     * Verifies that an {@link ESCIException} is raised if there is no ESCI user registered.
     */
    @Test
    public void testInvoiceNoESCIUser() {
        InvoiceType invoice = createInvoice();
        InvoiceServiceAdapter service = createInvoiceServiceAdapter();
        try {
            service.submitInvoice(invoice);
            fail("Expected submitInvoice() to fail");
        } catch (ESCIException exception) {
            assertEquals("ESCIA-0200: No ESCI user", exception.getMessage());
        }

        User user = TestHelper.createUser(); // not an ESCI user
        Authentication token = new TestingAuthenticationToken(user.getName(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(token);

        try {
            service.submitInvoice(invoice);
            fail("Expected submitInvoice() to fail");
        } catch (ESCIException exception) {
            assertEquals("ESCIA-0200: No ESCI user", exception.getMessage());
        }

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

        try {
            service.submitInvoice(invoice);
            fail("Expected submitInvoice() to fail");
        } catch (ESCIException exception) {
            assertEquals("ESCIA-0700: Failed to submit Invoice: Foo", exception.getMessage());
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
