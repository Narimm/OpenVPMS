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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Tests the {@link IMObjectExpressionEvaluator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectExpressionEvaluatorTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link IMObjectExpressionEvaluator#getValue(String)} method.
     */
    @Test
    public void testGetValue() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        Date date = java.sql.Date.valueOf("2006-08-04");
        BigDecimal lowTotal = new BigDecimal("100");
        act.setValue("startTime", date);
        act.setValue("lowTotal", lowTotal);
        act.setParticipant("participation.customer", party);

        ExpressionEvaluator eval
                = new IMObjectExpressionEvaluator(act.getAct(), service);
        assertEquals(date, eval.getValue("startTime"));
        assertEquals(lowTotal, eval.getValue("lowTotal"));
        assertEquals("J", eval.getValue("customer.entity.firstName"));
        assertEquals("Zoo", eval.getValue("customer.entity.lastName"));

        // test [] expressions
        assertEquals(new BigDecimal(2), eval.getValue("[1 + 1]"));

        String expression = "[party:getBillingAddress("
                            + "openvpms:get(., 'customer.entity'))]";
        assertEquals("1234 Foo St\nMelbourne VIC 3001",
                     eval.getValue(expression));

        // test invalid nodes
        assertEquals("Invalid property name: act.customer.foo",
                     eval.getValue("act.customer.foo"));
    }

    /**
     * Tests the {@link IMObjectExpressionEvaluator#getFormattedValue(String)}
     * method.
     */
    @Test
    public void testGetFormattedValue() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        Date date = java.sql.Date.valueOf("2006-08-04");
        BigDecimal lowTotal = new BigDecimal("100");
        act.setValue("startTime", date);
        act.setValue("lowTotal", lowTotal);
        act.setParticipant("participation.customer", party);

        ExpressionEvaluator eval
                = new IMObjectExpressionEvaluator(act.getAct(), service);
        assertEquals("4/08/2006",
                     eval.getFormattedValue("startTime")); // todo localise
        assertEquals("$100.00", eval.getFormattedValue("lowTotal"));
        assertEquals("J", eval.getFormattedValue("customer.entity.firstName"));
        assertEquals("Zoo", eval.getFormattedValue("customer.entity.lastName"));

        // test [] expressions
        assertEquals("2.00", eval.getFormattedValue("[1 + 1]"));

        String expression = "[party:getBillingAddress("
                            + "openvpms:get(., 'customer.entity'))]";
        assertEquals("1234 Foo St\nMelbourne VIC 3001",
                     eval.getFormattedValue(expression));

        // test invalid nodes
        assertEquals("Invalid property name: act.customer.foo",
                     eval.getValue("act.customer.foo"));
    }

}
