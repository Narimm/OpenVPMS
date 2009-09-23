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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.PropertyResolverException;
import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidObject;
import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidProperty;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Collection;


/**
 * {@link ArchetypeServiceFunctions} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServiceFunctionsTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Tests single-level node resolution given a root IMObject.
     */
    public void testIMObjectSingleLevelResolution() {
        Party party = createCustomer();
        JXPathContext context = JXPathHelper.newContext(party);

        checkEquals("J", "firstName", context);
        checkEquals("Zoo", "lastName", context);
        checkEquals("Customer(Person)", "displayName", context);
    }

    /**
     * Tests single-level node resolution given a root PropertySet.
     */
    public void testPropertySetSingleLevelResolution() {
        Party party = createCustomer();
        ObjectSet set = new ObjectSet();
        set.set("customer", party);
        JXPathContext context = JXPathHelper.newContext(set);

        checkEquals("J", "customer.firstName", context);
        checkEquals("Zoo", "customer.lastName", context);
        checkEquals("Customer(Person)", "customer.displayName", context);
    }

    /**
     * Tests multiple-level node resolution given a root IMObject.
     */
    public void testIMObjectMultiLevelResolution() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);

        JXPathContext context = JXPathHelper.newContext(act.getAct());
        checkEquals("J", "customer.entity.firstName", context);
        checkEquals("Zoo", "customer.entity.lastName", context);
    }

    /**
     * Tests multiple-level node resolution given a root PropertySet.
     */
    public void testPropertySetMultiLevelResolution() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        ObjectSet set = new ObjectSet();
        set.set("act", act.getAct());

        JXPathContext context = JXPathHelper.newContext(set);
        checkEquals("J", "act.customer.entity.firstName", context);
        checkEquals("Zoo", "act.customer.entity.lastName", context);
    }

    /**
     * Tests behaviour where an intermediate node doesn't exist.
     */
    public void testIMObjectMissingReference() {
        ActBean act = createAct("act.customerEstimation");
        JXPathContext context = JXPathHelper.newContext(act.getAct());
        assertNull(
                context.getValue("openvpms:get(., 'customer.entity.firstName')"));
    }

    /**
     * Tests behaviour where an intermediate node doesn't exist.
     */
    public void testPropertySetMissingReference() {
        ActBean act = createAct("act.customerEstimation");
        ObjectSet set = new ObjectSet();
        set.set("act", act.getAct());
        JXPathContext context = JXPathHelper.newContext(set);
        assertNull(
                context.getValue(
                        "openvpms:get(.,'act.customer.entity.firstName')"));
    }

    /**
     * Tests behaviour where an invalid node name is supplied.
     */
    public void testInvalidNode() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        JXPathContext context = JXPathHelper.newContext(act.getAct());

        // root node followed by invalid node
        try {
            context.getValue("openvpms:get(., 'customer.invalidNode')");
            fail("expected PropertyResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidProperty);
        }

        // intermediate node followed by invalid node
        try {
            context.getValue("openvpms:get(., 'customer.entity.invalidNode')");
            fail("expected PropertyResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidProperty);
        }

        // leaf node followed by invalid node
        try {
            context.getValue("openvpms:get(., 'startTime.displayName')");
            fail("expected PropertyResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidObject);
        }

        // invalid node with default value
        Object value = context.getValue(
                "openvpms:get(., 'invalidNode', 'default')");
        assertEquals("default", value);
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
        JXPathContext context = JXPathHelper.newContext(set);

        // invalid property name
        try {
            context.getValue("openvpms:get(., 'nonexistentprop')");
            fail("expected PropertyResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidObject);
        }

        // root node followed by invalid node
        try {
            context.getValue("openvpms:get(., 'act.customer.invalidNode')");
            fail("expected PropertyResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidProperty);
        }

        // intermediate node followed by invalid node
        try {
            context.getValue("openvpms:get(., 'act.customer.entity.invalidNode')");
            fail("expected PropertyResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidProperty);
        }

        // leaf node followed by invalid node
        try {
            context.getValue("openvpms:get(., 'act.startTime.displayName')");
            fail("expected PropertyResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidObject);
        }

        // invalid node with default value
        Object value = context.getValue(
                "openvpms:get(., 'act.invalidNode', 'default')");
        assertEquals("default", value);
    }

    /**
     * Tests the openvpms:set() function.
     */
    public void testSet() {
        Party party = createCustomer();
        JXPathContext context = JXPathHelper.newContext(party);
        context.getValue("openvpms:set(., 'lastName', 'testSet')");
        party = (Party) service.get(party.getObjectReference());
        assertNotNull(party);
        IMObjectBean bean = new IMObjectBean(party);
        assertEquals("testSet", bean.getString("lastName"));
    }

    /**
     * Tests the openvpms:lookup() function.
     */
    public void testLookup() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setStatus("IN_PROGRESS");
        act.setParticipant("participation.customer", party);

        JXPathContext context = JXPathHelper.newContext(act.getAct());

        checkLookup("In Progress", "status", context);
        checkLookup("Mr", "customer.entity.title", context);

        // test invalid node
        try {
            context.getValue("openvpms:lookup(., 'displayName')");
            fail("expected PropertyResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidProperty);
        }

        // test invalid node with default
        Object value = context.getValue(
                "openvpms:lookup(., 'displayName', 'default')");
        assertEquals("default", value);
    }

    /**
     * Tests the openvpms:defaultLookup function.
     */
    public void testDefaultLookup() {
        // ensure existing lookups are non-default and create some new ones,
        // making the last one created the default.
        Collection<Lookup> lookups
                = LookupServiceHelper.getLookupService().getLookups(
                "lookup.staff");
        for (Lookup lookup : lookups) {
            if (lookup.isDefaultLookup()) {
                lookup.setDefaultLookup(false);
                service.save(lookup);
            }
        }
        Lookup lookup = null;
        for (int i = 0; i < 10; ++i) {
            lookup = (Lookup) service.create("lookup.staff");
            lookup.setCode("CODE" + Math.random());
            lookup.setName(lookup.getCode());
            lookup.setDescription(lookup.getCode());
            service.save(lookup);
        }
        assertNotNull(lookup);
        lookup.setDefaultLookup(true);
        service.save(lookup);

        Party party = createCustomer();
        JXPathContext context = JXPathHelper.newContext(party);
        Object value = context.getValue(
                "openvpms:defaultLookup(.,'classifications')");
        assertNotNull(value);
        assertTrue(value instanceof Lookup);
        assertEquals(lookup, value);
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
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
     * Verifies that an expression evaluates to the expected result.
     *
     * @param expected the expected result
     * @param node     the exoression node
     * @param context  the context
     */
    private void checkEquals(String expected, String node,
                             JXPathContext context) {
        String expression = "openvpms:get(., '" + node + "')";
        assertEquals(expected, context.getValue(expression));
    }

    /**
     * Verifies that a lookup expression evaluates to the expected result.
     *
     * @param expected the expected result
     * @param node     the exoression node
     * @param context  the context
     */
    private void checkLookup(String expected, String node,
                             JXPathContext context) {
        String expression = "openvpms:lookup(., '" + node + "')";
        assertEquals(expected, context.getValue(expression));
    }

    /**
     * Verifies that an exception matches that expected.
     *
     * @param exception the root exception
     * @param code      the code of the expected exception
     */
    private void checkException(JXPathInvalidAccessException exception,
                                PropertyResolverException.ErrorCode code) {
        Throwable target = exception.getCause();
        assertNotNull(target);
        assertTrue(target instanceof PropertyResolverException);
        PropertyResolverException cause = (PropertyResolverException) target;
        assertEquals(code, cause.getErrorCode());
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
