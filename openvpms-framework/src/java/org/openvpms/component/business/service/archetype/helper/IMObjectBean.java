/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.RelationshipRef;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.AbstractPropertySet;
import org.openvpms.component.system.common.util.PropertySet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActive;
import static org.openvpms.component.business.service.archetype.functor.RelationshipRef.SOURCE;
import static org.openvpms.component.business.service.archetype.functor.RelationshipRef.TARGET;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.InvalidClassCast;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.NodeDescriptorNotFound;


/**
 * Helper to access an {@link IMObject}'s properties via their names.
 *
 * @author Tim Anderson
 */
public class IMObjectBean {

    /**
     * The object.
     */
    private final IMObject object;

    /**
     * The archetype.
     */
    private ArchetypeDescriptor archetype;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * Used to convert node values to a particular type.
     */
    private PropertySet properties;


    /**
     * Constructs an {@link IMObjectBean}.
     *
     * @param object the object
     */
    public IMObjectBean(IMObject object) {
        this(object, null);
    }

    /**
     * Constructs an {@link IMObjectBean}.
     *
     * @param object  the object
     * @param service the archetype service. May be {@code null}
     */
    public IMObjectBean(IMObject object, IArchetypeService service) {
        if (object == null) {
            throw new IllegalArgumentException("Argument 'object' may not be null");
        }
        this.object = object;
        this.service = service;
        this.properties = new NodePropertySet();
    }

    /**
     * Returns the underlying object.
     *
     * @return the object
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns a reference to the underlying object.
     *
     * @return the reference
     */
    public IMObjectReference getReference() {
        return object.getObjectReference();
    }

    /**
     * Determines if the object is one of a set of archetypes.
     *
     * @param shortNames the archetype short names. May contain wildcards
     * @return {@code true} if the object is one of {@code shortNames}
     */
    public boolean isA(String... shortNames) {
        return TypeHelper.isA(object, shortNames);
    }

    /**
     * Determines if a node exists.
     *
     * @param name the node name
     * @return {@code true} if the node exists, otherwise
     *         {@code false}
     */
    public boolean hasNode(String name) {
        return (getDescriptor(name) != null);
    }

    /**
     * Returns the named node's descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to {@code name} or
     *         {@code null} if none exists.
     */
    public NodeDescriptor getDescriptor(String name) {
        return getArchetype().getNodeDescriptor(name);
    }

    /**
     * Returns the archetype display name.
     *
     * @return the archetype display name, or its short name if none is present.
     */
    public String getDisplayName() {
        return getArchetype().getDisplayName();
    }

    /**
     * Returns the display name of a node.
     *
     * @param name the node name
     * @return the node display name
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public String getDisplayName(String name) {
        NodeDescriptor node = getNode(name);
        return node.getDisplayName();
    }

    /**
     * Returns the archetype range associated with a node, expanding any wildcards.
     *
     * @param name the node name
     * @return the archetype range associated with a node, or an empty array if there is none
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the node doesn't exist
     */
    public String[] getArchetypeRange(String name) {
        NodeDescriptor node = getNode(name);
        return DescriptorHelper.getShortNames(node, getArchetypeService());
    }

    /**
     * Returns the boolean value of a node.
     *
     * @param name the node name
     * @return the value of the node, or {@code false} if the node is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public boolean getBoolean(String name) {
        return properties.getBoolean(name);
    }

    /**
     * Returns the boolean value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it
     *         is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        return properties.getBoolean(name, defaultValue);
    }

    /**
     * Returns the integer value of a node.
     *
     * @param name the node name
     * @return the value of the node, or {@code 0} if the node is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public int getInt(String name) {
        return properties.getInt(name);
    }

    /**
     * Returns the integer value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it
     *         is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public int getInt(String name, int defaultValue) {
        return properties.getInt(name, defaultValue);
    }

    /**
     * Returns the long value of a node.
     *
     * @param name the node name
     * @return the value of the node, or {@code 0} if the node is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public long getLong(String name) {
        return properties.getLong(name);
    }

    /**
     * Returns the long value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it
     *         is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public long getLong(String name, long defaultValue) {
        return properties.getLong(name, defaultValue);
    }

    /**
     * Returns the string value of a node.
     *
     * @param name the node name
     * @return the value of the node.
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public String getString(String name) {
        return properties.getString(name);
    }

    /**
     * Returns the string value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public String getString(String name, String defaultValue) {
        return properties.getString(name, defaultValue);
    }

    /**
     * Returns the {@code BigDecimal} value of a node.
     *
     * @param name the node name
     * @return the value of the node. May be {@code null}
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public BigDecimal getBigDecimal(String name) {
        return properties.getBigDecimal(name);
    }

    /**
     * Returns the {@code BigDecimal} value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it
     *         is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public BigDecimal getBigDecimal(String name, BigDecimal defaultValue) {
        return properties.getBigDecimal(name, defaultValue);
    }

    /**
     * Returns the {@code Money} value of a node.
     *
     * @param name the node name
     * @return the value of the node. May be {@code null}
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public Money getMoney(String name) {
        return properties.getMoney(name);
    }

    /**
     * Returns the {@code BigDecimal} value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it
     *         is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public Money getMoney(String name, Money defaultValue) {
        return properties.getMoney(name, defaultValue);
    }

    /**
     * Returns the {@code Date} value of a node.
     *
     * @param name the node name
     * @return the value of the node
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public Date getDate(String name) {
        return properties.getDate(name);
    }

    /**
     * Returns the {@code Date} value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it
     *         is null
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public Date getDate(String name, Date defaultValue) {
        return properties.getDate(name, defaultValue);
    }

    /**
     * Returns the reference value of a node.
     *
     * @param node the node name
     * @return the node value
     */
    public IMObjectReference getReference(String node) {
        return properties.getReference(node);
    }

    /**
     * Returns the object at the specified node.
     * <p/>
     * If the named object is an {@link IMObjectReference}, it will be
     * resolved.
     *
     * @param node the node name
     * @return the node value
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getObject(String node) {
        Object value = getValue(node);
        if (value instanceof IMObjectReference) {
            return resolve((IMObjectReference) value);
        }
        return (IMObject) value;
    }

    /**
     * Returns the value of a node.
     *
     * @param name the node name
     * @return the value of the node
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public Object getValue(String name) {
        NodeDescriptor node = getNode(name);
        return node.getValue(object);
    }

    /**
     * Returns the values of a collection node.
     *
     * @param name the node name
     * @return the collection corresponding to the node
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public List<IMObject> getValues(String name) {
        NodeDescriptor node = getNode(name);
        return node.getChildren(object);
    }

    /**
     * Returns the values of a collection node, converted to the supplied type.
     *
     * @param name the node name
     * @param type the expected object type
     * @return the collection corresponding to the node
     * @throws IMObjectBeanException if the node doesn't exist or an element is of the wrong type
     */
    @SuppressWarnings("unchecked")
    public <T extends IMObject> List<T> getValues(String name, Class<T> type) {
        List<IMObject> values = getValues(name);
        for (IMObject value : values) {
            if (!type.isInstance(value)) {
                throw new IMObjectBeanException(InvalidClassCast, type.getName(), value.getClass().getName());
            }
        }
        return (List<T>) values;
    }

    /**
     * Returns the values of a collection node that match the supplied predicate.
     *
     * @param name      the node name
     * @param predicate the predicate
     * @return the objects matching the predicate
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public List<IMObject> getValues(String name, Predicate predicate) {
        return select(getValues(name), predicate);
    }

    /**
     * Returns the values of a collection node that match the supplied predicate.
     *
     * @param name      the node name
     * @param predicate the predicate
     * @param type      the expected object type
     * @return the objects matching the predicate
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public <T extends IMObject> List<T> getValues(String name, Predicate predicate, Class<T> type) {
        return select(getValues(name, type), predicate);
    }

    /**
     * Returns the first value of a collection node that matches the supplied predicate.
     *
     * @param name      the node name
     * @param predicate the predicate
     * @return the first object matching the predicate, or {@code null} if none is found
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public IMObject getValue(String name, Predicate predicate) {
        for (IMObject object : getValues(name)) {
            if (predicate.evaluate(object)) {
                return object;
            }
        }
        return null;
    }

    /**
     * Returns the source object from the first active {@link IMObjectRelationship} with active source object, for the
     * specified relationship node.
     *
     * @param node the relationship node name
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeSourceObject(String node) {
        return getNodeSourceObject(node, true);
    }

    /**
     * Returns the source object from the first {@link IMObjectRelationship} for the specified node.
     *
     * @param node   the relationship node name
     * @param active determines if the relationship and source object must be active
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeSourceObject(String node, boolean active) {
        return getNodeSourceObject(node, getDefaultPredicate(active), active);
    }

    /**
     * Returns the source object from the first active {@link IMObjectRelationship} with active source object, matching
     * the specified predicate.
     *
     * @param node      the relationship node name
     * @param predicate the predicate
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeSourceObject(String node, Predicate predicate) {
        return getNodeSourceObject(node, predicate, true);
    }

    /**
     * Returns the source object from the first active {@link IMObjectRelationship} matching the specified predicate.
     *
     * @param node      the relationship node name
     * @param predicate the predicate
     * @param active    determines if the object must be active or not
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeSourceObject(String node, Predicate predicate, boolean active) {
        return getRelatedObject(node, predicate, SOURCE, active);
    }

    /**
     * Returns the target object from the first active {@link IMObjectRelationship} with active target object, for the
     * specified node.
     *
     * @param node the relationship node name
     * @return the target object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeTargetObject(String node) {
        return getNodeTargetObject(node, true);
    }

    /**
     * Returns the target object from the first {@link IMObjectRelationship} for the specified node.
     *
     * @param node   the relationship node
     * @param active determines if the relationship and target object must be active
     * @return the target object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeTargetObject(String node, boolean active) {
        return getNodeTargetObject(node, getDefaultPredicate(active), active);
    }

    /**
     * Returns the target object from the first active {@link IMObjectRelationship} with active target object, for the
     * specified node.
     *
     * @param node      the relationship node name
     * @param predicate the predicate
     * @return the target object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeTargetObject(String node, Predicate predicate) {
        return getNodeTargetObject(node, predicate, true);
    }

    /**
     * Returns the target object from the first active {@link IMObjectRelationship} for the specified node.
     *
     * @param node      the relationship node name
     * @param predicate the predicate
     * @param active    determines if the object must be active or not
     * @return the target object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeTargetObject(String node, Predicate predicate, boolean active) {
        return getRelatedObject(node, predicate, TARGET, active);
    }

    /**
     * Returns the target object from the first {@link PeriodRelationship} matching the specified short name. The
     * relationship must be active at the specified time, and have an active target object.
     *
     * @param node the relationship node name
     * @param time the time
     * @return the target object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeSourceObject(String node, Date time) {
        return getNodeSourceObject(node, time, true);
    }

    /**
     * Returns the source object from the first {@link PeriodRelationship} that is active at the specified time, for the
     * specified node.
     *
     * @param node   the relationship node
     * @param time   the time
     * @param active determines if the object must be active
     * @return the source object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeSourceObject(String node, Date time, boolean active) {
        return getNodeSourceObject(node, isActive(time), active);
    }

    /**
     * Returns the target object from the first {@link PeriodRelationship} with active target object that is active at
     * the specified time, for the specified node.
     *
     * @param node the relationship node
     * @param time the time
     * @return the target object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeTargetObject(String node, Date time) {
        return getNodeTargetObject(node, time, true);
    }

    /**
     * Returns the target object from the first {@link PeriodRelationship} that is active at the specified time, for the
     * specified node.
     *
     * @param node   the relationship node
     * @param time   the time
     * @param active determines if the object must be active
     * @return the target object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeTargetObject(String node, Date time, boolean active) {
        return getNodeTargetObject(node, isActive(time), active);
    }

    /**
     * Returns the active source objects from each active {@link IMObjectRelationship} for the specified node.
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param node the relationship node
     * @return a list of active source objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeSourceObjects(String node) {
        return getNodeSourceObjects(node, IMObject.class);
    }

    /**
     * Returns the active source objects from each active {@link IMObjectRelationship} for the specified node.
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param node the relationship node
     * @param type the object type
     * @return a list of active source objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeSourceObjects(String node, Class<T> type) {
        return getRelatedObjects(node, IsActiveRelationship.isActiveNow(), SOURCE, true, type, null);
    }

    /**
     * Returns the active source objects from each {@link PeriodRelationship} for the specified node that is active at
     * the specified time.
     *
     * @param node the relationship node
     * @param time the time
     * @return a list of active source objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeSourceObjects(String node, Date time) {
        return getNodeSourceObjects(node, time, IMObject.class);
    }

    /**
     * Returns the active source objects from each {@link PeriodRelationship} for the specified node that is active at
     * the specified time.
     *
     * @param node the relationship node
     * @param time the time
     * @param type the object type
     * @return a list of active source objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeSourceObjects(String node, Date time, Class<T> type) {
        return getNodeSourceObjects(node, time, true, type);
    }

    /**
     * Returns the source objects from each {@link PeriodRelationship} for the specified node that is active at the
     * specified time.
     *
     * @param node   the relationship node
     * @param time   the time
     * @param active determines if the objects must be active
     * @return a list of source objects. May contain inactive objects if {@code active} is {@code false}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeSourceObjects(String node, Date time, boolean active) {
        return getNodeSourceObjects(node, isActive(time), active);
    }

    /**
     * Returns the source objects from each {@link PeriodRelationship} for the specified node that is active at the
     * specified time.
     *
     * @param node   the relationship node
     * @param time   the time
     * @param active determines if the objects must be active
     * @param type   the object type
     * @return a list of source objects. May contain inactive objects if {@code active} is {@code false}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeSourceObjects(String node, Date time, boolean active, Class<T> type) {
        return getNodeSourceObjects(node, isActive(time), active, type);
    }

    /**
     * Returns the active source objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @return a list of source objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeSourceObjects(String node, Predicate predicate) {
        return getNodeSourceObjects(node, predicate, true);
    }

    /**
     * Returns the source objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @param active    determines if the objects must be active
     * @return a list of source objects. May contain inactive objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeSourceObjects(String node, Predicate predicate, boolean active) {
        return getNodeSourceObjects(node, predicate, active, IMObject.class);
    }

    /**
     * Returns the source objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @param active    determines if the objects must be active
     * @param type      the object type
     * @return a list of source objects. May contain inactive objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeSourceObjects(String node, Predicate predicate, boolean active,
                                                             Class<T> type) {
        return getRelatedObjects(node, predicate, SOURCE, active, type, null);
    }

    /**
     * Returns the active source objects from each {@link IMObjectRelationship} for the specified node, keyed on their
     * relationship.
     *
     * @param node             the relationship node
     * @param type             the source object type
     * @param relationshipType the relationship object type
     * @return the source objects, keyed on their relationships
     */
    public <T extends IMObject, R extends IMObjectRelationship> Map<R, T> getNodeSourceObjects(
            String node, Class<T> type, Class<R> relationshipType) {
        return getNodeSourceObjects(node, type, relationshipType, true);
    }

    /**
     * Returns the active source objects from each {@link IMObjectRelationship} for the specified node, keyed
     * on their relationship.
     *
     * @param node             the relationship node
     * @param type             the source object type
     * @param relationshipType the relationship object type
     * @param active           determines if the objects must be active
     * @return the source objects, keyed on their relationships
     */
    public <T extends IMObject, R extends IMObjectRelationship> Map<R, T> getNodeSourceObjects(
            String node, Class<T> type, Class<R> relationshipType, boolean active) {
        List<R> relationships = getValues(node, relationshipType);
        return getRelationshipObjects(relationships, getDefaultPredicate(active), SOURCE, active, type);
    }

    /**
     * Returns the active target objects from each active {@link IMObjectRelationship} for the specified node. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param node the relationship node
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeTargetObjects(String node) {
        return getNodeTargetObjects(node, IMObject.class);
    }

    /**
     * Returns the active target objects from each active {@link IMObjectRelationship} for the specified node.. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param node       the relationship node
     * @param comparator a comparator to sort relationships. May be {@code null}
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> List<IMObject> getNodeTargetObjects(String node, Comparator<R> comparator) {
        return getNodeTargetObjects(node, IMObject.class, comparator);
    }

    /**
     * Returns the active target objects from each active {@link IMObjectRelationship} for the specified node. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param node the relationship node
     * @param type the object type
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeTargetObjects(String node, Class<T> type) {
        return getNodeTargetObjects(node, type, (Comparator<IMObjectRelationship>) null);
    }

    /**
     * Returns the active target objects from each active {@link IMObjectRelationship} for the specified node. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param node       the relationship node
     * @param type       the object type
     * @param comparator if non-null, specifies a comparator to sort relationships
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject, R extends IMObjectRelationship> List<T> getNodeTargetObjects(String node, Class<T> type,
                                                                                             Comparator<R> comparator) {
        return getNodeTargetObjects(node, IsActiveRelationship.isActiveNow(), true, type, comparator);
    }

    /**
     * Returns the active target objects from each {@link PeriodRelationship} for the specified node that is active at
     * the specified time.
     *
     * @param node the relationship node
     * @param time the time
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeTargetObjects(String node, Date time) {
        return getNodeTargetObjects(node, time, IMObject.class);
    }

    /**
     * Returns the active target objects from each {@link PeriodRelationship} for the specified node that is active at
     * the specified time.
     *
     * @param node the relationship node
     * @param time the time
     * @param type the object type
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeTargetObjects(String node, Date time, Class<T> type) {
        return getNodeTargetObjects(node, time, true, type);
    }

    /**
     * Returns the target objects from each {@link PeriodRelationship} for the specified node that is active at the
     * specified time.
     *
     * @param node   the relationship node
     * @param time   the time
     * @param active determines if the objects must be active
     * @return a list of target objects. May contain inactive entities if {@code active} is {@code false}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeTargetObjects(String node, Date time, boolean active) {
        return getNodeTargetObjects(node, time, active, IMObject.class);
    }

    /**
     * Returns the target objects from each {@link PeriodRelationship} for the specified node that is active at the
     * specified time.
     *
     * @param node   the relationship node
     * @param time   the time
     * @param active determines if the objects must be active
     * @param type   the expected object type
     * @return a list of target objects. May contain inactive entities if {@code active} is {@code false}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeTargetObjects(String node, Date time, boolean active, Class<T> type) {
        return getNodeTargetObjects(node, isActive(time), active, type);
    }

    /**
     * Returns the active target objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @return a list of target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeTargetObjects(String node, Predicate predicate) {
        return getNodeTargetObjects(node, predicate, IMObject.class);
    }

    /**
     * Returns the active target objects from each relationship for the specified node that matches the specified
     * predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @param type      the object type
     * @return a list of target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeTargetObjects(String node, Predicate predicate, Class<T> type) {
        return getNodeTargetObjects(node, predicate, true, type);
    }

    /**
     * Returns the target objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @param active    determines if the objects must be active
     * @return a list of target objects. May  contain inactive objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeTargetObjects(String node, Predicate predicate, boolean active) {
        return getRelatedObjects(node, predicate, TARGET, active, IMObject.class, null);
    }

    /**
     * Returns the target objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @param active    determines if the objects must be active
     * @param type      the object type
     * @return a list of target objects. May  contain inactive objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeTargetObjects(String node, Predicate predicate, boolean active,
                                                             Class<T> type) {
        return getNodeTargetObjects(node, predicate, active, type, null);
    }

    /**
     * Returns the target objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param node       the relationship node
     * @param predicate  the predicate
     * @param active     determines if the objects must be active
     * @param type       the object type
     * @param comparator if non-null, specifies a comparator to sort relationships
     * @return a list of target objects. May  contain inactive objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject, R extends IMObjectRelationship> List<T> getNodeTargetObjects(
            String node, Predicate predicate, boolean active, Class<T> type, Comparator<R> comparator) {
        return getRelatedObjects(node, predicate, TARGET, active, type, comparator);
    }

    /**
     * Returns the active target objects from each {@link IMObjectRelationship} for the specified node, keyed on their
     * relationship.
     *
     * @param node             the relationship node
     * @param type             the target object type
     * @param relationshipType the relationship object type
     * @return the target objects, keyed on their relationships
     */
    public <T extends IMObject, R extends IMObjectRelationship> Map<R, T> getNodeTargetObjects(
            String node, Class<T> type, Class<R> relationshipType) {
        return getNodeTargetObjects(node, type, relationshipType, true);
    }

    /**
     * Returns the active target objects from each {@link IMObjectRelationship} for the specified node, keyed
     * on their relationship.
     *
     * @param node             the relationship node
     * @param type             the target object type
     * @param relationshipType the relationship object type
     * @param active           determines if the objects must be active
     * @return the target objects, keyed on their relationships
     */
    public <T extends IMObject, R extends IMObjectRelationship> Map<R, T> getNodeTargetObjects(
            String node, Class<T> type, Class<R> relationshipType, boolean active) {
        List<R> relationships = getValues(node, relationshipType);
        return getRelationshipObjects(relationships, getDefaultPredicate(active), TARGET, active, type);
    }

    /**
     * Returns the source object reference from the first active {@link IMObjectRelationship} for the specified
     * relationship node.
     *
     * @param node the relationship node name
     * @return the source object reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getNodeSourceObjectRef(String node) {
        List<IMObjectRelationship> relationships = getValues(node, IsActiveRelationship.isActiveNow(),
                                                             IMObjectRelationship.class);
        return getRelatedRef(relationships, null, SOURCE);
    }

    /**
     * Returns the source object references from each active {@link IMObjectRelationship} for the specified node.
     *
     * @param node the relationship node
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    public List<IMObjectReference> getNodeSourceObjectRefs(String node) {
        return getNodeSourceObjectRefs(node, IsActiveRelationship.isActiveNow());
    }

    /**
     * Returns the source object references from each {@link PeriodRelationship} that is active at the specified time,
     * for the specified node.
     *
     * @param node the relationship node
     * @param time the time
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    public List<IMObjectReference> getNodeSourceObjectRefs(String node, Date time) {
        return getNodeSourceObjectRefs(node, isActive(time));
    }

    /**
     * Returns the source object references from each {@link PeriodRelationship} for the specified node that matches the
     * supplied predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    public List<IMObjectReference> getNodeSourceObjectRefs(String node, Predicate predicate) {
        return getRelatedObjectRefs(node, predicate, SOURCE, null);
    }

    /**
     * Returns the target object references from each active {@link PeriodRelationship} for the specified node.
     *
     * @param node the relationship node
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    public List<IMObjectReference> getNodeTargetObjectRefs(String node) {
        return getNodeTargetObjectRefs(node, IsActiveRelationship.isActiveNow());
    }

    /**
     * Returns the target object reference from the first active {@link IMObjectRelationship} for the specified node.
     *
     * @param node the relationship node
     * @return the target reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getNodeTargetObjectRef(String node) {
        List<IMObjectRelationship> relationships = getValues(node, IsActiveRelationship.isActiveNow(),
                                                             IMObjectRelationship.class);
        return getRelatedRef(relationships, null, TARGET);
    }

    /**
     * Returns the target object references from each {@link PeriodRelationship} that is active at the specified time,
     * for the specified node.
     *
     * @param node the relationship node
     * @param time the time
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    public List<IMObjectReference> getNodeTargetObjectRefs(String node, Date time) {
        return getNodeTargetObjectRefs(node, isActive(time));
    }

    /**
     * Returns the target object references from each {@link IMObjectRelationship} for the specified node that matches
     * the supplied predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    public List<IMObjectReference> getNodeTargetObjectRefs(String node, Predicate predicate) {
        return getRelatedObjectRefs(node, predicate, TARGET, null);
    }

    /**
     * Returns the active source objects from each relationship that matches the specified short name.
     *
     * @param relationships the relationships
     * @param shortName     the short name
     * @param type          the expected object type
     * @return a list of source objects that match the given criteria
     */
    public <T extends IMObject, R extends IMObjectRelationship> List<T> getSourceObjects(Collection<R> relationships,
                                                                                         String shortName,
                                                                                         Class<T> type) {
        return getSourceObjects(relationships, new String[]{shortName}, type);
    }

    /**
     * Returns the active source objects from each relationship that matches the specified short names.
     *
     * @param relationships the relationships
     * @param shortNames    the short names
     * @param type          the expected object type
     * @return a list of source objects that match the given criteria
     */
    public <T extends IMObject, R extends IMObjectRelationship> List<T> getSourceObjects(Collection<R> relationships,
                                                                                         String[] shortNames,
                                                                                         Class<T> type) {
        return getSourceObjects(relationships, shortNames, true, type);
    }

    /**
     * Returns the source objects from each relationship that matches the specified short names.
     *
     * @param relationships the relationships
     * @param shortNames    the short names
     * @param active        determines if the relationship and source object must be active
     * @param type          the expected object type
     * @return a list of source objects that match the given criteria
     */
    public <T extends IMObject, R extends IMObjectRelationship> List<T> getSourceObjects(Collection<R> relationships,
                                                                                         String[] shortNames,
                                                                                         boolean active,
                                                                                         Class<T> type) {
        return getRelatedObjects(relationships, getActiveIsA(active, shortNames), SOURCE, active, type);
    }

    /**
     * Returns the active target objects from each relationship that matches the specified short names.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @param type          the expected object type
     * @return a list of target objects that match the given criteria
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject, R extends IMObjectRelationship> List<T> getTargetObjects(Collection<R> relationships,
                                                                                         String shortName,
                                                                                         Class<T> type) {
        return getTargetObjects(relationships, new String[]{shortName}, type);
    }

    /**
     * Returns the active target objects from each relationship that matches the specified short names.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param type          the expected object type
     * @return a list of target objects that match the given criteria
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject, R extends IMObjectRelationship> List<T> getTargetObjects(Collection<R> relationships,
                                                                                         String[] shortNames,
                                                                                         Class<T> type) {
        return getTargetObjects(relationships, shortNames, true, type);
    }

    /**
     * Returns the active target objects from each relationship that matches the specified short names.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param active        determines if the relationship and target object must be active
     * @param type          the expected object type
     * @return a list of target objects that match the given criteria
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject, R extends IMObjectRelationship> List<T> getTargetObjects(Collection<R> relationships,
                                                                                         String[] shortNames,
                                                                                         boolean active,
                                                                                         Class<T> type) {
        return getRelatedObjects(relationships, getActiveIsA(active, shortNames), TARGET, active, type);
    }

    /**
     * Returns the source object from the first active relationship with active source object, for the specified
     * relationship short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObject getSourceObject(Collection<R> relationships, String shortName) {
        return getSourceObject(relationships, new String[]{shortName});
    }

    /**
     * Returns the source object from the first relationship for the specified relationship short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @param active        determines if the relationship and object must be active
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObject getSourceObject(Collection<R> relationships, String shortName,
                                                                     boolean active) {
        return getSourceObject(relationships, new String[]{shortName}, active);
    }

    /**
     * Returns the source object from the first active relationship matching the specified relationship short names and
     * having an active source object.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObject getSourceObject(Collection<R> relationships, String[] shortNames) {
        return getSourceObject(relationships, shortNames, true);
    }

    /**
     * Returns the source object from the first relationship matching the specified relationship short names.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param active        determines if the relationship and source object must be active
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObject getSourceObject(Collection<R> relationships, String[] shortNames,
                                                                     boolean active) {
        return getRelatedObject(relationships, getActiveIsA(active, shortNames), SOURCE, active);
    }

    /**
     * Returns the target object from the first active relationship with active target object, for the specified
     * relationship short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @return the active object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObject getTargetObject(Collection<R> relationships, String shortName) {
        return getTargetObject(relationships, new String[]{shortName});
    }

    /**
     * Returns the target object from the first relationship for the specified relationship short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @param active        determines if the relationship and object must be active
     * @return the object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObject getTargetObject(Collection<R> relationships, String shortName,
                                                                     boolean active) {
        return getTargetObject(relationships, new String[]{shortName}, active);
    }

    /**
     * Returns the target object from the first active relationship matching the specified relationship short names and
     * having an active target object.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @return the target object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObject getTargetObject(Collection<R> relationships, String[] shortNames) {
        return getTargetObject(relationships, shortNames, true);
    }

    /**
     * Returns the target object from the first relationship matching the specified relationship short names.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param active        determines if the relationship and target object must be active
     * @return the target object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObject getTargetObject(Collection<R> relationships, String[] shortNames,
                                                                     boolean active) {
        return getRelatedObject(relationships, getActiveIsA(active, shortNames), TARGET, active);
    }

    /**
     * Returns the source object from the first relationship matching the specified short name. The relationship must be
     * active at the specified time, and have an active source object.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @param time          the time
     * @return the source object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends PeriodRelationship> IMObject getSourceObject(Collection<R> relationships, String shortName,
                                                                   Date time) {
        return getSourceObject(relationships, shortName, time, true);
    }

    /**
     * Returns the source object from the first relationship matching the specified short name. The relationship must be
     * active at the specified time.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @param time          the time
     * @param active        determines if the object must be active
     * @return the source object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends PeriodRelationship> IMObject getSourceObject(Collection<R> relationships, String shortName,
                                                                   Date time, boolean active) {
        return getSourceObject(relationships, new String[]{shortName}, time, active);
    }

    /**
     * Returns the source object from the first relationship matching the specified short names. The relationship must
     * be active at the specified time, and have an active source object.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param time          the time
     * @return the source object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends PeriodRelationship> IMObject getSourceObject(Collection<R> relationships, String[] shortNames,
                                                                   Date time) {
        return getSourceObject(relationships, shortNames, time, true);
    }

    /**
     * Returns the source object from the first relationship matching the specified short names. The relationship must
     * be active at the specified time.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param time          the time
     * @param active        determines if the object must be active
     * @return the source object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends PeriodRelationship> IMObject getSourceObject(Collection<R> relationships, String[] shortNames,
                                                                   Date time, boolean active) {
        return getRelatedObject(relationships, getIsActiveRelationship(time, shortNames), SOURCE, active);
    }

    /**
     * Returns the source object from the first relationship matching the specified short name. The relationship must be
     * active at the specified time, and have an active target object.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @param time          the time
     * @return the target object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends PeriodRelationship> IMObject getTargetObject(Collection<R> relationships, String shortName,
                                                                   Date time) {
        return getTargetObject(relationships, shortName, time, true);
    }

    /**
     * Returns the target object from the first relationship
     * matching the specified short name. The relationship must be active at
     * the specified time.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @param time          the time
     * @param active        determines if the object must be active
     * @return the target object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends PeriodRelationship> IMObject getTargetObject(Collection<R> relationships, String shortName,
                                                                   Date time, boolean active) {
        return getTargetObject(relationships, new String[]{shortName}, time, active);
    }

    /**
     * Returns the target object from the first relationship
     * matching the specified short names. The relationship must be active at
     * the specified time, and have an active target object.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param time          the time
     * @return the target object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends PeriodRelationship> IMObject getTargetObject(Collection<R> relationships, String[] shortNames,
                                                                   Date time) {
        return getTargetObject(relationships, shortNames, time, true);
    }

    /**
     * Returns the target object from the first relationship matching the specified short names. The relationship must
     * be active at the specified time.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param time          the time
     * @param active        determines if the relationship must be active
     * @return the target object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends PeriodRelationship> IMObject getTargetObject(Collection<R> relationships, String[] shortNames,
                                                                   Date time, boolean active) {
        return getRelatedObject(relationships, getIsActiveRelationship(time, shortNames), TARGET, active);
    }

    /**
     * Returns the source object reference from the first active object relationship matching the specified short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @return the source reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObjectReference getSourceObjectRef(Collection<R> relationships,
                                                                                 String shortName) {
        return getSourceObjectRef(relationships, shortName, true);
    }

    /**
     * Returns the source object reference from the first relationship matching the specified short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @param active        determines if the relationship must be active
     * @return the source reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObjectReference getSourceObjectRef(Collection<R> relationships,
                                                                                 String shortName, boolean active) {
        return getSourceObjectRef(relationships, new String[]{shortName}, active);
    }

    /**
     * Returns the source object reference from the first relationship matching the specified short names.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param active        determines if the relationship must be active
     * @return the source reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObjectReference getSourceObjectRef(Collection<R> relationships,
                                                                                 String[] shortNames, boolean active) {
        return getObjectRef(relationships, shortNames, active, SOURCE);
    }

    /**
     * Returns the target object reference from the first active object relationship matching the specified short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @return the target reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObjectReference getTargetObjectRef(Collection<R> relationships,
                                                                                 String shortName) {
        return getTargetObjectRef(relationships, shortName, true);
    }

    /**
     * Returns the target object reference from the first relationship matching the specified short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @param active        determines if the relationship must be active
     * @return the target reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObjectReference getTargetObjectRef(Collection<R> relationships,
                                                                                 String shortName, boolean active) {
        return getTargetObjectRef(relationships, new String[]{shortName}, active);
    }

    /**
     * Returns the target object reference from the first relationship matching the specified short name.
     *
     * @param relationships the relationships
     * @param shortNames    the relationship short names
     * @param active        determines if the relationship must be active
     * @return the target reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends IMObjectRelationship> IMObjectReference getTargetObjectRef(Collection<R> relationships,
                                                                                 String[] shortNames, boolean active) {
        return getObjectRef(relationships, shortNames, active, TARGET);
    }

    /**
     * Determines if there is an active {@link PeriodRelationship} with {@code object} as its target, for the node
     * {@code node}.
     *
     * @param node   the relationship node
     * @param object the target object
     * @return {@code true} if there is an active relationship to {@code object}
     */
    public boolean hasNodeTarget(String node, IMObject object) {
        return getNodeTargetObjectRefs(node).contains(object.getObjectReference());
    }

    /**
     * Determines if there is a {@link PeriodRelationship} with {@code object} as its target, that is active at
     * {@code time} for the node {@code node}.
     *
     * @param node   the relationship node
     * @param object the target object
     * @param time   the time
     * @return {@code true} if there is an relationship to {@code object}, active at {@code time}
     */
    public boolean hasNodeTarget(String node, IMObject object, Date time) {
        return getNodeTargetObjectRefs(node, time).contains(object.getObjectReference());
    }

    /**
     * Sets the value of a node.
     *
     * @param name  the node name
     * @param value the new node value
     */
    public void setValue(String name, Object value) {
        properties.set(name, value);
    }

    /**
     * Adds a value to a collection.
     *
     * @param name  the node name
     * @param value the value to add
     * @throws IMObjectBeanException if the descriptor doesn't exist
     */
    public void addValue(String name, IMObject value) {
        NodeDescriptor node = getNode(name);
        node.addChildToCollection(object, value);
    }

    /**
     * Removes a value from a collection.
     *
     * @param name  the node name
     * @param value the value to remove
     */
    public void removeValue(String name, IMObject value) {
        NodeDescriptor node = getNode(name);
        node.removeChildFromCollection(object, value);
    }

    /**
     * Saves the object.
     * <p/>
     * Any derived nodes will have their values derived prior to the object
     * being saved.
     *
     * @throws ArchetypeServiceException if the object can't be saved
     */
    public void save() {
        IArchetypeService service = getArchetypeService();
        service.deriveValues(object);
        service.save(object);
    }

    /**
     * Resolves a reference, verifying the object is of the expected type.
     *
     * @param ref    the reference to resolve
     * @param type   the expected object type
     * @param active determines if the object must be active
     * @return the resolved object, or {@code null} if it cannot be found or doesn't match the active criteria
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if an object isn't of the expected type
     */
    @SuppressWarnings("unchecked")
    protected <T extends IMObject> T resolve(IMObjectReference ref, Class<T> type, boolean active) {
        T result = null;
        IMObject object = resolve(ref);
        if (object != null) {
            if (!type.isInstance(object)) {
                throw new IMObjectBeanException(InvalidClassCast, type.getName(), object.getClass().getName());
            }
            if (active && object.isActive() || !active) {
                result = (T) object;
            }
        }
        return result;
    }

    /**
     * Helper to resolve a reference.
     *
     * @param ref the reference. May be {@code null}
     * @return the object corresponding to the reference or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObject resolve(IMObjectReference ref) {
        IMObject result = null;
        if (ref != null) {
            return getArchetypeService().get(ref);
        }
        return result;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        if (service == null) {
            service = ArchetypeServiceHelper.getArchetypeService();
        }
        return service;
    }

    /**
     * Returns the archetype descriptor.
     *
     * @return the archetype descriptor
     * @throws IMObjectBeanException if the archetype does not exist
     */
    protected ArchetypeDescriptor getArchetype() {
        if (archetype == null) {
            archetype = DescriptorHelper.getArchetypeDescriptor(object, getArchetypeService());
            if (archetype == null) {
                throw new IMObjectBeanException(ArchetypeNotFound, object.getArchetypeId().getShortName());
            }
        }
        return archetype;
    }

    /**
     * Returns all objects for the specified relationship node that match the specified criteria.
     *
     * @param node       the relationship node
     * @param predicate  the criteria to filter relationships
     * @param accessor   the object accessor
     * @param active     determines if the objects must be active
     * @param type       the expected object type
     * @param comparator if non-null, specifies a comparator to sort relationships
     * @return a list of objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    protected <T extends IMObject, R extends IMObjectRelationship> List<T> getRelatedObjects(
            String node, Predicate predicate, RelationshipRef accessor, boolean active, Class<T> type,
            Comparator<R> comparator) {
        List<IMObjectReference> refs = getRelatedObjectRefs(node, predicate, accessor, comparator);
        return resolve(refs, type, active);
    }

    /**
     * Returns all related references for the specified node that match the specified criteria.
     *
     * @param node       the relationship node
     * @param predicate  the criteria
     * @param accessor   the object accessor
     * @param comparator if non-null, specifies a comparator to sort relationships
     * @return the matching references
     */
    @SuppressWarnings("unchecked")
    protected <R extends IMObjectRelationship> List<IMObjectReference> getRelatedObjectRefs(
            String node, Predicate predicate, RelationshipRef accessor, Comparator<R> comparator) {
        List<R> relationships = (List<R>) getValues(node, IMObjectRelationship.class);
        if (comparator != null) {
            Collections.sort(relationships, comparator);
        }
        return getRelatedRefs(relationships, predicate, accessor);
    }

    /**
     * Returns all object references from the supplied relationships that match the specified criteria.
     *
     * @param relationships the relationships
     * @param predicate     the criteria
     * @param accessor      the object accessor
     * @return the matching references
     */
    protected <R extends IMObjectRelationship> List<IMObjectReference> getRelatedRefs(
            Collection<R> relationships, Predicate predicate, RelationshipRef accessor) {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        relationships = select(relationships, predicate);
        for (R relationship : relationships) {
            IMObjectReference ref = accessor.transform(relationship);
            if (ref != null) {
                result.add(ref);
            }
        }
        return result;
    }

    /**
     * Returns all objects for the specified relationships that match the specified criteria.
     *
     * @param relationships the relationships to search
     * @param predicate     the criteria to filter relationships
     * @param accessor      the object accessor
     * @param active        determines if the objects must be active
     * @param type          the expected object type
     * @return a list of objects
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if an object isn't of the expected type
     */
    protected <R extends IMObjectRelationship, T extends IMObject> Map<R, T> getRelationshipObjects(
            Collection<R> relationships, Predicate predicate, RelationshipRef accessor, boolean active,
            Class<T> type) {
        Map<R, T> result;
        Map<R, IMObjectReference> refs = getRelationshipRefs(relationships, predicate, accessor);
        if (refs.isEmpty()) {
            result = Collections.emptyMap();
        } else {
            result = new HashMap<R, T>();
            for (Map.Entry<R, IMObjectReference> entry : refs.entrySet()) {
                T object = resolve(entry.getValue(), type, active);
                if (object != null) {
                    result.put(entry.getKey(), object);
                }
            }
        }
        return result;
    }

    /**
     * Returns all object references from the supplied relationships that match the specified criteria, keyed
     * on their relationship.
     *
     * @param relationships the relationships
     * @param predicate     the criteria
     * @param accessor      the object accessor
     * @return the matching references
     */
    protected <R extends IMObjectRelationship> Map<R, IMObjectReference> getRelationshipRefs(
            Collection<R> relationships, Predicate predicate, RelationshipRef accessor) {
        Map<R, IMObjectReference> result = new HashMap<R, IMObjectReference>();
        relationships = select(relationships, predicate);
        for (R relationship : relationships) {
            IMObjectReference ref = accessor.transform(relationship);
            if (ref != null) {
                result.put(relationship, ref);
            }
        }
        return result;
    }

    /**
     * Returns the object from the first relationship for the specified node that matches the specified criteria.
     *
     * @param node      the relationship node
     * @param predicate the criteria
     * @param accessor  the object accessor
     * @param active    determines if the object must be active
     * @return the first object, or {@code null} if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObject getRelatedObject(String node, Predicate predicate, RelationshipRef accessor, boolean active) {
        List<IMObjectRelationship> relationships = getValues(node, IMObjectRelationship.class);
        return getRelatedObject(relationships, predicate, accessor, active);
    }

    /**
     * Returns all objects for the specified relationships that match the specified criteria.
     *
     * @param relationships the relationships to search
     * @param predicate     the criteria to filter relationships
     * @param accessor      the object accessor
     * @param active        determines if the objects must be active
     * @param type          the expected object type
     * @return a list of objects
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if an object isn't of the expected type
     */
    protected <R extends IMObjectRelationship, T extends IMObject> List<T> getRelatedObjects(
            Collection<R> relationships, Predicate predicate, RelationshipRef accessor, boolean active,
            Class<T> type) {
        List<IMObjectReference> refs = getRelatedRefs(relationships, predicate, accessor);
        return resolve(refs, type, active);
    }

    /**
     * Resolves references.
     * <p/>
     * If an object cannot be resolved, or doesn't match the active criteria, it is ignored.
     *
     * @param refs   the references to resolve
     * @param type   the expected object type
     * @param active determines if the objects must be active
     * @return a list of objects
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if an object isn't of the expected type
     */
    protected <T extends IMObject> List<T> resolve(List<IMObjectReference> refs, Class<T> type, boolean active) {
        if (refs.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<T>();
        for (IMObjectReference ref : refs) {
            T object = resolve(ref, type, active);
            if (object != null) {
                result.add(object);
            }
        }
        return result;
    }

    /**
     * Returns the source or target from the first relationship matching the specified criteria.
     * <p/>
     * If active is {@code true} the object must be active in order to be returned.
     * <p/>
     * If active is {@code false}, then an active object will be returned in preference to an inactive one.
     *
     * @param relationships the relationships
     * @param predicate     the predicate
     * @param accessor      the relationship reference accessor
     * @param active        determines if the object must be active or not
     * @return the first object matching the criteria or {@code null} if none
     *         is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected <R extends IMObjectRelationship> IMObject getRelatedObject(Collection<R> relationships,
                                                                         Predicate predicate, RelationshipRef accessor,
                                                                         boolean active) {
        IMObject inactive = null;
        for (R relationship : relationships) {
            if (predicate.evaluate(relationship)) {
                IMObjectReference ref = accessor.transform(relationship);
                IMObject object = resolve(ref);
                if (object != null) {
                    if (object.isActive()) {
                        // found a match, so return it
                        return object;
                    } else if (!active) {
                        // can return inactive, but keep looking for an active
                        // match
                        inactive = object;
                    }
                }
            }
        }
        // no active match found
        return (!active && inactive != null) ? inactive : null;
    }

    /**
     * Returns a relationship short name for a specific node that the target may be added to.
     *
     * @param name       the node name
     * @param target     the target of the relationship
     * @param targetName the relationship node name that will reference the target
     * @return the relationship short name
     * @throws IMObjectBeanException if {@code name} is an invalid node, there is no relationship that supports
     *                               {@code target}, or multiple relationships can support {@code target}
     */
    protected String getRelationshipShortName(String name, IMObject target, String targetName) {
        return getRelationshipShortName(name, target.getObjectReference(), targetName);
    }

    /**
     * Returns a relationship short name for a specific node that the target may be added to.
     *
     * @param name       the node name
     * @param target     the target of the relationship
     * @param targetName the relationship node name that will reference the target
     * @return the relationship short name
     * @throws IMObjectBeanException if {@code name} is an invalid node, there is no relationship that supports
     *                               {@code target}, or multiple relationships can support {@code target}
     */
    protected String getRelationshipShortName(String name, IMObjectReference target, String targetName) {
        String[] range = getArchetypeRange(name);
        IArchetypeService service = getArchetypeService();
        String result = null;
        ArchetypeId archetypeId = target.getArchetypeId();
        for (String shortName : range) {
            ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(shortName);
            if (descriptor != null) {
                NodeDescriptor node = descriptor.getNodeDescriptor(targetName);
                if (node != null && TypeHelper.isA(archetypeId, node.getArchetypeRange())) {
                    if (result != null) {
                        throw new IMObjectBeanException(IMObjectBeanException.ErrorCode.MultipleRelationshipsForTarget,
                                                        archetypeId.getShortName(), name);
                    }
                    result = shortName;
                }
            }
        }
        if (result == null) {
            throw new IMObjectBeanException(IMObjectBeanException.ErrorCode.CannotAddTargetToNode,
                                            archetypeId.getShortName(), name);
        }
        return result;
    }

    /**
     * Returns the first object reference from the supplied relationship that matches the specified criteria.
     *
     * @param relationships the relationships
     * @param predicate     the criteria. May be {@code null}
     * @param accessor      the relationship reference accessor
     * @return the matching reference, or {@code null}
     */
    protected <R extends IMObjectRelationship> IMObjectReference getRelatedRef(
            Collection<R> relationships, Predicate predicate, RelationshipRef accessor) {
        for (R relationship : relationships) {
            if (predicate == null || predicate.evaluate(relationship)) {
                IMObjectReference reference = accessor.transform(relationship);
                if (reference != null) {
                    return reference;
                }
            }
        }
        return null;
    }

    /**
     * Returns the first object reference matching the specified criteria.
     *
     * @param relationships the relationships
     * @param shortNames    the short names
     * @param active        determines if the relationship must be active
     * @param accessor      the object accessor
     * @return the first matching reference,or {@code null}
     */
    protected <R extends IMObjectRelationship> IMObjectReference getObjectRef(Collection<R> relationships,
                                                                              String[] shortNames, boolean active,
                                                                              RelationshipRef accessor) {
        IMObjectReference ref = getRelatedRef(relationships, getActiveIsA(shortNames), accessor);
        if (ref == null && !active) {
            ref = getRelatedRef(relationships, new IsA(shortNames), accessor);
        }
        return ref;
    }

    /**
     * Selects all objects matching a predicate.
     *
     * @param objects   the objects to match
     * @param predicate the predicate
     * @return the objects matching the predicate
     */
    protected <T> List<T> select(Collection<T> objects, Predicate predicate) {
        List<T> result = new ArrayList<T>();
        for (T object : objects) {
            if (predicate.evaluate(object)) {
                result.add(object);
            }
        }
        return result;
    }

    /**
     * Selects the first objects matching a predicate.
     *
     * @param objects   the objects to match
     * @param predicate the predicate
     * @return the first object matching the predicate or {@code null} if none is found
     */
    protected <T> T selectFirst(Collection<T> objects, Predicate predicate) {
        for (T object : objects) {
            if (predicate.evaluate(object)) {
                return object;
            }
        }
        return null;
    }

    /**
     * Helper to return a predicate that checks that a relationship is
     * active at the specified time, and is one of a set of archetypes.
     *
     * @param time       the time
     * @param shortNames the relationship short names to match
     * @return a new predicate
     */
    protected Predicate getIsActiveRelationship(Date time, String... shortNames) {
        return new AndPredicate(isActive(time), new IsA(shortNames));
    }

    /**
     * Helper to rerturn a predicate that checks that a relationship is
     * active now, if {@code active} is {@code true}, and is one of a set
     * of archetypes.
     *
     * @param active     determines if the relationship must be active
     * @param shortNames the relationship short names to match
     * @return a new predicate
     */
    protected Predicate getActiveIsA(boolean active, String... shortNames) {
        IsA isA = new IsA(shortNames);
        return (active) ? new AndPredicate(IsActiveRelationship.isActiveNow(), isA) : isA;
    }

    /**
     * Helper to return a predicate that checks that a relationship is
     * active now, and is one of a set of archetypes.
     *
     * @param shortNames the relationship short names to match
     * @return a new predicate
     */
    protected Predicate getActiveIsA(String... shortNames) {
        return new AndPredicate(IsActiveRelationship.isActiveNow(), new IsA(shortNames));
    }

    /**
     * Helper to return the default predicate for evaluating relationships.
     *
     * @param active determines if the relationship must be active
     * @return the default predicate
     */
    protected Predicate getDefaultPredicate(boolean active) {
        return (active) ? IsActiveRelationship.isActiveNow() : PredicateUtils.truePredicate();
    }

    /**
     * Returns a node descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to {@code name}
     * @throws IMObjectBeanException if the descriptor doesn't exist
     */
    protected NodeDescriptor getNode(String name) {
        NodeDescriptor node = getArchetype().getNodeDescriptor(name);
        if (node == null) {
            String shortName = object.getArchetypeId().getShortName();
            throw new IMObjectBeanException(NodeDescriptorNotFound, name,
                                            shortName);
        }
        return node;
    }

    /**
     * Implementation of {@link PropertySet} for nodes.
     */
    private class NodePropertySet extends AbstractPropertySet {

        /**
         * Returns the property names.
         *
         * @return the property names
         */
        public Set<String> getNames() {
            return getArchetype().getNodeDescriptors().keySet();
        }

        /**
         * Returns the value of a property.
         *
         * @param name the property name
         * @return the value of the property
         * @throws IMObjectBeanException if the descriptor doesn't exist
         */
        public Object get(String name) {
            NodeDescriptor node = getNode(name);
            return node.getValue(object);
        }

        /**
         * Sets the value of a property.
         *
         * @param name  the propery name
         * @param value the property value
         * @throws IMObjectBeanException if the descriptor doesn't exist
         * @throws OpenVPMSException     if the property cannot be set
         */
        public void set(String name, Object value) {
            NodeDescriptor node = getNode(name);
            node.setValue(object, value);
        }
    }

}
