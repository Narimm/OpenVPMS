package org.openvpms.tools.archetype.comparator;

/**
 * Tracks changes between NodeDescriptor fields.
 *
 * @author Tim Anderson
 */
public class NodeFieldChange extends DescriptorChange<Object> {

    public enum Field {
        DISPLAY_NAME("displayName"),
        PATH("path"),
        BASE_NAME("baseName"),
        DEFAULT_VALUE("defaultValue"),
        DERIVED_VALUE("derivedValue"),
        MIN_CARDINALITY("minCardinality"),
        MAX_CARDINALITY("maxCardinality"),
        ASSERTION("assertion"),
        FILTER("filter");

        @Override
        public String toString() {
            return displayName;
        }

        private Field(String displayName) {
            this.displayName = displayName;
        }

        private final String displayName;
    }

    private final Field field;

    /**
     * Constructs an {@link NodeFieldChange}.
     *
     * @param field      the field
     * @param oldVersion the old version. May be {@code null}
     * @param newVersion the new version. May be {@code null}
     */
    public NodeFieldChange(Field field, Object oldVersion, Object newVersion) {
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
            NodeFieldChange other = (NodeFieldChange) object;
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
