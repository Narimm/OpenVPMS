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

package org.openvpms.report;

import org.apache.commons.jxpath.Functions;
import org.junit.Test;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * Tests the {@link IMObjectExpressionEvaluator} class.
 *
 * @author Tim Anderson
 */
public class IMObjectExpressionEvaluatorTestCase extends AbstractReportTest {

    /**
     * The JXPath extension functions.
     */
    @Autowired
    private Functions functions;

    /**
     * Tests the {@link IMObjectExpressionEvaluator#getValue(String)} method.
     */
    @Test
    public void testGetValue() {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        Date date = java.sql.Date.valueOf("2006-08-04");
        BigDecimal lowTotal = new BigDecimal("100");
        act.setValue("startTime", date);
        act.setValue("lowTotal", lowTotal);
        act.setParticipant("participation.customer", party);

        ExpressionEvaluator eval = new IMObjectExpressionEvaluator(act.getAct(), null, null, service, lookups,
                                                                   functions);
        assertEquals(date, eval.getValue("startTime"));
        assertEquals(lowTotal, eval.getValue("lowTotal"));
        assertEquals("J", eval.getValue("customer.entity.firstName"));
        assertEquals("Zoo", eval.getValue("customer.entity.lastName"));

        // test [] expressions
        assertEquals(new BigDecimal(2), eval.getValue("[1 + 1]"));

        String expression = "[party:getBillingAddress(openvpms:get(., 'customer.entity'))]";
        assertEquals("1234 Foo St\nMelbourne VIC 3001", eval.getValue(expression));

        // test invalid nodes
        assertEquals("No node named act found in archetype act.customerEstimation", eval.getValue("act.customer.foo"));
    }

    /**
     * Tests the {@link IMObjectExpressionEvaluator#getFormattedValue(String)}
     * method.
     */
    @Test
    public void testGetFormattedValue() {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        Date date = java.sql.Date.valueOf("2006-08-04");
        BigDecimal lowTotal = new BigDecimal("100");
        act.setValue("startTime", date);
        act.setValue("lowTotal", lowTotal);
        act.setParticipant("participation.customer", party);

        ExpressionEvaluator eval = new IMObjectExpressionEvaluator(act.getAct(), null, null, service, lookups,
                                                                   functions);
        String expectedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        assertEquals(expectedDate, eval.getFormattedValue("startTime"));
        assertEquals("$100.00", eval.getFormattedValue("lowTotal"));
        assertEquals("J", eval.getFormattedValue("customer.entity.firstName"));
        assertEquals("Zoo", eval.getFormattedValue("customer.entity.lastName"));

        // test [] expressions
        assertEquals("2.00", eval.getFormattedValue("[1 + 1]"));

        String expression = "[party:getBillingAddress(openvpms:get(., 'customer.entity'))]";
        assertEquals("1234 Foo St\nMelbourne VIC 3001", eval.getFormattedValue(expression));

        // test invalid nodes
        assertEquals("No node named act found in archetype act.customerEstimation", eval.getValue("act.customer.foo"));
    }

    /**
     * Verifies that fields are also declared as variables, to enable them to be used in jxpath functions.
     */
    @Test
    public void testFieldsDeclaredAsVariables() {
        Map<String, Object> fields = new HashMap<>();
        Party party = createCustomer();
        fields.put("OpenVPMS.customer", party);
        fields.put("OpenVPMS.supplier", null);

        ExpressionEvaluator eval = new IMObjectExpressionEvaluator(party, null, fields, getArchetypeService(),
                                                                   getLookupService(), functions);

        assertEquals(party, eval.getValue("OpenVPMS.customer"));
        assertEquals("Zoo,J", eval.getValue("OpenVPMS.customer.name"));

        assertEquals("No node named OpenVPMS found in archetype party.customerperson",
                     eval.getValue("OpenVPMS.patient"));

        // test variable evaluation
        assertEquals(party, eval.getValue("[$OpenVPMS.customer]"));
        assertEquals("Zoo,J", eval.getValue("[$OpenVPMS.customer.name]"));

        assertEquals("Expression Error", eval.getValue("[$OpenVPMS.patient]")); // undefined variable
        assertEquals("Expression Error", eval.getValue("[$OpenVPMS.supplier]")); // undefined variable

        // test conditional variable evaluation
        assertEquals("Zoo,J", eval.getValue("[expr:var('OpenVPMS.customer.name', 'No current customer')]"));
        assertEquals("No current patient", eval.getValue("[expr:var('OpenVPMS.patient', 'No current patient')]"));
        assertEquals("No current patient", eval.getValue("[expr:var('OpenVPMS.patient.name', 'No current patient')]"));
        assertEquals("No current supplier", eval.getValue("[expr:var('OpenVPMS.supplier.name', " +
                                                          "'No current supplier')]"));
    }

    /**
     * Verifies that parameters can be supplied and evaluated using
     * {@link IMObjectExpressionEvaluator#evaluate(String)}.
     */
    @Test
    public void testParameters() {
        Party party = createCustomer();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", 1);
        parameters.put("param2", "abc");
        ExpressionEvaluator eval = new IMObjectExpressionEvaluator(party, new Parameters(parameters), null,
                                                                   getArchetypeService(), getLookupService(),
                                                                   functions);
        assertEquals(1, eval.evaluate("$P.param1"));
        assertEquals("abc", eval.evaluate("$P.param2"));
    }

}
