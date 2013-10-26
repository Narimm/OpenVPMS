package org.openvpms.tools.archetype.comparator;

import org.apache.commons.lang.ObjectUtils;

/**
 * Tracks a change between two versions of a descriptor.
 *
 * @author Tim Anderson
 */
public class DescriptorChange<T> {

    /**
     * The old version.
     */
    private final T oldVersion;

    /**
     * The new version.
     */
    private final T newVersion;

    /**
     * Constructs a {@link DescriptorChange}.
     * <p/>
     * Note: at least one of the arguments must be non-null;
     *
     * @param oldVersion the old version. May be {@code null}
     * @param newVersion the new version. May be {@code null}
     */
    public DescriptorChange(T oldVersion, T newVersion) {
        if (oldVersion == null && newVersion == null) {
            throw new IllegalArgumentException("Both oldVersion and newVersion may not be null");
        }
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    /**
     * Determines if the change is an addition.
     *
     * @return {@code true} if the change is an addition, {@code false} if it is an update or deletion
     */
    public boolean isAdd() {
        return oldVersion == null && newVersion != null;
    }

    /**
     * Determines if the change is an update.
     *
     * @return {@code true} if the change is an update, {@code false} if it is an addition or deletion
     */
    public boolean isUpdate() {
        return oldVersion != null && newVersion != null;
    }

    /**
     * Determines if the change is a deletion.
     *
     * @return {@code true} if the change is a deletion, {@code false} if it is an addition or update
     */
    public boolean isDelete() {
        return !isAdd() && !isUpdate();
    }

    /**
     * Returns the old version of the descriptor.
     *
     * @return the old version. May be {@code null}
     */
    public T getOldVersion() {
        return oldVersion;
    }

    /**
     * Returns the new version of the descriptor.
     *
     * @return the new version. May be {@code null}
     */
    public T getNewVersion() {
        return newVersion;
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
        } else if (object instanceof DescriptorChange) {
            DescriptorChange other = (DescriptorChange) object;
            return ObjectUtils.equals(oldVersion, other.getOldVersion())
                   && ObjectUtils.equals(newVersion, other.getNewVersion());
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     * *
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = (newVersion != null) ? newVersion.hashCode() : -1;
        hash ^= (oldVersion != null) ? oldVersion.hashCode() : -1;
        return hash;
    }
}