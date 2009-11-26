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
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
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
     * Holds a reference to the entity service
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
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeId("party.person.1.0"), false)
                .add(new NodeConstraint("id", "1"));
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
                .add(new NodeConstraint("id", "1"));
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
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeId("party.person.1.0"), false)
                .add(new NodeConstraint("name", "sa*"));
        checkQuery(query, expected);
    }

    /**
     * Test the query on archetype id, name and a sort criteria.
     */
    public void testQueryByArchetypeIdAndNameAndSort() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "inner join party0.contacts as contacts0 "
                                + "with (contacts0.archetypeId.shortName = :shortName1) "
                                + "where (party0.archetypeId.shortName = :shortName0 and "
                                + "party0.name like :name0) "
                                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeId("party.person.1.0"), false)
                .add(new NodeConstraint("name", "sa*"))
                .add(new NodeSortConstraint("name", true))
                .add(new CollectionNodeConstraint("contacts",
                                                  new ShortNameConstraint(
                                                          "contact.location",
                                                          false, false)));
        checkQuery(query, expected);
    }

    /**
     * Test the query on archetype short name, name and a sort criteria
     */
    public void testQueryByArchetypeShortNameAndNameAndSort() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "inner join party0.contacts as contacts0 "
                                + "with (contacts0.archetypeId.shortName = :shortName2) "
                                + "where ((party0.archetypeId.shortName = :shortName0 or "
                                + "party0.archetypeId.shortName = :shortName1) and "
                                + "party0.id = :id0 and party0.name like :name0) "
                                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint(
                        new String[]{"party.person", "organization.organization"},
                        false, false))
                .add(new NodeConstraint("id", "1"))
                .add(new NodeConstraint("name", "sa*"))
                .add(new NodeSortConstraint("name", true))
                .add(new CollectionNodeConstraint("contacts",
                                                  new ShortNameConstraint(
                                                          "contact.location",
                                                          false, false)));
        checkQuery(query, expected);
    }

    /**
     * Test the query on a collection constraint without specifying
     * archetype constraint info.
     */
    public void testCollectionNodeConstraintWithNodeNameOnly() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "inner join party0.contacts as contacts0 "
                                + "with (contacts0.archetypeId.shortName like :shortName2) "
                                + "where ((party0.archetypeId.shortName = :shortName0 or "
                                + "party0.archetypeId.shortName = :shortName1) and "
                                + "party0.id = :id0 and party0.name like :name0) "
                                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint(
                        new String[]{"party.person",
                                     "organization.organization"},
                        false, false))
                .add(new NodeConstraint("id", "1"))
                .add(new NodeConstraint("name", "sa*"))
                .add(new NodeSortConstraint("name", true))
                .add(new CollectionNodeConstraint(
                        "contacts", new ShortNameConstraint("contact.*", false, false)));
        checkQuery(query, expected);
    }

    /**
     * Test query with an archetype and node based sort constraint.
     */
    public void testWithMultipleSortConstraints() {
        String expected = "select party0 "
                          + "from " + PartyDO.class.getName() + " as party0 "
                          + "inner join party0.contacts as contacts0 "
                          + "with (contacts0.archetypeId.shortName like :shortName2) "
                          + "where ((party0.archetypeId.shortName = :shortName0 or "
                          + "party0.archetypeId.shortName = :shortName1) and "
                          + "party0.id = :id0 and party0.name like :name0) "
                          + "order by party0.name asc, party0.archetypeId.shortName asc, "
                          + "contacts0.archetypeId.shortName desc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint(
                        new String[]{"party.person",
                                     "organization.organization"},
                        false, false))
                .add(new NodeConstraint("id", "1"))
                .add(new NodeConstraint("name", "sa*"))
                .add(new NodeSortConstraint("name", true))
                .add(new ArchetypeSortConstraint(true))
                .add(new CollectionNodeConstraint(
                        "contacts",
                        new ShortNameConstraint("contact.*", false,
                                                false))
                        .add(new ArchetypeSortConstraint(false)));
        checkQuery(query, expected);
    }

    /**
     * Test target lookups.
     */
    public void testTargetLookups() {
        final String expected = "select lookup0 "
                                + "from " + LookupDO.class.getName() + " as lookup0 "
                                + "inner join lookup0.targetLookupRelationships "
                                + "as targetLookupRelationships0 "
                                + "with (targetLookupRelationships0.archetypeId.shortName = :shortName1) "
                                + "where (lookup0.archetypeId.shortName = :shortName0) "
                                + "order by lookup0.name asc";
        ArchetypeQuery query = new ArchetypeQuery("lookup", "country",
                                                  false, false)
                .add(new CollectionNodeConstraint(
                        "target", "lookupRelationship.countryState",
                        false, false))
                .add(new NodeSortConstraint("name", true))
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
                                + "inner join product0.classifications as classifications0 "
                                + "with (classifications0.archetypeId.shortName = :shortName1 and "
                                + "(classifications0.code = :code0 or classifications0.code = :code1)) "
                                + "where (product0.archetypeId.shortName = :shortName0 and "
                                + "product0.active = :active0)";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint("product.product", false,
                                        true))
                .add(new CollectionNodeConstraint(
                        "classifications",
                        new ShortNameConstraint("lookup.staff",
                                                false, false))
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
                                + "left outer join product0.classifications as classifications0 "
                                + "with (classifications0.archetypeId.shortName = :shortName1 "
                                + "and classifications0.name = :name0)"
                                + " where (product0.archetypeId.shortName = :shortName0)";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint("product.product", false,
                                        false))
                .add(new CollectionNodeConstraint("classifications", false)
                        .setJoinType(JoinConstraint.JoinType.LeftOuterJoin)
                        .add(new ArchetypeNodeConstraint(RelationalOp.EQ,
                                                         "lookup.species"))
                        .add(new NodeConstraint("name", RelationalOp.EQ,
                                                "Canine")));
        checkQuery(query, expected);
    }

    /**
     * Test query across multiple tables, joined by link constraints.
     */
    public void testQueryAcrossMultipleTables() {
        final String expected = "select distinct act "
                                + "from " + ActDO.class.getName() + " as act "
                                + "inner join act.participations as participation "
                                + "with (participation.archetypeId.shortName = :shortName1 and "
                                + "participation.active = :active1) "
                                + "inner join " + EntityRelationshipDO.class.getName() + " as owner "
                                + "with (owner.archetypeId.shortName = :shortName2 and owner.active = :active2) "
                                + "inner join " + PartyDO.class.getName() + " as patient "
                                + "with (patient.archetypeId.shortName = :shortName3 and "
                                + "patient.active = :active3) "
                                + "inner join " + PartyDO.class.getName() + " as customer "
                                + "with (customer.archetypeId.shortName like :shortName4 and "
                                + "customer.active = :active4) "
                                + "where (act.archetypeId.shortName = :shortName0 and "
                                + "act.active = :active0 and act.status = :status0 and "
                                + "act.id = participation.act.id and "
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

        query.add(new NodeConstraint("status", "IN_PROGRESS"));
        query.add(new CollectionNodeConstraint("patient", participation));
        query.add(new IdConstraint("act", "participation.act"));
        query.add(owner);
        query.add(patient);
        query.add(customer);
        query.add(new IdConstraint("participation.entity", "patient"));
        query.add(new IdConstraint("patient", "owner.target"));
        query.add(new IdConstraint("customer", "owner.source"));
        query.add(new NodeSortConstraint("customer", "name"));
        query.add(new NodeSortConstraint("patient", "name"));

        checkQuery(query, expected);
    }

    /**
     * Tests select constraints.
     */
    public void testMultipleSelect() {
        final String expected = "select estimation.name, estimation.description, estimation.status, "
                                + "estimationItem from " + ActDO.class.getName() + " as estimation "
                                + "inner join estimation.sourceActRelationships as items "
                                + "with (items.archetypeId.shortName = :shortName1 and items.active = :active1) "
                                + "inner join " + ActDO.class.getName() + " as estimationItem "
                                + "with (estimationItem.archetypeId.shortName = :shortName2 and "
                                + "estimationItem.active = :active2) "
                                + "where (estimation.archetypeId.shortName = :shortName0 "
                                + "and estimation.active = :active0 and items.source.id = estimation.id "
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
                                + "inner join act0.participations as participations0 "
                                + "with ((participations0.entity.id = :id0 or "
                                + "participations0.entity.archetypeId.shortName = :shortName1)) "
                                + "where (act0.archetypeId.shortName = :shortName0)";

        // create a query that returns all customer estimations for a particular
        // customer or that has an author.
        ObjectRefNodeConstraint customer = new ObjectRefNodeConstraint(
                "entity", new IMObjectReference(
                        new ArchetypeId("participation.customer"), 12345));
        ObjectRefNodeConstraint author = new ObjectRefNodeConstraint(
                "entity", new ArchetypeId("participation.author"));
        ArchetypeQuery query
                = new ArchetypeQuery("act.customerEstimation",
                                     false, false);
        query.add(new CollectionNodeConstraint("participants")
                .add(new OrConstraint().add(customer).add(author)));

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
                                + "left outer join product0.classifications as c "
                                + "with (c.archetypeId.shortName = :shortName1) "
                                + "where (product0.archetypeId.shortName = :shortName0 "
                                + "and (c.code = :code0 or c.code is NULL))";

        // test production when ArchetypeId is used to specify the collection
        // archetype
        ArchetypeQuery query1 = new ArchetypeQuery("product.product", false, false)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(new CollectionNodeConstraint("c.classifications")
                        .setJoinType(JoinConstraint.JoinType.LeftOuterJoin)
                        .add(new ArchetypeNodeConstraint(RelationalOp.EQ, "lookup.species")))
                .add(new OrConstraint()
                        .add(new NodeConstraint("c.code", RelationalOp.EQ, "Canine"))
                        .add(new NodeConstraint("c.code", RelationalOp.IS_NULL)));
        checkQuery(query1, expected);

        // test production when ShortNameConstraint is used to specify the
        // collection archetype
        ArchetypeQuery query2 = new ArchetypeQuery("product.product", false, false)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(new CollectionNodeConstraint("c.classifications",
                                                  new ShortNameConstraint("lookup.species", false))
                        .setJoinType(JoinConstraint.JoinType.LeftOuterJoin))
                .add(new OrConstraint()
                        .add(new NodeConstraint("c.code", RelationalOp.EQ, "Canine"))
                        .add(new NodeConstraint("c.code", RelationalOp.IS_NULL)));
        checkQuery(query2, expected);
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
    }

}
