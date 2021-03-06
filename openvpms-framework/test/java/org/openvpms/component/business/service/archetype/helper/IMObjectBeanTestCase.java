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

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.collections.Predicate;
import org.junit.Test;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.business.domain.im.common.SequencedRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.model.act.Participation;
import org.openvpms.component.model.bean.Policies;
import org.openvpms.component.model.bean.Policy;
import org.openvpms.component.model.bean.Predicates;
import org.openvpms.component.model.object.Relationship;
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
        Set<String> shortNames = new HashSet<>(Arrays.asList(bean.getArchetypeRange("contacts")));
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
            fail();
        } catch (IMObjectBeanException expected) {
            assertEquals(NodeDescriptorNotFound, expected.getErrorCode());
        }

        try {
            bean.setValue("badNode", "value");
            fail();
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
        assertNull(bean.getValue("firstName"));
        bean.setValue("firstName", "Joe");
        assertEquals("Joe", bean.getValue("firstName"));
    }

    /**
     * Tests the {@link IMObjectBean#getBoolean(String)} method.
     */
    @Test
    public void testGetBoolean() {
        IMObjectBean bean = createBean("act.types");
        assertFalse(bean.getBoolean("flag"));
        assertTrue(bean.getBoolean("flag", true));

        bean.setValue("flag", true);
        assertTrue(bean.getBoolean("flag"));
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
        assertEquals(0, BigDecimal.ONE.compareTo(bean.getBigDecimal("quantity")));
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
     * {@link IMObjectBean#addValue(String, org.openvpms.component.model.object.IMObject)} and
     * {@link IMObjectBean#removeValue(String, org.openvpms.component.model.object.IMObject)} methods.
     */
    @Test
    public void testCollection() {
        IMObjectBean bean = createBean("party.customerperson");
        List<org.openvpms.component.model.object.IMObject> values = bean.getValues("contacts");
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
     * Tests the {@link IMObjectBean#removeValues(String)} method.
     */
    @Test
    public void testRemoveValues() {
        IMObjectBean bean = createBean("party.customerperson");
        IMObject location = create("contact.location");
        IMObject phone = create("contact.phoneNumber");
        assertNotNull(location);
        assertNotNull(phone);

        bean.addValue("contacts", location);
        bean.addValue("contacts", phone);
        checkEquals(bean.getValues("contacts"), location, phone);

        checkEquals(bean.removeValues("contacts"), location, phone);
        assertEquals(0, bean.getValues("contacts").size());

        checkEquals(bean.removeValues("contacts"));
        assertEquals(0, bean.getValues("contacts").size());
    }

    /**
     * Tests the {@link IMObjectBean#getValues(String, Class)} method.
     */
    @Test
    public void testGetValuesTypeSafeCast() {
        IMObjectBean bean = createBean("party.customerperson");
        List<org.openvpms.component.model.object.IMObject> values = bean.getValues("contacts");
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
     * Tests the {@link IMObjectBean#getValue(String, Predicate, Class)} method.
     */
    @Test
    public void testGetValuePredicate() {
        IMObjectBean bean = createBean("party.customerperson");
        IMObject location = create("contact.location");
        IMObject phone = create("contact.phoneNumber");
        assertNotNull(location);
        assertNotNull(phone);

        bean.addValue("contacts", location);
        bean.addValue("contacts", phone);

        assertEquals(location, bean.getValue("contacts", Contact.class, Predicates.isA("contact.location")));
        assertEquals(phone, bean.getValue("contacts", Contact.class, Predicates.isA("contact.phoneNumber")));
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
        IMObjectBean sourceBean = createBean("act.simple");
        sourceBean.save();

        IMObjectBean targetBean = createBean("act.simple");
        targetBean.save();
        IMObjectRelationship relationship = sourceBean.addTarget("actRelationships", targetBean.getObject(),
                                                                 "actRelationships");
        sourceBean.save(targetBean.getObject());

        // test IMObject retrieval. Note that this shouldn't be used for collections with cardinality > 1, as
        // the results are non-deterministic, but for these tests it doesn't matter.
        assertEquals(relationship, sourceBean.getObject("actRelationships"));
        assertEquals(relationship, targetBean.getObject("actRelationships"));

        // test IMObjectReference retrieval
        IMObjectBean relBean = new IMObjectBean(relationship);
        assertEquals(sourceBean.getObject(), relBean.getObject("source"));
        assertEquals(targetBean.getObject(), relBean.getObject("target"));
    }

    /**
     * Tests the {@link IMObjectBean#getSources(String),
     * {@link IMObjectBean#getSources(String, Class)},
     * {@link IMObjectBean#getSources(String, Policy)} and
     * {@link IMObjectBean#getSources(String, Class, Policy)} methods.
     */
    @Test
    public void testGetSources() {
        Party customer1 = createCustomer();
        Party customer2 = createCustomer();
        Party customer3 = createCustomer();
        Party patient = createPatient();
        EntityRelationship rel1 = addOwnerRelationship(customer1, patient);
        addOwnerRelationship(customer2, patient);
        addOwnerRelationship(customer3, patient);
        save(customer1, customer2, customer3, patient);
        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        IMObjectBean bean = new IMObjectBean(patient);
        checkEquals(bean.getSources("customers"), customer1, customer2, customer3);
        checkEquals(bean.getSources("customers", Party.class), customer1, customer2, customer3);

        // set the relationship times to the past verify it is filtered out
        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);
        checkEquals(bean.getSources("customers", Policies.active(now)), customer2, customer3);
        checkEquals(bean.getSources("customers", Party.class, Policies.active(now)), customer2, customer3);

        customer3.setActive(false);
        save(customer3);
        checkEquals(bean.getSources("customers", Policies.active(now)), customer2);
        checkEquals(bean.getSources("customers", Policies.active(now, false)), customer2, customer3);

        checkEquals(bean.getSources("customers", Party.class, Policies.active(now)), customer2);
        checkEquals(bean.getSources("customers", Party.class, Policies.active(now, false)), customer2, customer3);
    }

    /**
     * Tests the {@link IMObjectBean#getTargets(String)},
     * {@link IMObjectBean#getTargets(String, Class)},
     * {@link IMObjectBean#getTargets(String, Policy)} and
     * {@link IMObjectBean#getTargets(String, Class, Policy)} methods.
     */
    @Test
    public void testGetTargets() {
        Party customer = createCustomer();
        Party patient1 = createPatient();
        Party patient2 = createPatient();
        Party patient3 = createPatient();
        EntityRelationship rel1 = addOwnerRelationship(customer, patient1);
        addOwnerRelationship(customer, patient2);
        addOwnerRelationship(customer, patient3);
        save(customer, patient1, patient2, patient3);
        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        IMObjectBean bean = new IMObjectBean(customer);
        checkEquals(bean.getTargets("patients"), patient1, patient2, patient3);
        checkEquals(bean.getTargets("patients", Party.class), patient1, patient2, patient3);

        // set the relationship times to the past verify it is filtered out
        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);
        checkEquals(bean.getTargets("patients", Policies.active(now)), patient2, patient3);
        checkEquals(bean.getTargets("patients", Party.class, Policies.active(now)), patient2, patient3);

        patient3.setActive(false);
        save(patient3);
        checkEquals(bean.getTargets("patients", Policies.active(now)), patient2);
        checkEquals(bean.getTargets("patients", Policies.active(now, false)), patient2, patient3);

        checkEquals(bean.getTargets("patients", Party.class, Policies.active(now, false)), patient2, patient3);
    }

    /**
     * Tests the {@link IMObjectBean#getTargets(String, Policy)} and
     * {@link IMObjectBean#getTargets(String, Class, Policy)} methods when the policy provides a comparator.
     */
    @Test
    public void testGetTargetsWithComparator() {
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
        Policy<SequencedRelationship> active = Policies.active(SequencedRelationship.class,
                                                               SequenceComparator.INSTANCE);
        List<org.openvpms.component.model.object.IMObject> patients1 = bean.getTargets("patients", active);
        checkOrder(patients1, patient3, patient2, patient1);

        List<Party> patients2 = bean.getTargets("patients", Party.class, active);
        checkOrder(patients2, patient3, patient2, patient1);

        // set the relationship times to the past verify it is filtered out
        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);

        Policy<SequencedRelationship> active2 = Policies.active(now, SequencedRelationship.class,
                                                                SequenceComparator.INSTANCE);
        List<Party> patients3 = bean.getTargets("patients", Party.class, active2);
        checkEquals(patients3, patient3, patient2);
    }

    /**
     * Tests the {@link IMObjectBean#getSourceRef(String)} and {@link IMObjectBean#getTargetRef(String)}.
     */
    @Test
    public void testGetSourceTargetRef() {
        Party customer = createCustomer();
        Party patient1 = createPatient();
        save(customer, patient1);

        IMObjectBean custBean = new IMObjectBean(customer);
        assertNull(custBean.getTargetRef("patients"));

        IMObjectBean patBean = new IMObjectBean(patient1);
        assertNull(patBean.getSourceRef("customers"));

        EntityRelationship rel1 = addOwnerRelationship(customer, patient1);
        assertEquals(patient1.getObjectReference(), custBean.getTargetRef("patients"));
        assertEquals(customer.getObjectReference(), patBean.getSourceRef("customers"));

        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        // set the relationship times to the past verify it is filtered out
        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);

        assertNull(custBean.getTargetRef("patients", Policies.active()));
        assertEquals(patient1.getObjectReference(), custBean.getTargetRef("patients"));
        assertNull(patBean.getSourceRef("customers", Policies.active()));
        assertEquals(customer.getObjectReference(), patBean.getSourceRef("customers"));
    }

    /**
     * Tests the {@link IMObjectBean#addTarget(String, org.openvpms.component.model.object.IMObject)}
     * and {@link IMObjectBean#removeTarget(String, org.openvpms.component.model.object.IMObject)} method.
     */
    @Test
    public void testAddRemoveTarget() {
        Party customer = createCustomer();
        Party location = createLocation();
        Party patient = createPatient();

        IMObjectBean bean = new IMObjectBean(customer);
        Relationship rel1 = bean.addTarget("location", location);
        assertTrue(rel1 instanceof EntityLink);
        bean.save();

        Relationship rel2 = bean.addTarget("owns", patient);
        assertTrue(rel2 instanceof EntityRelationship);
        patient.addEntityRelationship((EntityRelationship) rel2);
        save(customer, patient);

        customer = get(customer);
        assertNotNull(customer);
        bean = new IMObjectBean(customer);

        assertEquals(location, bean.getTarget("location"));
        assertEquals(location.getObjectReference(), bean.getTargetRef("location"));
        assertEquals(patient, bean.getTarget("owns"));
        assertEquals(patient.getObjectReference(), bean.getTargetRef("owns"));

        assertEquals(rel1, bean.removeTarget("location", location));
        assertNull(bean.getTarget("location"));
    }

    /**
     * Verifies that {@link IMObjectBean#addTarget(String, String, org.openvpms.component.model.object.IMObject)}
     * works for {@link Participation} instances.
     */
    @Test
    public void testAddTargetForParticipation() {
        Party customer = createCustomer();
        save(customer);

        IMObjectBean bean = createBean("act.customerEstimation");

        assertNull(bean.getTargetRef("customer"));
        assertNull(bean.getTarget("customer"));

        Relationship relationship = bean.addTarget("customer", customer);
        assertTrue(relationship instanceof Participation);
        assertEquals(customer.getObjectReference(), bean.getTargetRef("customer"));
        assertEquals(customer, bean.getTarget("customer"));
    }

    /**
     * Tests the {@link IMObjectBean#getTarget}, {@link IMObjectBean#getTarget(String, Class)},
     * {@link IMObjectBean#getTarget(String, Class, Policy)}, {@link IMObjectBean#getTarget(Collection, Policy)}
     * and {@link IMObjectBean#getTarget(Collection, Class, Policy)} methods.
     */
    @Test
    public void testGetTarget() {
        Party customer = createCustomer();
        Party patient = createPatient();

        Date now = new Date();
        IMObjectBean bean = new IMObjectBean(customer);
        PeriodRelationship owns = (PeriodRelationship) bean.addTarget("owns", patient);
        bean.save();

        assertEquals(patient, bean.getTarget("owns"));
        assertEquals(patient, bean.getTarget("owns", Party.class));
        assertEquals(patient, bean.getTarget("owns", IMObject.class, Policies.active()));

        // test the collection form
        Policy<org.openvpms.component.model.entity.EntityRelationship> active
                = Policies.active(org.openvpms.component.model.entity.EntityRelationship.class);
        assertEquals(patient, bean.getTarget(customer.getSourceEntityRelationships(), active));
        assertEquals(patient, bean.getTarget(customer.getSourceEntityRelationships(), Party.class, active));

        // set the relationship times to the past verify it is filtered out
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        owns.setActiveStartTime(start1);
        owns.setActiveEndTime(end1);

        assertEquals(patient, bean.getTarget("owns"));
        assertEquals(patient, bean.getTarget("owns", Party.class));
        assertNull(bean.getTarget("owns", Party.class, Policies.active()));
        assertEquals(patient, bean.getTarget("owns", Party.class, Policies.any()));

        // test the collection form
        assertNull(bean.getTarget(customer.getSourceEntityRelationships(), active));
        assertNull(bean.getTarget(customer.getSourceEntityRelationships(), Party.class, active));
    }

    /**
     * Tests the {@link IMObjectBean#hasTarget(String, org.openvpms.component.model.object.IMObject)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testHasTarget() throws Exception {
        Party customer = createCustomer();
        Party patient1 = createPatient();
        Party patient2 = createPatient();

        IMObjectBean bean = new IMObjectBean(customer);
        assertFalse(bean.hasTarget("owns", patient1));
        assertFalse(bean.hasTarget("owns", patient2));

        // now add a relationship
        Relationship relationship1 = bean.addTarget("owns", patient1);
        assertTrue(bean.hasTarget("owns", patient1));
        assertFalse(bean.hasTarget("owns", patient2));

        // add another relationship
        bean.addTarget("owns", patient2);
        assertTrue(bean.hasTarget("owns", patient1));
        assertTrue(bean.hasTarget("owns", patient2));

        // verify both active and inactive relationships are examined
        relationship1.setActive(false);
        Thread.sleep(500);
        assertTrue(bean.hasTarget("owns", patient1));
        assertTrue(bean.hasTarget("owns", patient2));
    }

    /**
     * Tests the {@link IMObjectBean#addTarget(String, org.openvpms.component.model.object.IMObject, String)} method.
     */
    @Test
    public void testAddTargetBidirectional() {
        Party customer = createCustomer();
        Party patient = createPatient();

        IMObjectBean bean = new IMObjectBean(customer);
        IMObjectRelationship rel2 = bean.addTarget("owns", patient, "customers");
        assertTrue(rel2 instanceof EntityRelationship);
        save(customer, patient);

        customer = get(customer);
        assertNotNull(customer);
        bean = new IMObjectBean(customer);

        assertEquals(patient, bean.getTarget("owns"));
        assertEquals(patient.getObjectReference(), bean.getTargetRef("owns"));
    }

    /**
     * Tests the {@link IMObjectBean#getRelated(String)}},
     * {@link IMObjectBean#getRelated(String, Class)},
     * {@link IMObjectBean#getRelated(String, Policy)} and
     * {@link IMObjectBean#getRelated(String, Class, Policy)} methods.
     */
    @Test
    public void testGetRelated() {
        Party customer = createCustomer();
        Party patient1 = createPatient();
        Party patient2 = createPatient();
        Party patient3 = createPatient();
        IMObjectBean bean = new IMObjectBean(customer);
        EntityRelationship rel1 = (EntityRelationship) bean.addTarget("owns", patient1, "customers");
        bean.addTarget("owns", patient2, "customers");
        bean.addTarget("owns", patient3, "customers");

        save(customer, patient1, patient2, patient3);
        Date now = new Date();
        Date start1 = new Date(now.getTime() - 60 * 1000);
        Date end1 = new Date(now.getTime() - 50 * 1000);

        checkEquals(bean.getRelated("patients"), patient1, patient2, patient3);
        checkEquals(bean.getRelated("patients", Party.class), patient1, patient2, patient3);

        // set the relationship times to the past verify it is filtered out
        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);
        checkEquals(bean.getRelated("patients", Policies.active(now)), patient2, patient3);
        checkEquals(bean.getRelated("patients", Party.class, Policies.active(now)), patient2, patient3);

        patient3.setActive(false);
        save(patient3);
        checkEquals(bean.getRelated("patients", Policies.active(now)), patient2);
        checkEquals(bean.getRelated("patients", Policies.active(now, false)), patient2, patient3);

        checkEquals(bean.getRelated("patients", Party.class, Policies.active(now, false)), patient2, patient3);

        IMObjectBean patient1Bean = new IMObjectBean(patient1);
        checkEquals(patient1Bean.getRelated("customers"));
        checkEquals(patient1Bean.getRelated("customers", Party.class));
        checkEquals(patient1Bean.getRelated("customers", Party.class, Policies.any()), customer);

        IMObjectBean patient2Bean = new IMObjectBean(patient2);
        checkEquals(patient2Bean.getRelated("customers"), customer);
        checkEquals(patient2Bean.getRelated("customers", Party.class), customer);
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
        IMObject object = service.get(bean.getReference());
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
     * {@link IMObjectBean#getNodeSourceObjects(String, Class, Class, boolean)} and
     * {@link IMObjectBean#hasNodeSource(String, IMObject)} methods.
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
        assertTrue(bean.hasNodeSource("customers", customer1));
        assertTrue(bean.hasNodeSource("customers", customer2));
        assertTrue(bean.hasNodeSource("customers", customer3));

        // set the relationship times to the past verify it is filtered out
        rel1.setActiveStartTime(start1);
        rel1.setActiveEndTime(end1);
        checkEquals(bean.getNodeSourceObjects("customers", now), customer2, customer3);
        checkEquals(bean.getNodeSourceObjects("customers", now, Party.class), customer2, customer3);
        assertFalse(bean.hasNodeSource("customers", customer1));
        assertTrue(bean.hasNodeSource("customers", customer2));
        assertTrue(bean.hasNodeSource("customers", customer3));

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

        Map<EntityRelationship, Entity> expected1 = new HashMap<>();
        expected1.put(rel2, customer2);
        assertEquals(expected1, bean.getNodeSourceObjects("customers", Party.class, EntityRelationship.class));

        Map<EntityRelationship, Entity> expected2 = new HashMap<>();
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
     * {@link IMObjectBean#hasNodeTarget(String, org.openvpms.component.model.object.IMObject)} and
     * {@link IMObjectBean#hasNodeTarget(String, org.openvpms.component.model.object.IMObject, Date)} methods.
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

        Map<EntityRelationship, Entity> expected1 = new HashMap<>();
        expected1.put(rel2, patient2);
        assertEquals(expected1, bean.getNodeTargetObjects("patients", Party.class, EntityRelationship.class));

        Map<EntityRelationship, Entity> expected2 = new HashMap<>();
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
     * Tests the {@link IMObjectBean#addNodeTarget(String, IMObject)} method.
     */
    @Test
    public void testAddNodeTarget() {
        Party customer = createCustomer();
        Party location = createLocation();
        Party patient = createPatient();

        IMObjectBean bean = new IMObjectBean(customer);
        IMObjectRelationship rel1 = bean.addNodeTarget("location", location);
        assertTrue(rel1 instanceof EntityLink);
        bean.save();

        IMObjectRelationship rel2 = bean.addNodeTarget("owns", patient);
        assertTrue(rel2 instanceof EntityRelationship);
        patient.addEntityRelationship((EntityRelationship) rel2);
        save(customer, patient);

        customer = get(customer);
        assertNotNull(customer);
        bean = new IMObjectBean(customer);

        assertEquals(location, bean.getNodeTargetObject("location"));
        assertEquals(location.getObjectReference(), bean.getNodeTargetObjectRef("location"));
        assertEquals(patient, bean.getNodeTargetObject("owns"));
        assertEquals(patient.getObjectReference(), bean.getNodeTargetObjectRef("owns"));
    }

    /**
     * Tests the {@link IMObjectBean#getDefaultValue(String)} method.
     */
    @Test
    public void testGetDefaultValue() {
        Party patient = createPatient();
        IMObjectBean bean = new IMObjectBean(patient);
        assertEquals(true, bean.getDefaultValue("active"));
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
        assertTrue(bean.isDefaultValue("active"));
        assertTrue(bean.isDefaultValue("deceased"));

        bean.setValue("active", false);
        assertFalse(bean.isDefaultValue("active"));

        assertFalse(bean.isDefaultValue("name")); // no default value
    }

}

