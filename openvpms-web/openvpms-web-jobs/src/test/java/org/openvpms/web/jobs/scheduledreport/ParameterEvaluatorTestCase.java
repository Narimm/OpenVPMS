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

package org.openvpms.web.jobs.scheduledreport;

import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.report.ParameterType;
import org.openvpms.web.workspace.admin.job.scheduledreport.ExpressionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ParameterEvaluator} class.
 *
 * @author Tim Anderson
 */
public class ParameterEvaluatorTestCase extends ArchetypeServiceTest {

    @Test
    public void testEvaluateStaticParameters() {
        ParameterEvaluator evaluator = new ParameterEvaluator();

        IMObjectBean config = createConfig();
        Set<ParameterType> types = new HashSet<>();
        setParameter(config, 0, Integer.class, 45, types);
        setParameter(config, 1, BigDecimal.class, BigDecimal.TEN, types);
        setParameter(config, 2, Date.class, DateRules.getToday(), types);
        setParameter(config, 3, String.class, "foo", types);

        Map<String, Object> parameters = evaluator.evaluate(config, types);
        assertEquals(4, parameters.size());

        assertEquals(45, parameters.get("value0"));
        checkEquals(BigDecimal.TEN, (BigDecimal) parameters.get("value1"));
        assertEquals(DateRules.getToday(), parameters.get("value2"));
        assertEquals("foo", parameters.get("value3"));
    }

    /**
     * Tests evaluation of date parameters.
     */
    @Test
    public void testEvaluateDates() {
        // configurations are limited to 14 parameters, so need 2
        IMObjectBean bean1 = createConfig();
        IMObjectBean bean2 = createConfig();

        // use a fixed date for evaluated dates, for testing purposes
        Date today = TestHelper.getDate("2018-07-30");

        ParameterEvaluator evaluator = new ParameterEvaluator() {
            @Override
            protected LocalDate today() {
                return DateRules.toLocalDate(today);
            }
        };
        Date date = TestHelper.getDate("2018-06-06");

        int index1 = 0;
        int index2 = 0;
        Set<ParameterType> types1 = new HashSet<>();
        Set<ParameterType> types2 = new HashSet<>();
        setDate(bean1, index1++, ExpressionType.VALUE, date, types1);
        setDate(bean1, index1++, ExpressionType.NOW, null, types1);
        setDate(bean1, index1++, ExpressionType.TODAY, null, types1);
        setDate(bean1, index1++, ExpressionType.YESTERDAY, null, types1);
        setDate(bean1, index1++, ExpressionType.TOMORROW, null, types1);
        setDate(bean1, index1++, ExpressionType.START_OF_MONTH, null, types1);
        setDate(bean1, index1++, ExpressionType.END_OF_MONTH, null, types1);
        setDate(bean1, index1++, ExpressionType.START_OF_LAST_MONTH, null, types1);
        setDate(bean1, index1++, ExpressionType.END_OF_LAST_MONTH, null, types1);
        setDate(bean1, index1++, ExpressionType.START_OF_NEXT_MONTH, null, types1);
        setDate(bean1, index1++, ExpressionType.END_OF_NEXT_MONTH, null, types1);
        setDate(bean1, index1++, ExpressionType.START_OF_YEAR, null, types1);
        setDate(bean1, index1, ExpressionType.END_OF_YEAR, null, types1);

        setDate(bean2, index2++, ExpressionType.START_OF_LAST_YEAR, null, types2);
        setDate(bean2, index2++, ExpressionType.END_OF_LAST_YEAR, null, types2);
        setDate(bean2, index2++, ExpressionType.START_OF_NEXT_YEAR, null, types2);
        setDate(bean2, index2++, ExpressionType.END_OF_NEXT_YEAR, null, types2);
        setDate(bean2, index2++, ExpressionType.JUNE_30, null, types2);
        setDate(bean2, index2++, ExpressionType.LAST_JUNE_30, null, types2);
        setDate(bean2, index2++, ExpressionType.NEXT_JUNE_30, null, types2);
        setDate(bean2, index2++, ExpressionType.JULY_1, null, types2);
        setDate(bean2, index2++, ExpressionType.LAST_JULY_1, null, types2);
        setDate(bean2, index2, ExpressionType.NEXT_JULY_1, null, types2);

        Date now1 = new Date();
        Map<String, Object> parameters1 = evaluator.evaluate(bean1, types1);
        Date now2 = new Date();
        assertEquals(13, parameters1.size());

        Map<String, Object> parameters2 = evaluator.evaluate(bean2, types2);
        assertEquals(10, parameters2.size());

        // VALUE
        assertEquals(date, parameters1.get("date0"));

        // NOW
        Date evaluatedNow = (Date) parameters1.get("date1");
        assertNotNull(evaluatedNow);
        assertTrue(now1.compareTo(evaluatedNow) <= 0);
        assertTrue(now2.compareTo(evaluatedNow) >= 0);

        // TODAY
        assertEquals(today, parameters1.get("date2"));

        // YESTERDAY
        assertEquals(TestHelper.getDate("2018-07-29"), parameters1.get("date3"));

        // TOMORROW
        assertEquals(TestHelper.getDate("2018-07-31"), parameters1.get("date4"));

        // START_OF_MONTH
        assertEquals(TestHelper.getDate("2018-07-01"), parameters1.get("date5"));

        // END_OF_MONTH
        assertEquals(TestHelper.getDate("2018-07-31"), parameters1.get("date6"));

        // START_OF_LAST_MONTH
        assertEquals(TestHelper.getDate("2018-06-01"), parameters1.get("date7"));

        // END_OF_LAST_MONTH
        assertEquals(TestHelper.getDate("2018-06-30"), parameters1.get("date8"));

        // START_OF_NEXT_MONTH
        assertEquals(TestHelper.getDate("2018-08-01"), parameters1.get("date9"));

        // END_OF_NEXT_MONTH
        assertEquals(TestHelper.getDate("2018-08-31"), parameters1.get("date10"));

        // START_OF_YEAR
        assertEquals(TestHelper.getDate("2018-01-01"), parameters1.get("date11"));

        // END_OF_YEAR
        assertEquals(TestHelper.getDate("2018-12-31"), parameters1.get("date12"));

        // START_OF_LAST_YEAR
        assertEquals(TestHelper.getDate("2017-01-01"), parameters2.get("date0"));

        // END_OF_LAST_YEAR
        assertEquals(TestHelper.getDate("2017-12-31"), parameters2.get("date1"));

        // START_OF_NEXT_YEAR
        assertEquals(TestHelper.getDate("2019-01-01"), parameters2.get("date2"));

        // END_OF_NEXT_YEAR
        assertEquals(TestHelper.getDate("2019-12-31"), parameters2.get("date3"));

        // JUNE_30
        assertEquals(TestHelper.getDate("2018-06-30"), parameters2.get("date4"));

        // LAST_JUNE_30
        assertEquals(TestHelper.getDate("2017-06-30"), parameters2.get("date5"));

        // NEXT_JUNE_30
        assertEquals(TestHelper.getDate("2019-06-30"), parameters2.get("date6"));

        // JULY_1
        assertEquals(TestHelper.getDate("2018-07-01"), parameters2.get("date7"));

        // LAST_JULY_1
        assertEquals(TestHelper.getDate("2017-07-01"), parameters2.get("date8"));

        // NEXT_JULY_1
        assertEquals(TestHelper.getDate("2019-07-01"), parameters2.get("date9"));
    }

    /**
     * Creates a new job configuration.
     *
     * @return a new job configuration
     */
    private IMObjectBean createConfig() {
        IMObject config = create("entity.jobScheduledReport");
        return getBean(config);
    }

    /**
     * Helper to set a date parameter.
     *
     * @param config         the job configuration
     * @param index          the parameter index
     * @param expressionType the expression type
     * @param value          the date value. May be {@code null}
     * @param types          the parameter types to add to
     */
    private void setDate(IMObjectBean config, int index, ExpressionType expressionType, Date value,
                         Set<ParameterType> types) {
        String name = "date" + index;
        setParameter(config, name, index, Date.class, expressionType, value, types);
    }

    /**
     * Helper to set a fixed parameter.
     *
     * @param config the job configuration
     * @param index  the parameter index
     * @param type   the parameter type
     * @param value  the parameter value. May be {@code null}
     * @param types  the parameter types to add to
     */
    private void setParameter(IMObjectBean config, int index, Class type, Object value, Set<ParameterType> types) {
        String name = "value" + index;
        setParameter(config, name, index, type, null, value, types);

    }

    /**
     * Helper to set a fixed parameter.
     *
     * @param config the job configuration
     * @param name   the parameter name
     * @param index  the parameter index
     * @param type   the parameter type
     * @param value  the parameter value. May be {@code null}
     * @param types  the parameter types to add to
     */
    private void setParameter(IMObjectBean config, String name, int index, Class type, ExpressionType expressionType,
                              Object value, Set<ParameterType> types) {
        config.setValue("paramName" + index, name);
        config.setValue("paramType" + index, type.getName());
        config.setValue("paramExprType" + index, expressionType);
        config.setValue("paramValue" + index, value);
        types.add(new ParameterType(name, type, null));
    }

}
