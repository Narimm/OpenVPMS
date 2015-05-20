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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.PredicateUtils;
import org.openvpms.archetype.test.TestHelper;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Base class for {@link RepeatExpression} test cases.
 *
 * @author Tim Anderson
 */
public class AbstractRepeatExpressionTest {

    /**
     * Verifies that the {@link RepeatExpression#getRepeatAfter(Date, Predicate)} returns the expected value.
     *
     * @param startTime  the start time
     * @param expression the expression
     * @param expected   the expected date/time, as a string
     * @return the next date
     */
    protected Date checkNext(Date startTime, RepeatExpression expression, String expected) {
        return checkNext(startTime, expression, TestHelper.getDatetime(expected));
    }

    /**
     * Verifies that the {@link RepeatExpression#getRepeatAfter(Date, Predicate)} returns the expected value.
     *
     * @param startTime  the start time
     * @param expression the expression
     * @param expected   the expected value
     * @return the next date
     */
    protected Date checkNext(Date startTime, RepeatExpression expression, Date expected) {
        Date next = expression.getRepeatAfter(startTime, PredicateUtils.<Date>truePredicate());
        assertEquals(expected, next);
        return next;
    }

    /**
     * Verifies an expression can be parsed.
     *
     * @param expression the expression
     * @return the parsed expression
     */
    protected CronRepeatExpression parse(String expression) {
        CronRepeatExpression result = CronRepeatExpression.parse(expression);
        assertNotNull(expression);
        return result;
    }
}
