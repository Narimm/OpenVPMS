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

package org.openvpms.web.component.function;

import org.apache.commons.jxpath.FunctionLibrary;
import org.openvpms.archetype.function.factory.ArchetypeFunctionsFactory;
import org.openvpms.archetype.function.factory.DefaultArchetypeFunctionsFactory;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.patient.PatientAgeFormatter;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.FunctionsFactory;
import org.openvpms.macro.Macros;
import org.openvpms.macro.impl.MacroFunctions;

/**
 * Default {@link FunctionsFactory} for the web-app. In addition to the functions specified in
 * {@link ArchetypeFunctionsFactory}, this registers:
 * <ul>
 * <li><em>macro</em> - {@link MacroFunctions}</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class DefaultFunctionsFactory extends DefaultArchetypeFunctionsFactory {

    /**
     * The macros.
     */
    private Macros macros;

    /**
     * Constructs an {@link DefaultFunctionsFactory}.
     *
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param currencies hte currencies
     * @param formatter  the patient age formatter. May be {@code null}
     */
    public DefaultFunctionsFactory(IArchetypeService service, ILookupService lookups, Currencies currencies,
                                   PatientAgeFormatter formatter) {
        super(service, lookups, currencies, formatter);
    }

    /**
     * Registers the macros.
     *
     * @param macros the macros
     */
    public void setMacros(Macros macros) {
        this.macros = macros;
    }

    /**
     * Creates a new {@code FunctionLibrary} containing functions that use the specified {@link IArchetypeService}.
     *
     * @param service the archetype service
     * @return the functions
     */
    @Override
    public FunctionLibrary create(IArchetypeService service) {
        FunctionLibrary library = super.create(service);
        library.addFunctions(create("macro", new MacroFunctions(macros)));
        return library;
    }
}
