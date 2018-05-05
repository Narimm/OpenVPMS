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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


/**
 * {@link NodeResolver} test case.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class NodeResolverTestCase extends AbstractIMObjectBeanTestCase {

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookups;

    /**
     * Tests single-level node resolution.
     */
    @Test
    public void testSingleLevelResolution() {
        Party party = createCustomer();
        NodeResolver resolver = new NodeResolver(party, getArchetypeService());
        assertEquals("Foo", resolver.getObject("firstName"));
        assertEquals("Bar", resolver.getObject("lastName"));
        assertEquals("Customer(Person)", resolver.getObject("displayName"));
        assertEquals("party.customerperson", resolver.getObject("shortName"));
    }

    /**
     * Tests multiple-level node resolution.
     */
    @Test
    public void testMultiLevelResolution() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        NodeResolver resolver = new NodeResolver(act.getAct(), getArchetypeService());
        assertEquals("Foo", resolver.getObject("customer.entity.firstName"));
        assertEquals("Bar", resolver.getObject("customer.entity.lastName"));

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
    @Test
    public void testMissingReference() {
        ActBean act = createAct("act.customerEstimation");
        NodeResolver resolver = new NodeResolver(act.getAct(), getArchetypeService());
        assertNull(resolver.getObject("customer.entity.firstName"));
    }

    /**
     * Tests behaviour where an invalid node name is supplied.
     */
    @Test
    public void testInvalidNode() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        NodeResolver resolver = new NodeResolver(act.getAct(), getArchetypeService());

        // root node followed by invalid node
        try {
            resolver.getObject("customer.invalidNode");
            fail("expected IMObjectReportException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidProperty,
                         exception.getErrorCode());
        }

        // intermediate node followed by invalid node
        try {
            resolver.getObject("customer.entity.invalidNode");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidProperty,
                         exception.getErrorCode());
        }

        // leaf node followed by invalid node
        try {
            resolver.getObject("startTime.displayName");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidObject,
                         exception.getErrorCode());
        }
    }

    /**
     * Verifies that the name <em>uid</em> can be used to access the id of an
     * object, in order to support legacy users.
     */
    @Test
    public void testUid() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");

        // verify the archetypes have no uid node.
        ArchetypeDescriptor custDesc = getArchetypeDescriptor(party.getArchetypeId().getShortName());
        ArchetypeDescriptor estimationDesc = getArchetypeDescriptor(act.getAct().getArchetypeId().getShortName());
        assertNull(custDesc.getNodeDescriptor("uid"));
        assertNull(estimationDesc.getNodeDescriptor("uid"));

        // now verify that using uid as a node name returns the id of the object
        act.setParticipant("participation.customer", party);
        NodeResolver resolver = new NodeResolver(act.getAct(), getArchetypeService());
        assertEquals(act.getAct().getId(), resolver.getObject("uid"));
        assertEquals(party.getId(), resolver.getObject("customer.entity.uid"));
    }

    /**
     * Tests lookups.
     */
    @Test
    public void testLookups() {
        Party patient = (Party) create("party.patientpet");
        Lookup species = LookupUtil.createLookup(getArchetypeService(), "lookup.species", "CANINE", "Canine");
        save(species);

        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("species", species.getCode());

        NodeResolver resolver1 = new NodeResolver(patient, getArchetypeService(), lookups);

        assertEquals(species.getCode(), resolver1.getObject("species"));
        assertEquals(species.getCode(), resolver1.getObject("species.code"));
        assertEquals("Canine", resolver1.getObject("species.name"));
        assertEquals(species.getId(), resolver1.getObject("species.id"));
        assertEquals("Species", resolver1.getObject("species.displayName"));

        // test resolution without the lookup service.
        NodeResolver resolver2 = new NodeResolver(patient, getArchetypeService());
        assertEquals(species.getCode(), resolver2.getObject("species"));
        try {
            resolver2.getObject("species.code");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidObject, exception.getErrorCode());
        }
    }

    /**
     * Tests local lookups.
     */
    @Test
    public void testLocalLookups() {
        Party party = createCustomer();
        NodeResolver resolver1 = new NodeResolver(party, getArchetypeService(), lookups);

        assertEquals("MR", resolver1.getObject("title"));
        assertEquals("MR", resolver1.getObject("title.code"));
        assertEquals("Mr", resolver1.getObject("title.name"));

        // test resolution without the lookup service.
        NodeResolver resolver2 = new NodeResolver(party, getArchetypeService());
        assertEquals("MR", resolver2.getObject("title"));
        try {
            resolver2.getObject("species.code");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidNode, exception.getErrorCode());
        }
    }

    /**
     * Tests the {@link NodeResolver#getObjects(String)} method.
     */
    @Test
    public void testGetObjects() {
        Party customer = createCustomer();
        Party patient1 = createPatient(customer);
        Party patient2 = createPatient(customer);

        NodeResolver resolver = new NodeResolver(customer, getArchetypeService(), lookups);
        checkEquals(resolver.getObjects("title"), "MR");
        checkEquals(resolver.getObjects("title.code"), "MR");
        checkEquals(resolver.getObjects("title.name"), "Mr");
        checkEquals(resolver.getObjects("patients.target"), patient1, patient2);
        checkEquals(resolver.getObjects("patients.target.id"), patient1.getId(), patient2.getId());
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

}
