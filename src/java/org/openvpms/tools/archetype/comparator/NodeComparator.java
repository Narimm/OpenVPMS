package org.openvpms.tools.archetype.comparator;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.tools.archetype.comparator.NodeFieldChange.Field;

/**
 * Compares {@link NodeDescriptor}s for differences.
 *
 * @author Tim Anderson
 */
public class NodeComparator extends AbstractComparator {

    /**
     * Compares two versions of a {@link NodeDescriptor}.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return the changes, or {@code null} if they are the same
     */
    public NodeChange compare(NodeDescriptor oldVersion, NodeDescriptor newVersion) {
        List<NodeFieldChange> changes = new ArrayList<NodeFieldChange>();
        compare(Field.DISPLAY_NAME, oldVersion.getDisplayName(), newVersion.getDisplayName(), changes);
        compare(Field.PATH, oldVersion.getPath(), newVersion.getPath(), changes);
        compare(Field.BASE_NAME, oldVersion.getBaseName(), newVersion.getBaseName(), changes);
        compare(Field.DEFAULT_VALUE, oldVersion.getDefaultValue(), newVersion.getDefaultValue(), changes);
        compare(Field.DERIVED_VALUE, oldVersion.getDerivedValue(), newVersion.getDerivedValue(), changes);
        compare(Field.MIN_CARDINALITY, oldVersion.getMinCardinality(), newVersion.getMinCardinality(), changes);
        compare(Field.MAX_CARDINALITY, oldVersion.getMaxCardinality(), newVersion.getMaxCardinality(), changes);
        compare(Field.FILTER, oldVersion.getFilter(), newVersion.getFilter(), changes);
        compareAssertions(oldVersion, newVersion, changes);

        return (changes.isEmpty()) ? null : new NodeChange(oldVersion, newVersion, changes);
    }

    /**
     * Compares two versions of a field.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @param changes    the changes to update
     */
    private void compare(Field field, Object oldVersion, Object newVersion,
                         List<NodeFieldChange> changes) {
        if (!ObjectUtils.equals(oldVersion, newVersion)) {
            changes.add(new NodeFieldChange(field, oldVersion, newVersion));
        }
    }

    /**
     * Compares assertions between versions of a {@link NodeDescriptor}.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @param changes    the changes to update
     */
    private void compareAssertions(NodeDescriptor oldVersion, NodeDescriptor newVersion,
                                   List<NodeFieldChange> changes) {
        Map<String, AssertionDescriptor> oldAssertions = oldVersion.getAssertionDescriptors();
        Map<String, AssertionDescriptor> newAssertions = newVersion.getAssertionDescriptors();
        Set<String> deleted = getDeleted(oldAssertions, newAssertions);
        Set<String> added = getAdded(oldAssertions, newAssertions);
        Set<String> retained = getRetained(oldAssertions, newAssertions);
        for (String deletedKey : deleted) {
            changes.add(new NodeFieldChange(Field.ASSERTION, oldAssertions.get(deletedKey), null));
        }
        for (String retainedKey : retained) {
            AssertionDescriptor oldAssertion = oldAssertions.get(retainedKey);
            AssertionDescriptor newAssertion = newAssertions.get(retainedKey);
            compareAssertion(oldAssertion, newAssertion, changes);
        }
        for (String addedKey : added) {
            changes.add(new NodeFieldChange(Field.ASSERTION, null, newAssertions.get(addedKey)));
        }
    }

    /**
     * Compares two versions of an {@link AssertionDescriptor}.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @param changes    the changes to update
     */
    private void compareAssertion(AssertionDescriptor oldVersion, AssertionDescriptor newVersion,
                                  List<NodeFieldChange> changes) {
        boolean equals = ObjectUtils.equals(newVersion.getErrorMessage(), oldVersion.getErrorMessage());
        if (equals) {
            PropertyMap oldMap = oldVersion.getPropertyMap();
            PropertyMap newMap = newVersion.getPropertyMap();
            equals = comparePropertyMap(oldMap, newMap);
        }
        if (!equals) {
            changes.add(new NodeFieldChange(Field.ASSERTION, oldVersion, newVersion));
        }
    }

    /**
     * Compares two versions of a {@link NamedProperty}.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return {@code true} if they are equal, otherwise {@code false}
     */
    private boolean compareNamedProperty(NamedProperty oldVersion, NamedProperty newVersion) {
        if (oldVersion.getName().equals(newVersion.getName())) {
            if (oldVersion instanceof PropertyMap && newVersion instanceof PropertyMap) {
                return comparePropertyMap((PropertyMap) oldVersion, (PropertyMap) newVersion);
            } else if (oldVersion instanceof PropertyList && newVersion instanceof PropertyList) {
                return comparePropertyList((PropertyList) oldVersion, (PropertyList) newVersion);
            } else if (oldVersion instanceof AssertionProperty && newVersion instanceof AssertionProperty) {
                return compareAssertionProperty((AssertionProperty) oldVersion, (AssertionProperty) newVersion);
            }
        }
        return false;
    }

    /**
     * Compares two versions of a {@link PropertyMap}.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return {@code true} if they are equal, otherwise {@code false}
     */
    private boolean comparePropertyMap(PropertyMap oldVersion, PropertyMap newVersion) {
        Set<String> oldNames = oldVersion.getProperties().keySet();
        Set<String> newNames = newVersion.getProperties().keySet();
        if (oldNames.equals(newNames)) {
            for (String name : oldNames) {
                NamedProperty oldProperty = oldVersion.getProperties().get(name);
                NamedProperty newProperty = newVersion.getProperties().get(name);
                if (!compareNamedProperty(oldProperty, newProperty)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Compares two versions of a {@link PropertyList}.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return {@code true} if they are equal, otherwise {@code false}
     */
    private boolean comparePropertyList(PropertyList oldVersion, PropertyList newVersion) {
        NamedProperty[] oldProperties = oldVersion.getPropertiesAsArray();
        NamedProperty[] newProperties = newVersion.getPropertiesAsArray();
        if (oldProperties.length == newProperties.length) {
            for (int i = 0; i < oldProperties.length; ++i) {
                if (!compareNamedProperty(oldProperties[i], newProperties[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Compares two versions of an {@link AssertionProperty}.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return {@code true} if they are equal, otherwise {@code false}
     */
    private boolean compareAssertionProperty(AssertionProperty oldVersion, AssertionProperty newVersion) {
        return ObjectUtils.equals(oldVersion.getType(), newVersion.getType())
               && ObjectUtils.equals(oldVersion.getValue(), newVersion.getValue());
    }

}
