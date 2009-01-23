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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * {@link PropertySetResolver} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PropertySetResolverTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Tests single-level property resolution.
     */
    public void testSingleLevelResolution() {
        ObjectSet set = new ObjectSet();
        set.set("firstName", "J");
        set.set("lastName", "Zoo");
        PropertySetResolver resolver = new PropertySetResolver(set, service);
        assertEquals("J", resolver.getObject("firstName"));
        assertEquals("Zoo", resolver.getObject("lastName"));
    }

    /**
     * Tests multiple-level property resolution.
     */
    public void testMultiLevelResolution() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        ObjectSet set = new ObjectSet();
        set.set("act", act.getAct());

        PropertySetResolver resolver = new PropertySetResolver(set, service);
        assertEquals("J", resolver.getObject("act.customer.entity.firstName"));
        assertEquals("Zoo", resolver.getObject("act.customer.entity.lastName"));

        assertEquals("Estimation", resolver.getObject("act.displayName"));
        assertEquals("Act Customer",
                     resolver.getObject("act.customer.displayName"));
        assertEquals("Customer(Person)",
                     resolver.getObject("act.customer.entity.displayName"));
        assertEquals("party.customerperson",
                     resolver.getObject("act.customer.entity.shortName"));
    }

    /**
     * Tests resolution where the property is an object reference.
     */
    public void testResolutionByReference() {
        Party party = createCustomer();
        ObjectSet set = new ObjectSet();
        set.set("ref", party.getObjectReference());
        PropertySetResolver resolver = new PropertySetResolver(set, service);
        assertEquals("J", resolver.getObject("ref.firstName"));
        assertEquals("Zoo", resolver.getObject("ref.lastName"));
        assertEquals("Customer(Person)",
                     resolver.getObject("ref.displayName"));
        assertEquals("party.customerperson",
                     resolver.getObject("ref.shortName"));
    }

    /**
     * Tests behaviour where an intermediate node doesn't exist.
     */
    public void testMissingReference() {
        ActBean act = createAct("act.customerEstimation");
        ObjectSet set = new ObjectSet();
        set.set("act", act.getAct());
        PropertySetResolver resolver = new PropertySetResolver(set, service);
        assertNull(resolver.getObject("act.customer.entity.firstName"));
    }

    /**
     * Tests behaviour where an invalid property name is supplied.
     */
    public void testInvalidProperty() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        ObjectSet set = new ObjectSet();
        set.set("act", act.getAct());
        PropertySetResolver resolver = new PropertySetResolver(set, service);

        // root node followed by invalid node
        try {
            resolver.getObject("act.customer.invalidNode");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidProperty,
                         exception.getErrorCode());
        }

        // intermediate node followed by invalid node
        try {
            resolver.getObject("act.customer.entity.invalidNode");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidProperty,
                         exception.getErrorCode());
        }

        // leaf node followed by invalid node
        try {
            resolver.getObject("act.startTime.displayName");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidObject,
                         exception.getErrorCode());
        }
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"};
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
        assertNotNull(service);
    }

    /**
     * Helper to create a new object.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    private IMObject create(String shortName) {
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return object;
    }

    /**
     * Helper to create a new object wrapped in a bean.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    private IMObjectBean createBean(String shortName) {
        return new IMObjectBean(create(shortName));
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
