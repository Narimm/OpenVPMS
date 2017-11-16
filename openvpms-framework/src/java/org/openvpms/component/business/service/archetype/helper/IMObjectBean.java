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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.bean.Relationships;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.RelationshipRef;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.util.PropertySet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActive;
import static org.openvpms.component.business.service.archetype.functor.RelationshipRef.SOURCE;
import static org.openvpms.component.business.service.archetype.functor.RelationshipRef.TARGET;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.InvalidClassCast;


/**
 * Helper to access an {@link IMObject}'s properties via their names.
 *
 * @author Tim Anderson
 */
public class IMObjectBean implements org.openvpms.component.business.domain.bean.IMObjectBean {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The lookup service.
     */
    private ILookupService lookups;

    /**
     * Used to convert node values to a particular type.
     */
    private NodePropertySet properties;


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
        this.service = service;
        this.properties = new NodePropertySet(object);
    }

    /**
     * Constructs an {@link IMObjectBean}.
     *
     * @param object  the object
     * @param service the archetype service. May be {@code null}
     * @param lookups the lookup service. May be {@code null}
     */
    public IMObjectBean(IMObject object, IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
        this.properties = new NodePropertySet(object);
    }

    /**
     * Returns the underlying object.
     *
     * @return the object
     */
    public IMObject getObject() {
        return properties.getObject();
    }

    /**
     * Returns a reference to the underlying object.
     *
     * @return the reference
     */
    public IMObjectReference getReference() {
        return getObject().getObjectReference();
    }

    /**
     * Determines if the object is one of a set of archetypes.
     *
     * @param shortNames the archetype short names. May contain wildcards
     * @return {@code true} if the object is one of {@code shortNames}
     */
    public boolean isA(String... shortNames) {
        return TypeHelper.isA(getObject(), shortNames);
    }

    /**
     * Determines if a node exists.
     *
     * @param name the node name
     * @return {@code true} if the node exists, otherwise
     * {@code false}
     */
    public boolean hasNode(String name) {
        return (getDescriptor(name) != null);
    }

    /**
     * Returns the named node's descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to {@code name} or
     * {@code null} if none exists.
     */
    public NodeDescriptor getDescriptor(String name) {
        return getArchetype().getNodeDescriptor(name);
    }

    /**
     * Returns the named node's descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to {@code name} or {@code null} if none exists.
     */
    @Override
    public NodeDescriptor getNode(String name) {
        return getDescriptor(name);
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
     * Returns the archetype descriptor.
     *
     * @return the archetype descriptor
     * @throws IMObjectBeanException if the archetype does not exist
     */
    public ArchetypeDescriptor getArchetype() {
        return properties.getArchetype();
    }

    /**
     * Returns the display name of a node.
     *
     * @param name the node name
     * @return the node display name
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public String getDisplayName(String name) {
        NodeDescriptor node = toNode(name);
        return node.getDisplayName();
    }

    /**
     * Returns the maximum length of a node.
     *
     * @param name the node name
     * @return the maximum length
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public int getMaxLength(String name) {
        NodeDescriptor node = toNode(name);
        return node.getMaxLength();
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
        NodeDescriptor node = toNode(name);
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
     * is null
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
     * is null
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
     * is null
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
     * is null
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
     * is null
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
     * is null
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
     * <p>
     * If the named object is an {@link IMObjectReference}, it will be
     * resolved.
     *
     * @param name the node name
     * @return the node value
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getObject(String name) {
        Object value = getValue(name);
        if (value instanceof IMObjectReference) {
            return resolve((IMObjectReference) value, false);
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
        NodeDescriptor node = toNode(name);
        return node.getValue(getObject());
    }

    /**
     * Returns the values of a collection node.
     *
     * @param name the node name
     * @return the collection corresponding to the node
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public List<IMObject> getValues(String name) {
        NodeDescriptor node = toNode(name);
        return node.getChildren(getObject());
    }

    /**
     * Returns an active lookup based on the value of a node.
     *
     * @param name the node name
     * @return the value. May be {@code null}
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public Lookup getLookup(String name) {
        return getLookup(name, true);
    }

    /**
     * Returns a lookup based on the value of a node
     *
     * @param name   the node name
     * @param active if {@code true}, only return the lookup if it is active
     * @return the value. May be {@code null}
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public Lookup getLookup(String name, boolean active) {
        NodeDescriptor node = toNode(name);
        Lookup result = null;
        IMObject object = getObject();
        Object value = node.getValue(object);
        if (value != null) {
            LookupAssertion assertion = LookupAssertionFactory.create(node, service, getLookups());
            Lookup lookup = assertion.getLookup(object, value.toString());
            if (lookup != null && (!active || lookup.isActive())) {
                result = lookup;
            }
        }
        return result;
    }

    /**
     * Returns the values of a collection node that match the supplied predicate.
     *
     * @param name      the node name
     * @param predicate the predicate
     * @return the objects matching the predicate
     * @throws IMObjectBeanException if the node doesn't exist
     */
    @Override
    public List<IMObject> getValues(String name, java.util.function.Predicate<IMObject> predicate) {
        return getValues(name).stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Returns the values of a collection node that match the supplied predicate.
     *
     * @param name      the node name
     * @param type      the expected object type
     * @param predicate the predicate
     * @return the objects matching the predicate
     * @throws IMObjectBeanException if the node doesn't exist
     */
    @Override
    public <T extends IMObject> List<T> getValues(String name, Class<T> type,
                                                  java.util.function.Predicate<T> predicate) {
        return getValues(name, type).stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Returns the first value of a collection node.
     *
     * @param name the node name
     * @return the first object in the collection, or {@code null} if none is found
     * @throws IMObjectBeanException if the node doesn't exist
     */
    @Override
    public IMObject getFirst(String name) {
        List<IMObject> values = getValues(name);
        return !values.isEmpty() ? values.get(0) : null;
    }

    /**
     * Returns the first value of a collection node.
     *
     * @param name the node name
     * @param type the object type
     * @return the first object in the collection, or {@code null} if none is found
     */
    @Override
    public <T extends IMObject> T getFirst(String name, Class<T> type) {
        return type.cast(getFirst(name));
    }

    /**
     * Returns the first value of a collection node that matches the supplied predicate.
     *
     * @param name      the node name
     * @param predicate the predicate
     * @return the first object matching the predicate, or {@code null} if none is found
     * @throws IMObjectBeanException if the node doesn't exist
     */
    @Override
    public IMObject getFirst(String name, java.util.function.Predicate<IMObject> predicate) {
        return getFirst(name, IMObject.class, predicate);
    }

    /**
     * Returns the first value of a collection node that matches the supplied predicate.
     *
     * @param name      the node name
     * @param type      the expected object type
     * @param predicate the predicate
     * @return the first object matching the predicate, or {@code null} if none is found
     * @throws IMObjectBeanException if the node doesn't exist
     */
    @Override
    public <T extends IMObject> T getFirst(String name, Class<T> type, java.util.function.Predicate<T> predicate) {
        return getValues(name, type).stream().filter(predicate).findFirst().get();
    }

    /**
     * Returns the source object from the first active {@link IMObjectRelationship} with active source object, for the
     * specified relationship node.
     *
     * @param name the relationship node name
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public IMObject getSource(String name) {
        return getSource(name, true);
    }

    /**
     * Returns the source object from the first active {@link IMObjectRelationship} with active source object, for the
     * specified relationship node.
     *
     * @param name the relationship node name
     * @param type the object type
     * @return the source object, or {@code null} if none is found
     */
    @Override
    public <T extends IMObject> T getSource(String name, Class<T> type) {
        return getSource(name, true, type);
    }

    /**
     * Returns the source object from the first {@link IMObjectRelationship} for the specified node.
     * <p>
     * Where an active object is required, the first active relationship with an active source will be used.<br/>
     * If an active object is not required, but a relationship matching the predicate exists and has an active object,
     * it will be returned over an inactive one.
     *
     * @param name   the relationship node name
     * @param active determines if the relationship and source object must be active
     * @return the source object, or {@code null} if none is found
     */
    @Override
    public IMObject getSource(String name, boolean active) {
        return getSource(name, active, getPredicate(active));
    }

    /**
     * Returns the source object from the first active {@link IMObjectRelationship} with active source object, for the
     * specified relationship node.
     *
     * @param name   the relationship node name
     * @param active determines if the relationship and source object must be active
     * @param type   the object type
     * @return the source object, or {@code null} if none is found
     */
    @Override
    public <T extends IMObject> T getSource(String name, boolean active, Class<T> type) {
        return type.cast(getSource(name, active, getPredicate(active)));
    }

    /**
     * Returns the source object from the first {@link IMObjectRelationship} matching the predicate, with active source
     * object.
     *
     * @param name      the relationship node name
     * @param predicate the predicate
     * @return the source object, or {@code null} if none is found
     */
    @Override
    public IMObject getSource(String name, java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getSource(name, true, predicate);
    }

    /**
     * Returns the source object from the first {@link IMObjectRelationship} matching the predicate, subject to
     * the specified active constraint.
     * <p>
     * Where an active object is required, the first relationship matching the predicate with an active source will
     * be used.<br/>
     * If an active object is not required, but a relationship matching the predicate exists and has an active object,
     * it will be returned over an inactive one.
     *
     * @param name      the relationship node name
     * @param active    determines if the object must be active or not
     * @param predicate the predicate  @return the source object, or {@code null} if none is found
     */
    @Override
    public IMObject getSource(String name, boolean active,
                              java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getRelatedObject(name, active, predicate, true);
    }

    /**
     * Returns the target object from the first active {@link IMObjectRelationship} with active target object, for the
     * specified relationship node.
     *
     * @param name the relationship node name
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public IMObject getTarget(String name) {
        return getTarget(name, true);
    }

    /**
     * Returns the target object from the first active {@link IMObjectRelationship} with active target object, for the
     * specified relationship node.
     *
     * @param name the relationship node name
     * @param type the object type
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public <T extends IMObject> T getTarget(String name, Class<T> type) {
        return type.cast(getTarget(name, true));
    }

    /**
     * Returns the target object from the first {@link IMObjectRelationship} for the specified node.
     * <p>
     * Where an active object is required, the first active relationship with an active target will be used.<br/>
     * If an active object is not required, but a relationship matching the predicate exists and has an active object,
     * it will be returned over an inactive one.
     *
     * @param name   the relationship node name
     * @param active determines if the relationship and target object must be active
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public IMObject getTarget(String name, boolean active) {
        return getTarget(name, active, getPredicate(active));
    }

    /**
     * Returns the target object from the first {@link IMObjectRelationship} for the specified node.
     * <p>
     * Where an active object is required, the first active relationship with an active target will be used.<br/>
     * If an active object is not required, but a relationship matching the predicate exists and has an active object,
     * it will be returned over an inactive one.
     *
     * @param name   the relationship node name
     * @param active determines if the relationship and target object must be active
     * @param type   the object type
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public <T extends IMObject> T getTarget(String name, boolean active, Class<T> type) {
        return type.cast(getTarget(name, active));
    }

    /**
     * Returns the target object from the first {@link IMObjectRelationship} matching the predicate, with active target
     * object.
     *
     * @param name      the relationship node name
     * @param predicate the predicate
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public IMObject getTarget(String name, java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getTarget(name, true, predicate);
    }

    /**
     * Returns the target object from the first {@link IMObjectRelationship} matching the predicate, subject to
     * the specified active constraint.
     * <p>
     * Where an active object is required, the first relationship matching the predicate with an active target will
     * be used.<br/>
     * If an active object is not required, but a relationship matching the predicate exists and has an active object,
     * it will be returned over an inactive one.
     *
     * @param name      the relationship node name
     * @param active    determines if the object must be active or not
     * @param predicate the predicate
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public IMObject getTarget(String name, boolean active,
                              java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getRelatedObject(name, active, predicate, false);
    }

    /**
     * Sets the target of a relationship.
     * <p>
     * If no relationship exists and:
     * <ul>
     * <li>{@code target} is non-null, one will be created.
     * <li>{@code target} is {@code null}, no relationship will be created</li>
     * </ul>
     * If multiple relationships exist, the first available will be selected.<br/>
     * <em>NOTE that this is not deterministic.</em>
     * <p>
     * If the returned relationship is bidirectional and new, the caller is responsible for adding it to the target.
     *
     * @param name   the node name
     * @param target the target of the relationship. May be {@code null}
     * @return the relationship, or {@code null} if {@code target} is {@code null} and there is no existing relationship
     */
    @Override
    public IMObjectRelationship setTarget(String name, IMObjectReference target) {
        IMObjectRelationship relationship = getFirst(name, IMObjectRelationship.class);
        if (relationship == null) {
            if (target != null) {
                relationship = addTarget(name, target);
            }
        } else {
            relationship.setTarget(target);
        }
        return relationship;
    }

    /**
     * Sets the target of a relationship.
     * <p>
     * If no relationship exists and:
     * <ul>
     * <li>{@code target} is non-null, one will be created.
     * <li>{@code target} is {@code null}, no relationship will be created</li>
     * </ul>
     * If multiple relationships exist, the first available will be selected.<br/>
     * <em>NOTE that this is not deterministic.</em>
     * <p>
     * If the returned relationship is bidirectional and new, the caller is responsible for adding it to the target.
     *
     * @param name   the node name
     * @param target the target of the relationship. May be {@code null}
     * @return the relationship, or {@code null} if {@code target} is {@code null} and there is no existing relationship
     */
    @Override
    public IMObjectRelationship setTarget(String name, IMObject target) {
        return setTarget(name, target != null ? target.getObjectReference() : null);
    }

    /**
     * Returns the active source objects from each active {@link IMObjectRelationship} for the specified node.
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node
     * @return a list of active source objects
     */
    @Override
    public List<IMObject> getSources(String name) {
        return getSources(name, IMObject.class);
    }

    /**
     * Returns the active source objects from each active {@link IMObjectRelationship} for the specified node.
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node
     * @param type the object type
     * @return a list of active source objects
     */
    @Override
    public <T extends IMObject> List<T> getSources(String name, Class<T> type) {
        return getSources(name, true, type, Relationships.activeNow());
    }

    /**
     * Returns the active source objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param name      the relationship node
     * @param predicate the predicate
     * @return a list of source objects
     */
    @Override
    public List<IMObject> getSources(String name, java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getSources(name, true, predicate);
    }

    /**
     * Returns the source objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param name      the relationship node
     * @param active    determines if the objects must be active
     * @param predicate the predicate
     * @return a list of source objects. May contain inactive objects, if {@code active} is {@code false}
     */
    @Override
    public List<IMObject> getSources(String name, boolean active,
                                     java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getSources(name, active, IMObject.class, predicate);
    }

    /**
     * Returns the source objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param name      the relationship node
     * @param active    determines if the objects must be active
     * @param type      the object type
     * @param predicate the predicate
     * @return a list of source objects. May contain inactive objects, if {@code active} is {@code false}
     */
    @Override
    public <T extends IMObject> List<T> getSources(String name, boolean active, Class<T> type,
                                                   java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getRelatedObjects(name, active, type, predicate, true, null);
    }

    /**
     * Returns the active target objects from each active {@link IMObjectRelationship} for the specified node. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node
     * @return a list of active target objects
     */
    @Override
    public List<IMObject> getTargets(String name) {
        return getTargets(name, IMObject.class);
    }

    /**
     * Returns the active target objects from each active {@link IMObjectRelationship} for the specified node. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node
     * @param type the object type
     * @return a list of active target objects
     */
    @Override
    public <T extends IMObject> List<T> getTargets(String name, Class<T> type) {
        return getTargets(name, true, type, Relationships.activeNow());
    }

    /**
     * Returns the active target objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     *
     * @param name      the relationship node
     * @param predicate the predicate
     * @return a list of target objects
     */
    @Override
    public List<IMObject> getTargets(String name, java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getTargets(name, true, IMObject.class, predicate);
    }

    /**
     * Returns the active target objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name      the relationship node
     * @param type      the object type
     * @param predicate the predicate
     * @return a list of active target objects
     */
    @Override
    public <T extends IMObject> List<T> getTargets(String name, Class<T> type,
                                                   java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getTargets(name, true, type, predicate);
    }

    /**
     * Returns the target objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name      the relationship node
     * @param active    determines if the objects must be active
     * @param predicate the predicate
     * @return a list of target objects. May contain inactive objects, if {@code active} is {@code false}
     */
    @Override
    public List<IMObject> getTargets(String name, boolean active,
                                     java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getTargets(name, active, IMObject.class, predicate);
    }

    /**
     * Returns the source objects from each {@link IMObjectRelationship} for the specified node that matches the
     * specified predicate.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name      the relationship node
     * @param active    determines if the objects must be active
     * @param type      the object type
     * @param predicate the predicate
     * @return a list of target objects. May contain inactive objects, if {@code active} is {@code false}
     */
    @Override
    public <T extends IMObject> List<T> getTargets(String name, boolean active, Class<T> type,
                                                   java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getRelatedObjects(name, active, type, predicate, false, null);
    }

    /**
     * Returns the source object reference from the first active {@link IMObjectRelationship} for the specified
     * relationship node.
     *
     * @param name the relationship node name
     * @return the source object reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public IMObjectReference getSourceRef(String name) {
        return getSourceRef(name, true);
    }

    /**
     * Returns the source object reference from the first {@link IMObjectRelationship} for the specified
     * relationship node.
     *
     * @param name   the relationship node name
     * @param active determines if the relationship must be active
     * @return the source object reference, or {@code null} if none is found
     */
    @Override
    public IMObjectReference getSourceRef(String name, boolean active) {
        return getSourceRef(name, getPredicate(active));
    }

    /**
     * Returns the source object reference from the \{@link IMObjectRelationship} matching the predicate.
     *
     * @param name      the relationship node name
     * @param predicate the predicate
     * @return the source object reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public IMObjectReference getSourceRef(String name, java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getRelatedRef(name, predicate, true);
    }

    /**
     * Returns the source object references from each active {@link IMObjectRelationship} for the specified node.
     *
     * @param name the relationship node
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    @Override
    public List<IMObjectReference> getSourceRefs(String name) {
        return getSourceRefs(name, Relationships.activeNow());
    }

    /**
     * Returns the source object references from each for the specified node that matches the
     * supplied predicate.
     *
     * @param name      the relationship node
     * @param predicate the predicate
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    @Override
    public List<IMObjectReference> getSourceRefs(String name,
                                                 java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getRelatedObjectRefs(name, predicate, true, null);
    }

    /**
     * Returns the target object reference from the first active {@link IMObjectRelationship} for the specified
     * relationship node.
     *
     * @param name the relationship node name
     * @return the target object reference, or {@code null} if none is found
     */
    @Override
    public IMObjectReference getTargetRef(String name) {
        return getTargetRef(name, true);
    }

    /**
     * Returns the target object reference from the first {@link IMObjectRelationship} for the specified
     * relationship node.
     *
     * @param name   the relationship node name
     * @param active determines if the relationship must be active
     * @return the target object reference, or {@code null} if none is found
     */
    @Override
    public IMObjectReference getTargetRef(String name, boolean active) {
        return getTargetRef(name, getPredicate(active));
    }

    /**
     * Returns the target object reference from the {@link IMObjectRelationship} matching the predicate.
     *
     * @param name      the relationship node name
     * @param predicate the predicate
     * @return the target object reference, or {@code null} if none is found
     */
    @Override
    public IMObjectReference getTargetRef(String name, java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getRelatedRef(name, predicate, false);
    }

    /**
     * Returns the target object references from each active {@link IMObjectRelationship} for the specified node.
     *
     * @param name the relationship node
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    @Override
    public List<IMObjectReference> getTargetRefs(String name) {
        return getTargetRefs(name, Relationships.activeNow());
    }

    /**
     * Returns the target object references from each {@link IMObjectRelationship} for the specified node that matches
     * the supplied predicate.
     *
     * @param name      the relationship node
     * @param predicate the predicate
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    @Override
    public List<IMObjectReference> getTargetRefs(String name,
                                                 java.util.function.Predicate<IMObjectRelationship> predicate) {
        return getRelatedObjectRefs(name, predicate, false, null);
    }

    /**
     * Determines if there is an active {@link IMObjectRelationship} with {@code object} as its source, for the node
     * {@code node}.
     *
     * @param name   the relationship node
     * @param object the target object
     * @return {@code true} if there is an active relationship to {@code object}
     */
    @Override
    public boolean hasSource(String name, IMObject object) {
        return getSourceRefs(name).contains(object.getObjectReference());
    }

    /**
     * Determines if there is an active {@link PeriodRelationship} with {@code object} as its target, for the node
     * {@code node}.
     *
     * @param name   the relationship node
     * @param object the target object
     * @return {@code true} if there is an active relationship to {@code object}
     */
    @Override
    public boolean hasTarget(String name, IMObject object) {
        return getTargetRefs(name).contains(object.getObjectReference());
    }

    /**
     * Adds a new relationship between the current object (the source), and the supplied target.
     * <p>
     * If the relationship is bidirectional, the caller is responsible for adding the returned relationship
     * to the target.
     *
     * @param name   the name
     * @param target the target
     * @return the new relationship
     */
    @Override
    public IMObjectRelationship addTarget(String name, IMObjectReference target) {
        String archetype = getRelationshipArchetype(name, target, "target");
        return addTarget(name, archetype, target);
    }

    /**
     * Adds a new relationship between the current object (the source), and the supplied target.
     * <p>
     * If the relationship is bidirectional, the caller is responsible for adding the returned relationship
     * to the target.
     *
     * @param name   the name
     * @param target the target
     * @return the new relationship
     */
    @Override
    public IMObjectRelationship addTarget(String name, IMObject target) {
        return addTarget(name, target.getObjectReference());
    }

    /**
     * Adds a new relationship between the current object (the source), and the supplied target.
     * <p>
     * If the relationship is bidirectional, the caller is responsible for adding the returned relationship
     * to the target.
     *
     * @param name      the node name
     * @param archetype the relationship archetype short name
     * @param target    the target
     * @return the new relationship
     */
    @Override
    public IMObjectRelationship addTarget(String name, String archetype, IMObject target) {
        return addTarget(name, archetype, target.getObjectReference());
    }

    /**
     * Adds a new relationship between the current object (the source), and the supplied target.
     * <p>
     * If the relationship is bidirectional, the caller is responsible for adding the returned relationship
     * to the target.
     *
     * @param name      the node name
     * @param archetype the relationship archetype short name
     * @param target    the target
     * @return the new relationship
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the relationship archetype is not found
     */
    @Override
    public IMObjectRelationship addTarget(String name, String archetype, IMObjectReference target) {
        IMObjectRelationship r = (IMObjectRelationship) getArchetypeService().create(archetype);
        if (r == null) {
            throw new IMObjectBeanException(ArchetypeNotFound, archetype);
        }
        r.setSource(getReference());
        r.setTarget(target);
        addValue(name, r);
        return r;
    }

    /**
     * Saves the object, and those supplied, in a single transaction.
     *
     * @param objects the other objects to save
     */
    @Override
    public void save(IMObject... objects) {
        List<IMObject> toSave = new ArrayList<>();
        toSave.add(getObject());
        toSave.addAll(Arrays.asList(objects));
        service.save(toSave);
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
     * Returns the first value of a collection node that matches the supplied predicate.
     *
     * @param name      the node name
     * @param predicate the predicate
     * @param type      the expected object type
     * @return the first object matching the predicate, or {@code null} if none is found
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public <T extends IMObject> T getValue(String name, Predicate predicate, Class<T> type) {
        for (T object : getValues(name, type)) {
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
        return getSource(node);
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
        return getSource(node, active);
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
        return getSource(node, predicate::evaluate);
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
        return getSource(node, active, predicate::evaluate);
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
        return getTarget(node);
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
        return getTarget(node, active);
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
        return getTarget(node, predicate::evaluate);
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
        return getTarget(node, active, predicate::evaluate);
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
        return getSource(node, Relationships.activeAt(time));
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
        return getSource(node, active, Relationships.activeAt(time));
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
        return getTarget(node, true, Relationships.activeAt(time));
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
        return getTarget(node, active, Relationships.activeAt(time));
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
        return getSources(node);
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
        return getSources(node, type);
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
        return getSources(node, Relationships.activeAt(time));
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
        return getSources(node, true, type, Relationships.activeAt(time));
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
        return getSources(node, active, Relationships.activeAt(time));
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
        return getSources(node, active, type, Relationships.activeAt(time));
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
        return getSources(node, true, predicate::evaluate);
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
        return getSources(node, active, IMObject.class, predicate::evaluate);
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
        return getSources(node, active, type, predicate::evaluate);
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
        return getTargets(node);
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
        return getTargets(node, type);
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
        return getTargets(node, Relationships.activeAt(time));
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
        return getTargets(node, true, type, Relationships.activeAt(time));
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
        return getTargets(node, active, IMObject.class, Relationships.activeAt(time));
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
        return getTargets(node, active, type, Relationships.activeAt(time));
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
        return getTargets(node, predicate::evaluate);
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
        return getTargets(node, true, type, predicate::evaluate);
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
        return getTargets(node, active, predicate::evaluate);
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
        return getTargets(node, active, type, predicate::evaluate);
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
        return getSourceRef(node);
    }

    /**
     * Returns the source object reference from the first {@link IMObjectRelationship} for the specified
     * relationship node.
     *
     * @param node   the relationship node name
     * @param active determines if the relationship must be active
     * @return the source object reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getNodeSourceObjectRef(String node, boolean active) {
        return getSourceRef(node, active);
    }

    /**
     * Returns the source object references from each active {@link IMObjectRelationship} for the specified node.
     *
     * @param node the relationship node
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    public List<IMObjectReference> getNodeSourceObjectRefs(String node) {
        return getSourceRefs(node);
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
        return getSourceRefs(node, Relationships.activeAt(time));
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
        return getSourceRefs(node, predicate::evaluate);
    }

    /**
     * Returns the target object references from each active {@link PeriodRelationship} for the specified node.
     *
     * @param node the relationship node
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    public List<IMObjectReference> getNodeTargetObjectRefs(String node) {
        return getTargetRefs(node);
    }

    /**
     * Returns the target object reference from the first active {@link IMObjectRelationship} for the specified node.
     *
     * @param node the relationship node
     * @return the target reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getNodeTargetObjectRef(String node) {
        return getTargetRef(node);
    }

    /**
     * Returns the target object reference from the first {@link IMObjectRelationship} for the specified node.
     *
     * @param node   the relationship node
     * @param active determines if the relationship must be active
     * @return the target reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getNodeTargetObjectRef(String node, boolean active) {
        return getTargetRef(node, active);
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
        return getTargetRefs(node, Relationships.activeAt(time));
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
        return getTargetRefs(node, predicate::evaluate);
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
        return hasTarget(node, object);
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
     * Determines if there is an active {@link IMObjectRelationship} with {@code object} as its source, for the node
     * {@code node}.
     *
     * @param node   the relationship node
     * @param object the target object
     * @return {@code true} if there is an active relationship to {@code object}
     */
    public boolean hasNodeSource(String node, IMObject object) {
        return hasSource(node, object);
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
        NodeDescriptor node = toNode(name);
        node.addChildToCollection(properties.getObject(), value);
    }

    /**
     * Removes a value from a collection.
     *
     * @param name  the node name
     * @param value the value to remove
     */
    public void removeValue(String name, IMObject value) {
        NodeDescriptor node = toNode(name);
        node.removeChildFromCollection(properties.getObject(), value);
    }

    /**
     * Adds a new relationship between the current object (the source), and the supplied target.
     * <p>
     * If the relationship is bidirectional, the caller is responsible for adding the returned relationship
     * to the target.
     *
     * @param name   the name
     * @param target the target
     * @return the new relationship
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the relationship archetype is not found
     */
    public IMObjectRelationship addNodeTarget(String name, IMObjectReference target) {
        return addTarget(name, target);
    }

    /**
     * Adds a new relationship between the current object (the source), and the supplied target.
     * <p>
     * If the relationship is bidirectional, the caller is responsible for adding the returned relationship
     * to the target.
     *
     * @param name   the name
     * @param target the target
     * @return the new relationship
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the relationship archetype is not found
     */
    public IMObjectRelationship addNodeTarget(String name, IMObject target) {
        return addTarget(name, target);
    }

    /**
     * Adds a new relationship between the current object (the source), and the supplied target.
     * <p>
     * If the relationship is bidirectional, the caller is responsible for adding the returned relationship
     * to the target.
     *
     * @param name      the node name
     * @param shortName the relationship archetype short name
     * @param target    the target
     * @return the new relationship
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the relationship archetype is not found
     */
    public IMObjectRelationship addNodeTarget(String name, String shortName, IMObject target) {
        return addTarget(name, shortName, target);
    }

    /**
     * Adds a new relationship between the current object (the source), and the supplied target.
     * <p>
     * If the relationship is bidirectional, the caller is responsible for adding the returned relationship
     * to the target.
     *
     * @param name      the node name
     * @param shortName the relationship archetype short name
     * @param target    the target
     * @return the new relationship
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the relationship archetype is not found
     */
    public IMObjectRelationship addNodeTarget(String name, String shortName, IMObjectReference target) {
        return addTarget(name, shortName, target);
    }

    /**
     * Evaluates the default value if a node, if it has one.
     *
     * @param name the node name
     * @return the evaluation of {@link NodeDescriptor#getDefaultValue()} (which may evaluate {@code null}),
     * or {@code null} if the node doesn't have a default value
     * @throws IMObjectBeanException if the descriptor doesn't exist
     */
    public Object getDefaultValue(String name) {
        Object result = null;
        NodeDescriptor node = toNode(name);
        String expression = node.getDefaultValue();
        if (!StringUtils.isEmpty(expression)) {
            result = evaluate(expression);
        }
        return result;
    }

    /**
     * Determines if a node is unchanged from its default value.
     *
     * @param name the node name
     * @return {@code true} if the node is unchanged from its default value
     */
    public boolean isDefaultValue(String name) {
        boolean result = false;
        NodeDescriptor node = toNode(name);
        String expression = node.getDefaultValue();
        if (!StringUtils.isEmpty(expression)) {
            Object value = evaluate(expression);
            result = ObjectUtils.equals(getValue(name), value);
        }
        return result;
    }

    /**
     * Derived values for the object.
     * <p>
     * For each node with {@code derived=true}, the node will be evaluated and the corresponding value set.
     *
     * @throws ArchetypeServiceException if values cannot be derived
     */
    public void deriveValues() {
        getArchetypeService().deriveValues(getObject());
    }

    /**
     * Saves the object.
     * <p>
     * Any derived nodes will have their values derived prior to the object
     * being saved.
     *
     * @throws ArchetypeServiceException if the object can't be saved
     */
    public void save() {
        IMObject object = getObject();
        IArchetypeService service = getArchetypeService();
        service.deriveValues(object);
        service.save(object);
    }

    /**
     * Helper to return an object as a bean.
     *
     * @param object the object
     * @return the bean
     */
    @Override
    public IMObjectBean getBean(IMObject object) {
        return new IMObjectBean(object, service, lookups);
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
        IMObject object = resolve(ref, active);
        if (object != null) {
            if (!type.isInstance(object)) {
                throw new IMObjectBeanException(InvalidClassCast, type.getName(), object.getClass().getName());
            }
        }
        return (T) object;
    }

    /**
     * Helper to resolve a reference.
     *
     * @param ref        the reference. May be {@code null}
     * @param activeOnly if {@code true}, only return the object if it is active
     * @return the object corresponding to the reference or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObject resolve(IMObjectReference ref, boolean activeOnly) {
        IMObject result = null;
        if (ref != null) {
            result = getArchetypeService().get(ref, activeOnly);
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
            // TODO - this should not be supported. IArchetypeService should always be supplied at construction.
            service = ArchetypeServiceHelper.getArchetypeService();
        }
        return service;
    }

    /**
     * Returns the lookup service.
     *
     * @return the lookup service
     */
    protected ILookupService getLookups() {
        if (lookups == null) {
            // TODO - this should not be supported. ILookupService should always be supplied at construction.
            lookups = LookupServiceHelper.getLookupService();
        }
        return lookups;
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
        List<IMObjectReference> result = new ArrayList<>();
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
            result = new HashMap<>();
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
        Map<R, IMObjectReference> result = new HashMap<>();
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
     * @param active    determines if the object must be active
     * @param predicate the criteria
     * @param source    if {@code true}, return the source of the relationship, otherwise return the target
     * @return the first object, or {@code null} if none is found
     */
    @SuppressWarnings("unchecked")
    protected IMObject getRelatedObject(String node, boolean active,
                                        java.util.function.Predicate<IMObjectRelationship> predicate, boolean source) {
        List<IMObject> relationships = getValues(node);
        return getRelatedObject(relationships, predicate, active, source);
    }

    /**
     * Returns the source or target from the first relationship matching the specified criteria.
     * <p>
     * If active is {@code true} the object must be active in order to be returned.
     * <p>
     * If active is {@code false}, then an active object will be returned in preference to an inactive one.
     *
     * @param relationships the relationships
     * @param predicate     the predicate
     * @param active        determines if the object must be active or not
     * @param source        if {@code true}, return the source of the relationship, otherwise return the target
     * @return the first object matching the criteria or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObject getRelatedObject(List<IMObject> relationships,
                                        java.util.function.Predicate<IMObjectRelationship> predicate,
                                        boolean active, boolean source) {
        Function<IMObjectRelationship, IMObjectReference> accessor
                = (source) ? IMObjectRelationship::getSource : IMObjectRelationship::getTarget;
        IMObject inactive = null;
        for (IMObject r : relationships) {
            IMObjectRelationship relationship = IMObjectRelationship.class.cast(r);
            if (predicate.test(relationship)) {
                IMObjectReference ref = accessor.apply(relationship);
                IMObject object = resolve(ref, active);
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
     * Returns the first object reference from the supplied relationship node that matches the specified criteria.
     *
     * @param name      the node name
     * @param predicate the criteria
     * @param source    if {@code true}, return the source of the relationship, otherwise return the target
     * @return the matching reference, or {@code null}
     */
    protected IMObjectReference getRelatedRef(String name, java.util.function.Predicate<IMObjectRelationship> predicate,
                                              boolean source) {
        Function<IMObjectRelationship, IMObjectReference> accessor
                = (source) ? IMObjectRelationship::getSource : IMObjectRelationship::getTarget;
        for (IMObject object : getValues(name)) {
            IMObjectRelationship relationship = IMObjectRelationship.class.cast(object);
            if (predicate.test(relationship)) {
                IMObjectReference reference = accessor.apply(relationship);
                if (reference != null) {
                    return reference;
                }
            }
        }
        return null;
    }

    /**
     * Returns all objects for the specified relationship node that match the specified criteria.
     *
     * @param node       the relationship node
     * @param active     determines if the objects must be active
     * @param type       the expected object type
     * @param predicate  the criteria to filter relationships
     * @param source     if {@code true}, return the source of the relationship, otherwise return the target
     * @param comparator if non-null, specifies a comparator to sort relationships  @return a list of objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    protected <T extends IMObject, R extends IMObjectRelationship> List<T> getRelatedObjects(
            String node, boolean active, Class<T> type, java.util.function.Predicate<R> predicate, boolean source,
            Comparator<R> comparator) {
        List<IMObjectReference> refs = getRelatedObjectRefs(node, predicate, source, comparator);
        return resolve(refs, type, active);
    }

    /**
     * Returns all related references for the specified node that match the specified criteria.
     *
     * @param node       the relationship node
     * @param predicate  the criteria
     * @param source     if {@code true}, return the source of the relationship, otherwise return the target
     * @param comparator if non-null, specifies a comparator to sort relationships  @return the matching references
     */
    @SuppressWarnings("unchecked")
    protected <R extends IMObjectRelationship> List<IMObjectReference> getRelatedObjectRefs(
            String node, java.util.function.Predicate predicate, boolean source, Comparator<R> comparator) {
        List<R> relationships = (List<R>) getValues(node, IMObjectRelationship.class);
        if (comparator != null) {
            Collections.sort(relationships, comparator);
        }
        return getRelatedRefs(relationships, predicate, source);
    }

    /**
     * Returns all object references from the supplied relationships that match the specified criteria.
     *
     * @param relationships the relationships
     * @param predicate     the criteria
     * @param source        if {@code true}, return the source of the relationship, otherwise return the target
     * @return the matching references
     */
    protected <R extends IMObjectRelationship> List<IMObjectReference> getRelatedRefs(
            Collection<R> relationships, java.util.function.Predicate<R> predicate,
            boolean source) {
        Function<IMObjectRelationship, IMObjectReference> accessor
                = (source) ? IMObjectRelationship::getSource : IMObjectRelationship::getTarget;
        List<IMObjectReference> result = new ArrayList<>();
        relationships.stream().filter(predicate).forEach(r -> {
            IMObjectReference reference = accessor.apply(r);
            if (reference != null) {
                result.add(reference);
            }
        });
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
    protected <R extends IMObjectRelationship, T extends IMObject> List<T> getRelatedObjects(
            Collection<R> relationships, Predicate predicate, RelationshipRef accessor, boolean active,
            Class<T> type) {
        List<IMObjectReference> refs = getRelatedRefs(relationships, predicate, accessor);
        return resolve(refs, type, active);
    }

    /**
     * Resolves references.
     * <p>
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
        List<T> result = new ArrayList<>();
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
     * <p>
     * If active is {@code true} the object must be active in order to be returned.
     * <p>
     * If active is {@code false}, then an active object will be returned in preference to an inactive one.
     *
     * @param relationships the relationships
     * @param predicate     the predicate
     * @param accessor      the relationship reference accessor
     * @param active        determines if the object must be active or not
     * @return the first object matching the criteria or {@code null} if none
     * is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected <R extends IMObjectRelationship> IMObject getRelatedObject(Collection<R> relationships,
                                                                         Predicate predicate, RelationshipRef accessor,
                                                                         boolean active) {
        IMObject inactive = null;
        for (R relationship : relationships) {
            if (predicate.evaluate(relationship)) {
                IMObjectReference ref = accessor.transform(relationship);
                IMObject object = resolve(ref, false);
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
    protected String getRelationshipArchetype(String name, IMObject target, String targetName) {
        return getRelationshipArchetype(name, target.getObjectReference(), targetName);
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
    protected String getRelationshipArchetype(String name, IMObjectReference target, String targetName) {
        String[] range = getArchetypeRange(name);
        IArchetypeService service = getArchetypeService();
        String result = null;
        ArchetypeId archetypeId = target.getArchetypeId();
        for (String shortName : range) {
            ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(shortName);
            if (descriptor != null) {
                NodeDescriptor node = descriptor.getNodeDescriptor(targetName);
                if (node != null && isA(node, archetypeId)) {
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
        List<T> result = new ArrayList<>();
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

    protected <R extends IMObjectRelationship> java.util.function.Predicate<R> getPredicate(boolean active) {
        return active ? Relationships.activeNow() : x -> true;
    }

    /**
     * Returns a node descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to {@code name}
     * @throws IMObjectBeanException if the descriptor doesn't exist
     */
    protected NodeDescriptor toNode(String name) {
        return properties.getNode(name);
    }

    /**
     * Determines if a node is a particular archetype.
     *
     * @param node        the node
     * @param archetypeId the archetype identifier
     * @return {@code true} if the node is the archetype, otherwise {@code false}
     */
    private boolean isA(NodeDescriptor node, ArchetypeId archetypeId) {
        String[] shortNames = node.getArchetypeRange();
        return (shortNames.length != 0) ? TypeHelper.isA(archetypeId, shortNames)
                                        : TypeHelper.isA(archetypeId, node.getFilter());
    }

    /**
     * Evaluates a JXPath expression.
     *
     * @param expression the expression
     * @return the result of the expression
     */
    private Object evaluate(String expression) {
        Object result;
        JXPathContext context = JXPathHelper.newContext(properties.getObject());
        result = context.getValue(expression);
        return result;
    }

    /**
     * Implementation of {@link PropertySet} for nodes.
     */
    private class NodePropertySet extends AbstractNodePropertySet {

        /**
         * Constructs an {@link NodePropertySet}.
         *
         * @param object the object
         */
        public NodePropertySet(IMObject object) {
            super(object);
        }

        /**
         * Returns the archetype service.
         *
         * @return the archetype service
         */
        @Override
        protected IArchetypeService getArchetypeService() {
            return IMObjectBean.this.getArchetypeService();
        }
    }

}
