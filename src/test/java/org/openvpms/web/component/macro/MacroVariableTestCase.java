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

package org.openvpms.web.component.macro;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.deposit.DepositArchetypes;
import org.openvpms.archetype.rules.finance.till.TillArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link MacroVariables} class.
 *
 * @author Tim Anderson
 */
public class MacroVariableTestCase extends ArchetypeServiceTest {

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookupService;

    /**
     * The context.
     */
    private Context context;

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The location.
     */
    private Party location;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The till.
     */
    private Party till;

    /**
     * The deposit account.
     */
    private Party depositAccount;

    /**
     * The user.
     */
    private User user;

    /**
     * The stock location.
     */
    private Party stockLocation;

    /**
     * The invoice.
     */
    private IMObject invoice;

    /**
     * The visit.
     */
    private IMObject visit;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        context = new LocalContext();
        practice = (Party) create(PracticeArchetypes.PRACTICE);
        context.setPractice(practice);
        location = (Party) create(PracticeArchetypes.LOCATION);
        context.setLocation(location);
        stockLocation = (Party) create(StockArchetypes.STOCK_LOCATION);
        context.setStockLocation(stockLocation);
        customer = (Party) create(CustomerArchetypes.PERSON);
        context.setCustomer(customer);
        patient = (Party) create(PatientArchetypes.PATIENT);
        context.setPatient(patient);
        clinician = TestHelper.createClinician(); // a security.user, with classifications
        context.setClinician(clinician);
        till = (Party) create(TillArchetypes.TILL);
        context.setTill(till);
        depositAccount = (Party) create(DepositArchetypes.DEPOSIT_ACCOUNT);
        context.setDeposit(depositAccount);
        user = (User) create(UserArchetypes.USER);
        context.setUser(user);
        invoice = create(CustomerAccountArchetypes.INVOICE);
        context.addObject(invoice);
        visit = create(PatientArchetypes.CLINICAL_EVENT);
        context.addObject(visit);
    }

    /**
     * Verifies that the variables can be retrieved by name.
     */
    @Test
    public void testVariables() {
        MacroVariables variables = new MacroVariables(context, getArchetypeService(), lookupService);
        assertEquals(practice, variables.get(MacroVariables.PRACTICE));
        assertEquals(location, variables.get(MacroVariables.LOCATION));
        assertEquals(stockLocation, variables.get(MacroVariables.STOCK_LOCATION));
        assertEquals(customer, variables.get(MacroVariables.CUSTOMER));
        assertEquals(patient, variables.get(MacroVariables.PATIENT));
        assertEquals(clinician, variables.get(MacroVariables.CLINICIAN));
        assertEquals(till, variables.get(MacroVariables.TILL));
        assertEquals(depositAccount, variables.get(MacroVariables.DEPOSIT_ACCOUNT));
        assertEquals(user, variables.get(MacroVariables.USER));
        assertEquals(invoice, variables.get(MacroVariables.INVOICE));
        assertEquals(visit, variables.get(MacroVariables.VISIT));
    }

    /**
     * Verifies that nodes of {@code IMObject} variables can be resolved. E.g. for the patient variable, its
     * name and species node can be accessed.
     */
    @Test
    public void testNodeResolution() {
        MacroVariables variables = new MacroVariables(context, getArchetypeService(), lookupService);
        Lookup species = TestHelper.getLookup("lookup.species", "CANINE");
        species.setName("Canine");
        save(species);

        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("name", "Fido");
        bean.setValue("species", "CANINE");

        assertEquals("Fido", variables.get("patient.name"));
        assertEquals("Canine", variables.get("patient.species"));
    }

    /**
     * Verifies that the clinician and user resolve to different instances, despite being the same archetype.
     */
    @Test
    public void testClinicianAndUserInstances() {
        MacroVariables variables = new MacroVariables(context, getArchetypeService(), lookupService);
        Object clinician = variables.get(MacroVariables.CLINICIAN);
        Object user = variables.get(MacroVariables.USER);
        assertNotNull(clinician);
        assertNotNull(user);
        assertNotEquals(clinician, user);
    }

    /**
     * Tests that missing variables return null.
     */
    @Test
    public void testMissingVariable() {
        MacroVariables variables = new MacroVariables(context, getArchetypeService(), lookupService);
        assertNull(variables.get("nosuchvariable"));
    }

}
