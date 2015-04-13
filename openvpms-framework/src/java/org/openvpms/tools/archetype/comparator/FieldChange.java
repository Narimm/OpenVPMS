package org.openvpms.tools.archetype.comparator;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;

/**
 * Tracks a field change between versions of an {@link ArchetypeDescriptor}.
 *
 * @author Tim Anderson
 */
public class FieldChange extends DescriptorChange<Object> {

    public enum Field {
        NAME("name"),
        DISPLAY_NAME("displayName"),
        CLASS_NAME("className"),
        PRIMARY("primary"),
        ACTIVE("active");

        @Override
        public String toString() {
            return displayName;
        }

        private Field(String displayName) {
            this.displayName = displayName;
        }

        private final String displayName;
    }

    /**
     * The field.
     */
    private final Field field;

    /**
     * Constructs an {@link FieldChange}.
     *
     * @param field      the field
     * @param oldVersion the old value. May be {@code null}
     * @param newVersion the new value. May be {@code null}
     */
    public FieldChange(Field field, Object oldVersion, Object newVersion) {
        super(oldVersion, newVersion);
        this.field = field;
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    public Field getField() {
        return field;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param object the reference object with which to compare.
     * @return {@code true} if this object is the same as the object argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (super.equals(object)) {
            FieldChange other = (FieldChange) object;
            return field == other.field;
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return field.hashCode() ^ super.hashCode();
    }
}
