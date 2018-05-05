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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.collections.Predicate;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.model.bean.Policies;
import org.openvpms.component.model.bean.Policy;
import org.openvpms.component.model.bean.Predicates;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.PeriodRelationship;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.InvalidClassCast;
import static org.openvpms.component.model.bean.Policies.active;
import static org.openvpms.component.model.bean.Policies.any;
import static org.openvpms.component.model.bean.Predicates.targetEquals;


/**
 * Helper to access an {@link IMObject}'s properties via their names.
 *
 * @author Tim Anderson
 */
public class IMObjectBean implements org.openvpms.component.model.bean.IMObjectBean {

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
    public IMObjectBean(org.openvpms.component.model.object.IMObject object) {
        this(object, null);
    }

    /**
     * Constructs an {@link IMObjectBean}.
     *
     * @param object  the object
     * @param service the archetype service. May be {@code null}
     */
    public IMObjectBean(org.openvpms.component.model.object.IMObject object, IArchetypeService service) {
        this.service = service;
        this.properties = new NodePropertySet((IMObject) object);
    }

    /**
     * Constructs an {@link IMObjectBean}.
     *
     * @param object  the object
     * @param service the archetype service. May be {@code null}
     * @param lookups the lookup service. May be {@code null}
     */
    public IMObjectBean(org.openvpms.component.model.object.IMObject object, IArchetypeService service,
                        ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
        this.properties = new NodePropertySet((IMObject) object);
    }

    /**
     * Returns the underlying object.
     *
     * @return the object
     */
    @Override
    public IMObject getObject() {
        return properties.getObject();
    }

    /**
     * Returns a reference to the underlying object.
     *
     * @return the reference
     */
    @Override
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
        return getObject().isA(shortNames);
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
     * If the named object is an {@link Reference}, it will be resolved.
     * <p>
     * If the node is a collection, the first value will be returned. If the collection has multiple elements, the
     * element that is returned is non-deterministic, so this should be only used for collections with 0..1 cardinality.
     *
     * @param name the node name
     * @return the node value, or {@code null} if no value exists. Returned objects may be inactive
     */
    @Override
    public IMObject getObject(String name) {
        IMObject result;
        NodeDescriptor node = toNode(name);
        if (node.isCollection()) {
            List<org.openvpms.component.business.domain.im.common.IMObject> values = getValues(node);
            result = !values.isEmpty() ? values.get(0) : null;
        } else {
            Object value = getValue(node);
            if (value instanceof Reference) {
                result = resolve((Reference) value, Policy.State.ANY);
            } else {
                result = (IMObject) value;
            }
        }
        return result;
    }

    /**
     * Returns the object at the specified node.
     * <p>
     * If the named object is an {@link Reference}, it will be resolved.
     * <p>
     * If the node is a collection, the first value will be returned. If the collection has multiple elements, the
     * element that is returned is non-deterministic, so this should be only used for collections with 0..1 cardinality.
     *
     * @param name the node name
     * @param type the object type
     * @return the node value, or {@code null} if no value exists. Returned objects may be inactive
     */
    @Override
    public <T extends org.openvpms.component.model.object.IMObject> T getObject(String name, Class<T> type) {
        IMObject result = getObject(name);
        return type.cast(result);
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
        return getValue(node);
    }

    /**
     * Returns the values of a collection node.
     *
     * @param name the node name
     * @return the collection corresponding to the node
     * @throws IMObjectBeanException if the node doesn't exist
     */
    @SuppressWarnings("unchecked")
    public List<org.openvpms.component.model.object.IMObject> getValues(String name) {
        NodeDescriptor node = toNode(name);
        return (List) getValues(node);
    }

    /**
     * Returns a lookup based on the value of a node.
     *
     * @param name the node name
     * @return the value. May be {@code null}, or be inactive
     * @throws IMObjectBeanException if the node doesn't exist
     */
    public Lookup getLookup(String name) {
        NodeDescriptor node = toNode(name);
        Lookup result = null;
        org.openvpms.component.business.domain.im.common.IMObject object = getObject();
        Object value = node.getValue(object);
        if (value != null) {
            LookupAssertion assertion = LookupAssertionFactory.create(node, service, getLookups());
            result = assertion.getLookup(object, value.toString());
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
    public List<org.openvpms.component.model.object.IMObject> getValues(
            String name, java.util.function.Predicate<org.openvpms.component.model.object.IMObject> predicate) {
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
    public <T extends org.openvpms.component.model.object.IMObject> List<T> getValues(
            String name, Class<T> type, java.util.function.Predicate<T> predicate) {
        return getValues(name, type).stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Returns the source object from the first {@link Relationship} with source object, for the specified relationship
     * node.
     * <br/>
     * If there are multiple relationships, the first active object will be returned in preference to an inactive one.
     *
     * @param name the relationship node name
     * @return the source object, or {@code null} if none is found
     */
    @Override
    public IMObject getSource(String name) {
        return getSource(name, IMObject.class);
    }

    /**
     * Returns the source object from the first {@link Relationship} with source object, for the specified relationship
     * node.
     * <br/>
     * If there are multiple relationships, the first active object will be returned in preference to an inactive one.
     *
     * @param name the relationship node name
     * @param type the object type
     * @return the source object, or {@code null} if none is found
     */
    @Override
    public <T extends org.openvpms.component.model.object.IMObject> T getSource(String name, Class<T> type) {
        return getSource(name, type, any());
    }

    /**
     * Returns the source object from the first {@link Relationship} for the specified node matching the policy.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return the source object, or {@code null} if none is found
     */
    @Override
    public <R extends Relationship> IMObject getSource(String name, Policy<R> policy) {
        return getSource(name, IMObject.class, policy);
    }

    /**
     * Returns the source object from the first {@link Relationship} for the specified node matching the policy.
     *
     * @param name   the relationship node name
     * @param type   the object type
     * @param policy the policy for relationship selection and object retrieval
     * @return the source object, or {@code null} if none is found
     */
    @Override
    public <T extends org.openvpms.component.model.object.IMObject, R extends Relationship> T getSource(
            String name, Class<T> type, Policy<R> policy) {
        return type.cast(getRelatedObject(name, policy, true));
    }

    /**
     * Returns the target object from the first {@link Relationship} with target object, for the specified relationship
     * node.
     * <br/>
     * If there are multiple relationships, the first active object will be returned in preference to an inactive one.
     *
     * @param name the relationship node name
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public IMObject getTarget(String name) {
        return getTarget(name, IMObject.class);
    }

    /**
     * Returns the target object from the first {@link Relationship} with target object, for the specified relationship
     * node.
     * <br/>
     * If there are multiple relationships, the first active object will be returned in preference to an inactive one.
     *
     * @param name the relationship node name
     * @param type the object type
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public <T extends org.openvpms.component.model.object.IMObject> T getTarget(String name, Class<T> type) {
        return getTarget(name, type, Policies.any());
    }

    /**
     * Returns the target object from the first {@link Relationship} for the specified node matching the policy.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public <R extends Relationship> IMObject getTarget(String name, Policy<R> policy) {
        return getTarget(name, IMObject.class, policy);
    }

    /**
     * Returns the target object from the first {@link Relationship} for the specified node matching the policy.
     *
     * @param name   the relationship node name
     * @param type   the object type
     * @param policy the policy for relationship selection and object retrieval
     * @return the target object, or {@code null} if none is found
     */
    @Override
    public <T extends org.openvpms.component.model.object.IMObject, R extends Relationship> T getTarget(
            String name, Class<T> type, Policy<R> policy) {
        return type.cast(getRelatedObject(name, policy, false));
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
    public Relationship setTarget(String name, Reference target) {
        Relationship relationship = getObject(name, Relationship.class);
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
    public Relationship setTarget(String name, org.openvpms.component.model.object.IMObject target) {
        return setTarget(name, target != null ? target.getObjectReference() : null);
    }

    /**
     * Returns the source objects from each {@link Relationship} for the specified node.
     * <br/>
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node
     * @return a list of source objects. May be both active and inactive
     */
    @Override
    public List<org.openvpms.component.model.object.IMObject> getSources(String name) {
        return getSources(name, org.openvpms.component.model.object.IMObject.class);
    }

    /**
     * Returns the source objects from each {@link Relationship} for the specified node.
     * <br/>
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node
     * @param type the object type
     * @return a list of source objects. May be both active and inactive
     */
    @Override
    public <T extends org.openvpms.component.model.object.IMObject> List<T> getSources(String name, Class<T> type) {
        return getSources(name, type, Policies.any());
    }

    /**
     * Returns the source objects from each {@link Relationship} matching the policy, for the specified node.
     * <br/>
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node
     * @param policy the policy
     * @return a list of source objects matching the policy
     */
    @Override
    public <R extends Relationship> List<org.openvpms.component.model.object.IMObject> getSources(
            String name, Policy<R> policy) {
        return getSources(name, org.openvpms.component.model.object.IMObject.class, policy);
    }

    /**
     * Returns the source objects from each {@link Relationship} matching the policy, for the specified node.
     * <br/>
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node
     * @param type   the object type
     * @param policy the policy
     * @return a list of source objects matching the policy
     */
    @Override
    public <T extends org.openvpms.component.model.object.IMObject, R extends Relationship> List<T> getSources(
            String name, Class<T> type, Policy<R> policy) {
        return getRelatedObjects(name, type, policy, true);
    }

    /**
     * Returns the target objects from each {@link Relationship} for the specified node.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node name
     * @return a list of target objects. May be both active and inactive
     */
    @Override
    public List<org.openvpms.component.model.object.IMObject> getTargets(String name) {
        return getTargets(name, org.openvpms.component.model.object.IMObject.class);
    }

    /**
     * Returns the target objects from each {@link Relationship} for the specified node.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node name
     * @param type the object type
     * @return a list of target objects. May be both active and inactive
     */
    @Override
    public <T extends org.openvpms.component.model.object.IMObject> List<T> getTargets(String name, Class<T> type) {
        return getTargets(name, type, any());
    }

    /**
     * Returns the target objects from each {@link Relationship} matching the policy, for the specified node.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node
     * @param policy the policy
     * @return a list of target objects matching the policy
     */
    @Override
    public <R extends Relationship> List<org.openvpms.component.model.object.IMObject> getTargets(
            String name, Policy<R> policy) {
        return getTargets(name, org.openvpms.component.model.object.IMObject.class, policy);
    }

    /**
     * Returns the target objects from each {@link Relationship} matching the policy, for the specified node.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node
     * @param type   the object type
     * @param policy the policy
     * @return a list of target objects matching the policy
     */
    @Override
    public <T extends org.openvpms.component.model.object.IMObject, R extends Relationship> List<T> getTargets(
            String name, Class<T> type, Policy<R> policy) {
        return getRelatedObjects(name, type, policy, false);
    }

    /**
     * Returns the source object reference from the first {@link Relationship} for the specified relationship node.
     *
     * @param name the relationship node name
     * @return the source object reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Reference getSourceRef(String name) {
        return getSourceRef(name, any());
    }

    /**
     * Returns the source object reference from the first {@link Relationship} for the specified node matching
     * the policy.
     *
     * @param name   the relationship node name
     * @param policy the policy
     * @return the source object reference, or {@code null} if none is found
     */
    @Override
    public <R extends Relationship> Reference getSourceRef(String name, Policy<R> policy) {
        return getRelatedRef(name, policy, true);
    }

    /**
     * Returns the source object references from each {@link Relationship} for the specified node.
     *
     * @param name the relationship node
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    @Override
    public List<Reference> getSourceRefs(String name) {
        return getSourceRefs(name, any());
    }

    /**
     * Returns the source object references from each for the specified node that matches the supplied policy.
     *
     * @param name   the relationship node name
     * @param policy the policy
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    @Override
    public <R extends Relationship> List<Reference> getSourceRefs(String name, Policy<R> policy) {
        return getRelatedRefs(name, policy, true);
    }

    /**
     * Returns the target object reference from the first {@link Relationship} for the specified relationship node.
     *
     * @param name the relationship node name
     * @return the target object reference, or {@code null} if none is found
     */
    @Override
    public Reference getTargetRef(String name) {
        return getTargetRef(name, any());
    }

    /**
     * Returns the target object reference from the first {@link Relationship} for the specified relationship
     * node.
     *
     * @param name   the relationship node name
     * @param policy the policy
     * @return the target object reference, or {@code null} if none is found
     */
    @Override
    public <R extends Relationship> IMObjectReference getTargetRef(String name, Policy<R> policy) {
        return getRelatedRef(name, policy, false);
    }

    /**
     * Returns the target object references from each {@link Relationship} for the specified node.
     *
     * @param name the relationship node name
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    @Override
    public List<Reference> getTargetRefs(String name) {
        return getTargetRefs(name, any());
    }

    /**
     * Returns the target object references from each {@link Relationship} for the specified node that matches
     * the supplied predicate.
     *
     * @param name   the relationship node
     * @param policy the predicate
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    @Override
    public <R extends Relationship> List<Reference> getTargetRefs(String name, Policy<R> policy) {
        return getRelatedRefs(name, policy, false);
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
    public Relationship addTarget(String name, Reference target) {
        String archetype = getRelationshipArchetype(name, target);
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
    public Relationship addTarget(String name, org.openvpms.component.model.object.IMObject target) {
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
    public Relationship addTarget(String name, String archetype, org.openvpms.component.model.object.IMObject target) {
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
    public Relationship addTarget(String name, String archetype, Reference target) {
        Relationship r = (Relationship) getArchetypeService().create(archetype);
        if (r == null) {
            throw new IMObjectBeanException(ArchetypeNotFound, archetype);
        }
        r.setSource(getReference());
        r.setTarget(target);
        addValue(name, r);
        return r;
    }

    /**
     * Adds a bidirectional relationship between the current object (the source) and the supplied target.
     *
     * @param sourceName the source node name
     * @param target     the target
     * @param targetName the target node name
     * @return a new relationship
     */
    @Override
    public IMObjectRelationship addTarget(String sourceName, org.openvpms.component.model.object.IMObject target,
                                          String targetName) {
        Relationship relationship = addTarget(sourceName, target);
        getBean(target).addValue(targetName, relationship);
        return (IMObjectRelationship) relationship;
    }

    /**
     * Removes all bidirectional relationships between the current object (the source), and the supplied target.
     *
     * @param sourceName the source node name
     * @param target     the target
     * @param targetName the target node name
     */
    @Override
    public void removeTargets(String sourceName, org.openvpms.component.model.object.IMObject target,
                              String targetName) {
        List<Relationship> relationships = getValues(sourceName, Relationship.class, targetEquals(target));
        if (!relationships.isEmpty()) {
            IMObjectBean targetBean = getBean(target);
            for (Relationship relationship : relationships) {
                removeValue(sourceName, relationship);
                targetBean.removeValue(targetName, relationship);
            }
        }
    }

    /**
     * Returns the related objects from each active {@link Relationship} for the specified node.
     * <p>
     * This will return the target of the relationships if this is the source, or the source of the relationships
     * if this is the target.
     * <br/>
     * If a reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node name
     * @return a list of active related objects
     */
    public List<IMObject> getRelated(String name) {
        return getRelated(name, IMObject.class);
    }

    /**
     * Returns the related objects from each active {@link Relationship} for the specified node.
     * <p>
     * This will return the target of the relationships if this is the source, or the source of the relationships
     * if this is the target.
     * <br/>
     * If a reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node name
     * @param type the object type
     * @return a list of active related objects
     */
    public <T extends IMObject> List<T> getRelated(String name, Class<T> type) {
        return getRelated(name, type, active());
    }

    /**
     * Returns the related objects from each active {@link Relationship} for the specified node.
     * <p>
     * This will return the target of the relationships if this is the source, or the source of the relationships
     * if this is the target.
     * <br/>
     * If a reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return a list of active related objects
     */
    public <R extends Relationship> List<IMObject> getRelated(String name, Policy<R> policy) {
        return getRelated(name, IMObject.class, policy);
    }

    /**
     * Returns the related objects from each active {@link Relationship} for the specified node.
     * <p>
     * This will return the target of the relationships if this is the source, or the source of the relationships
     * if this is the target.
     * <br/>
     * If a reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node name
     * @param type   the object type
     * @param policy the policy for relationship selection and object retrieval
     * @return a list of active related objects
     */
    public <T extends IMObject, R extends Relationship> List<T> getRelated(String name, Class<T> type,
                                                                           Policy<R> policy) {
        List<T> result = new ArrayList<>();
        Reference ref = getReference();
        java.util.function.Predicate<R> predicate = policy.getPredicate();
        Comparator<R> comparator = policy.getComparator();
        List<R> relationships = (predicate != null) ? getValues(name, policy.getType(), predicate)
                                                    : getValues(name, policy.getType());
        if (comparator != null) {
            Collections.sort(relationships, comparator);
        }
        Policy.State state = policy.getState();
        for (Relationship relationship : relationships) {
            IMObject related = getSourceOrTarget(relationship, ref, state);
            if (related != null) {
                result.add(type.cast(related));
            }
        }
        return result;
    }

    /**
     * Saves the object, and those supplied, in a single transaction.
     *
     * @param objects the other objects to save
     */
    @Override
    public void save(org.openvpms.component.model.object.IMObject... objects) {
        List<IMObject> toSave = new ArrayList<>();
        toSave.add(getObject());
        for (org.openvpms.component.model.object.IMObject object : objects) {
            toSave.add((IMObject) object);
        }
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
    public <T extends org.openvpms.component.model.object.IMObject> List<T> getValues(String name, Class<T> type) {
        List<org.openvpms.component.model.object.IMObject> values = getValues(name);
        for (org.openvpms.component.model.object.IMObject value : values) {
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
    public List<org.openvpms.component.model.object.IMObject> getValues(String name, Predicate predicate) {
        java.util.function.Predicate<org.openvpms.component.model.object.IMObject> evaluate = predicate::evaluate;
        return getValues(name, evaluate);
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
    public <T extends org.openvpms.component.model.object.IMObject> List<T> getValues(String name, Predicate predicate,
                                                                                      Class<T> type) {
        return getValues(name, type, predicate::evaluate);
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
        for (org.openvpms.component.model.object.IMObject object : getValues(name)) {
            if (predicate.evaluate(object)) {
                return (IMObject) object;
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
     * Returns the source object from the first active {@link Relationship} with active source object, for the
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
     * Returns the source object from the first {@link Relationship} for the specified node.
     *
     * @param node   the relationship node name
     * @param active determines if the relationship and source object must be active
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeSourceObject(String node, boolean active) {
        return (active) ? getSource(node, active()) : getSource(node);
    }

    /**
     * Returns the source object from the first active {@link Relationship} with active source object, matching
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
     * Returns the source object from the first active {@link Relationship} matching the specified predicate.
     *
     * @param node      the relationship node name
     * @param predicate the predicate
     * @param active    determines if the object must be active or not
     * @return the source object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeSourceObject(String node, Predicate predicate, boolean active) {
        return getSource(node, Policies.match(active, predicate::evaluate));
    }

    /**
     * Returns the target object from the first active {@link Relationship} with active target object, for the
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
     * Returns the target object from the first {@link Relationship} for the specified node.
     *
     * @param node   the relationship node
     * @param active determines if the relationship and target object must be active
     * @return the target object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeTargetObject(String node, boolean active) {
        return (active) ? getTarget(node, active()) : getTarget(node);
    }

    /**
     * Returns the target object from the first active {@link Relationship} with active target object, for the
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
     * Returns the target object from the first active {@link Relationship} for the specified node.
     *
     * @param node      the relationship node name
     * @param predicate the predicate
     * @param active    determines if the object must be active or not
     * @return the target object, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getNodeTargetObject(String node, Predicate predicate, boolean active) {
        return getTarget(node, Policies.match(active, predicate::evaluate));
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
        return getSource(node, active(time));
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
        return getSource(node, active(time, active));
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
        return getTarget(node, active(time));
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
        return getTarget(node, active(time, active));
    }

    /**
     * Returns the active source objects from each active {@link Relationship} for the specified node.
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param node the relationship node
     * @return a list of active source objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    public List<IMObject> getNodeSourceObjects(String node) {
        List<org.openvpms.component.model.object.IMObject> sources = getSources(node, active());
        return (List<IMObject>) (List) sources;
    }

    /**
     * Returns the active source objects from each active {@link Relationship} for the specified node.
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param node the relationship node
     * @param type the object type
     * @return a list of active source objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeSourceObjects(String node, Class<T> type) {
        return getSources(node, type, active());
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
    @SuppressWarnings("unchecked")
    public List<IMObject> getNodeSourceObjects(String node, Date time) {
        List<org.openvpms.component.model.object.IMObject> sources = getSources(node, active(time));
        return (List<IMObject>) (List) sources;
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
        return getSources(node, type, active(time));
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
    @SuppressWarnings("unchecked")
    public List<IMObject> getNodeSourceObjects(String node, Date time, boolean active) {
        List<org.openvpms.component.model.object.IMObject> sources = getSources(node, active(time, active));
        return (List<IMObject>) (List) sources;
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
        return getSources(node, type, active(time, active));
    }

    /**
     * Returns the active source objects from each {@link Relationship} for the specified node that matches the
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
     * Returns the source objects from each {@link Relationship} for the specified node that matches the
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
     * Returns the source objects from each {@link Relationship} for the specified node that matches the
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
        return getSources(node, type, Policies.match(active, predicate::evaluate));
    }

    /**
     * Returns the active source objects from each {@link Relationship} for the specified node, keyed on their
     * relationship.
     *
     * @param node             the relationship node
     * @param type             the source object type
     * @param relationshipType the relationship object type
     * @return the source objects, keyed on their relationships
     */
    public <T extends IMObject, R extends Relationship> Map<R, T> getNodeSourceObjects(
            String node, Class<T> type, Class<R> relationshipType) {
        return getNodeSourceObjects(node, type, relationshipType, true);
    }

    /**
     * Returns the source objects from each {@link Relationship} for the specified node, keyed
     * on their relationship.
     *
     * @param node             the relationship node
     * @param type             the source object type
     * @param relationshipType the relationship object type
     * @param active           determines if the objects must be active
     * @return the source objects, keyed on their relationships
     */
    public <T extends IMObject, R extends Relationship> Map<R, T> getNodeSourceObjects(
            String node, Class<T> type, Class<R> relationshipType, boolean active) {
        List<R> relationships = getValues(node, relationshipType);
        Policy<R> policy = active ? active(relationshipType) : any(relationshipType);
        return getRelationshipObjects(relationships, type, policy, true);
    }

    /**
     * Returns the active target objects from each active {@link Relationship} for the specified node. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param node the relationship node
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    public List<IMObject> getNodeTargetObjects(String node) {
        List<org.openvpms.component.model.object.IMObject> targets = getTargets(node, active());
        return (List<IMObject>) (List) targets;
    }

    /**
     * Returns the active target objects from each active {@link Relationship} for the specified node.. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param node       the relationship node
     * @param comparator a comparator to sort relationships. May be {@code null}
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends Relationship> List<IMObject> getNodeTargetObjects(String node, Comparator<R> comparator) {
        return getNodeTargetObjects(node, IMObject.class, comparator);
    }

    /**
     * Returns the active target objects from each active {@link Relationship} for the specified node. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param node the relationship node
     * @param type the object type
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject> List<T> getNodeTargetObjects(String node, Class<T> type) {
        return getTargets(node, type, active());
    }

    /**
     * Returns the active target objects from each active {@link Relationship} for the specified node. If a
     * target reference cannot be resolved, it will be ignored.
     *
     * @param node       the relationship node
     * @param type       the object type
     * @param comparator if non-null, specifies a comparator to sort relationships
     * @return a list of active target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends IMObject, R extends Relationship> List<T> getNodeTargetObjects(String node, Class<T> type,
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
    @SuppressWarnings("unchecked")
    public List<IMObject> getNodeTargetObjects(String node, Date time) {
        List<org.openvpms.component.model.object.IMObject> targets = getTargets(node, active(time));
        return (List<IMObject>) (List) targets;
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
        return getTargets(node, type, active(time, active));
    }

    /**
     * Returns the active target objects from each {@link Relationship} for the specified node that matches the
     * specified predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @return a list of target objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    public List<IMObject> getNodeTargetObjects(String node, Predicate predicate) {
        List<org.openvpms.component.model.object.IMObject> targets = getTargets(node, active(predicate::evaluate));
        return (List<IMObject>) (List) targets;
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
        return getTargets(node, type, active(predicate::evaluate));
    }

    /**
     * Returns the target objects from each {@link Relationship} for the specified node that matches the
     * specified predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @param active    determines if the objects must be active
     * @return a list of target objects. May  contain inactive objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> getNodeTargetObjects(String node, Predicate predicate, boolean active) {
        return getNodeTargetObjects(node, predicate, active, IMObject.class);
    }

    /**
     * Returns the target objects from each {@link Relationship} for the specified node that matches the
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
        return getTargets(node, type, Policies.match(active, predicate::evaluate));
    }

    /**
     * Returns the target objects from each {@link Relationship} for the specified node that matches the
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
    @SuppressWarnings("unchecked")
    public <T extends IMObject, R extends Relationship> List<T> getNodeTargetObjects(
            String node, Predicate predicate, boolean active, Class<T> type, Comparator<R> comparator) {
        Policy policy = Policies.match(active, predicate::evaluate, (Comparator) comparator);
        return getRelatedObjects(node, type, policy, false);
    }

    /**
     * Returns the active target objects from each {@link Relationship} for the specified node, keyed on their
     * relationship.
     *
     * @param node             the relationship node
     * @param type             the target object type
     * @param relationshipType the relationship object type
     * @return the target objects, keyed on their relationships
     */
    public <T extends IMObject, R extends Relationship> Map<R, T> getNodeTargetObjects(
            String node, Class<T> type, Class<R> relationshipType) {
        return getNodeTargetObjects(node, type, relationshipType, true);
    }

    /**
     * Returns the active target objects from each {@link Relationship} for the specified node, keyed
     * on their relationship.
     *
     * @param node             the relationship node
     * @param type             the target object type
     * @param relationshipType the relationship object type
     * @param active           determines if the objects must be active
     * @return the target objects, keyed on their relationships
     */
    public <T extends IMObject, R extends Relationship> Map<R, T> getNodeTargetObjects(
            String node, Class<T> type, Class<R> relationshipType, boolean active) {
        List<R> relationships = getValues(node, relationshipType);
        Policy<R> policy = active ? active(relationshipType) : any(relationshipType);
        return getRelationshipObjects(relationships, type, policy, false);
    }

    /**
     * Returns the source object reference from the first active {@link Relationship} for the specified
     * relationship node.
     *
     * @param node the relationship node name
     * @return the source object reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getNodeSourceObjectRef(String node) {
        return getNodeSourceObjectRef(node, true);
    }

    /**
     * Returns the source object reference from the first {@link Relationship} for the specified
     * relationship node.
     *
     * @param node   the relationship node name
     * @param active determines if the relationship must be active
     * @return the source object reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getNodeSourceObjectRef(String node, boolean active) {
        return (IMObjectReference) getSourceRef(node, active ? active() : any());
    }

    /**
     * Returns the source object references from each active {@link Relationship} for the specified node.
     *
     * @param node the relationship node
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    @SuppressWarnings("unchecked")
    public List<IMObjectReference> getNodeSourceObjectRefs(String node) {
        List<Reference> result = getSourceRefs(node, active());
        return (List<IMObjectReference>) (List) result;
    }

    /**
     * Returns the source object references from each {@link PeriodRelationship} that is active at the specified time,
     * for the specified node.
     *
     * @param node the relationship node
     * @param time the time
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    @SuppressWarnings("unchecked")
    public List<IMObjectReference> getNodeSourceObjectRefs(String node, Date time) {
        List<Reference> result = getSourceRefs(node, active(time));
        return (List<IMObjectReference>) (List) result;
    }

    /**
     * Returns the source object references from each {@link PeriodRelationship} for the specified node that matches the
     * supplied predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    @SuppressWarnings("unchecked")
    public List<IMObjectReference> getNodeSourceObjectRefs(String node, Predicate predicate) {
        List<Reference> result = getSourceRefs(node, any(predicate::evaluate));
        return (List<IMObjectReference>) (List) result;
    }

    /**
     * Returns the target object references from each active {@link PeriodRelationship} for the specified node.
     *
     * @param node the relationship node
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    @SuppressWarnings("unchecked")
    public List<IMObjectReference> getNodeTargetObjectRefs(String node) {
        List<Reference> result = getTargetRefs(node, active());
        return (List<IMObjectReference>) (List) result;
    }

    /**
     * Returns the target object reference from the first active {@link Relationship} for the specified node.
     *
     * @param node the relationship node
     * @return the target reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getNodeTargetObjectRef(String node) {
        return getTargetRef(node, active());
    }

    /**
     * Returns the target object reference from the first {@link Relationship} for the specified node.
     *
     * @param node   the relationship node
     * @param active determines if the relationship must be active
     * @return the target reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getNodeTargetObjectRef(String node, boolean active) {
        return getTargetRef(node, active ? active() : any());
    }

    /**
     * Returns the target object references from each {@link PeriodRelationship} that is active at the specified time,
     * for the specified node.
     *
     * @param node the relationship node
     * @param time the time
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    @SuppressWarnings("unchecked")
    public List<IMObjectReference> getNodeTargetObjectRefs(String node, Date time) {
        List<Reference> result = getTargetRefs(node, active(time));
        return (List<IMObjectReference>) (List) result;
    }

    /**
     * Returns the target object references from each {@link Relationship} for the specified node that matches
     * the supplied predicate.
     *
     * @param node      the relationship node
     * @param predicate the predicate
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    @SuppressWarnings("unchecked")
    public List<IMObjectReference> getNodeTargetObjectRefs(String node, Predicate predicate) {
        List<Reference> result = getTargetRefs(node, any(predicate::evaluate));
        return (List<IMObjectReference>) (List) result;
    }

    /**
     * Returns the active source objects from each relationship that matches the specified short name.
     *
     * @param relationships the relationships
     * @param shortName     the short name
     * @param type          the expected object type
     * @return a list of source objects that match the given criteria
     */
    public <T extends IMObject, R extends Relationship> List<T> getSourceObjects(Collection<R> relationships,
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
    public <T extends IMObject, R extends Relationship> List<T> getSourceObjects(Collection<R> relationships,
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
    @SuppressWarnings("unchecked")
    public <T extends IMObject, R extends Relationship> List<T> getSourceObjects(Collection<R> relationships,
                                                                                 String[] shortNames,
                                                                                 boolean active,
                                                                                 Class<T> type) {
        Policy<Relationship> match = Policies.match(active, getActiveIsA(active, shortNames));
        List<R> list = (relationships instanceof List) ? (List<R>) relationships : new ArrayList<>(relationships);
        return getRelatedObjects((List) list, type, match, true);
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
    public <T extends IMObject, R extends Relationship> List<T> getTargetObjects(Collection<R> relationships,
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
    public <T extends IMObject, R extends Relationship> List<T> getTargetObjects(Collection<R> relationships,
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
    @SuppressWarnings("unchecked")
    public <T extends IMObject, R extends Relationship> List<T> getTargetObjects(Collection<R> relationships,
                                                                                 String[] shortNames,
                                                                                 boolean active,
                                                                                 Class<T> type) {
        Policy<Relationship> policy = Policies.match(active, getActiveIsA(active, shortNames));
        List<R> list = (relationships instanceof List) ? (List<R>) relationships : new ArrayList<>(relationships);
        return getRelatedObjects((List) list, type, policy, false);
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
    public <R extends Relationship> IMObject getSourceObject(Collection<R> relationships, String shortName) {
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
    public <R extends Relationship> IMObject getSourceObject(Collection<R> relationships, String shortName,
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
    public <R extends Relationship> IMObject getSourceObject(Collection<R> relationships, String[] shortNames) {
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
    @SuppressWarnings("unchecked")
    public <R extends Relationship> IMObject getSourceObject(Collection<R> relationships, String[] shortNames,
                                                             boolean active) {
        Policy<Relationship> policy = Policies.match(active, getActiveIsA(active, shortNames));
        List<R> list = (relationships instanceof List) ? (List<R>) relationships : new ArrayList<>(relationships);
        return getRelatedObject((List) list, policy, true);
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
    public <R extends Relationship> IMObject getTargetObject(Collection<R> relationships, String shortName) {
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
    public <R extends Relationship> IMObject getTargetObject(Collection<R> relationships, String shortName,
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
    public <R extends Relationship> IMObject getTargetObject(Collection<R> relationships, String[] shortNames) {
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
    @SuppressWarnings("unchecked")
    public <R extends Relationship> IMObject getTargetObject(Collection<R> relationships, String[] shortNames,
                                                             boolean active) {
        Policy<Relationship> policy = Policies.match(active, getActiveIsA(active, shortNames));
        List<R> list = (relationships instanceof List) ? (List<R>) relationships : new ArrayList<>(relationships);
        return getRelatedObject((List) list, policy, false);
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
    @SuppressWarnings("unchecked")
    public <R extends PeriodRelationship> IMObject getSourceObject(Collection<R> relationships, String[] shortNames,
                                                                   Date time, boolean active) {
        Policy<Relationship> policy = Policies.match(active, getIsActiveRelationship(time, shortNames));
        List<R> list = (relationships instanceof List) ? (List<R>) relationships : new ArrayList<>(relationships);
        return getRelatedObject((List) list, policy, true);
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
    @SuppressWarnings("unchecked")
    public <R extends PeriodRelationship> IMObject getTargetObject(Collection<R> relationships, String[] shortNames,
                                                                   Date time, boolean active) {
        Policy<Relationship> policy = Policies.match(active, getIsActiveRelationship(time, shortNames));
        List<R> list = (relationships instanceof List) ? (List<R>) relationships : new ArrayList<>(relationships);
        return getRelatedObject((List) list, policy, false);
    }

    /**
     * Returns the source object reference from the first active object relationship matching the specified short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @return the source reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends Relationship> IMObjectReference getSourceObjectRef(Collection<R> relationships,
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
    public <R extends Relationship> IMObjectReference getSourceObjectRef(Collection<R> relationships,
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
    public <R extends Relationship> IMObjectReference getSourceObjectRef(Collection<R> relationships,
                                                                         String[] shortNames, boolean active) {
        return getObjectRef(relationships, shortNames, active, true);
    }

    /**
     * Returns the target object reference from the first active object relationship matching the specified short name.
     *
     * @param relationships the relationships
     * @param shortName     the relationship short name
     * @return the target reference, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <R extends Relationship> IMObjectReference getTargetObjectRef(Collection<R> relationships,
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
    public <R extends Relationship> IMObjectReference getTargetObjectRef(Collection<R> relationships,
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
    public <R extends Relationship> IMObjectReference getTargetObjectRef(Collection<R> relationships,
                                                                         String[] shortNames, boolean active) {
        return getObjectRef(relationships, shortNames, active, false);
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
     * Determines if there is an active {@link Relationship} with {@code object} as its source, for the node
     * {@code node}.
     *
     * @param node   the relationship node
     * @param object the target object
     * @return {@code true} if there is an active relationship to {@code object}
     */
    public boolean hasNodeSource(String node, IMObject object) {
        return getNodeSourceObjectRefs(node).contains(object.getObjectReference());
    }

    /**
     * Sets the value of a node.
     *
     * @param name  the node name
     * @param value the new node value
     */
    @Override
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
    @Override
    public void addValue(String name, org.openvpms.component.model.object.IMObject value) {
        NodeDescriptor node = toNode(name);
        node.addChildToCollection(properties.getObject(), value);
    }

    /**
     * Removes a value from a collection.
     *
     * @param name  the node name
     * @param value the value to remove
     */
    @Override
    public void removeValue(String name, org.openvpms.component.model.object.IMObject value) {
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
    public IMObjectRelationship addNodeTarget(String name, Reference target) {
        return (IMObjectRelationship) addTarget(name, target);
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
        return (IMObjectRelationship) addTarget(name, target);
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
        return (IMObjectRelationship) addTarget(name, shortName, target);
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
    public IMObjectRelationship addNodeTarget(String name, String shortName, Reference target) {
        return (IMObjectRelationship) addTarget(name, shortName, target);
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
    public IMObjectBean getBean(org.openvpms.component.model.object.IMObject object) {
        return new IMObjectBean(object, service, lookups);
    }

    /**
     * Resolves a reference, verifying the object is of the expected type.
     *
     * @param ref   the reference to resolve
     * @param type  the expected object type
     * @param state the expected object state
     * @return the resolved object, or {@code null} if it cannot be found or doesn't match the active criteria
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if an object isn't of the expected type
     */
    @SuppressWarnings("unchecked")
    protected <T extends org.openvpms.component.model.object.IMObject> T resolve(
            Reference ref, Class<T> type, Policy.State state) {
        IMObject object = resolve(ref, state);
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
     * @param ref   the reference. May be {@code null}
     * @param state the expected object state
     * @return the object corresponding to the reference or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObject resolve(Reference ref, Policy.State state) {
        IMObject result = null;
        if (ref != null) {
            IArchetypeService service = getArchetypeService();
            if (state == Policy.State.ANY) {
                result = service.get(ref);
            } else {
                boolean active = state == Policy.State.ACTIVE;
                result = service.get(ref, active);
            }
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
     * @param node   the relationship node name
     * @param type   the expected object type
     * @param policy the policy
     * @param source if {@code true}, return the source of the relationship, otherwise return the target
     * @return a list of objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    protected <T extends org.openvpms.component.model.object.IMObject, R extends Relationship> List<T> getRelatedObjects(
            String node, Class<T> type, Policy<R> policy, boolean source) {
        List<Reference> refs = getRelatedRefs(node, policy, source);
        return resolve(refs, type, policy.getState());
    }

    /**
     * Returns all objects for the specified relationships that match the specified criteria.
     *
     * @param relationships the relationships to search
     * @param type          the expected object type
     * @param policy        the policy
     * @param source        if {@code true}, return the source of the relationship, otherwise return the target
     * @return a list of objects
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if an object isn't of the expected type
     */
    protected <R extends Relationship, T extends IMObject> Map<R, T> getRelationshipObjects(
            Collection<R> relationships, Class<T> type, Policy<R> policy, boolean source) {
        Map<R, T> result;
        Map<R, Reference> refs = getRelationshipRefs(relationships, policy, source);
        if (refs.isEmpty()) {
            result = Collections.emptyMap();
        } else {
            result = new HashMap<>();
            Policy.State state = policy.getState();
            for (Map.Entry<R, Reference> entry : refs.entrySet()) {
                T object = resolve(entry.getValue(), type, state);
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
     * @param policy        the policy
     * @param source        if {@code true}, return the source of the relationship, otherwise return the target
     * @return the matching references
     */
    protected <R extends Relationship> Map<R, Reference> getRelationshipRefs(
            Collection<R> relationships, Policy<R> policy, boolean source) {
        Map<R, Reference> result = new HashMap<>();
        Function<Relationship, Reference> accessor = getAccessor(source);
        java.util.function.Predicate<R> predicate = policy.getPredicate();
        for (R relationship : relationships) {
            if (predicate == null || predicate.test(relationship)) {
                Reference ref = accessor.apply(relationship);
                if (ref != null) {
                    result.put(relationship, ref);
                }
            }
        }
        return result;
    }

    /**
     * Returns the object from the first relationship for the specified node that matches the specified criteria.
     *
     * @param node   the relationship node
     * @param policy the criteria
     * @param source if {@code true}, return the source of the relationship, otherwise return the target
     * @return the first object, or {@code null} if none is found
     */
    protected <R extends Relationship> IMObject getRelatedObject(String node, Policy<R> policy, boolean source) {
        List<org.openvpms.component.model.object.IMObject> relationships = getValues(node);
        return getRelatedObject(relationships, policy, source);
    }

    /**
     * Returns the source or target from the first relationship matching the specified criteria.
     * <p>
     * If active is {@code true} the object must be active in order to be returned.
     * <p>
     * If active is {@code false}, then an active object will be returned in preference to an inactive one.
     *
     * @param relationships the relationships
     * @param policy        the policy
     * @param source        if {@code true}, return the source of the relationship, otherwise return the target
     * @return the first object matching the criteria or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected <R extends Relationship> IMObject getRelatedObject(
            List<org.openvpms.component.model.object.IMObject> relationships, Policy<R> policy, boolean source) {
        IMObject result = null;
        Function<Relationship, Reference> accessor = getAccessor(source);
        Class<R> type = policy.getType();
        java.util.function.Predicate<R> predicate = policy.getPredicate();
        Policy.State state = policy.getState();

        for (org.openvpms.component.model.object.IMObject r : relationships) {
            R relationship = type.cast(r);
            if (predicate == null || predicate.test(relationship)) {
                Reference ref = accessor.apply(relationship);
                IMObject object = resolve(ref, state);
                if (object != null) {
                    if (object.isActive() || state == Policy.State.INACTIVE) {
                        // found a match, so return it
                        result = object;
                        break;
                    } else if (result == null) {
                        // can return inactive, but keep looking for an active  match
                        result = object;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the first object reference from the supplied relationship node that matches the specified criteria.
     *
     * @param name   the node name
     * @param policy the policy
     * @param source if {@code true}, return the source of the relationship, otherwise return the target
     * @return the matching reference, or {@code null}
     */
    protected <R extends Relationship> IMObjectReference getRelatedRef(
            String name, Policy<R> policy, boolean source) {
        Function<Relationship, Reference> accessor = getAccessor(source);
        java.util.function.Predicate<R> predicate = policy.getPredicate();
        Class<R> type = policy.getType();
        for (org.openvpms.component.model.object.IMObject object : getValues(name)) {
            R relationship = type.cast(object);
            if (predicate == null || predicate.test(relationship)) {
                Reference reference = accessor.apply(relationship);
                if (reference != null) {
                    return (IMObjectReference) reference;
                }
            }
        }
        return null;
    }

    /**
     * Returns all related references for the specified node that match the specified criteria.
     *
     * @param node   the relationship node name
     * @param policy the policy
     * @param source if {@code true}, return the source of the relationship, otherwise return the target
     */
    protected <R extends Relationship> List<Reference> getRelatedRefs(
            String node, Policy<R> policy, boolean source) {
        List<R> relationships = getValues(node, policy.getType());
        Comparator<R> comparator = policy.getComparator();
        if (comparator != null) {
            Collections.sort(relationships, comparator);
        }
        return getRelatedRefs(relationships, policy, source);
    }

    /**
     * Returns all object references from the supplied relationships that match the specified criteria.
     *
     * @param relationships the relationships
     * @param policy        the policy
     * @param source        if {@code true}, return the source of the relationship, otherwise return the target
     * @return the matching references
     */
    protected <R extends Relationship> List<Reference> getRelatedRefs(
            Collection<R> relationships, Policy<R> policy, boolean source) {
        Function<Relationship, Reference> accessor = getAccessor(source);
        List<Reference> result = new ArrayList<>();
        Stream<R> stream = relationships.stream();
        java.util.function.Predicate<R> predicate = policy.getPredicate();
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        stream.forEach(r -> {
            Reference reference = accessor.apply(r);
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
     * @param type          the expected object type
     * @param policy        the policy
     * @param source        if {@code true}, return the source of the relationship, otherwise return the target
     * @return a list of objects
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if an object isn't of the expected type
     */
    protected <R extends Relationship, T extends IMObject> List<T> getRelatedObjects(
            Collection<R> relationships, Class<T> type, Policy<R> policy, boolean source) {
        List<Reference> refs = getRelatedRefs(relationships, policy, source);
        return resolve(refs, type, policy.getState());
    }

    /**
     * Resolves references.
     * <p>
     * If an object cannot be resolved, or doesn't match the state criteria, it is ignored.
     *
     * @param refs  the references to resolve
     * @param type  the expected object type
     * @param state the expected object state
     * @return a list of objects
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if an object isn't of the expected type
     */
    protected <T extends org.openvpms.component.model.object.IMObject> List<T> resolve(
            List<Reference> refs, Class<T> type, Policy.State state) {
        List<T> result;
        if (refs.isEmpty()) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<>();
            for (Reference ref : refs) {
                T object = resolve(ref, type, state);
                if (object != null) {
                    result.add(object);
                }
            }
        }
        return result;
    }

    /**
     * Returns the source or target of a relationship that is not the same as the supplied reference.
     *
     * @param relationship the relationship
     * @param ref          the reference
     * @param state        the expected object state
     * @return the source or target, or {@code null}
     */
    protected IMObject getSourceOrTarget(Relationship relationship, Reference ref, Policy.State state) {
        Reference target = relationship.getTarget();
        Reference related = null;
        if (target != null && !target.equals(ref)) {
            related = target;
        } else {
            Reference source = relationship.getSource();
            if (source != null && !source.equals(ref)) {
                related = source;
            }
        }
        return (related != null) ? resolve(related, state) : null;
    }

    /**
     * Returns a relationship short name for a specific node that the target may be added to.
     *
     * @param name   the node name
     * @param target the target of the relationship
     * @return the relationship short name
     * @throws IMObjectBeanException if {@code name} is an invalid node, there is no relationship that supports
     *                               {@code target}, or multiple relationships can support {@code target}
     */
    protected String getRelationshipArchetype(String name, IMObject target) {
        return getRelationshipArchetype(name, target.getObjectReference());
    }

    /**
     * Returns a relationship short name for a specific node that the target may be added to.
     *
     * @param name   the node name
     * @param target the target of the relationship
     * @return the relationship short name
     * @throws IMObjectBeanException if {@code name} is an invalid node, there is no relationship that supports
     *                               {@code target}, or multiple relationships can support {@code target}
     */
    protected String getRelationshipArchetype(String name, Reference target) {
        String[] range = getArchetypeRange(name);
        IArchetypeService service = getArchetypeService();
        String result = null;
        String archetypeId = target.getArchetype();
        for (String shortName : range) {
            // NOTE: for historical reasons, participations use "entity" for the target
            String targetName = (TypeHelper.matches(shortName, "participation.*")) ? "entity" : "target";
            ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(shortName);
            if (descriptor != null) {
                NodeDescriptor node = descriptor.getNodeDescriptor(targetName);
                if (node != null && isA(node, archetypeId)) {
                    if (result != null) {
                        throw new IMObjectBeanException(IMObjectBeanException.ErrorCode.MultipleRelationshipsForTarget,
                                                        archetypeId, name);
                    }
                    result = shortName;
                }
            }
        }
        if (result == null) {
            throw new IMObjectBeanException(IMObjectBeanException.ErrorCode.CannotAddTargetToNode, archetypeId, name);
        }
        return result;
    }

    /**
     * Returns the first object reference from the supplied relationship that matches the specified criteria.
     *
     * @param relationships the relationships
     * @param predicate     the criteria. May be {@code null}
     * @param source        if {@code true}, return the source of the relationship, otherwise return the target
     * @return the matching reference, or {@code null}
     */
    protected <R extends Relationship> IMObjectReference getRelatedRef(
            Collection<R> relationships, java.util.function.Predicate<R> predicate, boolean source) {
        Function<Relationship, Reference> accessor = getAccessor(source);
        for (R relationship : relationships) {
            if (predicate == null || predicate.test(relationship)) {
                IMObjectReference reference = (IMObjectReference) accessor.apply(relationship);
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
     * @param source        if {@code true}, return the source of the relationship, otherwise return the target
     * @return the first matching reference,or {@code null}
     */
    @SuppressWarnings("unchecked")
    protected <R extends Relationship> IMObjectReference getObjectRef(Collection<R> relationships,
                                                                      String[] shortNames, boolean active,
                                                                      boolean source) {
        java.util.function.Predicate<Relationship> predicate = getActiveIsA(shortNames);
        IMObjectReference ref = getRelatedRef((Collection) relationships, predicate, source);
        if (ref == null && !active) {
            ref = getRelatedRef(relationships, Predicates.isA(shortNames), source);
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
    protected <T> List<T> select(Collection<T> objects, java.util.function.Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T object : objects) {
            if (predicate.test(object)) {
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
    protected java.util.function.Predicate<Relationship> getIsActiveRelationship(
            Date time, String... shortNames) {
        return Predicates.activeAt(time).and(Predicates.isA(shortNames));
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
    protected <R extends Relationship> java.util.function.Predicate<R> getActiveIsA(boolean active,
                                                                                    String... shortNames) {
        java.util.function.Predicate<R> isA = Predicates.isA(shortNames);
        return (active) ? Predicates.<R>activeNow().and(isA) : isA;
    }

    /**
     * Helper to return a predicate that checks that a relationship is
     * active now, and is one of a set of archetypes.
     *
     * @param shortNames the relationship short names to match
     * @return a new predicate
     */
    protected java.util.function.Predicate<Relationship> getActiveIsA(String... shortNames) {
        return Predicates.activeNow().and(Predicates.isA(shortNames));
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

    private Object getValue(NodeDescriptor node) {
        return node.getValue(getObject());
    }

    private List<org.openvpms.component.business.domain.im.common.IMObject> getValues(NodeDescriptor node) {
        return node.getChildren(getObject());
    }

    private <R extends Relationship> Function<R, Reference> getAccessor(boolean source) {
        return (source) ? R::getSource : R::getTarget;
    }

    /**
     * Determines if a node is a particular archetype.
     *
     * @param node      the node
     * @param archetype the archetype
     * @return {@code true} if the node is the archetype, otherwise {@code false}
     */
    private boolean isA(NodeDescriptor node, String archetype) {
        String[] shortNames = node.getArchetypeRange();
        return (shortNames.length != 0) ? TypeHelper.isA(archetype, shortNames)
                                        : TypeHelper.isA(archetype, node.getFilter());
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
        public NodePropertySet(org.openvpms.component.business.domain.im.common.IMObject object) {
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
