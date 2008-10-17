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

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * {@link NodeResolver} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeResolverTestCase
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
        NodeResolver resolver = new NodeResolver(party, service);
        assertEquals("J", resolver.getObject("firstName"));
        assertEquals("Zoo", resolver.getObject("lastName"));
        assertEquals("Customer(Person)", resolver.getObject("displayName"));
        assertEquals("party.customerperson", resolver.getObject("shortName"));
    }

    /**
     * Tests multiple-level node resolution.
     */
    public void testMultiLevelResolution() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        NodeResolver resolver = new NodeResolver(act.getAct(), service);
        assertEquals("J", resolver.getObject("customer.entity.firstName"));
        assertEquals("Zoo", resolver.getObject("customer.entity.lastName"));

        assertEquals("Estimation", resolver.getObject("displayName"));
        assertEquals("Act Customer",
                     resolver.getObject("customer.displayName"));
        assertEquals("Customer(Person)",
                     resolver.getObject("customer.entity.displayName"));
        assertEquals("party.customerperson",
                     resolver.getObject("customer.entity.shortName"));
    }

    /**
     * Tests behaviour where an intermediate node doesn't exist.
     */
    public void testMissingReference() {
        ActBean act = createAct("act.customerEstimation");
        NodeResolver resolver = new NodeResolver(act.getAct(), service);
        assertNull(resolver.getObject("customer.entity.firstName"));
    }

    /**
     * Tests behaviour where an invalid node name is supplied.
     */
    public void testInvalidNode() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        NodeResolver resolver = new NodeResolver(act.getAct(), service);

        // root node followed by invalid node
        try {
            resolver.getObject("customer.invalidNode");
            fail("expected IMObjectReportException to be thrown");
        } catch (NodeResolverException exception) {
            assertEquals(NodeResolverException.ErrorCode.InvalidNode,
                         exception.getErrorCode());
        }

        // intermediate node followed by invalid node
        try {
            resolver.getObject("customer.entity.invalidNode");
            fail("expected NodeResolverException to be thrown");
        } catch (NodeResolverException exception) {
            assertEquals(NodeResolverException.ErrorCode.InvalidNode,
                         exception.getErrorCode());
        }

        // leaf node followed by invalid node
        try {
            resolver.getObject("startTime.displayName");
            fail("expected NodeResolverException to be thrown");
        } catch (NodeResolverException exception) {
            assertEquals(NodeResolverException.ErrorCode.InvalidObject,
                         exception.getErrorCode());
        }
    }

    /**
     * Verifies that the name <em>uid</em> can be used to access the id of an
     * object, in order to support legacy users.
     */
    public void testUid() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");

        // verify the archetypes have no uid node.
        ArchetypeDescriptor custDesc = service.getArchetypeDescriptor(
                party.getArchetypeId());
        ArchetypeDescriptor estimationDesc = service.getArchetypeDescriptor(
                act.getAct().getArchetypeId());
        assertNull(custDesc.getNodeDescriptor("uid"));
        assertNull(estimationDesc.getNodeDescriptor("uid"));

        // now verify that using uid as a node name returns the id of the object
        act.setParticipant("participation.customer", party);
        NodeResolver resolver = new NodeResolver(act.getAct(), service);
        assertEquals(act.getAct().getId(), resolver.getObject("uid"));
        assertEquals(party.getId(), resolver.getObject("customer.entity.uid"));
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
