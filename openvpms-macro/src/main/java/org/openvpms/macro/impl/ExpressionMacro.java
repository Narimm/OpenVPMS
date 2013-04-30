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

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * A {@code Macro} that is evaluated from a JXPath expression.
 *
 * @author Tim Anderson
 */
class ExpressionMacro extends AbstractExpressionMacro {

    /**
     * Constructs an {@link ExpressionMacro}.
     *
     * @param lookup  the expression macro lookup
     * @param service the archetype service
     */
    public ExpressionMacro(Lookup lookup, IArchetypeService service) {
        super(lookup, service);
    }

}
