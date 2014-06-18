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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.collections.Predicate;
import org.junit.Test;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActive;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.InvalidClassCast;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.NodeDescriptorNotFound;


/**
 * Tests the {@link IMObjectBean} class.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class IMObjectBeanTestCase extends AbstractIMObjectBeanTestCase {

    /**
     * Tests the {@link IMObjectBean#isA} method.
     */
    @Test
    public void testIsA() {
        IMObjectBean bean = createBean("act.customerAccountPayment");
        String[] matches = {"act.customerAccountPaymentCash",
                            "act.customerAccountPayment"};
        assertTrue(bean.isA(matches));

        String[] nomatches = {"act.customerAccountPaymentCash",
                              "act.customerAccountPaymentCredit",
                              "act.customerAccountPaymentEFT"};
        assertFalse(bean.isA(nomatches));

        // test wildcards
        String[] wildmatch = {"act.customerEstimation*", "act.customerAccount*"};
        assertTrue(bean.isA(wildmatch));

        String[] wildnomatch = {"act.customerEstimation*",
                                "act.customerInvoice*"};
        assertFalse(bean.isA(wildnomatch));

    }

    /**
     * Tests the {@link IMObjectBean#hasNode(String)} method.
     */
    @Test
    public void testHasNode() {
        IMObjectBean bean = createBean("party.customerperson");
        assertTrue(bean.hasNode("firstName"));
        assertFalse(bean.hasNode("nonode"));
    }

    /**
     * Tests the {@link IMObjectBean#getDescriptor} method.
     */
    @Test
    public void testGetDescriptor() {
        IMObjectBean bean = createBean("party.customerperson");
        NodeDescriptor node = bean.getDescriptor("firstName");
        assertNotNull(node);
        assertEquals("firstName", node.getName());

        assertNull(bean.getDescriptor("nonode"));
    }

    /**
     * Tests the {@link IMObjectBean#getDisplayName()} method.
     */
    @Test
    public void testGetDisplayName() {
        IMObjectBean pet = createBean("party.animalpet");
        assertEquals("Patient(Pet)", pet.getDisplayName());

        // verify shortname is returned when no display name is present
        IMObjectBean customer = createBean("party.customerperson");
        assertEquals("Customer(Person)", customer.getDisplayName());
    }

    /**
     * Tests the {@link IMObjectBean#getDisplayName(String)} method.
     */
    @Test
    public void testNodeDisplayName() {
        IMObjectBean act = createBean("act.customerAccountPayment");
        assertEquals("Date", act.getDisplayName("startTime"));

        // verify that a node without a custom display name is an uncamel-cased
        // version of the node name
        IMObjectBean pet = createBean("party.animalpet");
        assertEquals("Date Of Birth", pet.getDisplayName("dateOfBirth"));
    }

    /**
     * Tests the {@link IMObjectBean#getArchetypeRange(String)} method.
     */
    @Test
    public void testGetArchetypeRange() {
        IMObjectBean bean = createBean("party.customerperson");

        // check a node with an archetype range assertion
        Set<String> shortNames = new HashSet<String>(Arrays.asList(bean.getArchetypeRange("contacts")));
        assertEquals(2, shortNames.size());
        assertTrue(shortNames.contains("contact.location"));
        assertTrue(shortNames.contains("contact.phoneNumber"));

        // check a node with no assertion
        assertEquals(0, bean.getArchetypeRange("name").length);
    }

    /**
     * Tests the {@link IMObjectBean#getValue(String)} and
     * {@link IMObjectBean#setValue(String, Object)} for a non-existent node.
     */
    @Test
    public void testGetSetInvalidNode() {
        IMObjectBean bean = createBean("party.customerperson");
        try {
            bean.getValue("badNode");
            assertTrue(false);
        } catch (IMObjectBeanException expected) {
            assertEquals(NodeDescriptorNotFound, expected.getErrorCode());
        }

        try {
            bean.setValue("badNode", "value");
            assertTrue(false);
        } catch (IMObjectBeanException expected) {
            assertEquals(NodeDescriptorNotFound, expected.getErrorCode());
        }
    }

    /**
     * Tests the {@link IMObjectBean#getValue(String)} method.
     */
    @Test
    public void testGetValue() {
        IMObjectBean bean = createBean("party.customerperson");
        assertEquals(bean.getValue("firstName"), null);
        bean.setValue("firstName", "Joe");
        assertEquals("Joe", bean.getValue("firstName"));
    }

    /**
     * Tests the {@link IMObjectBean#getBoolean(String)} method.
     */
    @Test
    public void testGetBoolean() {
        IMObjectBean bean = createBean("act.types");
        assertEquals(false, bean.getBoolean("flag"));
        assertEquals(true, bean.getBoolean("flag", true));

        bean.setValue("flag", true);
        assertEquals(true, bean.getBoolean("flag"));
    }

    /**
     * Tests the {@link IMObjectBean#getInt} methods.
     */
    @Test
    public void testGetInt() {
        IMObjectBean bean = createBean("act.types");
        assertEquals(0, bean.getInt("size"));
        assertEquals(-1, bean.getInt("size", -1));

        int size = 100;
        bean.setValue("size", size);
        assertEquals(size, bean.getInt("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getLong} methods.
     */
    @Test
    public void testGetLong() {
        IMObjectBean bean = createBean("act.types");
        assertEquals(0, bean.getLong("size"));
        assertEquals(-1, bean.getLong("size", -1));

        long size = 10000000L;
        bean.setValue("size", size);
        assertEquals(size, bean.getLong("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getString} methods.
     */
    @Test
    public void testGetString() {
        IMObjectBean bean = createBean("act.types");
        assertNull(bean.getValue("name"));
        assertEquals("foo", bean.getString("name", "foo"));

        bean.setValue("name", "bar");
        assertEquals("bar", bean.getValue("name"));

        // test conversion, long -> string
        long size = 10000000L;
        bean.setValue("size", size);
        assertEquals(Long.toString(size), bean.getString("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getBigDecimal} methods.
     */
    @Test
    public void testGetBigDecimal() {
        IMObjectBean bean = createBean("act.types");

        assertNull(bean.getBigDecimal("amount"));
        assertEquals(bean.getBigDecimal("amount", BigDecimal.ZERO),
                     BigDecimal.ZERO);

        BigDecimal expected = new BigDecimal("1234.56");
        bean.setValue("amount", expected);
        assertEquals(expected, bean.getBigDecimal("amount"));

        // quantity has a default value
        assertTrue(BigDecimal.ONE.compareTo(bean.getBigDecimal("quantity")) == 0);
    }

    /**
     * Tests the {@link IMObjectBean#getMoney} methods.
     */
    @Test
    public void testMoney() {
        IMObjectBean bean = createBean("act.types");

        assertNull(bean.getMoney("amount"));
        assertEquals(bean.getMoney("amount", new Money(0)), new Money(0));

        Money expected = new Money("1234.56");
        bean.setValue("amount", expected);
        assertEquals(expected, bean.getMoney("amount"));
    }

    /**
     * Tests the {@link IMObjectBean#getDate} methods.
     */
    @Test
    public void testGetDate() {
        IMObjectBean bean = createBean("act.types");

        Date now = new Date();
        assertNull(bean.getDate("endTime"));
        assertEquals(bean.getDate("endTime", now), now);

        bean.setValue("endTime", now);
        assertEquals(now, bean.getDate("endTime"));
    }

    /**
     * Tests the {@link IMObjectBean#getValues(String)},
     * {@link IMObjectBean#getValues(String, Predicate)},
     * {@link IMObjectBean#addValue(String, IMObject)} and
     * {@link IMObjectBean#removeValue(String, IMObject)} methods.
     */
    @Test
    public void testCollection() {
        IMObjectBean bean = createBean("party.customerperson");
        List<IMObject> values = bean.getValues("contacts");
        assertNotNull(values);
        assertEquals(0, values.size());
        IMObjectBean locationBean = createBean("contact.location");
        IMObjectBean phoneBean = createBean("contact.phoneNumber");

        IMObject location = locationBean.getObject();
        IMObject phone = phoneBean.getObject();
        assertNotNull(location);
        assertNotNull(phone);

        bean.addValue("contacts", location);
        bean.addValue("contacts", phone);
        checkEquals(bean.getValues("contacts"), location, phone);

        assertEquals(phone, bean.getValue("contacts", new IsA("contact.phoneNumber")));

        bean.removeValue("contacts", location);
        checkEquals(bean.getValues("contacts"), phone);

        bean.removeValue("contacts", phone);
        assertEquals(0, bean.getValues("contacts").size());

        // removal of non-existent object is a no-op
        bean.removeValue("contacts", phone);
    }

    /**
     * Tests the {@link IMObjectBean#getValues(String, Class)} method.
     */
    @Test
    public void testGetValuesTypeSafeCast() {
        IMObjectBean bean = createBean("party.customerperson");
        List<IMObject> values = bean.getValues("contacts");
        assertNotNull(values);
        assertEquals(0, values.size());
        IMObjectBean locationBean = createBean("contact.location");
        IMObjectBean phoneBean = createBean("contact.phoneNumber");

        Contact location = (Contact) locationBean.getObject();
        Contact phone = (Contact) phoneBean.getObject();
        assertNotNull(location);
        assertNotNull(phone);

        bean.addValue("contacts", location);
        bean.addValue("contacts", phone);
        List<Contact> contacts = bean.getValues("contacts", Contact.class);
        checkEquals(contacts, location, phone);

        try {
            bean.getValues("contacts", Act.class);
            fail("Expected IMObjectBeanException");
        } catch (IMObjectBeanException exception) {
            assertEquals(InvalidClassCast, exception.getErrorCode());
            assertEquals("Expected class of type " + Act.class.getName()
                         + " but got " + Contact.class.getName(),
                         exception.getMessage());
        }
    }

    /**
     * Tests the {@link IMObjectBean#getReference(String)} method.
     */
    @Test
    public void testGetReferenceNode() {
        IMObjectBean bean = createBean("actRelationship.simple");
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Act act = (Act) service.create("act.simple");
        bean.setValue("source", act.getObjectReference());
        assertEquals(act.getObjectReference(), bean.getReference("source"));
    }

    /**
     * Tests the {@link IMObjectBean#getObject(String)} method.
     */
    @Test
    public void testGetObjectNode() {
        IMObjectBean bean = createBean("actRelationship.simple");
        assertNull(bean.getObject("source"));

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Act act = (Act) service.create("act.simple");
        service.save(act);

        bean.setValue("source", act.getObjectReference());
        assertEquals(act, bean.getObject("source"));
    }

    /**
     * Tests the {@link IMObjectBean#save} method.
     */
    @Test
    public void testSave() {
        String name = "Bar,Foo";

        IMObjectBean bean = createBean("party.customerperson");
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar");
        bean.setValue("title", "MR");

        // name is derived, so should be null when accessed via the object
        assertNull(bean.getObject().getName());

        // ... but non-null when accessed via its node
        assertEquals(name, bean.getValue("name"));

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();

        bean.save();
        // verify that the name has been set on the object
        assertEquals(name, bean.getObject().getName());

        // verify that the object saved
        IMObject object = service.get(bean.getObject().getObjectReference());
        assertEquals(bean.getObject(), object);

        // verify that the name node was saved
        assertEquals(object.getName(), name);
    }

    /**
     * Tests {@link IMObjectBean#getSourceObjects(Collection, String, Class)},
     * {@link IMObjectBean#getSourceObjects(Collection, String[], Class)} and
     * {@link IMObjectBean#getSourceObjects(Collection, String[], boolean, Class)} methods.
     */
    @Test
    public void testGetSourceObjects() {
        Party customer1 = createCustomer();
        Party customer2 = createCustomer();
        Party customer3 = createCustomer();
        Party patient = createPatient();
        addOwnerRelationship(customer1, patient);
        EntityRelationship rel2 = addLocationRelationship(customer2, patient);
        addOwnerRelationship(customer3, patient);
        save(customer1, customer2, customer3, patient);
        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        IMObjectBean bean = new IMObjectBean(patient);
        checkEquals(bean.getSourceObjects(patient.getEntityRelationships(), OWNER, Party.class), customer1, customer3);
        checkEquals(bean.getSourceObjects(patient.getEntityRelationships(), new String[]{OWNER, LOCATION}, Party.class),
                    customer1, customer2, customer3);


        // disable customer3 and verify that its is excluded when active=true
        customer3.setActive(false);
        save(customer3);
        checkEquals(bean.getSourceObjects(patient.getEntityRelationships(), new String[]{OWNER, LOCATION}, true,
                                          Party.class),
                    customer1, customer2);
        checkEquals(bean.getSourceObjects(patient.getEntityRelationships(), new String[]{OWNER, LOCATION}, false,
                                          Party.class),
                    customer1, customer2, customer3);

        // disable the rel2 relationship and verify that customer2 is excluded when active=true
        rel2.setActiveStartTime(start1);
        rel2.setActiveEndTime(end1);

        checkEquals(bean.getSourceObjects(patient.getEntityRelationships(), new String[]{OWNER, LOCATION}, true,
                                          Party.class),
                    customer1);
        checkEquals(bean.getSourceObjects(patient.getEntityRelationships(), new String[]{OWNER, LOCATION}, false,
                                          Party.class),
                    customer1, customer2, customer3);
    }

    /**
     * Tests the {@link IMObjectBean#getNodeSourceObjects(String),
     * {@link IMObjectBean#getNodeSourceObjects(String, Class)},
     * {@link IMObjectBean#getNodeSourceObjects(String, Date)},
     * {@link IMObjectBean#getNodeSourceObjects(String, Date, Class)},
     * {@link IMObjectBean#getNodeSourceObjects(String, Date, boolean)},
     * {@link IMObjectBean#getNodeSourceObjects(String, Date, boolean, Class)},
     * {@link IMObjectBean#getNodeSourceObjects(String, Predicate)},
     * {@link IMObjectBean#getNodeSourceObjects(String, Predicate, boolean)},
     * {@link IMObjectBean#getNodeSourceObjects(String, Predicate, boolean, Class)}
     * {@link IMObjectBean#getNodeSourceObjects(String, Class, Class)} and
     * {@link IMObjectBean#getNodeSourceObjects(String, Class, Class, boolean)} methods.
     */
    @Test
    public void testGetNodeSourceObjects() {
        Party customer1 = createCustomer();
        Party customer2 = createCustomer();
        Party customer3 = createCustomer();
        Party patient = createPatient();
        EntityRelationship rel1 = addOwnerRelationship(customer1, patient);
        EntityRelationship rel2 = addOwnerRelationship(customer2, patient);
        EntityRelationship rel3 = addOwnerRelationship(customer3, patient);
        save(customer1, customer2, customer3, patient);
        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        IMObjectBean bean = new IMObjectBean(patient);
        checkEquals(bean.getNodeSourceObjects("customers"), customer1, customer2, customer3);
        checkEquals(bean.getNodeSourceObjects("customers", Party.class), customer1, customer2, customer3);

        // set the relationship times to the past verify it is filtered out
        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);
        checkEquals(bean.getNodeSourceObjects("customers", now), customer2, customer3);
        checkEquals(bean.getNodeSourceObjects("customers", now, Party.class), customer2, customer3);

        customer3.setActive(false);
        save(customer3);
        checkEquals(bean.getNodeSourceObjects("customers", now, true), customer2);
        checkEquals(bean.getNodeSourceObjects("customers", now, false), customer2, customer3);

        checkEquals(bean.getNodeSourceObjects("customers", now, true, Party.class), customer2);
        checkEquals(bean.getNodeSourceObjects("customers", now, false, Party.class), customer2, customer3);

        checkEquals(bean.getNodeSourceObjects("customers", isActive(now)), customer2);
        checkEquals(bean.getNodeSourceObjects("customers", isActive(now), false), customer2, customer3);
        checkEquals(bean.getNodeSourceObjects("customers", isActive(now), false, Party.class),
                    customer2, customer3);

        Map<EntityRelationship, Entity> expected1 = new HashMap<EntityRelationship, Entity>();
        expected1.put(rel2, customer2);
        assertEquals(expected1, bean.getNodeSourceObjects("customers", Party.class, EntityRelationship.class));

        Map<EntityRelationship, Entity> expected2 = new HashMap<EntityRelationship, Entity>();
        expected2.put(rel1, customer1);
        expected2.put(rel2, customer2);
        expected2.put(rel3, customer3);
        assertEquals(expected1, bean.getNodeSourceObjects("customers", Party.class, EntityRelationship.class, true));
        assertEquals(expected2, bean.getNodeSourceObjects("customers", Party.class, EntityRelationship.class, false));
    }

    /**
     * Tests {@link IMObjectBean#getTargetObjects(Collection, String, Class)},
     * {@link IMObjectBean#getTargetObjects(Collection, String[], Class)} and
     * {@link IMObjectBean#getTargetObjects(Collection, String[], boolean, Class)} methods.
     */
    @Test
    public void testGetTargetObjects() {
        Party patient1 = createPatient();
        Party patient2 = createPatient();
        Party patient3 = createPatient();
        Party customer = createCustomer();
        addOwnerRelationship(customer, patient1);
        EntityRelationship rel2 = addLocationRelationship(customer, patient2);
        addOwnerRelationship(customer, patient3);
        save(patient1, patient2, patient3, customer);
        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        IMObjectBean bean = new IMObjectBean(customer);
        checkEquals(bean.getTargetObjects(customer.getEntityRelationships(), OWNER, Party.class), patient1, patient3);
        checkEquals(bean.getTargetObjects(customer.getEntityRelationships(), new String[]{OWNER, LOCATION}, Party.class),
                    patient1, patient2, patient3);

        // disable customer3 and verify that its is excluded when active=true
        patient3.setActive(false);
        save(patient3);
        checkEquals(bean.getTargetObjects(customer.getEntityRelationships(), new String[]{OWNER, LOCATION}, true,
                                          Party.class),
                    patient1, patient2);
        checkEquals(bean.getTargetObjects(customer.getEntityRelationships(), new String[]{OWNER, LOCATION}, false,
                                          Party.class),
                    patient1, patient2, patient3);

        // disable the rel2 relationship and verify that customer2 is excluded when active=true
        rel2.setActiveStartTime(start1);
        rel2.setActiveEndTime(end1);

        checkEquals(bean.getTargetObjects(customer.getEntityRelationships(), new String[]{OWNER, LOCATION}, true,
                                          Party.class),
                    patient1);
        checkEquals(bean.getTargetObjects(customer.getEntityRelationships(), new String[]{OWNER, LOCATION}, false,
                                          Party.class),
                    patient1, patient2, patient3);
    }

    /**
     * Tests the {@link IMObjectBean#getNodeTargetObjects(String),
     * {@link IMObjectBean#getNodeTargetObjects(String, Class)},
     * {@link IMObjectBean#getNodeTargetObjects(String, Date)},
     * {@link IMObjectBean#getNodeTargetObjects(String, Date, Class)},
     * {@link IMObjectBean#getNodeTargetObjects(String, Date, boolean)},
     * {@link IMObjectBean#getNodeTargetObjects(String, Date, boolean, Class)},
     * {@link IMObjectBean#getNodeTargetObjects(String, Predicate)},
     * {@link IMObjectBean#getNodeTargetObjects(String, Predicate, boolean)},
     * {@link IMObjectBean#getNodeTargetObjects(String, Predicate, boolean, Class)}
     * {@link IMObjectBean#getNodeTargetObjects(String, Class, Class)},
     * {@link IMObjectBean#getNodeTargetObjects(String, Class, Class, boolean)}
     * {@link IMObjectBean#hasNodeTarget(String, IMObject)}}, and
     * {@link IMObjectBean#hasNodeTarget(String, IMObject, Date)} methods.
     */
    @Test
    public void testGetNodeTargetObjects() {
        Party customer = createCustomer();
        Party patient1 = createPatient();
        Party patient2 = createPatient();
        Party patient3 = createPatient();
        EntityRelationship rel1 = addOwnerRelationship(customer, patient1);
        EntityRelationship rel2 = addOwnerRelationship(customer, patient2);
        EntityRelationship rel3 = addOwnerRelationship(customer, patient3);
        save(customer, patient1, patient2, patient3);
        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        IMObjectBean bean = new IMObjectBean(customer);
        checkEquals(bean.getNodeTargetObjects("patients"), patient1, patient2, patient3);
        checkEquals(bean.getNodeTargetObjects("patients", Party.class), patient1, patient2, patient3);

        assertTrue(bean.hasNodeTarget("patients", patient1));
        assertTrue(bean.hasNodeTarget("patients", patient2));
        assertTrue(bean.hasNodeTarget("patients", patient3));
        assertTrue(bean.hasNodeTarget("patients", patient1, now));
        assertTrue(bean.hasNodeTarget("patients", patient2, now));
        assertTrue(bean.hasNodeTarget("patients", patient3, now));

        // set the relationship times to the past verify it is filtered out
        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);
        checkEquals(bean.getNodeTargetObjects("patients", now), patient2, patient3);
        checkEquals(bean.getNodeTargetObjects("patients", now, Party.class), patient2, patient3);

        assertFalse(bean.hasNodeTarget("patients", patient1));
        assertFalse(bean.hasNodeTarget("patients", patient1, now));
        assertTrue(bean.hasNodeTarget("patients", patient1, start1));
        assertTrue(bean.hasNodeTarget("patients", patient2));
        assertTrue(bean.hasNodeTarget("patients", patient3));

        patient3.setActive(false);
        save(patient3);
        checkEquals(bean.getNodeTargetObjects("patients", now, true), patient2);
        checkEquals(bean.getNodeTargetObjects("patients", now, false), patient2, patient3);
        assertTrue(bean.hasNodeTarget("patients", patient3)); // don't care if the target is inactive

        checkEquals(bean.getNodeTargetObjects("patients", now, true, Party.class), patient2);
        checkEquals(bean.getNodeTargetObjects("patients", now, false, Party.class), patient2, patient3);

        checkEquals(bean.getNodeTargetObjects("patients", isActive(now)), patient2);
        checkEquals(bean.getNodeTargetObjects("patients", isActive(now), false), patient2, patient3);
        checkEquals(bean.getNodeTargetObjects("patients", isActive(now), false, Party.class), patient2, patient3);

        Map<EntityRelationship, Entity> expected1 = new HashMap<EntityRelationship, Entity>();
        expected1.put(rel2, patient2);
        assertEquals(expected1, bean.getNodeTargetObjects("patients", Party.class, EntityRelationship.class));

        Map<EntityRelationship, Entity> expected2 = new HashMap<EntityRelationship, Entity>();
        expected2.put(rel1, patient1);
        expected2.put(rel2, patient2);
        expected2.put(rel3, patient3);
        assertEquals(expected1, bean.getNodeTargetObjects("patients", Party.class, EntityRelationship.class, true));
        assertEquals(expected2, bean.getNodeTargetObjects("patients", Party.class, EntityRelationship.class, false));
    }

    /**
     * Verifies that an exception is thrown if the archetype associated with an object cannot be found.
     */
    @Test
    public void testArchetypeNotFound() {
        IMObject object = new IMObject();
        object.setArchetypeId(new ArchetypeId("entity.badShortName"));
        IMObjectBean bean = new IMObjectBean(object);
        try {
            bean.getValue("foo");
            fail("Expected IMObjectBeanException to be thrown");
        } catch (IMObjectBeanException exception) {
            assertEquals(ArchetypeNotFound, exception.getErrorCode());
        }
    }

    /**
     * Tests the {@link IMObjectBean#getNodeTargetObjects(String, Comparator)},
     * {@link IMObjectBean#getNodeTargetObjects(String, Class, Comparator)} and
     * {@link IMObjectBean#getNodeTargetObjects(String, Predicate, boolean, Class, Comparator)} methods.
     */
    @Test
    public void testGetNodeTargetObjectsWithComparator() {
        Party customer = createCustomer();
        Party patient1 = createPatient();
        Party patient2 = createPatient();
        Party patient3 = createPatient();
        EntityRelationship rel1 = addOwnerRelationship(customer, patient1);
        EntityRelationship rel2 = addOwnerRelationship(customer, patient2);
        EntityRelationship rel3 = addOwnerRelationship(customer, patient3);
        rel1.setSequence(3);
        rel2.setSequence(2);
        rel3.setSequence(1);
        save(customer, patient1, patient2, patient3);

        IMObjectBean bean = new IMObjectBean(customer);
        List<IMObject> patients1 = bean.getNodeTargetObjects("patients", SequenceComparator.INSTANCE);
        checkOrder(patients1, patient3, patient2, patient1);

        List<Party> patients2 = bean.getNodeTargetObjects("patients", Party.class, SequenceComparator.INSTANCE);
        checkOrder(patients2, patient3, patient2, patient1);

        // set the relationship times to the past verify it is filtered out
        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);
        List<Party> patients3 = bean.getNodeTargetObjects("patients", IsActiveRelationship.isActive(now), true,
                                                          Party.class, SequenceComparator.INSTANCE);
        checkEquals(patients3, patient3, patient2);
    }

    /**
     * Tests the {@link IMObjectBean#getNodeSourceObjectRef(String)}
     * and {@link IMObjectBean#getNodeTargetObjectRef(String)} methods.
     */
    @Test
    public void testGetNodeSourceTargetObjectRef() {
        Party customer = createCustomer();
        Party patient1 = createPatient();
        save(customer, patient1);

        IMObjectBean custBean = new IMObjectBean(customer);
        assertNull(custBean.getNodeTargetObjectRef("patients"));

        IMObjectBean patBean = new IMObjectBean(patient1);
        assertNull(patBean.getNodeSourceObjectRef("customers"));

        EntityRelationship rel1 = addOwnerRelationship(customer, patient1);
        assertEquals(patient1.getObjectReference(), custBean.getNodeTargetObjectRef("patients"));
        assertEquals(customer.getObjectReference(), patBean.getNodeSourceObjectRef("customers"));

        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        // set the relationship times to the past verify it is filtered out
        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);

        assertNull(custBean.getNodeTargetObjectRef("patients"));
        assertNull(patBean.getNodeSourceObjectRef("customers"));
    }

    /**
     * Tests the {@link IMObjectBean#getDefaultValue(String)} method.
     */
    @Test
    public void testGetDefaultValue() {
        Party patient = createPatient();
        IMObjectBean bean = new IMObjectBean(patient);
        assertEquals(true, bean.getDefaultValue("Active"));
        assertEquals(false, bean.getDefaultValue("deceased"));
        assertNull(bean.getDefaultValue("name"));
    }

    /**
     * Tests the {@link IMObjectBean#isDefaultValue(String)}} method.
     */
    @Test
    public void testIsDefaultValue() {
        Party patient = createPatient();
        IMObjectBean bean = new IMObjectBean(patient);
        assertTrue(bean.isDefaultValue("Active"));
        assertTrue(bean.isDefaultValue("deceased"));

        bean.setValue("Active", false);
        assertFalse(bean.isDefaultValue("Active"));

        assertFalse(bean.isDefaultValue("name")); // no default value
    }
}

