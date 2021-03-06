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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.macro.impl;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.macro.MacroException;
import org.openvpms.report.ReportFactory;


/**
 * A factory for {@link Macro}s and {@link MacroRunner}s.
 *
 * @author Tim Anderson
 */
class MacroFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The report factory.
     */
    private final ReportFactory factory;

    /**
     * Constructs a {@link MacroFactory}.
     *
     * @param service the archetype service
     * @param factory the report factory
     */
    public MacroFactory(IArchetypeService service, ReportFactory factory) {
        this.service = service;
        this.factory = factory;
    }

    /**
     * Creates a macro from a macro lookup.
     *
     * @param lookup the lookup
     * @return a new macro
     * @throws IllegalArgumentException if the lookup is not supported
     * @throws MacroException           if the macro definition is invalid
     */
    public Macro create(Lookup lookup) {
        if (TypeHelper.isA(lookup, MacroArchetypes.EXPRESSION_MACRO)) {
            return new ExpressionMacro(lookup, service);
        } else if (TypeHelper.isA(lookup, MacroArchetypes.REPORT_MACRO)) {
            return new ReportMacro(lookup, service);
        }
        throw new IllegalArgumentException("Unsupported lookup type: " + lookup.getArchetype());
    }

    /**
     * Creates a macro runner to run a macro.
     *
     * @param macro   the macro to run
     * @param context the macro context
     * @return a new macro runner
     * @throws IllegalArgumentException if the macro is not supported
     */
    public MacroRunner create(Macro macro, MacroContext context) {
        if (macro instanceof ExpressionMacro) {
            return new ExpressionMacroRunner(context);
        } else if (macro instanceof ReportMacro) {
            return new ReportMacroRunner(context, factory);
        }
        throw new IllegalArgumentException("Unsupported macro type: " + macro.getClass().getName());
    }
}
