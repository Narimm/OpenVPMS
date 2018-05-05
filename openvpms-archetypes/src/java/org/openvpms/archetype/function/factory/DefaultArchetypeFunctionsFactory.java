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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.factory;

import org.openvpms.archetype.rules.contact.AddressFormatter;
import org.openvpms.archetype.rules.contact.BasicAddressFormatter;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.patient.PatientAgeFormatter;
import org.openvpms.archetype.rules.practice.PracticeService;
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
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The currencies.
     */
    private final Currencies currencies;

    /**
     * The address formatter.
     */
    private final AddressFormatter addressFormatter;

    /**
     * The patient age formatter.
     */
    private final PatientAgeFormatter ageFormatter;

    /**
     * Constructs a {@link DefaultArchetypeFunctionsFactory}.
     *
     * @param service         the archetype service
     * @param lookups         the lookup service
     * @param practiceService the practice service
     * @param currencies      the currencies
     * @param ageFormatter    the patient age formatter. May be {@code null}
     */
    public DefaultArchetypeFunctionsFactory(IArchetypeService service, ILookupService lookups,
                                            PracticeService practiceService, Currencies currencies,
                                            PatientAgeFormatter ageFormatter) {
        this(service, lookups, practiceService, currencies, new BasicAddressFormatter(service, lookups), ageFormatter);
    }

    /**
     * Constructs a {@link DefaultArchetypeFunctionsFactory}.
     *
     * @param service          the archetype service
     * @param lookups          the lookup service
     * @param practiceService  the practice service
     * @param currencies       the currencies
     * @param addressFormatter the address formatter
     * @param ageFormatter     the patient age formatter. May be {@code null}
     */
    public DefaultArchetypeFunctionsFactory(IArchetypeService service, ILookupService lookups,
                                            PracticeService practiceService, Currencies currencies,
                                            AddressFormatter addressFormatter, PatientAgeFormatter ageFormatter) {
        this.service = service;
        this.lookups = lookups;
        this.practiceService = practiceService;
        this.currencies = currencies;
        this.addressFormatter = addressFormatter;
        this.ageFormatter = ageFormatter;
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
     * Returns the practice service.
     *
     * @return the practice service
     */
    @Override
    protected PracticeService getPracticeService() {
        return practiceService;
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
     * Returns the address formatter.
     *
     * @return the address formatter
     */
    @Override
    protected AddressFormatter getAddressFormatter() {
        return addressFormatter;
    }

    /**
     * Returns the patient age formatter.
     *
     * @return the patient age formatter. May be {@code null}
     */
    @Override
    protected PatientAgeFormatter getPatientAgeFormatter() {
        return ageFormatter;
    }

}
