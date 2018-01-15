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

package org.openvpms.component.model.bean;

import org.openvpms.component.model.archetype.ArchetypeDescriptor;
import org.openvpms.component.model.archetype.NodeDescriptor;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.object.Relationship;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides access to {@link IMObject}s using their {@link ArchetypeDescriptor}.
 *
 * @author Tim Anderson
 */
public interface IMObjectBean {

    /**
     * Returns the underlying object.
     *
     * @return the object
     */
    IMObject getObject();

    /**
     * Returns a reference to the underlying object.
     *
     * @return the reference
     */
    Reference getReference();

    /**
     * Determines if the object is one of a set of archetypes.
     *
     * @param archetypes the archetype short names. May contain wildcards
     * @return {@code true} if the object is one of {@code archetypes}
     */
    boolean isA(String... archetypes);

    /**
     * Determines if a node exists.
     *
     * @param name the node name
     * @return {@code true} if the node exists, otherwise {@code false}
     */
    boolean hasNode(String name);

    /**
     * Returns the archetype display name.
     *
     * @return the archetype display name, or its short name if none is present.
     */
    String getDisplayName();

    /**
     * Returns the archetype descriptor.
     *
     * @return the archetype descriptor
     */
    ArchetypeDescriptor getArchetype();

    /**
     * Returns the named node's descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to {@code name} or {@code null} if none exists.
     */
    NodeDescriptor getNode(String name);

    /**
     * Returns the display name of a node.
     *
     * @param name the node name
     * @return the node display name
     */
    String getDisplayName(String name);

    /**
     * Returns the maximum length of a node.
     *
     * @param name the node name
     * @return the maximum length
     */
    int getMaxLength(String name);

    /**
     * Returns the archetype range associated with a node, expanding any wildcards.
     *
     * @param name the node name
     * @return the archetype range associated with a node, or an empty array if there is none
     */
    String[] getArchetypeRange(String name);

    /**
     * Returns the boolean value of a node.
     *
     * @param name the node name
     * @return the value of the node, or {@code false} if the node is null
     */
    boolean getBoolean(String name);

    /**
     * Returns the boolean value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it is null
     */
    boolean getBoolean(String name, boolean defaultValue);

    /**
     * Returns the integer value of a node.
     *
     * @param name the node name
     * @return the value of the node, or {@code 0} if the node is null
     */
    int getInt(String name);

    /**
     * Returns the integer value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it is null
     */
    int getInt(String name, int defaultValue);

    /**
     * Returns the long value of a node.
     *
     * @param name the node name
     * @return the value of the node, or {@code 0} if the node is null
     */
    long getLong(String name);

    /**
     * Returns the long value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it is null
     */
    long getLong(String name, long defaultValue);

    /**
     * Returns the string value of a node.
     *
     * @param name the node name
     * @return the value of the node.
     */
    String getString(String name);

    /**
     * Returns the string value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it is null
     */
    String getString(String name, String defaultValue);

    /**
     * Returns the {@code BigDecimal} value of a node.
     *
     * @param name the node name
     * @return the value of the node. May be {@code null}
     */
    BigDecimal getBigDecimal(String name);

    /**
     * Returns the {@code BigDecimal} value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it is null
     */
    BigDecimal getBigDecimal(String name, BigDecimal defaultValue);

    /**
     * Returns the {@code Date} value of a node.
     *
     * @param name the node name
     * @return the value of the node. May be {@code null}
     */
    Date getDate(String name);

    /**
     * Returns the {@code Date} value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or {@code defaultValue} if it is null
     */
    Date getDate(String name, Date defaultValue);

    /**
     * Returns the reference value of a node.
     *
     * @param name the node name
     * @return the value of the node. May be {@code null}
     */
    Reference getReference(String name);

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
    IMObject getObject(String name);

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
    <T extends IMObject> T getObject(String name, Class<T> type);

    /**
     * Returns the value of a node.
     *
     * @param name the node name
     * @return the value of the node. May be {@code null}
     */
    Object getValue(String name);

    /**
     * Returns a lookup based on the value of a node.
     *
     * @param name the node name
     * @return the value. May be {@code null}, or be inactive
     */
    Lookup getLookup(String name);

    /**
     * Returns the values of a collection node.
     *
     * @param name the node name
     * @return the collection corresponding to the node
     */
    List<IMObject> getValues(String name);

    /**
     * Returns the values of a collection node, cast to the supplied type.
     *
     * @param name the node name
     * @param type the expected object type
     * @return the collection corresponding to the node
     */
    <T extends IMObject> List<T> getValues(String name, Class<T> type);

    /**
     * Returns the values of a collection node that match the supplied predicate.
     *
     * @param name      the node name
     * @param predicate the predicate
     * @return the objects matching the predicate
     */
    List<IMObject> getValues(String name, Predicate<IMObject> predicate);

    /**
     * Returns the values of a collection node that match the supplied predicate.
     *
     * @param name      the node name
     * @param type      the expected object type
     * @param predicate the predicate
     * @return the objects matching the predicate
     */
    <T extends IMObject> List<T> getValues(String name, Class<T> type, Predicate<T> predicate);

    /**
     * Returns the source object from the first {@link Relationship} with source object, for the specified relationship
     * node.
     * <br/>
     * If there are multiple relationships, the first active object will be returned in preference to an inactive one.
     *
     * @param name the relationship node name
     * @return the source object, or {@code null} if none is found
     */
    IMObject getSource(String name);

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
    <T extends IMObject> T getSource(String name, Class<T> type);

    /**
     * Returns the source object from the first {@link Relationship} for the specified node matching the policy.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return the source object, or {@code null} if none is found
     */
    <R extends Relationship> IMObject getSource(String name, Policy<R> policy);

    /**
     * Returns the source object from the first {@link Relationship} for the specified node matching the policy.
     *
     * @param name   the relationship node name
     * @param type   the object type
     * @param policy the policy for relationship selection and object retrieval
     * @return the source object, or {@code null} if none is found
     */
    <T extends IMObject, R extends Relationship> T getSource(String name, Class<T> type, Policy<R> policy);

    /**
     * Returns the target object from the first {@link Relationship} with target object, for the specified relationship
     * node.
     * <br/>
     * If there are multiple relationships, the first active object will be returned in preference to an inactive one.
     *
     * @param name the relationship node name
     * @return the target object, or {@code null} if none is found
     */
    IMObject getTarget(String name);

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
    <T extends IMObject> T getTarget(String name, Class<T> type);

    /**
     * Returns the target object from the first {@link Relationship} for the specified node matching the policy.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return the target object, or {@code null} if none is found
     */
    <R extends Relationship> IMObject getTarget(String name, Policy<R> policy);

    /**
     * Returns the target object from the first {@link Relationship} for the specified node matching the policy.
     *
     * @param name   the relationship node name
     * @param type   the object type
     * @param policy the policy for relationship selection and object retrieval
     * @return the target object, or {@code null} if none is found
     */
    <T extends IMObject, R extends Relationship> T getTarget(String name, Class<T> type, Policy<R> policy);

    /**
     * Returns the source objects from each {@link Relationship} for the specified node.
     * <br/>
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node name
     * @return a list of source objects. May be both active and inactive
     */
    List<IMObject> getSources(String name);

    /**
     * Returns the source objects from each {@link Relationship} for the specified node.
     * <br/>
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node name
     * @param type the object type
     * @return a list of source objects. May be both active and inactive
     */
    <T extends IMObject> List<T> getSources(String name, Class<T> type);

    /**
     * Returns the source objects from each {@link Relationship} matching the policy, for the specified node.
     * <br/>
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return a list of source objects matching the policy
     */
    <R extends Relationship> List<IMObject> getSources(String name, Policy<R> policy);

    /**
     * Returns the source objects from each {@link Relationship} matching the policy, for the specified node.
     * <br/>
     * If a source reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node name
     * @param type   the object type
     * @param policy the policy for relationship selection and object retrieval
     * @return a list of source objects matching the policy
     */
    <T extends IMObject, R extends Relationship> List<T> getSources(String name, Class<T> type, Policy<R> policy);

    /**
     * Returns the target objects from each {@link Relationship} for the specified node.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node name
     * @return a list of target objects. May be both active and inactive
     */
    List<IMObject> getTargets(String name);

    /**
     * Returns the target objects from each {@link Relationship} for the specified node.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name the relationship node name
     * @param type the object type
     * @return a list of target objects. May be both active and inactive
     */
    <T extends IMObject> List<T> getTargets(String name, Class<T> type);

    /**
     * Returns the target objects from each {@link Relationship} matching the policy, for the specified node.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return a list of target objects matching the policy
     */
    <R extends Relationship> List<IMObject> getTargets(String name, Policy<R> policy);

    /**
     * Returns the target objects from each {@link Relationship} matching the policy, for the specified node.
     * <br/>
     * If a target reference cannot be resolved, it will be ignored.
     *
     * @param name   the relationship node name
     * @param type   the object type
     * @param policy the policy for relationship selection and object retrieval
     * @return a list of target objects matching the policy
     */
    <T extends IMObject, R extends Relationship> List<T> getTargets(String name, Class<T> type, Policy<R> policy);

    /**
     * Returns the source object reference from the first {@link Relationship} for the specified relationship node.
     *
     * @param name the relationship node name
     * @return the source object reference, or {@code null} if none is found
     */
    Reference getSourceRef(String name);

    /**
     * Returns the source object reference from the first {@link Relationship} for the specified node matching
     * the policy.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return the source object reference, or {@code null} if none is found
     */
    <R extends Relationship> Reference getSourceRef(String name, Policy<R> policy);

    /**
     * Returns the source object references from each {@link Relationship} for the specified node.
     *
     * @param name the relationship node
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    List<Reference> getSourceRefs(String name);

    /**
     * Returns the source object references from each for the specified node that matches the supplied policy.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return a list of source object references. May contain references to both active and inactive objects
     */
    <R extends Relationship> List<Reference> getSourceRefs(String name, Policy<R> policy);

    /**
     * Returns the target object reference from the first {@link Relationship} for the specified relationship node.
     *
     * @param name the relationship node name
     * @return the target object reference, or {@code null} if none is found
     */
    Reference getTargetRef(String name);

    /**
     * Returns the target object reference from the first {@link Relationship} for the specified
     * relationship node.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return the target object reference, or {@code null} if none is found
     */
    <R extends Relationship> Reference getTargetRef(String name, Policy<R> policy);

    /**
     * Returns the target object references from each {@link Relationship} for the specified node.
     *
     * @param name the relationship node name
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    List<Reference> getTargetRefs(String name);

    /**
     * Returns the target object references from each for the specified node that matches the supplied policy.
     *
     * @param name   the relationship node name
     * @param policy the policy for relationship selection and object retrieval
     * @return a list of target object references. May contain references to both active and inactive objects
     */
    <R extends Relationship> List<Reference> getTargetRefs(String name, Policy<R> policy);

    /**
     * Sets the value of a node.
     *
     * @param name  the node name
     * @param value the new node value
     */
    void setValue(String name, Object value);

    /**
     * Adds a value to a collection.
     *
     * @param name  the node name
     * @param value the value to add
     */
    void addValue(String name, IMObject value);

    /**
     * Removes a value from a collection.
     *
     * @param name  the node name
     * @param value the value to remove
     */
    void removeValue(String name, IMObject value);

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
    Relationship setTarget(String name, Reference target);

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
    Relationship setTarget(String name, IMObject target);

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
    Relationship addTarget(String name, Reference target);

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
    Relationship addTarget(String name, IMObject target);

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
    Relationship addTarget(String name, String archetype, IMObject target);

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
    Relationship addTarget(String name, String archetype, Reference target);

    /**
     * Adds a bidirectional relationship between the current object (the source) and the supplied target.
     *
     * @param sourceName the source node name
     * @param target     the target
     * @param targetName the target node name
     * @return a new relationship
     */
    Relationship addTarget(String sourceName, IMObject target, String targetName);

    /**
     * Removes all bidirectional relationships between the current object (the source), and the supplied target.
     *
     * @param sourceName the source node name
     * @param target     the target
     * @param targetName the target node name
     */
    void removeTargets(String sourceName, IMObject target, String targetName);

    /**
     * Evaluates the default value if a node, if it has one.
     *
     * @param name the node name
     * @return the evaluation of {@link NodeDescriptor#getDefaultValue()} (which may evaluate {@code null}),
     * or {@code null} if the node doesn't have a default value
     */
    Object getDefaultValue(String name);

    /**
     * Determines if a node is unchanged from its default value.
     *
     * @param name the node name
     * @return {@code true} if the node is unchanged from its default value
     */
    boolean isDefaultValue(String name);

    /**
     * Derived values for the object.
     * <p>
     * For each node with {@code derived=true}, the node will be evaluated and the corresponding value set.
     */
    void deriveValues();

    /**
     * Saves the object.
     * <p>
     * Any derived nodes will have their values derived prior to the object being saved.
     */
    void save();

    /**
     * Saves the object, and those supplied, in a single transaction.
     *
     * @param objects the other objects to save
     */
    void save(IMObject... objects);

    /**
     * Helper to return an object as a bean.
     *
     * @param object the object
     * @return the bean
     */
    IMObjectBean getBean(IMObject object);
}
