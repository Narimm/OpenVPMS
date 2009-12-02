/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: QueryBuilderTestCase.java 2915 2008-07-28 01:41:16Z tanderson $
 */

package org.openvpms.component.business.dao.hibernate.im.query;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openvpms.component.business.dao.hibernate.im.AssemblerImpl;
import org.openvpms.component.business.dao.hibernate.im.act.ActDO;
import org.openvpms.component.business.dao.hibernate.im.act.DocumentActDO;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityRelationshipDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.dao.hibernate.im.party.PartyDO;
import org.openvpms.component.business.dao.hibernate.im.product.ProductDO;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.system.common.query.ArchetypeNodeConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSelectConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * Tests the {@link QueryBuilder} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-07-28 01:41:16Z $
 */
public class QueryBuilderTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The query builder.
     */
    private QueryBuilder builder;


    /**
     * Test the query by id and archetype Id.
     */
    public void testQueryByIdAndArchetypeId() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "where (party0.archetypeId.shortName = :shortName0 and "
                                + "party0.id = :id0)";
        ArchetypeQuery query = new ArchetypeQuery(new ArchetypeId("party.person.1.0"), false)
                .add(Constraints.eq("id", "1"));
        checkQuery(query, expected);
    }

    /**
     * Test the query by id and archetype short name.
     */
    public void testQueryByIdAndArchetypeShortName() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "where (party0.archetypeId.shortName = :shortName0 and "
                                + "party0.id = :id0)";
        ArchetypeQuery query = new ArchetypeQuery("party.person", false, false)
                .add(Constraints.eq("id", "1"));
        checkQuery(query, expected);
    }

    /**
     * Test the query by archetype id and name.
     */
    public void testQueryByArchetypeIdAndName() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "where (party0.archetypeId.shortName = :shortName0 and "
                                + "party0.name like :name0)";
        ArchetypeQuery query = new ArchetypeQuery(new ArchetypeId("party.person.1.0"), false)
                .add(Constraints.eq("name", "sa*"));
        checkQuery(query, expected);
    }

    /**
     * Test the query on archetype id, name and a sort criteria.
     */
    public void testQueryByArchetypeIdAndNameAndSort() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "inner join party0.contacts as contacts "
                                + "where (party0.archetypeId.shortName = :shortName0 and "
                                + "party0.name like :name0 and contacts.archetypeId.shortName = :shortName1) "
                                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeId("party.person.1.0"), false)
                .add(Constraints.eq("name", "sa*"))
                .add(Constraints.sort("name", true))
                .add(Constraints.join("contacts", Constraints.shortName("contact.location")));
        checkQuery(query, expected);
    }

    /**
     * Test the query on archetype short name, name and a sort criteria
     */
    public void testQueryByArchetypeShortNameAndNameAndSort() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "inner join party0.contacts as contacts "
                                + "where ((party0.archetypeId.shortName = :shortName0 or "
                                + "party0.archetypeId.shortName = :shortName1) and "
                                + "party0.id = :id0 and party0.name like :name0 and "
                                + "contacts.archetypeId.shortName = :shortName2) "
                                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                Constraints.shortName(new String[]{"party.person", "organization.organization"}))
                .add(Constraints.eq("id", "1"))
                .add(Constraints.eq("name", "sa*"))
                .add(Constraints.sort("name", true))
                .add(Constraints.join("contacts", new ShortNameConstraint("contact.location", false, false)));
        checkQuery(query, expected);
    }

    /**
     * Test the query on a collection constraint without specifying
     * archetype constraint info.
     */
    public void testCollectionNodeConstraintWithNodeNameOnly() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "inner join party0.contacts as contacts "
                                + "where ((party0.archetypeId.shortName = :shortName0 or "
                                + "party0.archetypeId.shortName = :shortName1) and "
                                + "party0.id = :id0 and party0.name like :name0 and "
                                + "contacts.archetypeId.shortName like :shortName2) "
                                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                Constraints.shortName(new String[]{"party.person", "organization.organization"}))
                .add(Constraints.eq("id", "1"))
                .add(Constraints.eq("name", "sa*"))
                .add(Constraints.sort("name", true))
                .add(Constraints.join("contacts", new ShortNameConstraint("contact.*", false, false)));
        checkQuery(query, expected);
    }

    /**
     * Test query with an archetype and node based sort constraint.
     */
    public void testWithMultipleSortConstraints() {
        String expected = "select party0 "
                          + "from " + PartyDO.class.getName() + " as party0 "
                          + "inner join party0.contacts as contacts "
                          + "where ((party0.archetypeId.shortName = :shortName0 or "
                          + "party0.archetypeId.shortName = :shortName1) and "
                          + "party0.id = :id0 and party0.name like :name0 and "
                          + "(contacts.archetypeId.shortName like :shortName2)) "
                          + "order by party0.name asc, party0.archetypeId.shortName asc, "
                          + "contacts.archetypeId.shortName desc";
        ArchetypeQuery query = new ArchetypeQuery(
                Constraints.shortName(new String[]{"party.person", "organization.organization"}))
                .add(Constraints.eq("id", "1"))
                .add(Constraints.eq("name", "sa*"))
                .add(Constraints.sort("name", true))
                .add(new ArchetypeSortConstraint(true))
                .add(Constraints.join("contacts", Constraints.shortName("contact.*"))
                        .add(new ArchetypeSortConstraint(false)));
        checkQuery(query, expected);
    }

    /**
     * Test target lookups.
     */
    public void testTargetLookups() {
        final String expected = "select lookup0 "
                                + "from " + LookupDO.class.getName() + " as lookup0 "
                                + "inner join lookup0.targetLookupRelationships as target "
                                + "where (lookup0.archetypeId.shortName = :shortName0 and "
                                + "target.archetypeId.shortName = :shortName1) "
                                + "order by lookup0.name asc";
        ArchetypeQuery query = new ArchetypeQuery("lookup", "country",
                                                  false, false)
                .add(Constraints.join("target", Constraints.shortName("lookupRelationship.countryState")))
                .add(Constraints.sort("name"))
                .setFirstResult(0)
                .setMaxResults(-1);

        checkQuery(query, expected);
    }

    /**
     * Test for ovpms-240.
     */
    public void testOVPMS240() {
        final String expected = "select product0 "
                                + "from " + ProductDO.class.getName() + " "
                                + "as product0 "
                                + "inner join product0.classifications as classifications "
                                + "where (product0.archetypeId.shortName = :shortName0 and "
                                + "product0.active = :active0 and "
                                + "(classifications.archetypeId.shortName = :shortName1 and "
                                + "(classifications.code = :code0 or classifications.code = :code1)))";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint("product.product", false, true))
                .add(new CollectionNodeConstraint("classifications",
                                                  new ShortNameConstraint("lookup.staff", false, false))
                        .add(new OrConstraint()
                        .add(new NodeConstraint("code", "equine"))
                        .add(new NodeConstraint("code", "all"))));
        checkQuery(query, expected);
    }

    /**
     * Test for ovpms-245.
     */
    public void testOVPMS245() {
        final String expected = "select product0 "
                                + "from " + ProductDO.class.getName() + " as product0 "
                                + "left outer join product0.classifications as classifications "
                                + "with classifications.archetypeId.shortName = :shortName1 "
                                + "where (product0.archetypeId.shortName = :shortName0 and "
                                + "(classifications.code = :code0 or classifications.code is NULL))";
        ArchetypeQuery query = new ArchetypeQuery("product.product", false, false)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(new CollectionNodeConstraint("classifications")
                        .setJoinType(JoinConstraint.JoinType.LeftOuterJoin)
                        .add(new ArchetypeNodeConstraint(RelationalOp.EQ, "lookup.species")))
                .add(new OrConstraint()
                        .add(new NodeConstraint("classifications.code", RelationalOp.EQ, "CANINE"))
                        .add(new NodeConstraint("classifications.code", RelationalOp.IS_NULL)));
        checkQuery(query, expected);

        ArchetypeQuery query2 = new ArchetypeQuery("product.product", false, false)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(new CollectionNodeConstraint("classifications")
                        .setJoinType(JoinConstraint.JoinType.LeftOuterJoin))
                .add(new OrConstraint()
                        .add(new NodeConstraint("classifications.code", RelationalOp.EQ, "CANINE"))
                        .add(new NodeConstraint("classifications.code", RelationalOp.IS_NULL)));
        checkQuery(query2, expected);
    }

    /**
     * Test query across multiple tables, joined by link constraints.
     */
    public void testQueryAcrossMultipleTables() {
        final String expected = "select distinct act "
                                + "from " + ActDO.class.getName() + " as act "
                                + "inner join act.participations as participation, "
                                + EntityRelationshipDO.class.getName() + " as owner, "
                                + PartyDO.class.getName() + " as patient, "
                                + PartyDO.class.getName() + " as customer "
                                + "where (act.archetypeId.shortName = :shortName0 and "
                                + "act.active = :active0 and act.status = :status0 and "
                                + "(participation.archetypeId.shortName = :shortName1 and "
                                + "participation.active = :active1) and "
                                + "act.id = participation.act.id and "
                                + "(owner.archetypeId.shortName = :shortName2) and "
                                + "(patient.archetypeId.shortName = :shortName3 and patient.active = :active2) and "
                                + "(customer.archetypeId.shortName like :shortName4 and "
                                + "customer.active = :active3) and "
                                + "participation.entity.id = patient.id and "
                                + "patient.id = owner.target.id and "
                                + "customer.id = owner.source.id) "
                                + "order by customer.name asc, patient.name asc";

        ShortNameConstraint act = new ShortNameConstraint("act",
                                                          "act.patientReminder",
                                                          true);
        ShortNameConstraint participation = new ShortNameConstraint(
                "participation", "participation.patient", true);
        ShortNameConstraint owner = new ShortNameConstraint(
                "owner", "entityRelationship.patientOwner", true);
        ShortNameConstraint patient = new ShortNameConstraint(
                "patient", "party.patientpet", true);
        ShortNameConstraint customer = new ShortNameConstraint(
                "customer", "party.customer*", true);

        ArchetypeQuery query = new ArchetypeQuery(act);
        query.setDistinct(true);

        query.add(Constraints.eq("status", "IN_PROGRESS"));
        query.add(Constraints.join("patient", participation));
        query.add(new IdConstraint("act", "participation.act"));
        query.add(owner);
        query.add(patient);
        query.add(customer);
        query.add(new IdConstraint("participation.entity", "patient"));
        query.add(new IdConstraint("patient", "owner.target"));
        query.add(new IdConstraint("customer", "owner.source"));
        query.add(Constraints.sort("customer", "name"));
        query.add(Constraints.sort("patient", "name"));

        checkQuery(query, expected);
    }

    /**
     * Tests select constraints.
     */
    public void testMultipleSelect() {
        final String expected = "select estimation.name, estimation.description, estimation.status, "
                                + "estimationItem from " + ActDO.class.getName() + " as estimation "
                                + "inner join estimation.sourceActRelationships as items, "
                                + ActDO.class.getName() + " as estimationItem "
                                + "where (estimation.archetypeId.shortName = :shortName0 "
                                + "and estimation.active = :active0 and "
                                + "(items.archetypeId.shortName = :shortName1) and "
                                + "(estimationItem.archetypeId.shortName = :shortName2 and "
                                + "estimationItem.active = :active1) and "
                                + "items.source.id = estimation.id "
                                + "and items.target.id = estimationItem.id)";
        ShortNameConstraint estimation = new ShortNameConstraint(
                "estimation", "act.customerEstimation", false, true);
        ShortNameConstraint estimationItem = new ShortNameConstraint(
                "estimationItem", "act.customerEstimationItem", false, true);
        ShortNameConstraint items = new ShortNameConstraint(
                "items", "actRelationship.customerEstimationItem", false, true);
        ArchetypeQuery query = new ArchetypeQuery(estimation);
        query.add(new NodeSelectConstraint("estimation.name"));
        query.add(new NodeSelectConstraint("estimation.description"));
        query.add(new NodeSelectConstraint("estimation.status"));
        query.add(new ObjectSelectConstraint("estimationItem"));
        query.add(new CollectionNodeConstraint("items", items));
        query.add(estimationItem);
        query.add(new IdConstraint("items.source", "estimation"));
        query.add(new IdConstraint("items.target", "estimationItem"));

        checkQuery(query, expected);
    }

    /**
     * Tests queries on object reference nodes.
     */
    public void testObjectRefNodeConstraints() {
        final String expected = "select act0 from "
                                + ActDO.class.getName() + " as act0 "
                                + "inner join act0.participations as participants "
                                + "where (act0.archetypeId.shortName = :shortName0 and "
                                + "participants.archetypeId.shortName = :shortName1 and "
                                + "(participants.entity.id = :id0 or "
                                + "participants.entity.archetypeId.shortName = :shortName2))";

        // create a query that returns all customer estimations for a particular
        // customer or that has an author.
        IMObjectReference customerRef = new IMObjectReference(new ArchetypeId("participation.customer"), 12345);
        ObjectRefNodeConstraint customer = Constraints.eq("entity", customerRef);
        ObjectRefNodeConstraint author = Constraints.eq("entity", new ArchetypeId("participation.author"));

        ArchetypeQuery query = new ArchetypeQuery("act.customerEstimation", false, false);
        query.add(Constraints.join("participants").add(Constraints.or(customer, author)));
        checkQuery(query, expected);
    }


    /**
     * Tests queries where an {@link IMObjectReference} node is null.
     */
    public void testNullReference() {
        final String expected = "select documentAct0 from "
                                + DocumentActDO.class.getName() + " as documentAct0 "
                                + "where (documentAct0.archetypeId.shortName = :shortName0 and "
                                + "documentAct0.document.id is NULL)";
        ArchetypeQuery query = new ArchetypeQuery("document.act", false, false);
        query.add(new NodeConstraint("document", RelationalOp.IS_NULL));

        checkQuery(query, expected);
    }

    /**
     * Tests IN queries.
     */
    public void testIn() {
        final String expected = "select documentAct0 from "
                                + DocumentActDO.class.getName() + " as documentAct0 "
                                + "where (documentAct0.archetypeId.shortName = :shortName0 and "
                                + "documentAct0.document.id in (:document0, :document1))";
        ArchetypeId id = new ArchetypeId("document.common.1.0");
        Document d1 = new Document(id);
        Document d2 = new Document(id);

        ArchetypeQuery query = new ArchetypeQuery("document.act", false, false);
        query.add(new NodeConstraint("document", RelationalOp.IN,
                                     d1.getObjectReference(),
                                     d2.getObjectReference()));
        checkQuery(query, expected);
    }

    /**
     * Verifies that archetype constraints in left outer joins generate
     * is null clauses.
     */
    public void testLeftJoin() {
        final String expected = "select product0 from "
                                + ProductDO.class.getName() + " as product0 "
                                + "left outer join product0.classifications as classifications "
                                + "with classifications.archetypeId.shortName = :shortName1 "
                                + "where (product0.archetypeId.shortName = :shortName0 "
                                + "and (classifications.code = :code0 or classifications.code is NULL))";

        // test production when ArchetypeId is used to specify the collection
        // archetype
        ArchetypeQuery query1 = new ArchetypeQuery("product.product", false, false)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(Constraints.leftJoin("classifications")
                        .add(new ArchetypeNodeConstraint(RelationalOp.EQ, "lookup.species")))
                .add(Constraints.or(Constraints.eq("classifications.code", "Canine"),
                                    Constraints.isNull("classifications.code")));
        checkQuery(query1, expected);

        // test production when ShortNameConstraint is used to specify the
        // collection archetype
        ArchetypeQuery query2 = new ArchetypeQuery("product.product", false, false)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(Constraints.leftJoin("classifications", Constraints.shortName("lookup.species")))
                .add(Constraints.or(Constraints.eq("classifications.code", "Canine"),
                                    Constraints.isNull("classifications.code")));
        checkQuery(query2, expected);
    }

    public void testQ() {
        final String expected = "select act0 from " + ActDO.class.getName() + " as act0 "
                                + "inner join act0.participations as customer "
                                + "where (act0.archetypeId.shortName = :shortName0 and act0.active = :active0 and "
                                + "customer.archetypeId.shortName = :shortName1 and customer.entity.id = :id0)";
        ArchetypeQuery query = new ArchetypeQuery("act.customerEstimation", false, true);
        CollectionNodeConstraint participant
                = new CollectionNodeConstraint("customer");
        participant.add(new ObjectRefNodeConstraint("entity", new IMObjectReference(
                new ArchetypeId("party.customerperson"), 12345)));
        query.add(participant);
        checkQuery(query, expected);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
    */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/dao/hibernate/im/query/query-appcontext.xml"
        };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        IArchetypeDescriptorCache cache
                = (IArchetypeDescriptorCache) applicationContext.getBean(
                "archetypeDescriptorCache");
        builder = new QueryBuilder(cache, new AssemblerImpl(cache));
    }

    /**
     * Verifies that a query matches that expected, and can be parsed by
     * hibernate.
     *
     * @param query    the query to check
     * @param expected the expected HQL
     * @throws QueryBuilderException if the query is invalid
     * @throws HibernateException    if the query is invalid
     */
    private void checkQuery(ArchetypeQuery query, String expected) {
        String hql = builder.build(query).getQueryString();
        assertEquals(expected, hql);
        SessionFactory factory = (SessionFactory) applicationContext.getBean("sessionFactory");
        Session session = factory.openSession();
        session.createQuery(hql);
        session.close();
    }

}
