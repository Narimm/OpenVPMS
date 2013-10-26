package org.openvpms.tools.archetype.comparator;

import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptorWriter;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.tools.archetype.comparator.NodeFieldChange.Field;

/**
 * Tests the {@link NodeComparator}.
 *
 * @author Tim Anderson
 */
public class NodeComparatorTestCase {

    /**
     * The comparator.
     */
    private final NodeComparator comparator = new NodeComparator();

    /**
     * Tests display name changes.
     */
    @Test
    public void testDisplayName() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setDisplayName("foo");
        node2.setDisplayName("bar");
        checkChanges(node1, node2, new NodeFieldChange(Field.DISPLAY_NAME, "foo", "bar"));
    }

    /**
     * Tests path changes.
     */
    @Test
    public void testPath() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setPath("/foo");
        node2.setPath("/bar");
        checkChanges(node1, node2, new NodeFieldChange(Field.PATH, "/foo", "/bar"));
    }

    /**
     * Tests base name changes.
     */
    @Test
    public void testBaseName() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setBaseName("foo");
        node2.setBaseName("bar");
        checkChanges(node1, node2, new NodeFieldChange(Field.BASE_NAME, "foo", "bar"));
    }

    /**
     * Tests default value changes.
     */
    @Test
    public void testDefaultValue() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setDefaultValue("'one'");
        node2.setDefaultValue("1");
        checkChanges(node1, node2, new NodeFieldChange(Field.DEFAULT_VALUE, "'one'", "1"));
    }

    /**
     * Tests derived value changes.
     */
    @Test
    public void testDerivedValue() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setDerivedValue("/name");
        node2.setDerivedValue("/description");
        checkChanges(node1, node2, new NodeFieldChange(Field.DERIVED_VALUE, "/name", "/description"));
    }

    /**
     * Tests min cardinality changes.
     */
    @Test
    public void testMinCardinality() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setMinCardinality(0);
        node2.setMinCardinality(1);
        checkChanges(node1, node2, new NodeFieldChange(Field.MIN_CARDINALITY, 0, 1));
    }

    /**
     * Tests max cardinality changes.
     */
    @Test
    public void testMaxCardinality() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setMaxCardinality(1);
        node2.setMaxCardinality(-1);
        checkChanges(node1, node2, new NodeFieldChange(Field.MAX_CARDINALITY, 1, -1));
    }

    /**
     * Tests filter changes.
     */
    @Test
    public void testFilter() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setFilter("product.*");
        node2.setFilter("product.medication");
        checkChanges(node1, node2, new NodeFieldChange(Field.FILTER, "product.*", "product.medication"));
    }

    /**
     * Tests
     */
    @Test
    public void testAssertionChanges() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.addAssertionDescriptor(new AssertionDescriptor());
        node2.addAssertionDescriptor(new AssertionDescriptor());

    }

    @Test
    public void testPrintAssertionDescriptor() {
        AssertionDescriptor descriptor = new AssertionDescriptor();
        descriptor.setName("lookup.local");
        PropertyList property = new PropertyList();
        property.setName("entries");
        AssertionProperty inProgress = new AssertionProperty();
        inProgress.setName("IN_PROGRESS");
        inProgress.setValue("In Progress");
        property.addProperty(inProgress);
        descriptor.addProperty(property);
        ArchetypeDescriptorWriter writer = new ArchetypeDescriptorWriter(true, true);
        writer.write(descriptor, System.out);
    }

    private void checkChanges(NodeDescriptor oldVersion, NodeDescriptor newVersion, NodeFieldChange... expected) {
        List<NodeFieldChange> changes = comparator.compare(oldVersion, newVersion).getChanges();
        assertEquals(expected.length, changes.size());
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], changes.get(i));
        }

    }
}
