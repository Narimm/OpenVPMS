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

package org.openvpms.component.business.dao.hibernate.im.query;

import org.apache.commons.lang.reflect.FieldUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.criteria.internal.compile.CriteriaQueryTypeQueryAdapter;
import org.hibernate.query.spi.QueryImplementor;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.query.spi.QueryParameterBindings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.AssemblerImpl;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.query.criteria.CriteriaBuilder;
import org.openvpms.component.query.criteria.CriteriaQuery;
import org.openvpms.component.query.criteria.From;
import org.openvpms.component.query.criteria.Join;
import org.openvpms.component.query.criteria.Root;
import org.openvpms.component.query.criteria.Subquery;
import org.openvpms.component.system.common.query.criteria.CriteriaBuilderImpl;
import org.openvpms.component.system.common.query.criteria.CriteriaQueryImpl;
import org.openvpms.component.system.common.query.criteria.MappedCriteriaQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link MappedCriteriaQueryFactory}.
 * <p>
 * NOTE: these tests require that the Hibernate {@code SessionFactory} is configured with:<br/>
 * {@code hibernate.criteria.literal_handling_mode=BIND}
 * <p>
 * Failure to do this will result in numeric values being treated as literals in the generated queries, which may
 * affect query caching.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("/org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml")
public class MappedCriteriaQueryFactoryTestCase extends AbstractArchetypeServiceTest {

    /**
     * Helper for multiselect testing.
     */
    public static class Info {

        private final long id;

        private final String name;

        private boolean active;

        public Info(long id, String name, boolean active) {
            this.id = id;
            this.name = name;
            this.active = active;
        }
    }

    /**
     * The hibernate session factory.
     */
    @Autowired
    private SessionFactory sessionFactory;

    /**
     * The archetype descriptor cache.
     */
    @Autowired
    private IArchetypeDescriptorCache descriptorCache;

    /**
     * The session.
     */
    private Session session;

    /**
     * The assembler.
     */
    private CompoundAssembler assembler;

    /**
     * The criteria builder.
     */
    private CriteriaBuilder cb;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        session = sessionFactory.openSession();
        assembler = new AssemblerImpl(descriptorCache);
        cb = new CriteriaBuilderImpl(descriptorCache);
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown() {
        session.close();
    }

    /**
     * Tests the {@link CriteriaQuery#distinct(boolean)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDistinct() throws Exception {
        String expected = "select distinct a from ActDOImpl as a " +
                          "where a.archetypeId.shortName=:param0";
        CriteriaQuery<Act> query = cb.createQuery(Act.class).distinct(true);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#and(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testAndExpression() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) " +
                          "and ( ( a.activityStartTime>=:param1 ) and ( a.activityStartTime<:param2 ) )";
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(10);
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.and(cb.greaterThanOrEqualTo(acts.get("startTime"), from),
                           cb.lessThan(acts.get("startTime"), to)));
        checkQuery(query, expected, "act.simple", from, to);
    }

    /**
     * Tests the {@link CriteriaBuilder#and(Predicate...)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testAndPredicate() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.activityStartTime is null )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.and(cb.isNull(acts.get("startTime"))));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#or(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testOrExpression() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) " +
                          "and ( ( a.id<:param1 ) or ( a.id>:param2 ) )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.or(cb.lessThan(acts.get("id"), 1000),
                          cb.greaterThan(acts.get("id"), 2000)));
        checkQuery(query, expected, "act.simple", 1000L, 2000L);
    }

    /**
     * Tests the {@link CriteriaBuilder#or(Predicate...)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testOrPredicates() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) " +
                          "and ( ( a.activityStartTime is null ) or ( a.activityEndTime is null ) )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.or(cb.isNull(acts.get("startTime")),
                          cb.isNull(acts.get("endTime"))));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#isNull(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testIsNull() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) " +
                          "and ( a.activityStartTime is null )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.isNull(acts.get("startTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#isNotNull(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testIsNotNull() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) " +
                          "and ( a.activityStartTime is not null )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.isNotNull(acts.get("startTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#equal(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testEqualExpression() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.activityStartTime=a.activityEndTime )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.equal(acts.get("startTime"), acts.get("endTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#equal(Expression, Object)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testEqualObject() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.id=:param1 )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.equal(acts.get("id"), 1000));
        checkQuery(query, expected, "act.simple", 1000L);
    }

    /**
     * Tests the {@link CriteriaBuilder#equal(Expression, Object)} method when the path refers to a details node.
     *
     * @throws Exception for any error
     */
    @Test
    public void testEqualObjectForDetailsNode() throws Exception {
        String expected = "select p from PartyDOImpl as p inner join p.details as species with key(species)=:param0 " +
                          "where ( p.archetypeId.shortName=:param1 ) and ( species.value=:param2 )";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> pets = query.from(Party.class, "party.patientpet").alias("p");
        query.select(pets);
        query.where(cb.equal(pets.get("species").alias("species"), "CANINE"));
        checkQuery(query, expected, "species", "party.patientpet", "CANINE");
    }

    /**
     * Tests equality where the right hand side is a reference.
     */
    @Test
    public void testEqualReference() throws Exception {
        String expected = "select customers from PartyDOImpl as customers " +
                          "where ( customers.archetypeId.shortName=:param0 ) and ( ( customers.id=:param1 ) " +
                          "and ( customers.archetypeId.shortName=:param2 ) )";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customers = query.from(Party.class, "party.customerperson").alias("customers");
        query.select(customers);
        query.where(cb.equal(customers.reference(), new IMObjectReference("party.customerperson", 10)));
        checkQuery(query, expected, "party.customerperson", 10L, "party.customerperson");
    }

    /**
     * Tests inequality where the right hand side is a reference.
     */
    @Test
    public void testNotEqualReference() throws Exception {
        String expected = "select customers from PartyDOImpl as customers " +
                          "where ( customers.archetypeId.shortName=:param0 ) and ( customers.id<>:param1 )";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customers = query.from(Party.class, "party.customerperson").alias("customers");
        query.select(customers);
        query.where(cb.notEqual(customers.reference(), new IMObjectReference("party.customerperson", 10)));
        checkQuery(query, expected, "party.customerperson", 10L);
    }

    /**
     * Tests equality where the expressions are references.
     *
     * @throws Exception for any error
     */
    @Test
    public void testEqualReferenceExpression() throws Exception {
        String expected = "select customers from PartyDOImpl as customers, PartyDOImpl as pets " +
                          "inner join customers.sourceEntityRelationships as owns " +
                          "with owns.archetypeId.shortName=:param0 " +
                          "where ( customers.archetypeId.shortName=:param1 ) " +
                          "and ( pets.archetypeId.shortName=:param2 ) and ( ( owns.target.id=pets.id ) " +
                          "and ( owns.target.archetypeId.shortName=pets.archetypeId.shortName ) )";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customers = query.from(Party.class, "party.customerperson").alias("customers");
        Join<Party, IMObject> owns = customers.join("patients", "entityRelationship.patientOwner").alias("owns");
        Root<Party> patients = query.from(Party.class, "party.patientpet").alias("pets");
        query.select(customers);
        query.where(cb.equal(owns.get("target"), patients.reference()));
        checkQuery(query, expected, "entityRelationship.patientOwner", "party.customerperson", "party.patientpet");
    }

    /**
     * Tests inequality where the expressions are references.
     *
     * @throws Exception for any error
     */
    @Test
    public void testNotEqualReferenceExpression() throws Exception {
        String expected = "select customers from PartyDOImpl as customers, PartyDOImpl as pets " +
                          "inner join customers.sourceEntityRelationships as owns " +
                          "with owns.archetypeId.shortName=:param0 " +
                          "where ( customers.archetypeId.shortName=:param1 ) " +
                          "and ( pets.archetypeId.shortName=:param2 ) and ( owns.target.id<>pets.id )";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customers = query.from(Party.class, "party.customerperson").alias("customers");
        Join<Party, IMObject> owns = customers.join("patients", "entityRelationship.patientOwner").alias("owns");
        Root<Party> patients = query.from(Party.class, "party.patientpet").alias("pets");
        query.select(customers);
        query.where(cb.notEqual(owns.get("target"), patients.reference()));
        checkQuery(query, expected, "entityRelationship.patientOwner", "party.customerperson", "party.patientpet");
    }

    /**
     * Tests the {@link CriteriaBuilder#notEqual(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testNotEqualExpression() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.activityStartTime<>a.activityEndTime )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.notEqual(acts.get("startTime"), acts.get("endTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#notEqual(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testNotEqualObject() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.id<>:param1 )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.notEqual(acts.get("id"), 1000));
        checkQuery(query, expected, "act.simple", 1000L);
    }

    /**
     * Tests the {@link CriteriaBuilder#greaterThan(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGreaterThanExpression() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.activityEndTime>a.activityStartTime )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.greaterThan(acts.get("endTime"), acts.get("startTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#greaterThan(Expression, Comparable)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGreaterThanObject() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.id>:param1 )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.greaterThan(acts.get("id"), 1000));
        checkQuery(query, expected, "act.simple", 1000L);
    }

    /**
     * Tests the {@link CriteriaBuilder#greaterThanOrEqualTo(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGreaterThanOrEqualToExpression() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.activityEndTime>=a.activityStartTime )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.greaterThanOrEqualTo(acts.get("endTime"), acts.get("startTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#greaterThanOrEqualTo(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGreaterThanOrEqualToObject() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.id>=:param1 )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.greaterThanOrEqualTo(acts.get("id"), 1000));
        checkQuery(query, expected, "act.simple", 1000L);
    }

    /**
     * Tests the {@link CriteriaBuilder#lessThan(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLessThanExpression() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.activityStartTime<a.activityEndTime )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.lessThan(acts.get("startTime"), acts.get("endTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#lessThan(Expression, Comparable)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLessThanObject() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.id<:param1 )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.lessThan(acts.get("id"), 1000));
        checkQuery(query, expected, "act.simple", 1000L);
    }

    /**
     * Tests the {@link CriteriaBuilder#lessThanOrEqualTo(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLessThanOrEqualToExpression() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.activityStartTime<=a.activityEndTime )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.lessThanOrEqualTo(acts.get("startTime"), acts.get("endTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#lessThanOrEqualTo(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLessThanOrEqualToObject() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) and ( a.id<=:param1 )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(cb.lessThanOrEqualTo(acts.get("id"), 1000));
        checkQuery(query, expected, "act.simple", 1000L);
    }

    /**
     * Tests the {@link CriteriaBuilder#between(Expression, Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testBetweenExpression() throws Exception {
        String expected = "select acts from FinancialActDOImpl as acts where ( acts.archetypeId.shortName=:param0 ) " +
                          "and ( acts.fixedAmount between acts.fixedCost and acts.unitCost )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "financial.act").alias("acts");
        query.select(acts);
        query.where(cb.between(acts.get("fixedAmount"), acts.get("fixedCost"), acts.get("unitCost")));
        checkQuery(query, expected, "financial.act");
    }

    /**
     * Tests the {@link CriteriaBuilder#between(Expression, Comparable, Comparable)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testBetweenValue() throws Exception {
        String expected = "select payments from ActDOImpl as payments " +
                          "where ( payments.archetypeId.shortName=:param0 ) " +
                          "and ( payments.id between :param1 and :param2 )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> payments = query.from(Act.class, "act.customerAccountPayment").alias("payments");
        query.where(cb.between(payments.get("id"), 1, 1000));
        checkQuery(query, expected, "act.customerAccountPayment", 1, 1000);
    }

    /**
     * Tests the {@link CriteriaBuilder#like(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLikeExpression() throws Exception {
        String expected = "select acts from ActDOImpl as acts " +
                          "where ( acts.archetypeId.shortName=:param0 ) and ( acts.reason like acts.name )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("acts");
        query.select(acts);
        query.where(cb.like(acts.get("reason"), acts.get("name")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#like(Expression, Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLikeObject() throws Exception {
        String expected = "select customers from PartyDOImpl as customers " +
                          "where ( customers.archetypeId.shortName=:param0 ) and ( customers.name like :param1 )";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customers = query.from(Party.class, "party.customerperson").alias("customers");
        query.select(customers);
        query.where(cb.like(customers.get("name"), "Foo%"));
        checkQuery(query, expected, "party.customerperson", "Foo%");
    }

    /**
     * Tests the {@link Expression#isNull()} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExpressionIsNull() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) " +
                          "and ( a.activityStartTime is null )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(acts.get("startTime").isNull());
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link Expression#isNotNull()} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExpressionIsNotNull() throws Exception {
        String expected = "select a from ActDOImpl as a " +
                          "where ( a.archetypeId.shortName=:param0 ) " +
                          "and ( a.activityStartTime is not null )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("a");
        query.select(acts);
        query.where(acts.get("startTime").isNotNull());
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link Expression#in(Object...)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testInObjects() throws Exception {
        String expected = "select customers from PartyDOImpl as customers " +
                          "where ( customers.archetypeId.shortName=:param0 ) " +
                          "and ( customers.id in (:param1, :param2, :param3) )";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customers = query.from(Party.class, "party.customerperson").alias("customers");
        query.select(customers);
        query.where(customers.get("id").in(100, 101, 103));
        checkQuery(query, expected, "party.customerperson", 100L, 101L, 103L);
    }

    /**
     * Tests the {@link CriteriaBuilder#asc(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testOrderAsc() throws Exception {
        String expected = "select customers from PartyDOImpl as customers " +
                          "where customers.archetypeId.shortName=:param0 order by customers.name asc";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customers = query.from(Party.class, "party.customerperson").alias("customers");
        query.select(customers);
        query.orderBy(cb.asc(customers.get("name")));
        checkQuery(query, expected, "party.customerperson");
    }

    /**
     * Tests the {@link CriteriaBuilder#desc(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testOrderDesc() throws Exception {
        String expected = "select customers from PartyDOImpl as customers " +
                          "where customers.archetypeId.shortName=:param0 order by customers.id desc";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customers = query.from(Party.class, "party.customerperson").alias("customers");
        query.select(customers);
        query.orderBy(cb.desc(customers.get("id")));
        checkQuery(query, expected, "party.customerperson");
    }

    /**
     * Test the query by archetype and id.
     */
    @Test
    public void testQueryByArchetypeAndId() throws Exception {
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> root = query.from(Party.class, "party.patientpet").alias("patient");
        query.select(root);
        query.where(cb.equal(root.get("id"), 1001));

        checkQuery(query, "select patient from PartyDOImpl as patient " +
                          "where ( patient.archetypeId.shortName=:param0 ) and ( patient.id=:param1 )",
                   "party.patientpet", 1001L);
    }

    /**
     * Test the query on archetype id, name and a sort criteria.
     */
    @Test
    public void testQueryByArchetypeIdAndNameAndSort() throws Exception {
        String expected = "select customer from PartyDOImpl as customer " +
                          "inner join customer.contacts as location with location.archetypeId.shortName=:param0 " +
                          "where customer.archetypeId.shortName=:param1 order by customer.name asc";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> root = query.from(Party.class, "party.customerperson").alias("customer");
        query.select(root);
        root.join("contacts", "contact.location").alias("location");
        query.orderBy(cb.asc(root.get("name")));
        checkQuery(query, expected, "contact.location", "party.customerperson");
    }

    /**
     * Tests selection of an object reference.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSelectReference() throws Exception {
        String expected = "select new " + IMObjectReference.class.getName() + "(" +
                          "customer.archetypeId, customer.id, customer.linkId) " +
                          "from PartyDOImpl as customer " +
                          "where customer.archetypeId.shortName=:param0 " +
                          "order by customer.id asc";
        CriteriaQuery<Reference> query = cb.createQuery(Reference.class);
        Root<Party> root = query.from(Party.class, "party.customerperson").alias("customer");
        query.select(root.reference());
        query.orderBy(cb.asc(root.get("id")));
        checkQuery(query, expected, "party.customerperson");
    }

    /**
     * Tests selection of an object reference in a join.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSelectReferenceInJoin() throws Exception {
        String expected = "select new " + IMObjectReference.class.getName()
                          + "(patients.target.archetypeId, patients.target.id, patients.target.linkId) " +
                          "from PartyDOImpl as customer " +
                          "inner join customer.sourceEntityRelationships as patients " +
                          "with patients.archetypeId.shortName=:param0 " +
                          "where customer.archetypeId.shortName=:param1 order by customer.id asc";
        CriteriaQuery<Reference> query = cb.createQuery(Reference.class);
        Root<Party> root = query.from(Party.class, "party.customerperson").alias("customer");
        Join<Party, IMObject> relationship = root.join("patients", "entityRelationship.patientOwner").alias("patients");
        query.select(relationship.get("target").alias("patient"));
        query.orderBy(cb.asc(root.get("id")));
        checkQuery(query, expected, "entityRelationship.patientOwner", "party.customerperson");
    }

    /**
     * Tests {@link Join#on(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testJoinOnExpression() throws Exception {
        String expected = "select payments from ActDOImpl as payments " +
                          "inner join payments.participations as p " +
                          "with p.archetypeId.shortName=:param0 " +
                          "inner join p.entity as customer " +
                          "with ( customer.archetypeId.shortName=:param1 ) and ( customer.id=:param2 ) " +
                          "where payments.archetypeId.shortName=:param3 " +
                          "order by payments.activityStartTime asc, payments.id asc";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> payments = query.from(Act.class, "act.customerAccountPayment").alias("payments");
        Join<IMObject, IMObject> customer = payments.join("participants").alias("p").join("entity").alias("customer");
        customer.on(cb.equal(customer.get("id"), 1002));
        query.orderBy(cb.asc(payments.get("startTime")), cb.asc(payments.get("id")));
        checkQuery(query, expected, "participation.customer", "party.customerperson", 1002L,
                   "act.customerAccountPayment");
    }

    /**
     * Tests {@link Join#on(Expression)} method where the predicate is a reference.
     *
     * @throws Exception for any error
     */
    @Test
    public void testJoinOnReference() throws Exception {
        String expected = "select payments from ActDOImpl as payments " +
                          "inner join payments.participations as p " +
                          "with ( p.archetypeId.shortName=:param0 ) " +
                          "and ( ( p.entity.id=:param1 ) and ( p.entity.archetypeId.shortName=:param2 ) ) " +
                          "where payments.archetypeId.shortName=:param3 " +
                          "order by payments.activityStartTime asc, payments.id asc";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> payments = query.from(Act.class, "act.customerAccountPayment").alias("payments");
        Join<Act, IMObject> participants = payments.join("participants").alias("p");
        participants.on(cb.equal(participants.get("entity"), new IMObjectReference("party.customerperson", 1002)));
        query.orderBy(cb.asc(payments.get("startTime")), cb.asc(payments.get("id")));
        checkQuery(query, expected, "participation.customer", 1002L, "party.customerperson",
                   "act.customerAccountPayment");
    }

    /**
     * Tests {@link Join#on(Expression)} method where the predicate is comparing a reference with an id.
     *
     * @throws Exception for any error
     */
    @Test
    public void testJoinOnReferenceWithId() throws Exception {
        String expected = "select payments from ActDOImpl as payments " +
                          "inner join payments.participations as p " +
                          "with ( p.archetypeId.shortName=:param0 ) " +
                          "and ( p.entity.id=:param1 ) " +
                          "where payments.archetypeId.shortName=:param2 " +
                          "order by payments.activityStartTime asc, payments.id asc";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> payments = query.from(Act.class, "act.customerAccountPayment").alias("payments");
        Join<Act, IMObject> participants = payments.join("participants").alias("p");
        participants.on(cb.equal(participants.get("entity"), 1002L));
        query.orderBy(cb.asc(payments.get("startTime")), cb.asc(payments.get("id")));
        checkQuery(query, expected, "participation.customer", 1002L, "act.customerAccountPayment");
    }

    /**
     * Tests {@link From#join(String)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testJoin() throws Exception {
        String expected = "select acts from ActDOImpl as acts inner join acts.participations as p " +
                          "with p.archetypeId.shortName=:param0 " +
                          "where acts.archetypeId.shortName=:param1";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("acts");
        acts.join("participations").alias("p");
        checkQuery(query, expected, "participation.simple", "act.simple");
    }

    /**
     * Tests {@link From#join(String, String)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testJoinWithArchetype() throws Exception {
        String expected = "select pets from PartyDOImpl as pets " +
                          "inner join pets.targetEntityRelationships as owner " +
                          "with owner.archetypeId.shortName=:param0 " +
                          "where pets.archetypeId.shortName=:param1";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> pets = query.from(Party.class, "party.patientpet").alias("pets");
        pets.join("customers", "entityRelationship.patientOwner").alias("owner");
        checkQuery(query, expected, "entityRelationship.patientOwner", "party.patientpet");
    }

    /**
     * Tests {@link From#leftJoin(String)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLeftJoin() throws Exception {
        String expected = "select acts from ActDOImpl as acts left join acts.participations as p " +
                          "with p.archetypeId.shortName=:param0 " +
                          "where ( acts.archetypeId.shortName=:param1 ) and ( p.entity is null )";
        CriteriaQuery<Act> query = cb.createQuery(Act.class);
        Root<Act> acts = query.from(Act.class, "act.simple").alias("acts");
        Join<Act, IMObject> participations = acts.leftJoin("participations").alias("p");
        query.where(participations.get("entity").isNull());
        checkQuery(query, expected, "participation.simple", "act.simple");
    }

    /**
     * Tests {@link From#leftJoin(String, String)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLeftJoinWithArchetype() throws Exception {
        String expected = "select pets from PartyDOImpl as pets left join pets.targetEntityRelationships as owner " +
                          "with owner.archetypeId.shortName=:param0 where ( pets.archetypeId.shortName=:param1 ) " +
                          "and ( owner.target is null )";
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> pets = query.from(Party.class, "party.patientpet").alias("pets");
        Join<Party, IMObject> owner = pets.leftJoin("customers", "entityRelationship.patientOwner").alias("owner");
        query.where(owner.get("target").isNull());
        checkQuery(query, expected, "entityRelationship.patientOwner", "party.patientpet");
    }

    /**
     * Tests the {@link CriteriaBuilder#count(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCount() throws Exception {
        String expected = "select count(pets) from PartyDOImpl as pets where pets.archetypeId.shortName=:param0";
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        query.select(cb.count(query.from(Party.class, "party.patientpet").alias("pets")));
        checkQuery(query, expected, "party.patientpet");
    }

    /**
     * Tests the {@link CriteriaBuilder#countDistinct(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCountDistinct() throws Exception {
        String expected = "select count(distinct pets) from PartyDOImpl as pets " +
                          "where pets.archetypeId.shortName=:param0";
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        query.select(cb.countDistinct(query.from(Party.class, "party.patientpet").alias("pets")));
        checkQuery(query, expected, "party.patientpet");
    }

    /**
     * Tests the {@link CriteriaQuery#multiselect(Selection[])} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMultiSelect() throws Exception {
        String expected = "select new " + Info.class.getName() + "(pets.id, pets.name, pets.active) " +
                          "from PartyDOImpl as pets " +
                          "where pets.archetypeId.shortName=:param0";
        CriteriaQuery<Info> query = cb.createQuery(Info.class);
        Root<Party> pets = query.from(Party.class, "party.patientpet").alias("pets");
        query.multiselect(pets.get("id"), pets.get("name"), pets.get("active"));
        checkQuery(query, expected, "party.patientpet");
    }

    /**
     * Tests the {@link CriteriaQuery#multiselect(Selection[])} for tuples.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMultiSelectTuple() throws Exception {
        String expected = "select acts.id, acts.reason " +
                          "from ActDOImpl as acts " +
                          "where acts.archetypeId.shortName=:param0";
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Act> acts = query.from(Act.class, "act.simple").alias("acts");
        query.multiselect(acts.get("id"), acts.get("reason"));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Verifies that details nodes can be selected.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSelectDetailsNode() throws Exception {
        String expected = "select species.value, breed.value " +
                          "from PartyDOImpl as pets inner join pets.details as species with key(species)=:param0 " +
                          "inner join pets.details as breed with key(breed)=:param1 " +
                          "where pets.archetypeId.shortName=:param2";
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Party> pets = query.from(Party.class, "party.patientpet").alias("pets");
        query.multiselect(pets.get("species").alias("species"), pets.get("breed").alias("breed"));
        checkQuery(query, expected, "species", "breed", "party.patientpet");
    }

    /**
     * Tests subquery support.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSubquery() throws Exception {
        String expected = "select customer from PartyDOImpl as customer " +
                          "where ( customer.archetypeId.shortName=:param0 ) " +
                          "and ( (select count(pet.id) from PartyDOImpl as pet " +
                          "inner join pet.targetEntityRelationships as owners " +
                          "with owners.archetypeId.shortName=:param1 inner join owners.source as owner " +
                          "with owner.archetypeId.shortName=:param2 where ( pet.archetypeId.shortName=:param3 ) " +
                          "and ( customer.id=owner.id ))>=:param4 )";

        // generate a query to return all customers that have 2 or more patients
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customer = query.from(Party.class, "party.customerperson").alias("customer");

        // count patients
        Subquery<Long> subquery = query.subquery(Long.class);
        Root subRoot = subquery.from(Party.class, "party.patientpet").alias("pet");
        Join subCustomers = subRoot.join("customers", "entityRelationship.patientOwner").alias("owners");
        subquery.select(cb.count(subRoot.get("id")));
        subquery.where(cb.equal(customer.get("id"), subCustomers.join("source").alias("owner").get("id")));

        // limit subquery to >= 2 patients
        query.where(cb.greaterThanOrEqualTo(subquery, 2L));
        checkQuery(query, expected, "party.customerperson", "entityRelationship.patientOwner", "party.customerperson",
                   "party.patientpet", 2L);
    }

    /**
     * Tests the {@link CriteriaBuilder#exists(Subquery)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExists() throws Exception {
        String expected = "select customer from PartyDOImpl as customer " +
                          "where ( customer.archetypeId.shortName=:param0 ) and " +
                          "( exists (select pet.id from PartyDOImpl as pet " +
                          "inner join pet.targetEntityRelationships as owners " +
                          "with owners.archetypeId.shortName=:param1 " +
                          "inner join owners.source as owner with owner.archetypeId.shortName=:param2 " +
                          "where ( pet.archetypeId.shortName=:param3 ) and ( customer.id=owner.id )) )";

        // generate a query to return all customers that have patients
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customer = query.from(Party.class, "party.customerperson").alias("customer");

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Party> subRoot = subquery.from(Party.class, "party.patientpet").alias("pet");
        subquery.select(subRoot.get("id"));
        Join subCustomers = subRoot.join("customers", "entityRelationship.patientOwner").alias("owners");
        subquery.where(cb.equal(customer.get("id"), subCustomers.join("source").alias("owner").get("id")));

        query.where(cb.exists(subquery));
        checkQuery(query, expected, "party.customerperson", "entityRelationship.patientOwner", "party.customerperson",
                   "party.patientpet");
    }

    /**
     * Tests the {@link CriteriaQuery#groupBy(List)} and {@link CriteriaQuery#having(Expression)}.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGroupBy() throws Exception {
        String expected = "select customer from PartyDOImpl as customer " +
                          "inner join customer.sourceEntityRelationships as patients " +
                          "with patients.archetypeId.shortName=:param0 where customer.archetypeId.shortName=:param1 " +
                          "group by customer having count(patients)>:param2";

        // generate a query to return all customers that have more than one patient
        CriteriaQuery<Party> query = cb.createQuery(Party.class);
        Root<Party> customer = query.from(Party.class, "party.customerperson").alias("customer");
        query.select(customer);
        Join<Party, IMObject> patients = customer.join("patients", "entityRelationship.patientOwner").alias("patients");
        query.groupBy(customer);
        query.having(cb.greaterThan(cb.count(patients), 1L));

        checkQuery(query, expected, "entityRelationship.patientOwner", "party.customerperson", 1L);
    }

    /**
     * Tests the {@link CriteriaBuilder#sum(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSum() throws Exception {
        String expected = "select sum(act.total) from FinancialActDOImpl as act " +
                          "where act.archetypeId.shortName=:param0";

        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<Act> act = query.from(Act.class, "financial.act").alias("act");
        query.select(cb.sum(act.get("total")));
        checkQuery(query, expected, "financial.act");
    }

    /**
     * Tests the {@link CriteriaBuilder#max(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMax() throws Exception {
        String expected = "select max(act.total) from FinancialActDOImpl as act " +
                          "where act.archetypeId.shortName=:param0";

        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<Act> act = query.from(Act.class, "financial.act").alias("act");
        query.select(cb.max(act.get("total")));
        checkQuery(query, expected, "financial.act");
    }

    /**
     * Tests the {@link CriteriaBuilder#max(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMin() throws Exception {
        String expected = "select min(act.total) from FinancialActDOImpl as act " +
                          "where act.archetypeId.shortName=:param0";

        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<Act> act = query.from(Act.class, "financial.act").alias("act");
        query.select(cb.min(act.get("total")));
        checkQuery(query, expected, "financial.act");
    }

    /**
     * Tests the {@link CriteriaBuilder#greatest(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGreatest() throws Exception {
        String expected = "select max(act.activityStartTime) from ActDOImpl as act " +
                          "where act.archetypeId.shortName=:param0";

        CriteriaQuery<Date> query = cb.createQuery(Date.class);
        Root<Act> act = query.from(Act.class, "act.simple").alias("act");
        query.select(cb.greatest(act.get("startTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Tests the {@link CriteriaBuilder#least(Expression)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLeast() throws Exception {
        String expected = "select min(act.activityStartTime) from ActDOImpl as act " +
                          "where act.archetypeId.shortName=:param0";

        CriteriaQuery<Date> query = cb.createQuery(Date.class);
        Root<Act> act = query.from(Act.class, "act.simple").alias("act");
        query.select(cb.least(act.get("startTime")));
        checkQuery(query, expected, "act.simple");
    }

    /**
     * Verifies that building a query produces the expected results.
     * <p>
     * NOTE: this is a brittle as it uses reflection to gain access to the generated parameters.
     *
     * @param query    the criteria query
     * @param expected the expected JQL query
     * @param params   the expected parameters
     * @throws Exception for any errors
     */
    private <X> void checkQuery(CriteriaQuery<X> query, String expected, Object... params) throws Exception {
        MappedCriteriaQueryFactory builder = new MappedCriteriaQueryFactory(sessionFactory.getCriteriaBuilder(),
                                                                            assembler);
        MappedCriteriaQuery<X> criteriaQuery = builder.createCriteriaQuery((CriteriaQueryImpl<X>) query);
        javax.persistence.TypedQuery<?> typedQuery = session.createQuery(criteriaQuery.getQuery());
        assertTrue(typedQuery instanceof CriteriaQueryTypeQueryAdapter);
        CriteriaQueryTypeQueryAdapter adapter = (CriteriaQueryTypeQueryAdapter) typedQuery;
        String queryString = adapter.getQueryString();
        assertEquals(expected, queryString);

        Field queryField = FieldUtils.getDeclaredField(adapter.getClass(), "jpqlQuery", true);
        QueryImplementor<?> implementor = (QueryImplementor<?>) queryField.get(adapter);
        Field bindingField = FieldUtils.getDeclaredField(implementor.getClass(), "queryParameterBindings", true);
        QueryParameterBindings bindings = (QueryParameterBindings) bindingField.get(implementor);

        List<QueryParameter> parameters = new ArrayList<>(adapter.getParameterMetadata().getNamedParameters());

        if (!parameters.isEmpty()) {
            // order the parameters on increasing id
            Collections.sort(parameters, new Comparator<QueryParameter>() {
                @Override
                public int compare(QueryParameter o1, QueryParameter o2) {
                    return getId(o1.getName()) - getId(o2.getName());
                }

                int getId(String name) {
                    String id = name.replaceAll("\\D", "");
                    return id.isEmpty() ? 0 : Integer.parseInt(id);
                }
            });
        }

        // verify parameters match that expected
        List<Object> actual = new ArrayList<>();
        for (QueryParameter parameter : parameters) {
            QueryParameterBinding binding = bindings.getBinding(parameter);
            actual.add(binding.getBindValue());
        }
        assertEquals(Arrays.asList(params), actual);
    }
}
