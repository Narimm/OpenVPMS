package org.openvpms.tools.archetype.comparator;

import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openvpms.tools.archetype.comparator.FieldChange.Field;

/**
 * Tests the {@link ArchetypeComparator} class.
 *
 * @author Tim Anderson
 */
public class ArchetypeComparatorTestCase {

    /**
     * The comparator.
     */
    private final ArchetypeComparator comparator = new ArchetypeComparator();

    /**
     * Tests archetype changes.
     */
    @Test
    public void testArchetypeChange() {
        ArchetypeDescriptor descriptor1 = new ArchetypeDescriptor();
        ArchetypeDescriptor descriptor2 = new ArchetypeDescriptor();
        descriptor1.setDisplayName("d1");
        descriptor2.setDisplayName("d2");

        assertNull(comparator.compare(null, null));
        assertNull(comparator.compare(descriptor1, descriptor1));

        checkChange(descriptor1, null, false, false, true);
        checkChange(null, descriptor1, true, false, false);
        checkChange(descriptor1, descriptor2, false, true, false);
    }

    /**
     * Tests archetype name changes.
     */
    @Test
    public void testNameChange() {
        ArchetypeDescriptor descriptor1 = new ArchetypeDescriptor();
        ArchetypeDescriptor descriptor2 = new ArchetypeDescriptor();
        descriptor1.setName("act.foo.1");
        descriptor2.setName("act.foo.2");
        checkFieldChanges(descriptor1, descriptor2, new FieldChange(Field.NAME, "act.foo.1", "act.foo.2"));
    }

    /**
     * Tests archetype display name changes.
     */
    @Test
    public void testDisplayNameChange() {
        ArchetypeDescriptor descriptor1 = new ArchetypeDescriptor();
        ArchetypeDescriptor descriptor2 = new ArchetypeDescriptor();
        descriptor1.setDisplayName("Foo");
        descriptor2.setDisplayName("Bar");
        checkFieldChanges(descriptor1, descriptor2, new FieldChange(Field.DISPLAY_NAME, "Foo", "Bar"));
    }

    /**
     * Tests archetype class name changes.
     */
    @Test
    public void testClassNameChange() {
        ArchetypeDescriptor descriptor1 = new ArchetypeDescriptor();
        ArchetypeDescriptor descriptor2 = new ArchetypeDescriptor();
        descriptor1.setClassName("Foo");
        descriptor2.setClassName("Bar");
        checkFieldChanges(descriptor1, descriptor2, new FieldChange(Field.CLASS_NAME, "Foo", "Bar"));
    }

    /**
     * Tests archetype primary flag changes.
     */
    @Test
    public void testPrimaryChange() {
        ArchetypeDescriptor descriptor1 = new ArchetypeDescriptor();
        ArchetypeDescriptor descriptor2 = new ArchetypeDescriptor();
        descriptor1.setPrimary(false);
        descriptor2.setPrimary(true);
        checkFieldChanges(descriptor1, descriptor2, new FieldChange(Field.PRIMARY, false, true));
    }

    /**
     * Tests archetype active flag changes.
     */
    @Test
    public void testActive() {
        ArchetypeDescriptor descriptor1 = new ArchetypeDescriptor();
        ArchetypeDescriptor descriptor2 = new ArchetypeDescriptor();
        descriptor1.setActive(false);
        descriptor2.setActive(true);
        checkFieldChanges(descriptor1, descriptor2, new FieldChange(Field.ACTIVE, false, true));
    }

    /**
     * Tests node changes.
     */
    @Test
    public void testNodeChange() {
        ArchetypeDescriptor descriptor1 = new ArchetypeDescriptor();
        ArchetypeDescriptor descriptor2 = new ArchetypeDescriptor();
        NodeDescriptor node = createNode("foo");
        descriptor1.addNodeDescriptor(node);
        checkNodeChanges(descriptor1, descriptor2, new NodeChange(node, null));
        checkNodeChanges(descriptor2, descriptor1, new NodeChange(null, node));
    }

    /**
     * Helper to create a node descriptor.
     *
     * @param name the node name
     * @return a new descriptor
     */
    private NodeDescriptor createNode(String name) {
        NodeDescriptor result = new NodeDescriptor();
        result.setName(name);
        return result;
    }

    /**
     * Verifies that the specified expected field changes are detected between an old and new version of an
     * archetype descriptor.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @param changes    the expected field changes
     */
    private void checkFieldChanges(ArchetypeDescriptor oldVersion, ArchetypeDescriptor newVersion,
                                   FieldChange... changes) {
        ArchetypeChange archetypeChange = comparator.compare(oldVersion, newVersion);
        assertEquals(changes.length, archetypeChange.getFieldChanges().size());
        for (int i = 0; i < changes.length; ++i) {
            assertEquals(changes[i], archetypeChange.getFieldChanges().get(i));
        }
    }

    /**
     * Verifies that the specified expected node changes are detected between an old and new version of an
     * archetype descriptor.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @param changes    the expected node changes
     */
    private void checkNodeChanges(ArchetypeDescriptor oldVersion, ArchetypeDescriptor newVersion,
                                  NodeChange... changes) {
        ArchetypeChange archetypeChange = comparator.compare(oldVersion, newVersion);
        assertEquals(changes.length, archetypeChange.getNodeChanges().size());
        for (int i = 0; i < changes.length; ++i) {
            assertEquals(changes[i], archetypeChange.getNodeChanges().get(i));
        }
    }

    /**
     * Verifies that the add/update/delete status of a change is detected between an old and new version of an
     * archetype descriptor.
     *
     * @param oldVersion the old version. May be {@code null}
     * @param newVersion the new version. May be {@code null}
     * @param isAdd      if {@code true}, the change is expected to be an addition
     * @param isUpdate   if {@code true}, the change is expected to be an update
     * @param isDelete   if {@code true}, the change is expected to be a deletion
     */
    private void checkChange(ArchetypeDescriptor oldVersion, ArchetypeDescriptor newVersion,
                             boolean isAdd, boolean isUpdate, boolean isDelete) {
        ArchetypeChange change = comparator.compare(oldVersion, newVersion);
        assertEquals(change.getOldVersion(), oldVersion);
        assertEquals(change.getNewVersion(), newVersion);
        assertEquals(isAdd, change.isAdd());
        assertEquals(isUpdate, change.isUpdate());
        assertEquals(isDelete, change.isDelete());
    }
}
