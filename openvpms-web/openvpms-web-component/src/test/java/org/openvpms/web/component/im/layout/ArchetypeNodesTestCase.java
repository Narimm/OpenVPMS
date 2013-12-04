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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

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
        ArchetypeNodes nodes = new ArchetypeNodes();
        checkSimple(archetype, nodes, "id", "name", "description", "printedName", "drugSchedule", "activeIngredients",
                    "sellingUnits", "dispensingUnits", "dispensingVerb", "label", "dispInstructions", "usageNotes",
                    "active");
        checkComplex(archetype, nodes, "prices", "linked", "type", "investigationTypes", "suppliers", "stockLocations",
                     "reminders", "documents", "discounts", "species", "updates", "classifications", "identities",
                     "equivalents", "taxes", "sourceRelationships");
    }

    /**
     * Verifies that only simple nodes are returned if complex nodes are suppressed.
     */
    @Test
    public void testSimple() {
        ArchetypeNodes nodes = new ArchetypeNodes(true, false);
        checkSimple(archetype, nodes, "id", "name", "description", "printedName", "drugSchedule", "activeIngredients",
                    "sellingUnits", "dispensingUnits", "dispensingVerb", "label", "dispInstructions", "usageNotes",
                    "active");
        checkComplex(archetype, nodes);
    }

    /**
     * Verifies that only complex nodes are returned if simple nodes are suppressed.
     */
    @Test
    public void testComplex() {
        ArchetypeNodes nodes = new ArchetypeNodes(false, true);
        checkSimple(archetype, nodes);
        checkComplex(archetype, nodes, "prices", "linked", "type", "investigationTypes", "suppliers", "stockLocations",
                     "reminders", "documents", "discounts", "species", "updates", "classifications", "identities",
                     "equivalents", "taxes", "sourceRelationships");
    }

    /**
     * Verifies that a complex node can be treated as a simple node.
     */
    @Test
    public void testComplexAsSimple() {
        ArchetypeNodes nodes = new ArchetypeNodes().simple("species");
        checkSimple(archetype, nodes, "id", "name", "description", "printedName", "drugSchedule", "activeIngredients",
                    "sellingUnits", "dispensingUnits", "dispensingVerb", "label", "dispInstructions", "usageNotes",
                    "active", "species");
        checkComplex(archetype, nodes, "prices", "linked", "type", "investigationTypes", "suppliers", "stockLocations",
                     "reminders", "documents", "discounts", "updates", "classifications", "identities",
                     "equivalents", "taxes", "sourceRelationships");
    }

    /**
     * Verifies nodes can be excluded if they are empty.
     */
    @Test
    public void testExcludeIfEmpty() {
        ArchetypeNodes nodes = new ArchetypeNodes().excludeIfEmpty("label", "dispInstructions", "usageNotes", "prices");
        Product product = (Product) create(ProductArchetypes.MEDICATION);

        IMObjectBean bean = new IMObjectBean(product);
        bean.setValue("label", null);            // Boolean
        bean.setValue("dispInstructions", null); // String
        bean.setValue("usageNotes", "");
        product.getProductPrices().clear();

        // verify label, dispInstructions and usageNotes are excluded from simple nodes
        checkSimple(archetype, nodes, product, "id", "name", "description", "printedName", "drugSchedule",
                    "activeIngredients", "sellingUnits", "dispensingUnits", "dispensingVerb", "active");

        // verify prices are excluded from complex nodes
        checkComplex(archetype, nodes, product, "linked", "type", "investigationTypes", "suppliers", "stockLocations",
                     "reminders", "documents", "discounts", "species", "updates", "classifications", "identities",
                     "equivalents", "taxes", "sourceRelationships");

        // populate the nodes and verify they are now returned
        bean.setValue("label", true);
        bean.setValue("dispInstructions", "instructions");
        bean.setValue("usageNotes", "notes");
        product.addProductPrice((ProductPrice) create(ProductArchetypes.FIXED_PRICE));

        checkSimple(archetype, nodes, "id", "name", "description", "printedName", "drugSchedule", "activeIngredients",
                    "sellingUnits", "dispensingUnits", "dispensingVerb", "label", "dispInstructions", "usageNotes",
                    "active");
        checkComplex(archetype, nodes, "prices", "linked", "type", "investigationTypes", "suppliers", "stockLocations",
                     "reminders", "documents", "discounts", "species", "updates", "classifications", "identities",
                     "equivalents", "taxes", "sourceRelationships");
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
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], names[i]);
        }
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
