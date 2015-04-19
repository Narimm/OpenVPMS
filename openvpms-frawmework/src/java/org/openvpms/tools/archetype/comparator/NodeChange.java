package org.openvpms.tools.archetype.comparator;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import java.util.Collections;
import java.util.List;

/**
 * Tracks a changes between versions of an {@link NodeDescriptor}.
 *
 * @author Tim Anderson
 */
public class NodeChange extends DescriptorChange<NodeDescriptor> {

    /**
     * The changes.
     */
    private final List<NodeFieldChange> changes;

    /**
     * Constructs a {@link NodeChange}.
     *
     * @param oldVersion the old version. May be {@code null}
     * @param newVersion the new version. May be {@code null}
     */
    public NodeChange(NodeDescriptor oldVersion, NodeDescriptor newVersion) {
        this(oldVersion, newVersion, Collections.<NodeFieldChange>emptyList());
    }

    /**
     * Constructs a {@link NodeChange}.
     *
     * @param oldVersion the old version. May be {@code null}
     * @param newVersion the new version. May be {@code null}
     * @param changes    the changes
     */
    public NodeChange(NodeDescriptor oldVersion, NodeDescriptor newVersion, List<NodeFieldChange> changes) {
        super(oldVersion, newVersion);
        this.changes = changes;
    }

    /**
     * Returns the node name.
     *
     * @return the node name
     */
    public String getName() {
        return getNewVersion() != null ? getNewVersion().getName() : getOldVersion().getName();
    }

    /**
     * Returns the changes.
     *
     * @return the changes
     */
    public List<NodeFieldChange> getChanges() {
        return changes;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param object the reference object with which to compare.
     * @return {@code true} if this object is the same as the object argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof NodeChange) {
            NodeChange other = (NodeChange) object;
            return super.equals(object) && changes.equals(other.changes);
        }
        return false;
    }
}
