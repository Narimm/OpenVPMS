package org.openvpms.web.component.im.layout;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

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
        archetype = getArchetypeService().getArchetypeDescriptor("product.medication");
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
