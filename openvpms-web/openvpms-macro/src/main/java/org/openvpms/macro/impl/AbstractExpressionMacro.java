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

package org.openvpms.macro.impl;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.macro.MacroException;

/**
 * A {@code Macro} that evaluates an JXPath expression.
 *
 * @author Tim Anderson
 */
public abstract class AbstractExpressionMacro extends Macro {

    /**
     * The JXPath expression
     */
    private final String expression;


    /**
     * Constructs an {@link AbstractExpressionMacro}.
     *
     * @param lookup  the macro definition
     * @param service the archetype service
     * @throws MacroException if the expression is invalid
     */
    public AbstractExpressionMacro(Lookup lookup, IArchetypeService service) {
        this(new IMObjectBean(lookup, service));
    }

    /**
     * Constructs an {@link AbstractExpressionMacro}.
     *
     * @param bean the macro definition
     * @throws MacroException if the expression is invalid
     */
    protected AbstractExpressionMacro(IMObjectBean bean) {
        super((Lookup) bean.getObject());
        expression = bean.getString("expression");
        if (StringUtils.isEmpty(expression)) {
            throw new MacroException("Expression must be supplied for macro=" + getCode());
        }
    }

    /**
     * Returns the JXPath expression.
     *
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }
}
