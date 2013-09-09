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

package org.openvpms.component.business.dao.hibernate.im.query;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSelectConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.junit.Assert.assertEquals;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.exists;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.leftJoin;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.component.system.common.query.Constraints.shortName;
import static org.openvpms.component.system.common.query.Constraints.subQuery;


/**
 * Tests the {@link QueryBuilder} class.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
@ContextConfiguration("query-appcontext.xml")
public class QueryBuilderTestCase extends AbstractJUnit4SpringContextTests {

    /**
     * The query builder.
     */
    private QueryBuilder builder;


    /**
     * Test the query by id and archetype Id.
     */
    @Test
    public void testQueryByIdAndArchetypeId() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "where (party0.archetypeId.shortName = :shortName0 and "
                                + "party0.id = :id0)";
        ArchetypeQuery query = new ArchetypeQuery(new ArchetypeId("party.person.1.0"), false)
                .add(eq("id", "1"));
        checkQuery(query, expected);
    }

    /**
     * Test the query by id and archetype short name.
     */
    @Test
    public void testQueryByIdAndArchetypeShortName() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "where (party0.archetypeId.shortName = :shortName0 and "
                                + "party0.id = :id0)";
        ArchetypeQuery query = new ArchetypeQuery("party.person", false, false)
                .add(eq("id", "1"));
        checkQuery(query, expected);
    }

    /**
     * Test the query by archetype id and name.
     */
    @Test
    public void testQueryByArchetypeIdAndName() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "where (party0.archetypeId.shortName = :shortName0 and "
                                + "party0.name like :name0)";
        ArchetypeQuery query = new ArchetypeQuery(new ArchetypeId("party.person.1.0"), false)
                .add(eq("name", "sa*"));
        checkQuery(query, expected);
    }

    /**
     * Test the query on archetype id, name and a sort criteria.
     */
    @Test
    public void testQueryByArchetypeIdAndNameAndSort() {
        final String expected = "select party0 "
                                + "from " + PartyDO.class.getName() + " as party0 "
                                + "inner join party0.contacts as contacts "
                                + "where (party0.archetypeId.shortName = :shortName0 and "
                                + "party0.name like :name0 and contacts.archetypeId.shortName = :shortName1) "
                                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeId("party.person.1.0"), false)
                .add(eq("name", "sa*"))
                .add(Constraints.sort("name", true))
                .add(Constraints.join("contacts", shortName("contact.location")));
        checkQuery(query, expected);
    }

    /**
     * Test the query on archetype short name, name and a sort criteria
     */
    @Test
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
                shortName(new String[]{"party.person", "organization.organization"}))
                .add(eq("id", "1"))
                .add(eq("name", "sa*"))
                .add(Constraints.sort("name", true))
                .add(Constraints.join("contacts", new ShortNameConstraint("contact.location", false, false)));
        checkQuery(query, expected);
    }

    /**
     * Test the query on a collection constraint without specifying
     * archetype constraint info.
     */
    @Test
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
                shortName(new String[]{"party.person", "organization.organization"}))
                .add(eq("id", "1"))
                .add(eq("name", "sa*"))
                .add(Constraints.sort("name", true))
                .add(Constraints.join("contacts", new ShortNameConstraint("contact.*", false, false)));
        checkQuery(query, expected);
    }

    /**
     * Test query with an archetype and node based sort constraint.
     */
    @Test
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
                shortName(new String[]{"party.person", "organization.organization"}))
                .add(eq("id", "1"))
                .add(eq("name", "sa*"))
                .add(Constraints.sort("name", true))
                .add(new ArchetypeSortConstraint(true))
                .add(Constraints.join("contacts", shortName("contact.*"))
                             .add(new ArchetypeSortConstraint(false)));
        checkQuery(query, expected);
    }

    /**
     * Test target lookups.
     */
    @Test
    public void testTargetLookups() {
        final String expected = "select lookup0 "
                                + "from " + LookupDO.class.getName() + " as lookup0 "
                                + "inner join lookup0.targetLookupRelationships as target "
                                + "where (lookup0.archetypeId.shortName = :shortName0 and "
                                + "target.archetypeId.shortName = :shortName1) "
                                + "order by lookup0.name asc";
        ArchetypeQuery query = new ArchetypeQuery("lookup", "country",
                                                  false, false)
                .add(Constraints.join("target", shortName("lookupRelationship.countryState")))
                .add(Constraints.sort("name"))
                .setFirstResult(0)
                .setMaxResults(-1);

        checkQuery(query, expected);
    }

    /**
     * Test for ovpms-240.
     */
    @Test
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
     * Test for OVPMS-245.
     */
    @Test
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
    }

    /**
     * Test query across multiple tables, joined by id constraints.
     */
    @Test
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

        query.add(eq("status", "IN_PROGRESS"));
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
     * Test query across multiple tables, joined by id constraints, without specifying the types
     * of the relationships.
     * <p/>
     * This is a simplified version of {@link #testQueryAcrossMultipleTables()}.
     */
    @Test
    public void testQueryAcrossMultipleTablesWithImplicitJoins() {
        final String expected
                = "select distinct act from " + ActDO.class.getName() + " as act "
                  + "inner join act.participations as participation, "
                  + PartyDO.class.getName() + " as customer inner join customer.sourceEntityRelationships as owner, "
                  + PartyDO.class.getName() + " as patient "
                  + "where (act.archetypeId.shortName = :shortName0 and act.active = :active0 "
                  + "and act.status = :status0 and participation.archetypeId.shortName = :shortName1 "
                  + "and (customer.archetypeId.shortName like :shortName2 and customer.active = :active1 "
                  + "and (owner.archetypeId.shortName = :shortName3 or owner.archetypeId.shortName = :shortName4)) "
                  + "and (patient.archetypeId.shortName = :shortName5 and patient.active = :active2) "
                  + "and participation.entity.id = patient.id and patient.id = owner.target.id "
                  + "and customer.id = owner.source.id) "
                  + "order by customer.name asc, patient.name asc";

        ShortNameConstraint act = shortName("act", "act.patientReminder", true);
        ShortNameConstraint patient = shortName("patient", "party.patientpet", true);
        ShortNameConstraint customer = shortName("customer", "party.customer*", true);

        ArchetypeQuery query = new ArchetypeQuery(act);
        query.setDistinct(true);

        query.add(eq("status", "IN_PROGRESS"));
        query.add(Constraints.join("patient", "participation"));
        query.add(customer.add(Constraints.join("patients", "owner")));
        query.add(patient);
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
    @Test
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
        query.add(new NodeSelectConstraint("name"));
        query.add(new NodeSelectConstraint("description"));
        query.add(new NodeSelectConstraint("status"));
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
    @Test
    public void testObjectRefNodeConstraints() {
        final String expected = "select act0 from "
                                + ActDO.class.getName() + " as act0 "
                                + "inner join act0.participations as participants "
                                + "where (act0.archetypeId.shortName = :shortName0 and "
                                + "(participants.archetypeId.shortName = :shortName1 and "
                                + "(participants.entity.id = :id0 or "
                                + "participants.entity.archetypeId.shortName = :shortName2)))";

        // create a query that returns all customer estimations for a particular
        // customer or that has an author.
        IMObjectReference customerRef = new IMObjectReference(new ArchetypeId("participation.customer"), 12345);
        ObjectRefNodeConstraint customer = eq("entity", customerRef);
        ObjectRefNodeConstraint author = eq("entity", new ArchetypeId("participation.author"));

        ArchetypeQuery query = new ArchetypeQuery("act.customerEstimation", false, false);
        query.add(Constraints.join("participants").add(Constraints.or(customer, author)));
        checkQuery(query, expected);
    }


    /**
     * Tests queries where an {@link IMObjectReference} node is null.
     */
    @Test
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
    @Test
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
    @Test
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
                .add(leftJoin("classifications")
                             .add(new ArchetypeNodeConstraint(RelationalOp.EQ, "lookup.species")))
                .add(Constraints.or(eq("classifications.code", "Canine"),
                                    Constraints.isNull("classifications.code")));
        checkQuery(query1, expected);

        // test production when ShortNameConstraint is used to specify the
        // collection archetype
        ArchetypeQuery query2 = new ArchetypeQuery("product.product", false, false)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(leftJoin("classifications", shortName("lookup.species")))
                .add(Constraints.or(eq("classifications.code", "Canine"),
                                    Constraints.isNull("classifications.code")));
        checkQuery(query2, expected);
    }

    /**
     * Verifies that nested left outer joins are supported.
     */
    @Test
    public void testNestedLeftOuterJoin() {
        String expected = "select act0 from "
                          + ActDO.class.getName() + " as act0 "
                          + "left outer join act0.participations as clinician "
                          + "with (clinician.archetypeId.shortName = :shortName1 and clinician.entity.id = :id0) "
                          + "left outer join act0.participations as patient "
                          + "with (patient.archetypeId.shortName = :shortName2) "
                          + "left outer join patient.entity as entity "
                          + "with entity.archetypeId.shortName = :shortName3 "
                          + "where (act0.archetypeId.shortName = :shortName0) order by entity.name asc";
        ArchetypeQuery query = new ArchetypeQuery("act.patientReminder");
        IMObjectReference clinicanRef = new IMObjectReference(new ArchetypeId("security.user"), 12345);
        JoinConstraint clinician = leftJoin("clinician");
        clinician.add(eq("entity", clinicanRef));
        query.add(clinician);
        JoinConstraint patient = leftJoin("patient");
        JoinConstraint entity = leftJoin("entity");
        patient.add(entity);
        query.add(patient);
        query.add(Constraints.sort(entity.getAlias(), "name"));
        checkQuery(query, expected);
    }

    /**
     * Verifies that ObjectRefConstraints can refer an existing aliases.
     * <p/>
     * NOTE: when an ObjectRefConstraint is used in this way, a duplicate
     * <em>&lt;alias&gt;.archetypeId.shortName =: arg</em> expression will be generated.
     */
    @Test
    public void testRefConstraintWithExistingAlias() {
        String expected = "select customer "
                          + "from " + PartyDO.class.getName() + " as customer "
                          + "where (customer.archetypeId.shortName = :shortName0 "
                          + "and (customer.archetypeId.shortName = :shortName1 and customer.id = :id0))";
        ShortNameConstraint customer = shortName("customer", "party.person");
        IMObjectReference customerRef = new IMObjectReference(new ArchetypeId("party.person"), 12345);
        ArchetypeQuery query = new ArchetypeQuery(customer);
        query.add(new ObjectRefConstraint("customer", customerRef));

        checkQuery(query, expected);
    }

    /**
     * Tests ExistsConstraints.
     */
    @Test
    public void testExistsConstraint() {
        // query to find all pets that have a customer
        String expected =
                "select pet from " + PartyDO.class.getName() + " as pet where (pet.archetypeId.shortName = :shortName0 "
                + "and exists "
                + "(select customer from " + PartyDO.class.getName() + " as customer "
                + "inner join customer.sourceEntityRelationships as patientRel "
                + "where (customer.archetypeId.shortName = :shortName1 and "
                + "((patientRel.archetypeId.shortName = :shortName2 or patientRel.archetypeId.shortName = :shortName3) "
                + "and pet.id = patientRel.target.id))))";
        ArchetypeQuery query = new ArchetypeQuery(shortName("pet", "party.animalpet"));
        query.add(exists(subQuery("party.person", "customer")
                                 .add(join("patients", "patientRel").add(idEq("pet", "patientRel.target")))));
        checkQuery(query, expected);
    }

    /**
     * Tests not ExistsConstraints.
     */
    @Test
    public void testNotExistsConstraint() {
        // query to find all products that have a CANINE species classification, or no species classification
        String expected = "select p from "
                          + ProductDO.class.getName() + " as p "
                          + "left outer join p.classifications as s with s.archetypeId.shortName = :shortName1 "
                          + "where (p.archetypeId.shortName = :shortName0 and (s.code = :code0 or not exists ("
                          + "select p2 from "
                          + ProductDO.class.getName() + " as p2 "
                          + "inner join p2.classifications as classifications "
                          + "where (p2.archetypeId.shortName = :shortName2 and "
                          + "(classifications.archetypeId.shortName = :shortName3 and p.id = p2.id)))))";
        ArchetypeQuery query = new ArchetypeQuery(shortName("p", "product.product"));
        query.add(leftJoin("classifications", shortName("s", "lookup.species")));
        query.add(or(eq("s.code", "CANINE"),
                     notExists(subQuery("product.product", "p2")
                                       .add(join("classifications", shortName("lookup.species"))
                                                    .add(idEq("p", "p2"))))));
        checkQuery(query, expected);
    }

    /**
     * Verifies that an exception is thrown if an alias is duplicated.
     */
    @Test
    public void testDuplicateAlias() {
        ShortNameConstraint constraint1 = shortName("duplicate", "party.customerperson", true);
        ShortNameConstraint constraint2 = shortName("duplicate", "party.patientpet", true);
        ArchetypeQuery query = new ArchetypeQuery(constraint1);
        query.add(constraint2);
        try {
            builder.build(query).getQueryString();
            Assert.fail("Expected query build to fail");
        } catch (QueryBuilderException exception) {
            assertEquals(QueryBuilderException.ErrorCode.DuplicateAlias, exception.getErrorCode());
        }
    }

    /**
     * Verifies that an exception is thrown if an join is used with the same alias.
     */
    @Test
    public void testCannotJoinDuplicateAlias() {
        ShortNameConstraint act = shortName("act", "act.patientReminder", true);
        ArchetypeQuery query = new ArchetypeQuery(act);
        query.add(Constraints.join("patient", "participation"));
        query.add(Constraints.join("patient", "participation"));
        try {
            builder.build(query).getQueryString();
            Assert.fail("Expected query build to fail");
        } catch (QueryBuilderException exception) {
            assertEquals(QueryBuilderException.ErrorCode.CannotJoinDuplicateAlias, exception.getErrorCode());
        }
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        IArchetypeDescriptorCache cache
                = (IArchetypeDescriptorCache) applicationContext.getBean("archetypeDescriptorCache");
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
