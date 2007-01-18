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
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.NodeResolverException;
import static org.openvpms.component.business.service.archetype.helper.NodeResolverException.ErrorCode.InvalidNode;
import static org.openvpms.component.business.service.archetype.helper.NodeResolverException.ErrorCode.InvalidObject;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * {@link ArchetypeServiceFunctions} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ArchetypeServiceFunctionsTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Tests single-level node resolution.
     */
    public void testSingleLevelResolution() {
        Party party = createCustomer();
        JXPathContext context = JXPathHelper.newContext(party);

        checkEquals("J", "firstName", context);
        checkEquals("Zoo", "lastName", context);
        checkEquals("Customer(Person)", "displayName", context);
    }

    /**
     * Tests multiple-level node resolution.
     */
    public void testMultiLevelResolution() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);

        JXPathContext context = JXPathHelper.newContext(act.getAct());
        checkEquals("J", "customer.entity.firstName", context);
        checkEquals("Zoo", "customer.entity.lastName", context);
    }

    /**
     * Tests behaviour where an intermediate node doesn't exist.
     */
    public void testMissingReference() {
        ActBean act = createAct("act.customerEstimation");
        JXPathContext context = JXPathHelper.newContext(act.getAct());
        assertNull(
                context.getValue(
                        "openvpms:get(., 'customer.entity.firstName')"));
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
            fail("expected NodeResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidNode);
        }

        // intermediate node followed by invalid node
        try {
            context.getValue("openvpms:get(., 'customer.entity.invalidNode')");
            fail("expected NodeResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidNode);
        }

        // leaf node followed by invalid node
        try {
            context.getValue("openvpms:get(., 'startTime.displayName')");
            fail("expected NodeResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidObject);
        }

        // invalid node with default value
        Object value = context.getValue(
                "openvpms:get(., 'invalidNode', 'default')");
        assertEquals("default", value);
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
            fail("expected NodeResolverException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidNode);
        }

        // test invalid node with default
        Object value = context.getValue(
                "openvpms:lookup(., 'displayName', 'default')");
        assertEquals("default", value);
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
                                NodeResolverException.ErrorCode code) {
        Throwable target = exception.getCause();
        assertNotNull(target);
        assertTrue(target instanceof NodeResolverException);
        NodeResolverException cause = (NodeResolverException) target;
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
