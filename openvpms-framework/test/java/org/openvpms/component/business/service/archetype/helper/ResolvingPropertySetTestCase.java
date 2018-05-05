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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


/**
 * Tests the {@link ResolvingPropertySet} class.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class ResolvingPropertySetTestCase extends AbstractArchetypeServiceTest {

    /**
     * The lookup service.
     */
    @Autowired
    ILookupService lookups;

    /**
     * Tests single-level property resolution.
     */
    @Test
    public void testSingleLevelResolution() {
        PropertySet set = new ResolvingPropertySet(getArchetypeService(), lookups);
        set.set("firstName", "J");
        set.set("lastName", "Zoo");
        assertEquals("J", set.getString("firstName"));
        assertEquals("Zoo", set.getString("lastName"));
    }

    /**
     * Tests multiple-level property resolution.
     */
    @Test
    public void testMultiLevelResolution() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        PropertySet set = new ResolvingPropertySet(getArchetypeService(), lookups);
        set.set("act", act.getAct());

        assertEquals("J", set.getString("act.customer.entity.firstName"));
        assertEquals("Zoo", set.getString("act.customer.entity.lastName"));

        assertEquals("Estimation", set.getString("act.displayName"));
        assertEquals("Act Customer", set.getString("act.customer.displayName"));
        assertEquals("Customer(Person)", set.getString("act.customer.entity.displayName"));
        assertEquals("party.customerperson", set.getString("act.customer.entity.shortName"));
    }

    /**
     * Tests resolution where the property is an object reference.
     */
    @Test
    public void testResolutionByReference() {
        Party party = createCustomer();
        PropertySet set = new ResolvingPropertySet(getArchetypeService(), lookups);
        set.set("ref", party.getObjectReference());
        assertEquals("J", set.getString("ref.firstName"));
        assertEquals("Zoo", set.getString("ref.lastName"));
        assertEquals("Customer(Person)", set.getString("ref.displayName"));
        assertEquals("party.customerperson", set.getString("ref.shortName"));
    }

    /**
     * Tests behaviour where an intermediate node doesn't exist.
     */
    @Test
    public void testMissingReference() {
        ActBean act = createAct("act.customerEstimation");
        PropertySet set = new ResolvingPropertySet(getArchetypeService(), lookups);
        set.set("act", act.getAct());
        assertNull(set.get("act.customer.entity.firstName"));
    }

    /**
     * Tests behaviour where an invalid property name is supplied.
     */
    @Test
    public void testInvalidProperty() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        PropertySet set = new ResolvingPropertySet(getArchetypeService(), lookups);
        set.set("act", act.getAct());

        // root node followed by invalid node
        try {
            set.get("act.customer.invalidNode");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidProperty, exception.getErrorCode());
        }

        // intermediate node followed by invalid node
        try {
            set.get("act.customer.entity.invalidNode");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidProperty, exception.getErrorCode());
        }

        // leaf node followed by invalid node
        try {
            set.get("act.startTime.displayName");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidObject, exception.getErrorCode());
        }
    }

    @Test
    public void testPropertyNameWithDots() {
        Party customer = createCustomer();

        PropertySet set = new ResolvingPropertySet(getArchetypeService(), lookups);
        set.set("Globals.Name", "A");
        set.set("Globals.Customer", customer);

        assertEquals("A", set.get("Globals.Name"));
        assertEquals("Zoo", set.getString("Globals.Customer.lastName"));
    }

    /**
     * Helper to create a new act wrapped in a bean.
     *
     * @param shortName the act short name
     * @return a new act
     */
    private ActBean createAct(String shortName) {
        return new ActBean((Act) create(shortName));
    }

    /**
     * Helper to create and save a customer.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        IMObjectBean bean = createBean("party.customerperson");
        bean.setValue("title", "MR");
        bean.setValue("firstName", "J");
        bean.setValue("lastName", "Zoo");
        Contact contact = (Contact) create("contact.phoneNumber");
        assertNotNull(contact);
        bean.addValue("contacts", contact);
        bean.save();
        return (Party) bean.getObject();
    }
}
