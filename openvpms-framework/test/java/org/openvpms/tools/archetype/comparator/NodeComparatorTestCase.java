package org.openvpms.tools.archetype.comparator;

import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

        node2.setDisplayName("foo");
        checkSame(node1, node2);
    }

    /**
     * Tests type changes.
     */
    @Test
    public void testType() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setType("java.lang.Integer");
        node2.setType("java.lang.Long");
        checkChanges(node1, node2, new NodeFieldChange(Field.TYPE, "java.lang.Integer", "java.lang.Long"));

        node2.setType("java.lang.Integer");
        checkSame(node1, node2);
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

        node2.setBaseName("foo");
        checkSame(node1, node2);
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

        node1.setPath("/bar");
        checkSame(node1, node2);
    }

    /**
     * Tests parentChild changes.
     */
    @Test
    public void testParentChild() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setType("java.util.Collection");
        node2.setType("java.util.Collection");
        node1.setParentChild(true);
        node2.setParentChild(false);
        checkChanges(node1, node2, new NodeFieldChange(Field.PARENT_CHILD, true, false));

        node1.setParentChild(false);
        checkSame(node1, node2);
    }

    /**
     * Tests minLength changes.
     */
    @Test
    public void testMinLength() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setMinLength(10);
        node2.setMinLength(20);
        checkChanges(node1, node2, new NodeFieldChange(Field.MIN_LENGTH, 10, 20));

        node2.setMinLength(10);
        checkSame(node1, node2);
    }

    /**
     * Tests maxLength changes.
     */
    @Test
    public void testMaxLength() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setMaxLength(5);
        node2.setMaxLength(10);
        checkChanges(node1, node2, new NodeFieldChange(Field.MAX_LENGTH, 5, 10));

        node1.setMaxLength(10);
        checkSame(node1, node2);
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

        node1.setMinCardinality(1);
        checkSame(node1, node2);
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

        node1.setMaxCardinality(-1);
        checkSame(node1, node2);
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

        node1.setFilter("product.medication");
        checkSame(node1, node2);
    }

    /**
     * Tests default value changes.
     */
    @Test
    public void testDefaultValue() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setDefaultValue("one");
        node2.setDefaultValue("1");
        checkChanges(node1, node2, new NodeFieldChange(Field.DEFAULT_VALUE, "one", "1"));

        node2.setDefaultValue("one");
        checkSame(node1, node2);
    }

    /**
     * Tests read-only changes.
     */
    @Test
    public void testReadOnly() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setReadOnly(true);
        node2.setReadOnly(false);
        checkChanges(node1, node2, new NodeFieldChange(Field.READ_ONLY, true, false));

        node1.setReadOnly(false);
        checkSame(node1, node2);
    }

    /**
     * Tests hidden changes.
     */
    @Test
    public void testHidden() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setHidden(true);
        node2.setHidden(false);
        checkChanges(node1, node2, new NodeFieldChange(Field.HIDDEN, true, false));

        node2.setHidden(true);
        checkSame(node1, node2);
    }

    /**
     * Tests derived changes.
     */
    @Test
    public void testDerived() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        node1.setDerived(true);
        node2.setDerived(false);
        checkChanges(node1, node2, new NodeFieldChange(Field.DERIVED, true, false));

        node1.setDerived(false);
        checkSame(node1, node2);
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

        node1.setDerivedValue("/description");
        checkSame(node1, node2);
    }

    /**
     * Tests changes to the error message in assertion descriptors.
     */
    @Test
    public void testAssertionErrorMessageChanges() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        AssertionDescriptor assertion1 = new AssertionDescriptor();
        assertion1.setErrorMessage("foo");
        AssertionDescriptor assertion2 = new AssertionDescriptor();
        assertion2.setErrorMessage("bar");

        node1.addAssertionDescriptor(assertion1);
        node2.addAssertionDescriptor(assertion2);

        checkChanges(node1, node2, new NodeFieldChange(Field.ASSERTION, assertion1, assertion2));

        assertion2.setErrorMessage("foo");
        checkSame(node1, node2);
    }

    /**
     * Tests changes to the properties in assertion descriptors.
     */
    @Test
    public void testAssertionPropertyChanges() {
        NodeDescriptor node1 = new NodeDescriptor();
        NodeDescriptor node2 = new NodeDescriptor();
        AssertionDescriptor assertion1 = new AssertionDescriptor();
        assertion1.addProperty(createProperty("foo", "value"));
        AssertionDescriptor assertion2 = new AssertionDescriptor();
        assertion1.addProperty(createProperty("foo", "value2"));

        node1.addAssertionDescriptor(assertion1);
        node2.addAssertionDescriptor(assertion2);

        checkChanges(node1, node2, new NodeFieldChange(Field.ASSERTION, assertion1, assertion2));
    }

    /**
     * Verifies node descriptor changes against those expected.
     *
     * @param oldVersion the old version of the descriptor
     * @param newVersion the new version of the descriptor
     * @param expected   the expected changes
     */
    private void checkChanges(NodeDescriptor oldVersion, NodeDescriptor newVersion, NodeFieldChange... expected) {
        List<NodeFieldChange> changes = comparator.compare(oldVersion, newVersion).getChanges();
        assertEquals(expected.length, changes.size());
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], changes.get(i));
        }
    }

    /**
     * Verifies two descriptors are the same.
     *
     * @param oldVersion the old version of the descriptor
     * @param newVersion the new version of the descriptor
     */
    private void checkSame(NodeDescriptor oldVersion, NodeDescriptor newVersion) {
        assertNull(comparator.compare(oldVersion, newVersion));
    }

    /**
     * Creates an assertion property.
     *
     * @param name  the property name
     * @param value the property value
     * @return a new assertion property
     */
    private AssertionProperty createProperty(String name, String value) {
        AssertionProperty result = new AssertionProperty();
        result.setName(name);
        result.setValue(value);
        return result;
    }

}
