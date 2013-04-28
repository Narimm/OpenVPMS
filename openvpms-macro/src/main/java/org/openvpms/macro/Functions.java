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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.macro;

import org.apache.commons.jxpath.ExpressionContext;

/**
 * JXPath extension functions to evaluate macros.
 *
 * @author Tim Anderson
 */
public interface Functions {

    /**
     * Evaluates a macro against the context object.
     * <p/>
     * This may be used in jxpath expressions as:
     * <pre>
     *   macro:eval(&lt;name&gt;)
     * </pre>
     * E.g:
     * <pre>
     *   macro:eval('@customerName')
     * </pre>
     *
     * @param context the expression context
     * @param macro   the macro name
     * @return the result of the macro evaluation
     */
    String eval(ExpressionContext context, String macro);

    /**
     * Evaluates a macro against the specified context object.
     * <p/>
     * This may be used in jxpath expressions as:
     * <pre>
     *   macro:eval(<name>, <context>)
     * </pre>
     * E.g:
     * <pre>
     *   macro:eval('@name', .)
     *   macro:eval('@id', $customer)
     * </pre>
     *
     * @param macro   the macro name
     * @param context the macro context
     * @return the result of the macro evaluation
     */
    String eval(String macro, Object context);

}
