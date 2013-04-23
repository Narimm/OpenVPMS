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
package org.openvpms.archetype.function.macro;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.archetype.util.IMObjectVariables;
import org.openvpms.archetype.util.MacroCache;
import org.openvpms.archetype.util.MacroEvaluator;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;


/**
 * JXPath extension functions that support macro evaluation.
 * E.g. given a macro <em>'displayName'</em> with expression <em>openvpms:get(., 'displayName')</em>,
 * this could be evaluated as:
 * <ul>
 * <li>macro:eval('displayName')
 * <li>macro:eval('displayName', .)
 * <li>macro:eval('displayName', someexpression)
 * <li>macro:eval('displayName', $somevariable)
 * </ul>
 *
 * @author Tim Anderson
 */
public class MacroFunctions {

    /**
     * The macro evaluator.
     */
    private final MacroEvaluator evaluator;

    /**
     * Constructs a {@code MacroFunctions}.
     *
     * @param cache         the macro cache
     * @param service       the archetype service
     * @param lookupService the lookup service
     */
    public MacroFunctions(MacroCache cache, IArchetypeService service, ILookupService lookupService) {
        this.evaluator = new MacroEvaluator(cache, new IMObjectVariables(service, lookupService));
    }

    /**
     * Evaluates a macro against the context object.
     * <p/>
     * This may be used in jxpath expressions as:
     * <pre>
     *   macro:eval(&lt;name&gt;)
     * </pre>
     * E.g:
     * <pre>
     *   macro:eval('displayName')
     * </pre>
     *
     * @param context the expression context
     * @param macro   the macro name
     * @return the result of the macro evaluation
     */
    public String eval(ExpressionContext context, String macro) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        return eval(macro, value);
    }

    /**
     * Evaluates a macro against the specified context object.
     * <p/>
     * This may be used in jxpath expressions as:
     * <pre>
     *   macro:eval(<name>, <context>)
     * </pre>
     * E.g:
     * <pre>
     *   macro:eval('displayName', .)
     *   macro:eval('displayName', $customer)
     * </pre>
     *
     * @param macro   the macro name
     * @param context the macro context
     * @return the result of the macro evaluation
     */
    public String eval(String macro, Object context) {
        return evaluator.evaluate(macro, context);
    }

}
