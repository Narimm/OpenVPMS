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

package org.openvpms.archetype.function.list;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.function.expression.ExpressionFunctions;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ListFunctions} class.
 *
 * @author Tim Anderson
 */
public class ListFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * The JXPath context.
     */
    private JXPathContext ctx;

    /**
     * Test pet 1.
     */
    private Party pet1;

    /**
     * Test pet 2.
     */
    private Party pet2;

    /**
     * Test pet 3.
     */
    private Party pet3;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        pet1 = createPet("C", "Canine");
        pet2 = createPet("A", "Feline");
        pet3 = createPet("B", "Bovine");

        List<Party> objects = Arrays.asList(pet1, pet2, pet3);
        ctx = createContext(objects);
    }

    /**
     * Tests the {@link ListFunctions#first(Iterable)} and {@link ListFunctions#first(Object, String)} methods.
     */
    @Test
    public void testFirst() {
        assertEquals(pet1, ctx.getValue("list:first(.)"));
        assertEquals("C", ctx.getValue("list:first(., 'name')"));
    }

    /**
     * Tests the {@link ListFunctions#names(Object)}, {@link ListFunctions#names(Object, String)}
     * and {@link ListFunctions#names(Object), String, String} methods.
     */
    @Test
    public void testNames() {
        assertEquals("C, A, B", ctx.getValue("list:names(.)"));
        assertEquals("C;A;B", ctx.getValue("list:names(., ';')"));
        assertEquals("C, A & B", ctx.getValue("list:names(., ', ', ' & ')"));
        assertEquals("C, A and B are", ctx.getValue("list:names(., ', ', ' and ', ' is', ' are')"));
        assertEquals("C is", ctx.getValue("list:names(list:first(.), ', ', ' and ', ' is', ' are')"));
    }

    /**
     * Tests the {@link ListFunctions#sortNamesOf(ExpressionContext, String)},
     * {@link ListFunctions#sortNamesOf(Object, String)} and
     * {@link ListFunctions#sortNamesOf(Object, String, String, String, String, String)} methods.
     */
    @Test
    public void testSortNamesOf() {
        Party vet = (Party) create(SupplierArchetypes.SUPPLIER_VET);
        vet.addClassification(createVetSpecialty("Surgery"));
        vet.addClassification(createVetSpecialty("Large Animals"));
        vet.addClassification(createVetSpecialty("Snakes"));

        JXPathContext ctx = createContext(vet);

        assertEquals("Large Animals, Snakes, Surgery", ctx.getValue("list:sortNamesOf('classifications')"));

        assertEquals("Large Animals, Snakes, Surgery", ctx.getValue("list:sortNamesOf(., 'classifications')"));

        assertEquals("(Large Animals, Snakes, Surgery)",
                     ctx.getValue("expr:concatIf('(', list:sortNamesOf(., 'classifications'), ')')"));

        assertEquals("Large Animals, Snakes, Surgery classifications", ctx.getValue(
                "list:sortNamesOf(., 'classifications', ', ',', ',' classification',' classifications')"));
    }

    /**
     * Tests the {@link ListFunctions#sortNames(Object)} and {@link ListFunctions#sortNames(Object, String)}
     * methods.
     */
    @Test
    public void testSortNames() {
        assertEquals("A, B, C", ctx.getValue("list:sortNames(.)"));
        assertEquals("A;B;C", ctx.getValue("list:sortNames(., ';')"));
        assertEquals("A,B&C are", ctx.getValue("list:sortNames(., ',','&',' is', ' are')"));
        assertEquals("C is", ctx.getValue("list:sortNames(list:first(.), ',','&',' is', ' are')"));
    }

    /**
     * Tests the {@link ListFunctions#join(Object, String)}, {@link ListFunctions#join(Object, String, String)} and
     * {@link ListFunctions#join(Object, String, String, String)} methods for a collection of {@link IMObject}s.
     */
    @Test
    public void testJoin() {
        assertEquals("CANINE, FELINE, BOVINE", ctx.getValue("list:join(.,'species')"));
        assertEquals("CANINE;FELINE;BOVINE", ctx.getValue("list:join(.,'species',';')"));
        assertEquals("Canine, Feline and Bovine", ctx.getValue("list:join(.,'species.name',', ',' and ')"));
        assertEquals("CANINE, FELINE and BOVINE are", ctx.getValue("list:join(.,'species',', ',' and ',' is',' are')"));
        assertEquals("CANINE is", ctx.getValue("list:join(list:first(.),'species',', ',' and ',' is',' are')"));
    }

    /**
     * Tests the {@link ListFunctions#join(Object, String)}, {@link ListFunctions#join(Object, String, String)} and
     * {@link ListFunctions#join(Object, String, String, String)} methods when provided {@link PropertySet}s.
     */
    @Test
    public void testJoinPropertySet() {
        List<PropertySet> objects = createPetSets(pet1, pet2, pet3);
        JXPathContext context = createContext(objects);

        assertEquals("CANINE, FELINE, BOVINE", context.getValue("list:join(.,'pet.species')"));
        assertEquals("CANINE;FELINE;BOVINE", context.getValue("list:join(.,'pet.species',';')"));
        assertEquals("Canine, Feline and Bovine", context.getValue("list:join(.,'pet.species.name',', ',' and ')"));
    }

    /**
     * Tests the {@link ListFunctions#values(ExpressionContext, String)}
     * and {@link ListFunctions#values(Object, String)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testValues() {
        Party customer = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient(customer);
        Party patient2 = TestHelper.createPatient(customer);
        Party patient3 = TestHelper.createPatient(customer);
        JXPathContext context = createContext(customer);
        List<Party> values1 = (List<Party>) context.getValue("list:values(., 'patients.target')");
        checkValues(values1, patient1, patient2, patient3);

        List<Party> values2 = (List<Party>) context.getValue("list:values('patients.target')");
        checkValues(values2, patient1, patient2, patient3);

        List<Long> values3 = (List<Long>) context.getValue("list:values(., 'patients.target.id')");
        checkValues(values3, patient1.getId(), patient2.getId(), patient3.getId());

        List<Long> values4 = (List<Long>) context.getValue("list:values('patients.target.id')");
        checkValues(values4, patient1.getId(), patient2.getId(), patient3.getId());
    }

    /**
     * Tests the {@link ListFunctions#values(ExpressionContext, String)}
     * and {@link ListFunctions#values(Object, String)} methods when provided collections of {@link IMObject}s and
     * {@link PropertySet}s.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testValuesCollection() {
        pet1.addIdentity(PatientTestHelper.createMicrochip("M1"));
        pet1.addIdentity(PatientTestHelper.createPetTag("T1"));
        pet2.addIdentity(PatientTestHelper.createMicrochip("M2"));
        pet2.addIdentity(PatientTestHelper.createPetTag("T2"));
        pet2.addIdentity(PatientTestHelper.createMicrochip("M3"));
        pet2.addIdentity(PatientTestHelper.createPetTag("T3"));

        // test collections of IMObjects
        List<Party> pets = Arrays.asList(pet1, pet2, pet3);
        JXPathContext context1 = createContext(pets);
        List<Long> values1 = (List<Long>) context1.getValue("list:values(., 'id')");
        checkValues(values1, pet1.getId(), pet2.getId(), pet3.getId());

        List<Long> values2 = (List<Long>) context1.getValue("list:values('id')");
        checkValues(values2, pet1.getId(), pet2.getId(), pet3.getId());

        // check collections of collections
        List<String> values3 = (List<String>) context1.getValue("list:values('identities.name')");
        checkValues(values3, "M1", "T1", "M2", "T2", "M3", "T3");

        // test collections of PropertySets
        List<PropertySet> sets = createPetSets(pet1, pet2, pet3);
        JXPathContext context2 = createContext(sets);

        List<Long> values4 = (List<Long>) context2.getValue("list:values(., 'pet.id')");
        checkValues(values4, pet1.getId(), pet2.getId(), pet3.getId());

        List<Long> values5 = (List<Long>) context2.getValue("list:values('pet.id')");
        checkValues(values5, pet1.getId(), pet2.getId(), pet3.getId());

        // check collections of collections
        List<String> values6 = (List<String>) context2.getValue("list:values('pet.identities.name')");
        checkValues(values6, "M1", "T1", "M2", "T2", "M3", "T3");
    }

    /**
     * Tests the {@link ListFunctions#distinct(Object, String)} and {@link ListFunctions#set(Object, String)}
     * methods.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testDistinctSet() {
        Product product = TestHelper.createProduct();
        Party customer = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();
        patient1.setName("Fido");
        patient2.setName("Spot");
        save(patient1, patient2);
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(BigDecimal.TEN, customer, patient1, product,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct act1 = acts.get(1);
        FinancialAct act2 = FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM, patient2,
                                                                 product, BigDecimal.ONE);
        FinancialAct act3 = FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM, patient2,
                                                                 product, BigDecimal.ONE);
        ActBean bean = new ActBean(invoice);
        bean.addNodeRelationship("items", act2);
        bean.addNodeRelationship("items", act3);
        save(invoice, act1, act2, act3);

        JXPathContext context = createContext(invoice);

        List<Party> list1 = (List<Party>) context.getValue("list:distinct(., 'items.target.patient.entity')");
        checkValues(list1, patient1, patient2);

        Set<Party> set1 = (Set<Party>) context.getValue("list:set(., 'items.target.patient.entity')");
        checkValues(set1, patient1, patient2);

        // check the ExpressionContext version of the functions
        List<Party> values2 = (List<Party>) context.getValue("list:distinct('items.target.patient.entity')");
        checkValues(values2, patient1, patient2);

        Set<Party> set2 = (Set<Party>) context.getValue("list:set('items.target.patient.entity')");
        checkValues(set2, patient1, patient2);

        // check that they can be counted.
        Number count1 = (Number) context.getValue("count(list:distinct(., 'items.target.patient.entity'))");
        assertEquals(2, count1.intValue());
        Number count2 = (Number) context.getValue("count(list:set(., 'items.target.patient.entity'))");
        assertEquals(2, count2.intValue());

        List<Long> values3 = (List<Long>) context.getValue("list:distinct(., 'items.target.product.entity.id')");
        checkValues(values3, product.getId());

        // check names
        String names1 = (String) context.getValue("list:sortNames(list:distinct('items.target.patient.entity'))");
        assertEquals("Fido, Spot", names1);
        String names2 = (String) context.getValue("list:sortNames(list:set('items.target.patient.entity'))");
        assertEquals("Fido, Spot", names2);
    }

    /**
     * Verifies that collection values match that expected.
     *
     * @param collection the collection
     * @param values     the expected values
     */
    @SafeVarargs
    private final <T> void checkValues(Collection<T> collection, T... values) {
        assertEquals(values.length, collection.size());
        for (T value : values) {
            assertTrue(collection.contains(value));
        }
    }

    /**
     * Creates a pet.
     *
     * @param name    the pet name
     * @param species the pet species
     * @return a new pet
     */
    private Party createPet(String name, String species) {
        Party pet = TestHelper.createPatient(false);

        pet.setName(name);
        IMObjectBean bean = new IMObjectBean(pet);
        Lookup lookup = TestHelper.getLookup("lookup.species", species.toUpperCase(), species, true);
        bean.setValue("species", lookup.getCode());
        return pet;
    }

    /**
     * Helper to create an <em>lookup.veterinarySpeciality</em>.
     *
     * @param name the speciality name
     * @return a new lookup
     */
    private Lookup createVetSpecialty(String name) {
        Lookup lookup = (Lookup) create("lookup.veterinarySpeciality");
        lookup.setCode(name);
        lookup.setName(name);
        return lookup;
    }

    /**
     * Creates a new JXPathContext.
     *
     * @param object the context object
     * @return a new JXPathContext
     */
    private JXPathContext createContext(Object object) {
        FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(new ListFunctions(getArchetypeService(), getLookupService()));
        library.addFunctions(new ExpressionFunctions("expr"));
        return JXPathHelper.newContext(object, library);
    }

    /**
     * Helper to create a collection of property sets containing pets.
     *
     * @param pets the pets
     * @return the property sets
     */
    private List<PropertySet> createPetSets(Party... pets) {
        List<PropertySet> result = new ArrayList<>();
        for (Party pet : pets) {
            PropertySet set = new ObjectSet();
            set.set("pet", pet);
            result.add(set);
        }
        return result;
    }

}
