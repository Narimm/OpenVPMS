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

package org.openvpms.archetype.function.factory;

import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.patient.PatientAgeFormatter;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;

/**
 * Default implementation of {@link ArchetypeFunctionsFactory}.
 *
 * @author Tim Anderson
 */
public class DefaultArchetypeFunctionsFactory extends ArchetypeFunctionsFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The currencies.
     */
    private final Currencies currencies;

    /**
     * The patient age formatter.
     */
    private final PatientAgeFormatter formatter;

    /**
     * Constructs a {@link DefaultArchetypeFunctionsFactory}.
     *
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param currencies the currencies
     * @param formatter  the patient age formatter. May be {@code null}
     */
    public DefaultArchetypeFunctionsFactory(IArchetypeService service, ILookupService lookups, Currencies currencies,
                                            PatientAgeFormatter formatter) {
        this.service = service;
        this.lookups = lookups;
        this.currencies = currencies;
        this.formatter = formatter;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    @Override
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the lookup service.
     *
     * @return the lookup service
     */
    @Override
    protected ILookupService getLookupService() {
        return lookups;
    }

    /**
     * Returns the currencies.
     *
     * @return the currencies
     */
    @Override
    protected Currencies getCurrencies() {
        return currencies;
    }

    /**
     * Returns the patient age formatter.
     *
     * @return the patient age formatter. May be {@code null}
     */
    @Override
    protected PatientAgeFormatter getPatientAgeFormatter() {
        return formatter;
    }
}
