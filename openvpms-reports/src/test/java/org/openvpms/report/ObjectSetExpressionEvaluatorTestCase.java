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
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ObjectSet;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Tests the {@link ObjectSetExpressionEvaluator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ObjectSetExpressionEvaluatorTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link ObjectSetExpressionEvaluator#getValue(String)} method.
     */
    @Test
    public void testGetValue() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ObjectSet set = new ObjectSet();
        set.set("int", 10);
        set.set("string", "astring");
        Date date = new Date();
        set.set("date", date);
        Party customer = createCustomer("Foo", "Bar");
        set.set("act.customer", customer);
        set.set("anull", null);

        ObjectSetExpressionEvaluator eval
                = new ObjectSetExpressionEvaluator(set, service);
        assertEquals(10, eval.getValue("int"));
        assertEquals("astring", eval.getValue("string"));
        assertEquals(date, eval.getValue("date"));
        assertEquals(customer, eval.getValue("act.customer"));
        assertEquals("Foo", eval.getValue("act.customer.firstName"));
        assertEquals("Bar", eval.getValue("act.customer.lastName"));
        assertNull(eval.getValue("anull"));

        // test invalid nodes
        assertEquals("Invalid object/node name: foo", eval.getValue("foo"));
        assertEquals("Invalid property name: foo",
                     eval.getValue("act.customer.foo"));

        // test expressions
        assertEquals(new BigDecimal(2), eval.getValue("[1 + 1]"));
    }

    /**
     * Tests the {@link ObjectSetExpressionEvaluator#getFormattedValue(String)}
     * method.
     */
    @Test
    public void testGetFormattedValue() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ObjectSet set = new ObjectSet();
        set.set("int", 10);
        set.set("string", "astring");
        set.set("money", new Money(100));
        Date date = java.sql.Date.valueOf("2007-01-11");
        set.set("date", date);
        IMObject customer = createCustomer("Foo", "Bar");
        set.set("act.customer", customer);

        ObjectSetExpressionEvaluator eval
                = new ObjectSetExpressionEvaluator(set, service);
        assertEquals("10", eval.getFormattedValue("int"));
        assertEquals("astring", eval.getFormattedValue("string"));
        assertEquals("$100.00",
                     eval.getFormattedValue("money"));  // todo localise
        assertEquals("11/01/2007",
                     eval.getFormattedValue("date"));
        assertEquals(customer.getName(),
                     eval.getFormattedValue("act.customer"));
        assertEquals("Foo", eval.getFormattedValue("act.customer.firstName"));
        assertEquals("Bar", eval.getFormattedValue("act.customer.lastName"));

        // test invalid nodes
        assertEquals("Invalid object/node name: foo",
                     eval.getFormattedValue("foo"));
        assertEquals("Invalid property name: foo",
                     eval.getFormattedValue("act.customer.foo"));


        // test expressions
        assertEquals("2.00", eval.getFormattedValue("[1 + 1]"));
    }

}
