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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.query;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.act.ActDOImpl;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.act.FinancialAct;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.query.criteria.CriteriaBuilder;
import org.openvpms.component.query.criteria.CriteriaQuery;
import org.openvpms.component.query.criteria.Join;
import org.openvpms.component.query.criteria.Root;
import org.openvpms.component.query.criteria.Subquery;
import org.openvpms.component.system.common.query.criteria.MappedCriteriaQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link TypedQueryImpl} class.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("/org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml")
public class TypedQueryImplTestCase extends AbstractArchetypeServiceTest {

    /**
     * Helper for multiselect testing.
     */
    public static class Info {

        private final long id;

        private final String reason;

        public Info(long id, String reason) {
            this.id = id;
            this.reason = reason;
        }
    }

    /**
     * The DAO.
     */
    @Autowired
    IMObjectDAO dao;

    /**
     * The criteria builder.
     */
    private CriteriaBuilder cb;

    /**
     * Sets up the test.
     */
    @Before
    public void setUp() {
        cb = getArchetypeService().getCriteriaBuilder();
    }

    /**
     * Tests the {@link TypedQueryImpl#getResultList()} method.
     */
    @Test
    public void testResultList() {
        Act act1 = (Act) create("act.simple");
        Act act2 = (Act) create("act.simple");
        save(act1);
        save(act2);

        CriteriaQuery<Act> criteriaQuery = cb.createQuery(Act.class);
        Root<Act> acts = criteriaQuery.from(Act.class, "act.simple");
        criteriaQuery.where(acts.get("id").in(act1.getId(), act2.getId()));
        criteriaQuery.orderBy(cb.asc(acts.get("id")));

        MappedCriteriaQuery<ActDOImpl> jpaQuery = dao.createQuery(criteriaQuery);
        TypedQueryImpl<Act, ActDOImpl> query = new TypedQueryImpl<>(jpaQuery, Act.class, dao);
        List<Act> list = query.getResultList();
        assertEquals(2, list.size());
        assertEquals(act1, list.get(0));
        assertEquals(act2, list.get(1));
    }

    /**
     * Tests the {@link TypedQueryImpl#getSingleResult()} method.
     */
    @Test
    public void testGetSingleResult() {
        Act act = (Act) create("act.simple");
        save(act);

        CriteriaQuery<Act> criteriaQuery = cb.createQuery(Act.class);
        Root<Act> acts = criteriaQuery.from(Act.class, "act.simple");
        criteriaQuery.where(cb.equal(acts.get("id"), act.getId()));

        MappedCriteriaQuery<ActDOImpl> jpaQuery = dao.createQuery(criteriaQuery);
        TypedQueryImpl<Act, ActDOImpl> query = new TypedQueryImpl<>(jpaQuery, Act.class, dao);
        Act result = query.getSingleResult();
        assertEquals(act, result);
    }

    /**
     * Tests the {@link TypedQueryImpl#setFirstResult(int)} method.
     */
    @Test
    public void testSetFirstResult() {
        Act act1 = (Act) create("act.simple");
        Act act2 = (Act) create("act.simple");
        Act act3 = (Act) create("act.simple");
        save(act1);
        save(act2);
        save(act3);

        CriteriaQuery<Act> criteriaQuery = cb.createQuery(Act.class);
        Root<Act> acts = criteriaQuery.from(Act.class, "act.simple");
        criteriaQuery.where(cb.greaterThanOrEqualTo(acts.get("id"), act1.getId()));
        criteriaQuery.orderBy(cb.asc(acts.get("id")));

        MappedCriteriaQuery<ActDOImpl> jpaQuery = dao.createQuery(criteriaQuery);
        TypedQueryImpl<Act, ActDOImpl> query = new TypedQueryImpl<>(jpaQuery, Act.class, dao);

        List<Act> list1 = query.getResultList();
        assertEquals(3, list1.size());
        assertTrue(list1.contains(act1));
        assertTrue(list1.contains(act2));
        assertTrue(list1.contains(act3));

        query.setFirstResult(1);
        List<Act> list2 = query.getResultList();
        assertEquals(2, list2.size());
        assertFalse(list2.contains(act1));
        assertTrue(list2.contains(act2));
        assertTrue(list2.contains(act3));

        query.setFirstResult(2);
        List<Act> list3 = query.getResultList();
        assertEquals(1, list3.size());
        assertFalse(list3.contains(act1));
        assertFalse(list3.contains(act2));
        assertTrue(list3.contains(act3));

        query.setFirstResult(3);
        List<Act> list4 = query.getResultList();
        assertTrue(list4.isEmpty());
    }

    /**
     * Tests the {@link TypedQueryImpl#setMaxResults(int)} method.
     */
    @Test
    public void testSetMaxResults() {
        Act act1 = (Act) create("act.simple");
        Act act2 = (Act) create("act.simple");
        Act act3 = (Act) create("act.simple");
        save(act1);
        save(act2);
        save(act3);

        CriteriaQuery<Act> criteriaQuery = cb.createQuery(Act.class);
        Root<Act> acts = criteriaQuery.from(Act.class, "act.simple");
        criteriaQuery.where(cb.greaterThanOrEqualTo(acts.get("id"), act1.getId()));
        criteriaQuery.orderBy(cb.asc(acts.get("id")));

        MappedCriteriaQuery<ActDOImpl> jpaQuery = dao.createQuery(criteriaQuery);
        TypedQueryImpl<Act, ActDOImpl> query = new TypedQueryImpl<>(jpaQuery, Act.class, dao);

        List<Act> list1 = query.getResultList();
        assertEquals(3, list1.size());
        assertTrue(list1.contains(act1));
        assertTrue(list1.contains(act2));
        assertTrue(list1.contains(act3));

        query.setMaxResults(2);
        List<Act> list2 = query.getResultList();
        assertEquals(2, list2.size());
        assertTrue(list2.contains(act1));
        assertTrue(list2.contains(act2));
        assertFalse(list2.contains(act3));

        query.setMaxResults(Integer.MAX_VALUE);
        assertEquals(3, list1.size());
        assertTrue(list1.contains(act1));
        assertTrue(list1.contains(act2));
        assertTrue(list1.contains(act3));
    }

    /**
     * Tests multiselect.
     */
    @Test
    public void testMultiSelect() {
        Act act1 = createAct("A");
        Act act2 = createAct("B");
        Act act3 = createAct("C");

        CriteriaQuery<Info> criteriaQuery = cb.createQuery(Info.class);
        Root<Act> acts = criteriaQuery.from(Act.class, "act.simple");
        criteriaQuery.multiselect(acts.get("id"), acts.get("reason"));
        criteriaQuery.where(cb.greaterThanOrEqualTo(acts.get("id"), act1.getId()));
        criteriaQuery.orderBy(cb.asc(acts.get("id")));

        List<Info> list = execute(criteriaQuery);
        assertEquals(3, list.size());

        assertEquals(act1.getId(), list.get(0).id);
        assertEquals(act1.getReason(), list.get(0).reason);
        assertEquals(act2.getId(), list.get(1).id);
        assertEquals(act2.getReason(), list.get(1).reason);
        assertEquals(act3.getId(), list.get(2).id);
        assertEquals(act3.getReason(), list.get(2).reason);
    }

    /**
     * Tests multiselect with using tuples .
     */
    @Test
    public void testMultiselectWithTuple() {
        Act act1 = createAct("A");
        Act act2 = createAct("B");
        Act act3 = createAct("C");

        CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
        Root<Act> acts = criteriaQuery.from(Act.class, "act.simple");
        criteriaQuery.multiselect(acts.get("id"), acts.get("reason"));
        criteriaQuery.where(cb.greaterThanOrEqualTo(acts.get("id"), act1.getId()));
        criteriaQuery.orderBy(cb.asc(acts.get("id")));

        List<Tuple> list = execute(criteriaQuery);
        assertEquals(3, list.size());
        checkTuple(list.get(0), act1.getId(), "A");
        checkTuple(list.get(1), act2.getId(), "B");
        checkTuple(list.get(2), act3.getId(), "C");
    }

    /**
     * Tests multiselect using tuples, where a selected node is stored in a 'details' map.
     */
    @Test
    public void testMultiselectWithDetailsNodes() {
        Party pet1 = createPet("Spot", "CANINE", "DALMATION");
        Party pet2 = createPet("Fido", "CANINE", "KELPIE");
        CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
        Root<Party> pets = criteriaQuery.from(Party.class, "party.patientpet").alias("pets");
        criteriaQuery.multiselect(pets.get("name").alias("name"), pets.get("species").alias("species"),
                                  pets.get("breed").alias("breed"));
        criteriaQuery.where(pets.get("id").in(pet1.getId(), pet2.getId()));
        criteriaQuery.orderBy(cb.asc(pets.get("id")));

        List<Tuple> list = execute(criteriaQuery);
        assertEquals(2, list.size());
        checkTuple(list.get(0), "Spot", "CANINE", "DALMATION");
        checkTuple(list.get(1), "Fido", "CANINE", "KELPIE");
    }

    /**
     * Verifies that comparison can be performed on nodes that are stored in a 'details' map.
     * <p>
     * NOTE: support is limited, as values are stored as strings and are not cast to the correct type to make the
     * comparison.
     */
    @Test
    public void testEqualsWithDetailsNode() {
        Party pet1 = createPet("Spot", "CANINE", "DALMATION");
        Party pet2 = createPet("Fido", "CANINE", "KELPIE");

        CriteriaQuery<Party> criteriaQuery = cb.createQuery(Party.class);
        Root<Party> pets = criteriaQuery.from(Party.class, "party.patientpet").alias("p");
        criteriaQuery.select(pets);
        criteriaQuery.where(cb.equal(pets.get("breed"), "KELPIE"), pets.get("id").in(pet1.getId(), pet2.getId()));

        List<Party> list = execute(criteriaQuery);
        assertEquals(1, list.size());
        assertEquals(pet2, list.get(0));
    }

    /**
     * Tests equality where the expressions are references.
     *
     * @throws Exception for any error
     */
    @Test
    public void testEqualReference() throws Exception {
        Party c1p1 = createPet("C1P1", "CANINE", "DALMATION");
        Party c1p2 = createPet("C1P2", "CANINE", "KELPIE");
        Party c2p1 = createPet("C2P1", "CANINE", "PUG");
        Party c3p1 = createPet("C3P1", "CANINE", "GERMAN_SHEPHERD");
        Party c3p2 = createPet("C3P2", "CANINE", "LABRADOR");
        Party c3p3 = createPet("C3P3", "FELINE", "BOXER");

        Party c1 = createCustomer(c1p1, c1p2);
        Party c2 = createCustomer(c2p1);
        Party c3 = createCustomer(c3p1, c3p2, c3p3);
        Party c4 = createCustomer();

        CriteriaQuery<Party> criteriaQuery = cb.createQuery(Party.class).distinct(true);
        Root<Party> customers = criteriaQuery.from(Party.class, "party.customerperson").alias("customers");
        Join<Party, IMObject> owns = customers.join("patients", "entityRelationship.patientOwner").alias("owns");
        Root<Party> patients = criteriaQuery.from(Party.class, "party.patientpet").alias("pets");
        criteriaQuery.select(customers);
        Predicate withId = customers.get("id").in(c1.getId(), c2.getId(), c3.getId(), c4.getId());
        criteriaQuery.where(cb.equal(owns.get("target"), patients.reference()), withId);

        // should only return those customers with patients
        List<Party> list1 = execute(criteriaQuery);
        assertEquals(3, list1.size());
        assertEquals(c1, list1.get(0));
        assertEquals(c2, list1.get(1));
        assertEquals(c3, list1.get(2));
    }

    /**
     * Tests inequality where the expressions are references.
     *
     * @throws Exception for any error
     */
    @Test
    public void testNotEqualReference() throws Exception {
        Party c1p1 = createPet("C1P1", "CANINE", "DALMATION");
        Party c1p2 = createPet("C1P2", "CANINE", "KELPIE");
        Party c2p1 = createPet("C2P1", "CANINE", "PUG");
        Party c3p1 = createPet("C3P1", "CANINE", "GERMAN_SHEPHERD");
        Party c3p2 = createPet("C3P2", "CANINE", "LABRADOR");
        Party c3p3 = createPet("C3P3", "FELINE", "BOXER");

        Party c1 = createCustomer(c1p1, c1p2);
        Party c2 = createCustomer(c2p1);
        Party c3 = createCustomer(c3p1, c3p2, c3p3);
        Party c4 = createCustomer();

        CriteriaQuery<Party> criteriaQuery = cb.createQuery(Party.class).distinct(true);
        Root<Party> customers = criteriaQuery.from(Party.class, "party.customerperson").alias("customers");
        Join<Party, IMObject> owns = customers.join("patients", "entityRelationship.patientOwner").alias("owns");
        Root<Party> patients = criteriaQuery.from(Party.class, "party.patientpet").alias("pets");
        criteriaQuery.select(customers);
        criteriaQuery.where(cb.notEqual(owns.get("target"), patients.reference()),
                            customers.get("id").in(c1.getId(), c2.getId(), c3.getId(), c4.getId()));


        // should only return those customers with patients
        List<Party> list = execute(criteriaQuery);
        assertEquals(3, list.size());
        assertEquals(c1, list.get(0));
        assertEquals(c2, list.get(1));
        assertEquals(c3, list.get(2));
    }

    /**
     * Tests subquery support.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSubquery() throws Exception {
        Party c1p1 = createPet("C1P1", "CANINE", "DALMATION");
        Party c1p2 = createPet("C1P2", "CANINE", "KELPIE");
        Party c2p1 = createPet("C2P1", "CANINE", "PUG");
        Party c3p1 = createPet("C3P1", "CANINE", "GERMAN_SHEPHERD");
        Party c3p2 = createPet("C3P2", "CANINE", "LABRADOR");
        Party c3p3 = createPet("C3P3", "FELINE", "BOXER");

        Party c1 = createCustomer(c1p1, c1p2);
        Party c2 = createCustomer(c2p1);
        Party c3 = createCustomer(c3p1, c3p2, c3p3);
        Party c4 = createCustomer();

        // generate a query to return all customers that have 2 or more patients
        CriteriaQuery<Party> criteriaQuery = cb.createQuery(Party.class);
        Root<Party> customer = criteriaQuery.from(Party.class, "party.customerperson").alias("customer");

        // count patients
        Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
        Root subRoot = subquery.from(Party.class, "party.patientpet").alias("pet");
        Join subCustomers = subRoot.join("customers", "entityRelationship.patientOwner").alias("owners");
        subquery.select(cb.count(subRoot.get("id")));
        subquery.where(cb.equal(customer.get("id"), subCustomers.join("source").alias("owner").get("id")));

        // limit subquery to >= 2 patients
        criteriaQuery.where(customer.get("id").in(c1.getId(), c2.getId(), c3.getId(), c4.getId()),
                            cb.greaterThanOrEqualTo(subquery, 2L));
        criteriaQuery.orderBy(cb.asc(customer.get("id")));

        List<Party> list = execute(criteriaQuery);
        assertEquals(2, list.size());
        assertEquals(c1, list.get(0));
        assertEquals(c3, list.get(1));
    }

    /**
     * Tests the {@link CriteriaBuilder#exists(Subquery)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExists() throws Exception {
        Party c1p1 = createPet("C1P1", "CANINE", "DALMATION");
        Party c1p2 = createPet("C1P2", "CANINE", "KELPIE");
        Party c2p1 = createPet("C2P1", "CANINE", "PUG");
        Party c3p1 = createPet("C3P1", "CANINE", "GERMAN_SHEPHERD");
        Party c3p2 = createPet("C3P2", "CANINE", "LABRADOR");
        Party c3p3 = createPet("C3P3", "FELINE", "BOXER");

        Party c1 = createCustomer(c1p1, c1p2);
        Party c2 = createCustomer(c2p1);
        Party c3 = createCustomer(c3p1, c3p2, c3p3);
        Party c4 = createCustomer();

        // generate a query to return all customers that have patients
        CriteriaQuery<Party> criteriaQuery = cb.createQuery(Party.class);
        Root<Party> customer = criteriaQuery.from(Party.class, "party.customerperson").alias("customer");

        Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
        Root<Party> subRoot = subquery.from(Party.class, "party.patientpet").alias("pet");
        subquery.select(subRoot.get("id"));
        Join subCustomers = subRoot.join("customers", "entityRelationship.patientOwner").alias("owners");
        subquery.where(cb.equal(customer.get("id"), subCustomers.join("source").alias("owner").get("id")));

        criteriaQuery.where(cb.exists(subquery), customer.get("id").in(c1.getId(), c2.getId(), c3.getId(), c4.getId()));
        criteriaQuery.orderBy(cb.asc(customer.get("id")));

        List<Party> list = execute(criteriaQuery);
        assertEquals(3, list.size());
        assertEquals(c1, list.get(0));
        assertEquals(c2, list.get(1));
        assertEquals(c3, list.get(2));
    }

    /**
     * Tests the {@link CriteriaQuery#groupBy(List)} and {@link CriteriaQuery#having(Expression)} methods.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGroupBy() throws Exception {
        Party c1p1 = createPet("C1P1", "CANINE", "DALMATION");
        Party c1p2 = createPet("C1P2", "CANINE", "KELPIE");
        Party c2p1 = createPet("C2P1", "CANINE", "PUG");
        Party c3p1 = createPet("C3P1", "CANINE", "GERMAN_SHEPHERD");
        Party c3p2 = createPet("C3P2", "CANINE", "LABRADOR");
        Party c3p3 = createPet("C3P3", "FELINE", "BOXER");

        Party c1 = createCustomer(c1p1, c1p2);
        Party c2 = createCustomer(c2p1);
        Party c3 = createCustomer(c3p1, c3p2, c3p3);
        Party c4 = createCustomer();

        // generate a query to return all customers that have more than one patient
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customer = query.from(Party.class, "party.customerperson").alias("customer");
        query.select(customer);
        Join<Party, IMObject> patients = customer.join("patients", "entityRelationship.patientOwner").alias("patients");
        query.groupBy(customer);
        query.having(cb.greaterThan(cb.count(patients), 1L),
                     customer.get("id").in(c1.getId(), c2.getId(), c3.getId(), c4.getId()));
        query.orderBy(cb.asc(customer.get("id")));

        List<Party> list = execute(query);
        assertEquals(2, list.size());
        assertEquals(c1, list.get(0));
        assertEquals(c3, list.get(1));
    }

    /**
     * Tests the {@link CriteriaBuilder#sum(Expression)} method.
     */
    @Test
    public void testSum() {
        FinancialAct act1 = createFinancialAct(BigDecimal.ONE);
        FinancialAct act2 = createFinancialAct(BigDecimal.valueOf(5));
        FinancialAct act3 = createFinancialAct(BigDecimal.TEN);

        // generate a query to return all customers that have more than one patient
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<Act> act = query.from(Act.class, "financial.act").alias("act");
        query.select(cb.sum(act.get("total")));
        query.where(act.get("id").in(act1.getId(), act2.getId(), act3.getId()));

        BigDecimal result = getSingleResult(query);
        assertTrue(BigDecimal.valueOf(16).compareTo(result) == 0);
    }

    /**
     * Tests the {@link CriteriaBuilder#max(Expression)} method.
     */
    @Test
    public void testMax() {
        FinancialAct act1 = createFinancialAct(BigDecimal.ONE);
        FinancialAct act2 = createFinancialAct(BigDecimal.valueOf(5));
        FinancialAct act3 = createFinancialAct(BigDecimal.TEN);

        // generate a query to return all customers that have more than one patient
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<Act> act = query.from(Act.class, "financial.act").alias("act");
        query.select(cb.max(act.get("total")));
        query.where(act.get("id").in(act1.getId(), act2.getId(), act3.getId()));

        BigDecimal result = getSingleResult(query);
        assertTrue(BigDecimal.TEN.compareTo(result) == 0);
    }

    /**
     * Tests the {@link CriteriaBuilder#min(Expression)} method.
     */
    @Test
    public void testMin() {
        Party pet1 = createPet("P1", "CANINE", "DALMATION");
        Party pet2 = createPet("P2", "CANINE", "KELPIE");
        Party pet3 = createPet("P3", "CANINE", "PUG");

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Party> pets = query.from(Party.class, "party.patientpet").alias("pet");
        query.select(cb.min(pets.get("id")));
        query.where(pets.get("id").in(pet1.getId(), pet2.getId(), pet3.getId()));

        Long result = getSingleResult(query);
        assertEquals((Long) pet1.getId(), result);
    }

    /**
     * Helper to execute a query.
     *
     * @param criteriaQuery the query to execute
     * @return the result of the query
     */
    private <X> List<X> execute(CriteriaQuery<X> criteriaQuery) {
        MappedCriteriaQuery<?> jpaQuery = dao.createQuery(criteriaQuery);
        TypedQueryImpl<X, ?> query = new TypedQueryImpl<>(jpaQuery, criteriaQuery.getResultType(), dao);
        return query.getResultList();
    }


    /**
     * Helper to get a single result.
     *
     * @param criteriaQuery the query to execute
     * @return the result of the query
     */
    private <X> X getSingleResult(CriteriaQuery<X> criteriaQuery) {
        MappedCriteriaQuery<?> jpaQuery = dao.createQuery(criteriaQuery);
        TypedQueryImpl<X, ?> query = new TypedQueryImpl<>(jpaQuery, criteriaQuery.getResultType(), dao);
        return query.getSingleResult();
    }

    /**
     * Creates a new customer.
     *
     * @param pets the pets
     * @return the customer
     */
    private Party createCustomer(Party... pets) {
        Party customer = (Party) create("party.customerperson");
        IMObjectBean bean = getArchetypeService().getBean(customer);
        bean.setValue("title", "MS");
        bean.setValue("firstName", "S");
        bean.setValue("lastName", "Smith");
        for (Party pet : pets) {
            bean.addTarget("patients", "entityRelationship.patientOwner", pet);
        }
        save(customer);
        return customer;
    }

    /**
     * Verifies a tuple matches that expected.
     *
     * @param tuple   the tuple
     * @param objects the expected objects
     */
    private void checkTuple(Tuple tuple, Object... objects) {
        List<TupleElement<?>> elements = tuple.getElements();
        assertEquals(objects.length, elements.size());
        for (int i = 0; i < objects.length; ++i) {
            assertEquals(objects[i], tuple.get(i));
        }
    }

    /**
     * Creates an <em>act.simple</em> the the specified reason.
     *
     * @param reason the reason
     * @return a new act
     */
    private Act createAct(String reason) {
        Act act = (Act) create("act.simple");
        act.setReason(reason);
        save(act);
        return act;
    }

    /**
     * Creates a new pet.
     *
     * @param name    the pet name
     * @param species the pet species
     * @param breed   the pet breed
     * @return a new pet
     */
    private Party createPet(String name, String species, String breed) {
        Party pet = (Party) create("party.patientpet");
        IMObjectBean bean = getArchetypeService().getBean(pet);
        bean.setValue("name", name);
        bean.setValue("species", species);
        bean.setValue("breed", breed);
        save(pet);
        return pet;
    }

    /**
     * Creates an act with the specified total.
     *
     * @param total the total
     * @return a new act
     */
    private FinancialAct createFinancialAct(BigDecimal total) {
        FinancialAct act = (FinancialAct) create("financial.act");
        act.setTotal(total);
        save(act);
        return act;
    }

}
