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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link ArchetypeNodes} class.
 *
 * @author Tim Anderson
 */
public class ArchetypeNodesTestCase extends ArchetypeServiceTest {

    /**
     * The test archetype descriptor.
     */
    private ArchetypeDescriptor archetype;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        archetype = getArchetypeService().getArchetypeDescriptor(ProductArchetypes.MEDICATION);
        assertNotNull(archetype);
    }

    /**
     * Verifies that all nodes are returned by {@link ArchetypeNodes#getSimpleNodes} and
     * {@link ArchetypeNodes#getComplexNodes} for the default constructor and no other options.
     */
    @Test
    public void testAll() {
        ArchetypeNodes nodes = ArchetypeNodes.all().hidden(true);
        checkSimple(archetype, nodes, "id", "name", "description", "printedName", "drugSchedule", "activeIngredients",
                    "concentration", "concentrationUnits", "sellingUnits", "dispensingUnits", "dispensingVerb", "label",
                    "dispInstructions", "type", "pharmacy", "templateOnly", "patientIdentity", "active", "usageNotes",
                    "locations");
        checkComplex(archetype, nodes, "prices", "doses", "linked", "investigationTypes", "suppliers", "stockLocations",
                     "reminders", "alerts", "documents", "discounts", "species", "updates", "classifications",
                     "identities", "equivalents", "taxes", "sourceRelationships");
    }

    /**
     * Verifies that only simple nodes are returned if complex nodes are suppressed.
     */
    @Test
    public void testSimple() {
        ArchetypeNodes nodes = ArchetypeNodes.allSimple();
        checkSimple(archetype, nodes, "id", "name", "description", "printedName", "drugSchedule", "activeIngredients",
                    "concentration", "concentrationUnits", "sellingUnits", "dispensingUnits", "dispensingVerb", "label",
                    "dispInstructions", "type", "pharmacy", "templateOnly", "patientIdentity", "active", "usageNotes");
        checkComplex(archetype, nodes);
    }

    /**
     * Verifies that only complex nodes are returned if simple nodes are suppressed.
     */
    @Test
    public void testComplex() {
        ArchetypeNodes nodes = ArchetypeNodes.allComplex().hidden(true);
        checkSimple(archetype, nodes);
        checkComplex(archetype, nodes, "prices", "doses", "linked", "investigationTypes", "suppliers", "stockLocations",
                     "reminders", "alerts", "documents", "discounts", "species", "updates", "classifications",
                     "identities", "equivalents", "taxes", "sourceRelationships");
    }

    /**
     * Verifies that a complex node can be treated as a simple node.
     */
    @Test
    public void testComplexAsSimple() {
        ArchetypeNodes nodes = ArchetypeNodes.all().simple("species").hidden(true);
        checkSimple(archetype, nodes, "id", "name", "description", "printedName", "drugSchedule", "activeIngredients",
                    "concentration", "concentrationUnits", "sellingUnits", "dispensingUnits", "dispensingVerb", "label",
                    "dispInstructions", "type", "pharmacy", "templateOnly", "patientIdentity", "active", "usageNotes",
                    "locations", "species");
        checkComplex(archetype, nodes, "prices", "doses", "linked", "investigationTypes", "suppliers", "stockLocations",
                     "reminders", "alerts", "documents", "discounts", "updates", "classifications", "identities",
                     "equivalents", "taxes", "sourceRelationships");
    }

    /**
     * Tests the {@link ArchetypeNodes#onlySimple(String...)} method.
     */
    @Test
    public void testOnlySimple() {
        ArchetypeNodes nodes = ArchetypeNodes.onlySimple("id", "name", "description").hidden(true);
        checkSimple(archetype, nodes, "id", "name", "description");
        checkComplex(archetype, nodes);
    }

    /**
     * Verifies nodes can be excluded.
     */
    @Test
    public void testExclude() {
        ArchetypeNodes nodes = ArchetypeNodes.all().exclude("label", "dispInstructions", "usageNotes", "prices")
                .hidden(true);
        Product product = (Product) create(ProductArchetypes.MEDICATION);

        // verify label, dispInstructions and usageNotes are excluded from simple nodes
        checkSimple(archetype, nodes, product, "id", "name", "description", "printedName", "drugSchedule",
                    "activeIngredients", "concentration", "concentrationUnits", "sellingUnits", "dispensingUnits",
                    "dispensingVerb", "type", "pharmacy", "templateOnly", "patientIdentity", "active", "locations");

        // verify prices are excluded from complex nodes
        checkComplex(archetype, nodes, product, "doses", "linked", "investigationTypes", "suppliers", "stockLocations",
                     "reminders", "alerts", "documents", "discounts", "species", "updates", "classifications",
                     "identities", "equivalents", "taxes", "sourceRelationships");
    }

    /**
     * Verifies nodes can be excluded if they are empty.
     */
    @Test
    public void testExcludeIfEmpty() {
        ArchetypeNodes nodes = ArchetypeNodes.all().hidden(true)
                .excludeIfEmpty("label", "dispInstructions", "usageNotes", "prices");
        Product product = (Product) create(ProductArchetypes.MEDICATION);

        IMObjectBean bean = new IMObjectBean(product);
        bean.setValue("label", null);            // Boolean
        bean.setValue("dispInstructions", null); // String
        bean.setValue("usageNotes", "");
        product.getProductPrices().clear();

        // verify label, dispInstructions and usageNotes are excluded from simple nodes
        checkSimple(archetype, nodes, product, "id", "name", "description", "printedName", "drugSchedule",
                    "activeIngredients", "concentration", "concentrationUnits", "sellingUnits", "dispensingUnits",
                    "dispensingVerb", "type", "pharmacy", "templateOnly", "patientIdentity", "active", "locations");

        // verify prices are excluded from complex nodes
        checkComplex(archetype, nodes, product, "doses", "linked", "investigationTypes", "suppliers",
                     "stockLocations", "reminders", "alerts", "documents", "discounts", "species", "updates",
                     "classifications", "identities", "equivalents", "taxes", "sourceRelationships");

        // populate the nodes and verify they are now returned
        bean.setValue("label", true);
        bean.setValue("dispInstructions", "instructions");
        bean.setValue("usageNotes", "notes");
        product.addProductPrice((ProductPrice) create(ProductArchetypes.FIXED_PRICE));

        checkSimple(archetype, nodes, product, "id", "name", "description", "printedName", "drugSchedule",
                    "activeIngredients", "concentration", "concentrationUnits", "sellingUnits", "dispensingUnits",
                    "dispensingVerb", "label", "dispInstructions", "type", "pharmacy", "templateOnly",
                    "patientIdentity", "active", "usageNotes", "locations");
        checkComplex(archetype, nodes, product, "prices", "doses", "linked", "investigationTypes", "suppliers",
                     "stockLocations", "reminders", "alerts", "documents", "discounts", "species", "updates",
                     "classifications", "identities", "equivalents", "taxes", "sourceRelationships");
    }

    /**
     * Tests the {@link ArchetypeNodes#excludeStringLongerThan} method.
     */
    @Test
    public void testExcludeStringLongerThan() {
        ArchetypeDescriptor location = getArchetypeService().getArchetypeDescriptor(ContactArchetypes.LOCATION);
        assertNotNull(location);

        checkNodeNames(ArchetypeNodes.allSimple().excludeStringLongerThan(100), location, "preferred");
        checkNodeNames(ArchetypeNodes.allSimple().excludeStringLongerThan(255), location,
                       "name", "suburb", "postcode", "state", "preferred");
    }

    /**
     * Tests the behaviour of {@link ArchetypeNodes#order}.
     */
    @Test
    public void testOrder() {
        // default ordering
        checkSimple(archetype, ArchetypeNodes.all().hidden(true), "id", "name", "description", "printedName",
                    "drugSchedule", "activeIngredients", "concentration", "concentrationUnits", "sellingUnits",
                    "dispensingUnits", "dispensingVerb", "label", "dispInstructions", "type", "pharmacy",
                    "templateOnly", "patientIdentity", "active", "usageNotes", "locations");

        // now place the printedName before the description
        ArchetypeNodes nodes = ArchetypeNodes.all().hidden(true).order("printedName", "description");
        checkSimple(archetype, nodes, "id", "name", "printedName", "description", "drugSchedule", "activeIngredients",
                    "concentration", "concentrationUnits", "sellingUnits", "dispensingUnits", "dispensingVerb", "label",
                    "dispInstructions", "type", "pharmacy", "templateOnly", "patientIdentity", "active", "usageNotes",
                    "locations");
    }

    /**
     * Tests the {@link ArchetypeNodes#getNodeNames(List)} method.
     */
    @Test
    public void testGetNodeNames() {
        IArchetypeService service = getArchetypeService();
        ArchetypeDescriptor location = service.getArchetypeDescriptor(ContactArchetypes.LOCATION);
        ArchetypeDescriptor email = service.getArchetypeDescriptor(ContactArchetypes.EMAIL);
        assertNotNull(location);
        assertNotNull(email);
        List<ArchetypeDescriptor> archetypes = Arrays.asList(location, email);
        checkNodeNames(ArchetypeNodes.allSimple().hidden(true), archetypes, "id", "name", "description", "preferred",
                       "startDate", "endDate");
        checkNodeNames(ArchetypeNodes.allSimple().hidden(true).simple("address", "emailAddress")
                               .order("address", "emailAddress"),
                       archetypes, "id", "name", "description", "address", "emailAddress", "preferred", "startDate",
                       "endDate");
    }

    /**
     * Tests the {@link ArchetypeNodes#excludePassword(boolean)} method.
     */
    @Test
    public void testExcludePassword() {
        ArchetypeDescriptor user = getArchetypeService().getArchetypeDescriptor(UserArchetypes.USER);
        assertNotNull(user);
        checkNodeNames(ArchetypeNodes.allSimple().excludePassword(false), user, "id", "username", "password", "name",
                       "description", "active", "title", "firstName", "lastName", "qualifications", "userLevel",
                       "editPreferences", "colour");
        checkNodeNames(ArchetypeNodes.allSimple().excludePassword(true), user, "id", "username", "name",
                       "description", "active", "title", "firstName", "lastName", "qualifications", "userLevel",
                       "editPreferences", "colour");
    }

    /**
     * Verifies that {@link ArchetypeNodes#getNodeNames(List)} returns the expected nodes.
     *
     * @param nodes     the nodes
     * @param archetype the archetype to test
     * @param names     the expected names
     */
    private void checkNodeNames(ArchetypeNodes nodes, ArchetypeDescriptor archetype, String... names) {
        checkNodeNames(nodes, Collections.singletonList(archetype), names);
    }

    /**
     * Verifies that {@link ArchetypeNodes#getNodeNames(List)} returns the expected nodes.
     *
     * @param nodes      the nodes
     * @param archetypes the archetypes to test
     * @param names      the expected names
     */
    private void checkNodeNames(ArchetypeNodes nodes, List<ArchetypeDescriptor> archetypes, String... names) {
        List<String> expected = Arrays.asList(names);
        List<String> actual = nodes.getNodeNames(archetypes);
        assertEquals("Expected=" + StringUtils.join(expected, ",") + ". Actual=" + StringUtils.join(actual, ","),
                     expected, actual);
    }


    /**
     * Verifies that the expected simple nodes are returned, in the correct order.
     *
     * @param archetype the archetype descriptor
     * @param nodes     the nodes
     * @param expected  the expected nodes
     */
    private void checkSimple(ArchetypeDescriptor archetype, ArchetypeNodes nodes, String... expected) {
        List<NodeDescriptor> actual = nodes.getSimpleNodes(archetype);
        checkNodes(expected, actual);
    }

    /**
     * Verifies that the expected simple nodes are returned, in the correct order.
     *
     * @param archetype the archetype descriptor
     * @param nodes     the nodes
     * @param object    the object to return nodes for
     * @param expected  the expected nodes
     */
    private void checkSimple(ArchetypeDescriptor archetype, ArchetypeNodes nodes, IMObject object, String... expected) {
        List<NodeDescriptor> actual = nodes.getSimpleNodes(archetype, object, null);
        checkNodes(expected, actual);
    }

    /**
     * Verifies that the expected complex nodes are returned, in the correct order.
     *
     * @param archetype the archetype descriptor
     * @param nodes     the nodes
     * @param expected  the expected nodes
     */
    private void checkComplex(ArchetypeDescriptor archetype, ArchetypeNodes nodes, String... expected) {
        List<NodeDescriptor> actual = nodes.getComplexNodes(archetype);
        checkNodes(expected, actual);
    }

    /**
     * Verifies that the expected complex nodes are returned, in the correct order.
     *
     * @param archetype the archetype descriptor
     * @param nodes     the nodes
     * @param object    the object to return nodes for
     * @param expected  the expected nodes
     */
    private void checkComplex(ArchetypeDescriptor archetype, ArchetypeNodes nodes, IMObject object,
                              String... expected) {
        List<NodeDescriptor> actual = nodes.getComplexNodes(archetype, object, null);
        checkNodes(expected, actual);
    }

    /**
     * Verifies that the expected nodes are returned, in the correct order.
     *
     * @param expected the expected nodes
     * @param actual   the actual nodes
     */
    private void checkNodes(String[] expected, List<NodeDescriptor> actual) {
        String[] names = getNames(actual);
        assertArrayEquals("Expected=" + StringUtils.join(expected, ",") + ". Actual=" + StringUtils.join(names, ","),
                          expected, names);
    }

    /**
     * Returns the node names from a collection of node descriptors.
     *
     * @param descriptors the node descriptors
     * @return the node descriptor names
     */
    private String[] getNames(List<NodeDescriptor> descriptors) {
        String[] result = new String[descriptors.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = descriptors.get(i).getName();
        }
        return result;
    }

}
