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

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.lang.WordUtils;
import org.openvpms.archetype.function.expression.ExpressionFunctions;
import org.openvpms.archetype.function.history.HistoryFunctions;
import org.openvpms.archetype.function.list.ListFunctions;
import org.openvpms.archetype.function.lookup.LookupFunctions;
import org.openvpms.archetype.function.math.MathFunctions;
import org.openvpms.archetype.function.party.PartyFunctions;
import org.openvpms.archetype.function.reminder.ReminderFunctions;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientAgeFormatter;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.DateFunctions;
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
        return create(service);
    }

    /**
     * Creates a new {@code FunctionLibrary} containing functions that use the specified {@link IArchetypeService}.
     *
     * @param service the archetype service
     * @return the functions
     */
    public FunctionLibrary create(IArchetypeService service) {
        ILookupService lookups = getLookupService();
        PatientAgeFormatter formatter = getPatientAgeFormatter();

        PatientRules patientRules = new PatientRules(service, lookups, formatter);
        CustomerRules customerRules = new CustomerRules(service, lookups);
        ReminderRules reminderRules = new ReminderRules(service, patientRules);
        FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(create("date", DateFunctions.class));
        library.addFunctions(new ExpressionFunctions("expr"));
        library.addFunctions(create("history", new HistoryFunctions(service)));
        library.addFunctions(create("list", new ListFunctions(service, lookups)));
        library.addFunctions(create("lookup", LookupFunctions.class));
        library.addFunctions(create("math", new MathFunctions()));
        library.addFunctions(create("openvpms", new ArchetypeServiceFunctions(service, lookups)));
        library.addFunctions(create("party", new PartyFunctions(service, lookups, patientRules)));
        library.addFunctions(create("reminder", new ReminderFunctions(service, reminderRules, customerRules)));
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
