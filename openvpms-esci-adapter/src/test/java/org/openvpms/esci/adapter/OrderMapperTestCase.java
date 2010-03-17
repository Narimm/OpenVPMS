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

import org.junit.Before;
import org.junit.Test;
import org.oasis.ubl.OrderType;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.supplier.AbstractSupplierTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.ubl.io.UBLDocumentContext;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;


/**
 * Tests the {@link OrderMapper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderMapperTestCase extends AbstractSupplierTest {

    @Test
    public void testMapActToOrder() throws Exception {
        BigDecimal quantity = new BigDecimal(5);
        BigDecimal unitPrice = BigDecimal.ONE;

        // create an order with a single item, and post it
        FinancialAct orderItem = createOrderItem(quantity, 1, unitPrice);
        Act act = createOrder(orderItem);
        act.setStatus(ActStatus.POSTED);
        save(act);

        OrderMapper mapper = new OrderMapper();
        OrderType order = mapper.map(act);
        UBLDocumentContext context = new UBLDocumentContext();
        context.createWriter().write(order, new ByteArrayOutputStream());
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Party practice = TestHelper.getPractice();
        PartyRules rules = new PartyRules();
        Contact contact = rules.getContact(practice, ContactArchetypes.LOCATION, "BILLING");
        if (contact == null) {
            contact = (Contact) create(ContactArchetypes.LOCATION);
            practice.addContact(contact);
        } else {
            practice.removeContact(contact);
        }
        contact = TestHelper.createLocationContact("Seaspray Rd", "CAPE_WOOLAMAI", "VIC", "3925");
        practice.addContact(contact);
        save(practice);
    }
}
