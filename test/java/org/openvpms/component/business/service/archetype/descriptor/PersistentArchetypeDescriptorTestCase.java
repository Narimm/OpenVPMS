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
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.descriptor;

// hibernate

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.List;

/**
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentArchetypeDescriptorTestCase
        extends HibernateInfoModelTestCase {

    /**
     * Test the creation of a simple archetype
     */
    public void testCreateSimpleArchetype() throws Exception {

        Session session = getSession();
        Transaction tx = null;

        try {

            ArchetypeDescriptor desc = createArchetypeDescriptor(
                    "animal", "mypet", "1.0");

            // delete any existing descriptor
            deleteArchetypeDescriptorWithName(session, desc.getName());

            // get initial numbr of entries in address tabel
            int dcount = HibernateDescriptorUtil.getTableRowCount(session,
                                                                  "archetypeDescriptor");
            // execute the test
            tx = session.beginTransaction();
            session.save(desc);
            tx.commit();

            // ensure that there is still one more address
            int dcount1 = HibernateDescriptorUtil.getTableRowCount(session,
                                                                   "archetypeDescriptor");
            assertTrue(dcount1 == dcount + 1);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test the deletion of an archetype descriptor
     */
    public void testSimpleArchetypeDeletion()
            throws Exception {
        Session session = getSession();
        Transaction tx = null;

        try {
            ArchetypeDescriptor desc = createArchetypeDescriptor(
                    "animal", "mypet", "1.0");
            // delete an archetype with the same qName, which is done in its own 
            // transaction
            deleteArchetypeDescriptorWithName(session, desc.getName());

            // get initial numbr of entries in address tabel
            int dcount = HibernateDescriptorUtil.getTableRowCount(session,
                                                                  "archetypeDescriptor");
            // execute the test
            tx = session.beginTransaction();
            session.save(desc);
            tx.commit();
            int dcount1 = HibernateDescriptorUtil.getTableRowCount(session,
                                                                   "archetypeDescriptor");
            assertTrue(dcount1 == dcount + 1);

            // now delete the object and check again
            tx = session.beginTransaction();
            session.delete(desc);
            tx.commit();
            dcount1 = HibernateDescriptorUtil.getTableRowCount(session,
                                                               "archetypeDescriptor");
            assertTrue(dcount1 == dcount);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test creation of simple archetype with some node descriptors
     */
    public void testSimpleArchetypeWithNodeDescriptors()
            throws Exception {

        Session session = getSession();
        Transaction tx = null;

        try {

            // execute the test
            // get initial counts
            ArchetypeDescriptor desc = createArchetypeDescriptor(
                    "animal", "mypet", "1.0");

            // delete an archetype with the same qName, which is done in its own 
            // transaction
            deleteArchetypeDescriptorWithName(session, desc.getName());

            // set up the descriptor
            int acount = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            tx = session.beginTransaction();
            desc.setClassName(Party.class.getName());
            desc.addNodeDescriptor(createNodeDescriptor("uid", "/uid",
                                                        "java.lang.Long", 1,
                                                        1));
            desc.addNodeDescriptor(createNodeDescriptor("name", "/name",
                                                        "java.lang.String", 1,
                                                        1));
            desc.addNodeDescriptor(createNodeDescriptor("description",
                                                        "/description",
                                                        "java.lang.String", 1,
                                                        1));
            desc.addNodeDescriptor(createNodeDescriptor("breed", "/breed",
                                                        "java.lang.String", 1,
                                                        1));
            session.save(desc);
            tx.commit();
            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            assertTrue(acount1 == acount + 1);
            assertTrue(ncount1 == ncount + 4);

            // retrieve the object and ensure that the correct number of
            // node descriptors are present
            desc = (ArchetypeDescriptor) session.load(ArchetypeDescriptor.class,
                                                      desc.getUid());
            assertTrue(desc.getNodeDescriptors().size() == 4);
            assertTrue(desc.getNodeDescriptor("uid") != null);
            assertTrue(desc.getNodeDescriptor("name") != null);

            // delete the archetype descriptor and check results
            tx = session.beginTransaction();
            session.delete(desc);
            tx.commit();
            acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            ncount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            assertTrue(acount1 == acount);
            assertTrue(ncount1 == ncount);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test nested node descriptors
     */
    public void testArchetypeWithNestedNodeDescriptors()
            throws Exception {

        Session session = getSession();
        Transaction tx = null;

        try {

            // execute the test
            // get initial counts
            ArchetypeDescriptor desc = createArchetypeDescriptor(
                    "address", "mylocation", "1.0");

            // delete an archetype with the same qName, which is done in its own 
            // transaction
            deleteArchetypeDescriptorWithName(session, desc.getName());

            // set up the descriptor
            int acount = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            tx = session.beginTransaction();
            desc.setClassName(Contact.class.getName());
            desc.addNodeDescriptor(createNodeDescriptor("uid", "/uid",
                                                        "java.lang.Long", 1,
                                                        1));
            desc.addNodeDescriptor(createNodeDescriptor("name", "/name",
                                                        "java.lang.String", 1,
                                                        1));
            desc.addNodeDescriptor(createNodeDescriptor("description",
                                                        "/description",
                                                        "java.lang.String", 1,
                                                        1));

            NodeDescriptor details = createNodeDescriptor("details", "/details",
                                                          "org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap",
                                                          1, 1);
            desc.addNodeDescriptor(details);

            // add a couple of nested nodes
            details.addNodeDescriptor(createNodeDescriptor("address",
                                                           "/details/address",
                                                           "java.lang.String",
                                                           1, 1));
            details.addNodeDescriptor(createNodeDescriptor("postCode",
                                                           "/details/postCode",
                                                           "java.lang.String",
                                                           1, 1));
            session.save(desc);
            tx.commit();
            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            assertTrue(acount1 == acount + 1);
            assertTrue(ncount1 == ncount + 6);

            // retrieve the object and ensure that the correct number of
            // node descriptors are present
            ArchetypeDescriptor adesc = (ArchetypeDescriptor) session.load(
                    ArchetypeDescriptor.class, desc.getUid());
            assertTrue(adesc.getNodeDescriptors().size() == 4);
            assertTrue(adesc.getNodeDescriptor("uid") != null);
            assertTrue(adesc.getNodeDescriptor("name") != null);

            details = adesc.getNodeDescriptor("details");
            assertTrue(details != null);
            assertTrue(details.getNodeDescriptors().size() == 2);
            assertTrue(adesc.getNodeDescriptor("address") != null);
            assertTrue(adesc.getNodeDescriptor("postCode") != null);

            // delete the archetype descriptor and check results
            tx = session.beginTransaction();
            session.delete(adesc);
            tx.commit();
            acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            ncount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            assertTrue(acount1 == acount);
            assertTrue(ncount1 == ncount);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test an archetype with assertion descriptors
     */
    public void testArchetypeWithAssertionDescriptors()
            throws Exception {

        Session session = getSession();
        Transaction tx = null;

        try {
            // execute the test
            // get initial counts
            ArchetypeDescriptor desc = createArchetypeDescriptor(
                    "animal", "myOtherBreed", "1.0");

            // delete an archetype with the same qName, which is done in its own 
            // transaction
            deleteArchetypeDescriptorWithName(session, desc.getName());

            int acount = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            int ascount = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionDescriptor");

            // set up the descriptor
            NodeDescriptor ndesc;
            AssertionDescriptor adesc;
            desc.setClassName(Party.class.getName());
            ndesc = createNodeDescriptor("uid", "/uid", "java.lang.Long", 1, 1);
            desc.addNodeDescriptor(ndesc);

            ndesc = createNodeDescriptor("name", "/name", "java.lang.String", 1,
                                         1);
            ndesc.addAssertionDescriptor(
                    createAssertionDescriptor("regularExpression"));
            desc.addNodeDescriptor(ndesc);

            ndesc = createNodeDescriptor("description", "/description",
                                         "java.lang.String", 1, 1);
            adesc = createAssertionDescriptor("regularExpression");
            adesc.addProperty(createAssertionProperty("expression",
                                                      "java.lang.String",
                                                      ".*"));
            ndesc.addAssertionDescriptor(adesc);

            adesc = createAssertionDescriptor("maxLength");
            adesc.addProperty(createAssertionProperty("length",
                                                      "java.lang.Integer",
                                                      "20"));
            ndesc.addAssertionDescriptor(adesc);
            desc.addNodeDescriptor(ndesc);

            desc.addNodeDescriptor(createNodeDescriptor("breed", "/breed",
                                                        "java.lang.String", 1,
                                                        1));
            tx = session.beginTransaction();
            session.save(desc);
            tx.commit();

            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            int ascount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionDescriptor");
            assertTrue(acount1 == acount + 1);
            assertTrue(ncount1 == ncount + 4);
            assertTrue(ascount1 == ascount + 3);

        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test archetype with updating of node descriptors
     */
    public void testArchetypeWithUpdateNodeDescriptors()
            throws Exception {

        Session session = getSession();
        Transaction tx = null;

        try {

            // execute the test
            // get initial counts
            ArchetypeDescriptor desc = createArchetypeDescriptor(
                    "animal", "mypet", "1.0");

            // delete an archetype with the same qName, which is done in its own 
            // transaction
            deleteArchetypeDescriptorWithName(session, desc.getName());

            // set up the descriptor
            int acount = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            tx = session.beginTransaction();
            desc.setClassName(Party.class.getName());
            desc.addNodeDescriptor(createNodeDescriptor("uid", "/uid",
                                                        "java.lang.Long", 1,
                                                        1));
            desc.addNodeDescriptor(createNodeDescriptor("name", "/name",
                                                        "java.lang.String", 1,
                                                        1));
            desc.addNodeDescriptor(createNodeDescriptor("description",
                                                        "/description",
                                                        "java.lang.String", 1,
                                                        1));
            desc.addNodeDescriptor(createNodeDescriptor("breed", "/breed",
                                                        "java.lang.String", 1,
                                                        1));
            session.save(desc);
            tx.commit();

            // retrieve the object and ensure that the correct number of
            // node descriptors are present
            tx = session.beginTransaction();
            desc = (ArchetypeDescriptor) session.load(ArchetypeDescriptor.class,
                                                      desc.getUid());

            assertTrue(desc.getNodeDescriptor("description") != null);
            desc.removeNodeDescriptor("description");
            session.save(desc);
            tx.commit();
            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            assertTrue(acount1 == acount + 1);
            assertTrue(ncount1 == ncount + 3);

            desc = (ArchetypeDescriptor) session.load(ArchetypeDescriptor.class,
                                                      desc.getUid());
            assertTrue(desc.getNodeDescriptor("description") == null);

        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test archetype with the updsting of assertion descriptors
     */
    public void testArchetypeWithUpdateAssertionDescriptors()
            throws Exception {

        Session session = getSession();
        Transaction tx = null;

        try {

            // execute the test
            // get initial counts
            ArchetypeDescriptor desc = createArchetypeDescriptor(
                    "animal", "myOtherBreed", "1.0");

            // delete an archetype with the same qName, which is done in its own 
            // transaction
            deleteArchetypeDescriptorWithName(session, desc.getName());

            int acount = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            int ascount = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionDescriptor");

            // set up the descriptor
            NodeDescriptor ndesc;
            AssertionDescriptor adesc;
            desc.setClassName(Party.class.getName());
            ndesc = createNodeDescriptor("uid", "/uid", "java.lang.Long", 1, 1);
            desc.addNodeDescriptor(ndesc);

            ndesc = createNodeDescriptor("name", "/name", "java.lang.String", 1,
                                         1);
            ndesc.addAssertionDescriptor(
                    createAssertionDescriptor("regularExpression"));
            desc.addNodeDescriptor(ndesc);

            ndesc = createNodeDescriptor("description", "/description",
                                         "java.lang.String", 1, 1);
            adesc = createAssertionDescriptor("regularExpression");
            adesc.addProperty(createAssertionProperty("expression",
                                                      "java.lang.String",
                                                      ".*"));
            ndesc.addAssertionDescriptor(adesc);

            adesc = createAssertionDescriptor("maxLength");
            adesc.addProperty(createAssertionProperty("length",
                                                      "java.lang.Integer",
                                                      "20"));
            adesc.addProperty(createAssertionProperty("length2",
                                                      "java.lang.Integer",
                                                      "20"));
            ndesc.addAssertionDescriptor(adesc);
            desc.addNodeDescriptor(ndesc);

            desc.addNodeDescriptor(createNodeDescriptor("breed", "/breed",
                                                        "java.lang.String", 1,
                                                        1));
            tx = session.beginTransaction();
            session.save(desc);
            tx.commit();

            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            int ncount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            int ascount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionDescriptor");
            assertTrue(acount1 == acount + 1);
            assertTrue(ncount1 == ncount + 4);
            assertTrue(ascount1 == ascount + 3);

            // retrieve the object an delete 2 assertions and one property
            tx = session.beginTransaction();
            desc = (ArchetypeDescriptor) session.load(ArchetypeDescriptor.class,
                                                      desc.getUid());
            assertTrue(desc != null);
            desc.removeNodeDescriptor("breed");
            ndesc = desc.getNodeDescriptor("description");
            ndesc.removeAssertionDescriptor("regularExpression");
            adesc = ndesc.getAssertionDescriptor("maxLength");
            adesc.removeProperty("length2");
            session.save(desc);
            tx.commit();

            acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "archetypeDescriptor");
            ncount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "nodeDescriptor");
            ascount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionDescriptor");
            assertTrue(acount1 == acount + 1);
            assertTrue(ncount1 == ncount + 3);
            assertTrue(ascount1 == ascount + 2);

            desc = (ArchetypeDescriptor) session.load(ArchetypeDescriptor.class,
                                                      desc.getUid());
            assertTrue(desc.getNodeDescriptors().size() == 3);
            assertTrue(desc.getNodeDescriptor("description")
                    .getAssertionDescriptors().size() == 1);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Verifies that an {@link AssertionDescriptor}'s ProperyMap is correctly
     * loaded when its other attributes are null.
     *
     * @throws Exception for any error
     */
    public void testOBF112() throws Exception {
        Session session = getSession();
        Transaction tx = null;

        try {
            AssertionDescriptor assertion = new AssertionDescriptor();
            assertion.setName("assertionOBF112");
            assertion.addProperty(createAssertionProperty("expression",
                                                          "java.lang.String",
                                                          ".*"));
            assertNotNull(assertion.getPropertyMap());
            tx = session.beginTransaction();
            session.save(assertion);
            tx.commit();
            session.evict(
                    assertion); // evict the assertion to force load from db

            // reload and verify the property map was loaded
            AssertionDescriptor loaded = (AssertionDescriptor) session.load(
                    AssertionDescriptor.class,
                    assertion.getUid());
            PropertyMap map = loaded.getPropertyMap();
            assertNotNull(map);
            assertNotNull(loaded.getProperty("expression"));
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        getSession().flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

    /**
     * Create an archetype using the specified archetype id components.
     *
     * @param entityName the entity name
     * @param concept    the concept that the archetype denotes
     * @param version    the version of the archetype
     * @return ArchetypeDescriptor
     */
    private ArchetypeDescriptor createArchetypeDescriptor(String entityName,
                                                          String concept,
                                                          String version) {
        ArchetypeDescriptor desc = new ArchetypeDescriptor();
        desc.setName(new ArchetypeId(entityName, concept,
                                     version).getQualifiedName());
        desc.setLatest(true);

        return desc;
    }

    /**
     * Delete the archetype with the specified arhetypeId QName. This must
     * be called within a transaction
     *
     * @param session the hibernate session
     * @param qName   the qname to use
     */
    private void deleteArchetypeDescriptorWithName(Session session,
                                                   String qName)
            throws Exception {
        Transaction tx = session.beginTransaction();
        try {
            Query query = session.getNamedQuery(
                    "archetypeDescriptor.getByName");
            query.setParameter("name", qName);
            List result = query.list();

            for (Object object : result) {
                session.delete(object);
            }

            tx.commit();
        } catch (Exception exception) {
            tx.rollback();
            throw exception;
        }
    }

    /**
     * Create a {@link NodeDescriptor with the specified parameters
     *
     * @return NodeDescriptor
     */
    private NodeDescriptor createNodeDescriptor(String name, String path,
                                                String type, int minC,
                                                int maxC) {
        NodeDescriptor desc = new NodeDescriptor();
        desc.setName(name);
        desc.setPath(path);
        desc.setType(type);
        desc.setMinCardinality(minC);
        desc.setMaxCardinality(maxC);
        return desc;
    }

    /**
     * Create an {@link AssertionDescriptor} with the specified parameters
     *
     * @return AssertionDescriptor
     */
    private AssertionDescriptor createAssertionDescriptor(String type) {
        AssertionDescriptor desc = new AssertionDescriptor();
        desc.setName(type);
        desc.setErrorMessage("An error message");

        return desc;
    }

    /**
     * Create a {@link AssertionProperty} with the specified parameters
     *
     * @return AssertionProperty
     */
    private AssertionProperty createAssertionProperty(String name, String type,
                                                      String value) {
        AssertionProperty prop = new AssertionProperty();
        prop.setName(name);
        prop.setType(type);
        prop.setValue(value);

        return prop;
    }
}
