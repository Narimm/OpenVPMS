package org.openvpms.tools.archetype.comparator;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compares {@link ArchetypeDescriptor}s for differences.
 *
 * @author Tim Anderson
 */
public class ArchetypeComparator extends AbstractComparator {

    /**
     * The node comparator.
     */
    private final NodeComparator comparator = new NodeComparator();

    /**
     * Compares two archetypes.
     *
     * @param oldVersion the old version. May be {@code null}
     * @param newVersion the new version. May be {@code null}
     * @return the changes, or {@code null} if the archetypes are the same
     */
    public ArchetypeChange compare(ArchetypeDescriptor oldVersion, ArchetypeDescriptor newVersion) {
        ArchetypeChange result;
        if (oldVersion != null && newVersion != null) {
            List<FieldChange> fieldChanges = getFieldChanges(oldVersion, newVersion);
            List<NodeChange> nodeChanges = getNodeChanges(oldVersion, newVersion);
            result = (fieldChanges.isEmpty() && nodeChanges.isEmpty())
                     ? null : new ArchetypeChange(oldVersion, newVersion, fieldChanges, nodeChanges);
        } else if (oldVersion == null && newVersion == null) {
            result = null;
        } else {
            result = new ArchetypeChange(oldVersion, newVersion);
        }
        return result;
    }

    private List<NodeChange> getNodeChanges(ArchetypeDescriptor oldVersion, ArchetypeDescriptor newVersion) {
        List<NodeChange> result = new ArrayList<NodeChange>();
        Map<String, NodeDescriptor> oldNodes = oldVersion.getNodeDescriptors();
        Map<String, NodeDescriptor> newNodes = newVersion.getNodeDescriptors();
        Set<String> added = getAdded(oldNodes, newNodes);
        Set<String> deleted = getDeleted(oldNodes, newNodes);
        Set<String> retained = getRetained(oldNodes, newNodes);
        for (String addedNode : added) {
            NodeDescriptor node = newNodes.get(addedNode);
            result.add(new NodeChange(null, node));
        }
        for (String deletedNode : deleted) {
            NodeDescriptor node = oldNodes.get(deletedNode);
            result.add(new NodeChange(node, null));
        }
        for (String retainedNode : retained) {
            NodeDescriptor oldNode = oldNodes.get(retainedNode);
            NodeDescriptor newNode = newNodes.get(retainedNode);
            NodeChange change = comparator.compare(oldNode, newNode);
            if (change != null) {
                result.add(change);
            }
        }
        return result;
    }

    private List<FieldChange> getFieldChanges(ArchetypeDescriptor oldVersion, ArchetypeDescriptor newVersion) {
        List<FieldChange> result = new ArrayList<FieldChange>();
        compare(FieldChange.Field.NAME, oldVersion.getName(), newVersion.getName(), result);
        compare(FieldChange.Field.DISPLAY_NAME, oldVersion.getDisplayName(), newVersion.getDisplayName(), result);
        compare(FieldChange.Field.CLASS_NAME, oldVersion.getClassName(), newVersion.getClassName(), result);
        compare(FieldChange.Field.ACTIVE, oldVersion.isActive(), newVersion.isActive(), result);
        compare(FieldChange.Field.PRIMARY, oldVersion.isPrimary(), newVersion.isPrimary(), result);
        return result;
    }

    private void compare(FieldChange.Field field, Object oldVersion, Object newVersion, List<FieldChange> changes) {
        if (!ObjectUtils.equals(oldVersion, newVersion)) {
            changes.add(new FieldChange(field, oldVersion, newVersion));
        }
    }
}
