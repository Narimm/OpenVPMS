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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.factory;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.lang.WordUtils;
import org.openvpms.archetype.function.contact.AddressFunctions;
import org.openvpms.archetype.function.contact.EmailFunctions;
import org.openvpms.archetype.function.contact.PhoneFunctions;
import org.openvpms.archetype.function.date.DateFunctions;
import org.openvpms.archetype.function.expression.ExpressionFunctions;
import org.openvpms.archetype.function.history.HistoryFunctions;
import org.openvpms.archetype.function.insurance.InsuranceFunctions;
import org.openvpms.archetype.function.list.ListFunctions;
import org.openvpms.archetype.function.lookup.LookupFunctions;
import org.openvpms.archetype.function.math.MathFunctions;
import org.openvpms.archetype.function.party.PartyFunctions;
import org.openvpms.archetype.function.product.ProductFunctions;
import org.openvpms.archetype.function.reminder.ReminderFunctions;
import org.openvpms.archetype.function.supplier.SupplierFunctions;
import org.openvpms.archetype.function.user.CachingUserFunctions;
import org.openvpms.archetype.function.user.UserFunctions;
import org.openvpms.archetype.rules.contact.AddressFormatter;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientAgeFormatter;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.FunctionsFactory;
import org.openvpms.component.system.common.jxpath.ObjectFunctions;

/**
 * Abstract implementation of the {@link FunctionsFactory} interface, to support archetypes.
 * This registers the following functions:
 * <ul>
 * <li><em>date</em> - {@link DateFunctions}</li>
 * <li><em>expr</em> - {@link ExpressionFunctions}</li>
 * <li><em>history</em> - {@link HistoryFunctions}</li>
 * <li><em>list</em> - {@link ListFunctions}</li>
 * <li><em>lookup</em> - {@link LookupFunctions}</li>
 * <li><em>math</em> - {@link MathFunctions}</li>
 * <li><em>openvpms</em> - {@link ArchetypeServiceFunctions}</li>
 * <li><em>party</em> - {@link PartyFunctions}</li>
 * <li><em>reminder</em> - {@link ReminderFunctions}</li>
 * <li><em>user</em> - {@link UserFunctions}</li>
 * <li><em>word</em> - {@code WordUtils}</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public abstract class ArchetypeFunctionsFactory implements FunctionsFactory {

    /**
     * Creates a new {@code Functions}.
     *
     * @return the functions
     */
    @Override
    public Functions create() {
        IArchetypeService service = getArchetypeService();
        return create(service, false);
    }

    /**
     * Creates a new {@code FunctionLibrary} containing functions that use the specified {@link IArchetypeService}.
     *
     * @param service the archetype service
     * @param cache   if {@code true}, indicates that functions may use caching
     * @return the functions
     */
    public FunctionLibrary create(IArchetypeService service, boolean cache) {
        ILookupService lookups = getLookupService();
        PatientAgeFormatter ageFormatter = getPatientAgeFormatter();
        AddressFormatter addressFormatter = getAddressFormatter();

        PracticeService practiceService = getPracticeService();
        PracticeRules rules = new PracticeRules(service, getCurrencies());
        PatientRules patientRules = new PatientRules(rules, service, lookups, ageFormatter);
        CustomerRules customerRules = new CustomerRules(service, lookups, addressFormatter);
        ReminderRules reminderRules = new ReminderRules(service, patientRules);
        SupplierRules supplierRules = new SupplierRules(service);
        FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(create("date", new DateFunctions()));
        library.addFunctions(new ExpressionFunctions("expr"));
        library.addFunctions(new HistoryFunctions(service));
        library.addFunctions(new ListFunctions(service, lookups));
        library.addFunctions(create("lookup", LookupFunctions.class));
        library.addFunctions(create("math", new MathFunctions()));
        library.addFunctions(create("openvpms", new ArchetypeServiceFunctions(service, lookups)));
        library.addFunctions(create("party", new PartyFunctions(service, lookups, patientRules, addressFormatter)));
        library.addFunctions(create("address", new AddressFunctions(customerRules)));
        library.addFunctions(create("email", new EmailFunctions(customerRules, service)));
        library.addFunctions(create("phone", new PhoneFunctions(customerRules)));
        library.addFunctions(new ProductFunctions(new ProductPriceRules(service), practiceService, service));
        library.addFunctions(create("supplier", new SupplierFunctions(supplierRules)));
        library.addFunctions(new ReminderFunctions(service, reminderRules, customerRules));
        if (cache) {
            library.addFunctions(new CachingUserFunctions(service, practiceService, lookups, library, 1024));
        } else {
            library.addFunctions(new UserFunctions(service, practiceService, lookups, library));
        }
        library.addFunctions(create("insurance", new InsuranceFunctions(service)));
        library.addFunctions(create("word", WordUtils.class));
        return library;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected abstract IArchetypeService getArchetypeService();

    /**
     * Returns the lookup service.
     *
     * @return the lookup service
     */
    protected abstract ILookupService getLookupService();

    /**
     * Returns the practice service.
     *
     * @return the practice service
     */
    protected abstract PracticeService getPracticeService();

    /**
     * Returns the currencies.
     *
     * @return the currencies
     */
    protected abstract Currencies getCurrencies();

    /**
     * Returns the address formatter.
     *
     * @return the address formatter
     */
    protected abstract AddressFormatter getAddressFormatter();

    /**
     * Returns the patient age formatter.
     *
     * @return the patient age formatter. May be {@code null}
     */
    protected abstract PatientAgeFormatter getPatientAgeFormatter();

    /**
     * Creates a new {@code Functions} that delegates to the supplied object.
     *
     * @param namespace the function namespace
     * @param object    the object
     * @return the functions
     */
    protected Functions create(String namespace, Object object) {
        return new ObjectFunctions(object, namespace);
    }

    /**
     * Creates a new {@code Functions} for a class.
     *
     * @param namespace     the function namespace
     * @param functionClass the function class
     * @return the functions
     */
    protected Functions create(String namespace, Class functionClass) {
        return new ClassFunctions(functionClass, namespace);
    }
}
