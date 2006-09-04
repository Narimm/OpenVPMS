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

package org.openvpms.report.jxpath;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.report.IMObjectReportException;
import static org.openvpms.report.IMObjectReportException.ErrorCode.InvalidNode;
import static org.openvpms.report.IMObjectReportException.ErrorCode.InvalidObject;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * {@link ReportFunctions} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportFunctionsTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService _service;


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
                context.getValue("report:get(., 'customer.entity.firstName')"));
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
            context.getValue("report:get(., 'customer.invalidNode')");
            fail("expected IMObjectReportException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidNode);
        }

        // intermediate node followed by invalid node
        try {
            context.getValue("report:get(., 'customer.entity.invalidNode')");
            fail("expected IMObjectReportException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidNode);
        }

        // leaf node followed by invalid node
        try {
            context.getValue("report:get(., 'startTime.displayName')");
            fail("expected IMObjectReportException to be thrown");
        } catch (JXPathInvalidAccessException exception) {
            checkException(exception, InvalidObject);
        }
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        _service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
        assertNotNull(_service);
    }

    private void checkEquals(String expected, String node,
                             JXPathContext context) {
        String expression = "report:get(., '" + node + "')";
        assertEquals(expected, context.getValue(expression));
    }

    private void checkException(JXPathInvalidAccessException exception,
                                IMObjectReportException.ErrorCode code) {
        Throwable target = exception.getCause();
        assertNotNull(target);
        assertTrue(target instanceof IMObjectReportException);
        IMObjectReportException cause = (IMObjectReportException) target;
        assertEquals(code, cause.getErrorCode());
    }

    /**
     * Helper to create a new object.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    private IMObject create(String shortName) {
        IMObject object = _service.create(shortName);
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
        bean.setValue("firstName", "J");
        bean.setValue("lastName", "Zoo");
        Contact contact = (Contact) create("contact.phoneNumber");
        assertNotNull(contact);
        bean.addValue("contacts", contact);
        bean.save();
        return (Party) bean.getObject();
    }
}
