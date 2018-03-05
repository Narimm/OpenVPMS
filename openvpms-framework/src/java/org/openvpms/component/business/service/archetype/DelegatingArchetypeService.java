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

package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.service.archetype.ValidationError;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Implementation of {@link IArchetypeService} that delegates to another instance.
 *
 * @author Tim Anderson
 */
public abstract class DelegatingArchetypeService implements IArchetypeService {

    /**
     * The archetype service to delegate to.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link DelegatingArchetypeService}.
     *
     * @param service the archetype service to delegate requests to
     */
    public DelegatingArchetypeService(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the {@link ArchetypeDescriptor} with the specified short name.
     *
     * @param shortName the short name
     * @return the descriptor corresponding to the short name, or {@code null} if none is found
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public ArchetypeDescriptor getArchetypeDescriptor(String shortName) {
        return service.getArchetypeDescriptor(shortName);
    }

    /**
     * Returns the {@link ArchetypeDescriptor} for the specified {@link ArchetypeId}.
     *
     * @param id the archetype identifier
     * @return the archetype descriptor corresponding to {@code id} or {@code null} if none is found
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id) {
        return service.getArchetypeDescriptor(id);
    }

    /**
     * Create a domain object given a short name. The short name is a reference to an {@link ArchetypeDescriptor}.
     *
     * @param shortName the short name
     * @return a new object, or {@code null} if there is no corresponding archetype descriptor for {@code shortName}
     * @throws ArchetypeServiceException if the object can't be created
     */
    @Override
    public IMObject create(String shortName) {
        return service.create(shortName);
    }

    /**
     * Create a domain object given an {@link ArchetypeId}.
     *
     * @param id the archetype id
     * @return a new object, or {@code null} if there is no corresponding archetype descriptor for {@code shortName}
     * @throws ArchetypeServiceException if the object can't be created
     */
    @Override
    public IMObject create(ArchetypeId id) {
        return service.create(id);
    }

    /**
     * Validate the specified {@link IMObject}. To validate the object it will retrieve the archetype and iterate
     * through the assertions.
     *
     * @param object the object to validate
     * @return any validation errors
     */
    @Override
    public List<ValidationError> validate(IMObject object) {
        return service.validate(object);
    }

    /**
     * Validate the specified {@link IMObject}. To validate the object it will retrieve the archetype and iterate
     * through the assertions.
     *
     * @param object the object to validate
     * @throws ValidationException if there are validation errors
     */
    @Override
    public void validateObject(IMObject object) {
        service.validateObject(object);
    }

    /**
     * Derived values for the specified {@link IMObject}, based on its corresponding {@link ArchetypeDescriptor}.
     *
     * @param object the object to derived values for
     * @throws ArchetypeServiceException if values cannot be derived
     */
    @Override
    public void deriveValues(IMObject object) {
        service.deriveValues(object);
    }

    /**
     * Derive the value for the {@link NodeDescriptor} with the specified name.
     *
     * @param object the object to operate on.
     * @param node   the name of the {@link NodeDescriptor}, which will be used to derive the value
     * @throws ArchetypeServiceException if the value cannot be derived
     */
    @Override
    public void deriveValue(IMObject object, String node) {
        service.deriveValue(object, node);
    }

    /**
     * Returns all the {@link ArchetypeDescriptor} managed by this service.
     *
     * @return the archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public List<ArchetypeDescriptor> getArchetypeDescriptors() {
        return service.getArchetypeDescriptors();
    }

    /**
     * Return all the {@link ArchetypeDescriptor} instances that match the specified shortName.
     *
     * @param shortName the short name. May contain wildcards
     * @return a list of matching archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName) {
        return service.getArchetypeDescriptors(shortName);
    }

    /**
     * Return the {@link AssertionTypeDescriptor} with the specified name.
     *
     * @param name the name of the assertion type
     * @return the assertion type descriptor corresponding to {@code name} or {@code null} if none is found
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name) {
        return service.getAssertionTypeDescriptor(name);
    }

    /**
     * Return all the {@link AssertionTypeDescriptor} instances supported by this service.
     *
     * @return the assertion type descriptors
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors() {
        return service.getAssertionTypeDescriptors();
    }

    /**
     * Saves an object, executing any <em>save</em> rules associated with its archetype.
     *
     * @param object the object to save
     * @throws ArchetypeServiceException if the service cannot save the specified object
     * @throws ValidationException       if the object cannot be validated
     */
    @Override
    public void save(IMObject object) {
        save(object, true);
    }

    /**
     * Save a collection of {@link IMObject} instances. executing any  <em>save</em> rules associated with their
     * archetypes.
     * <p>
     * Rules will be executed in the order that the objects are supplied.
     *
     * @param objects the objects to save
     * @throws ArchetypeServiceException if an object can't be saved
     * @throws ValidationException       if an object can't be validated
     */
    @Override
    public void save(Collection<? extends IMObject> objects) {
        save(objects, true);
    }

    /**
     * Saves an object, executing any <em>save</em> rules associated with its archetype.
     *
     * @param object   the object to save
     * @param validate if {@code true} validate the object prior to saving it
     * @throws ArchetypeServiceException if the service cannot save the specified object
     * @throws ValidationException       if the specified object cannot be validated
     */
    @Override
    @Deprecated
    public void save(final IMObject object, final boolean validate) {
        service.save(object, validate);
    }

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects the objects to save
     * @throws ArchetypeServiceException if an object can't be saved
     * @throws ValidationException       if an object can't be validated
     */
    @Override
    @Deprecated
    public void save(final Collection<? extends IMObject> objects, final boolean validate) {
        service.save(objects, validate);
    }

    /**
     * Remove the specified object.
     *
     * @param object the object to remove
     * @throws ArchetypeServiceException if the object cannot be removed
     */
    @Override
    public void remove(final IMObject object) {
        service.remove(object);
    }

    /**
     * Retrieves an object given its reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or {@code null} if none is found
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public IMObject get(Reference reference) {
        return service.get(reference);
    }

    /**
     * Retrieves an object given its reference.
     *
     * @param reference the object reference
     * @param active    if {@code true}, only return the object if it is active
     * @return the corresponding object, or {@code null} if none is found
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public IMObject get(Reference reference, boolean active) {
        return service.get(reference, active);
    }

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public IPage<IMObject> get(IArchetypeQuery query) {
        return service.get(query);
    }

    /**
     * Retrieves partially populated objects that match the query.
     * This may be used to selectively load parts of object graphs to improve performance.
     * <p>
     * All simple properties of the returned objects are populated - the {@code nodes} argument is used to specify which
     * collection nodes to populate. If empty, no collections will be loaded, and the behaviour of accessing them is
     * undefined.
     *
     * @param query the archetype query
     * @param nodes the collection node names
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public IPage<IMObject> get(IArchetypeQuery query, Collection<String> nodes) {
        return service.get(query, nodes);
    }

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
        return service.getObjects(query);
    }

    /**
     * Retrieves the nodes from the objects that match the query criteria.
     *
     * @param query the archetype query
     * @param nodes the node names
     * @return the nodes for each object that matches the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public IPage<NodeSet> getNodes(IArchetypeQuery query, Collection<String> nodes) {
        return service.getNodes(query, nodes);
    }

    /**
     * Return a list of archetype short names given the specified criteria.
     *
     * @param entityName  the entity name
     * @param conceptName the concept name
     * @param primaryOnly indicates whether to return primary objects only.
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public List<String> getArchetypeShortNames(String entityName, String conceptName, boolean primaryOnly) {
        return service.getArchetypeShortNames(entityName, conceptName, primaryOnly);
    }

    /**
     * Return all archetype short names.
     *
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public List<String> getArchetypeShortNames() {
        return service.getArchetypeShortNames();
    }

    /**
     * Return all archetype short names that match the specified short name.
     *
     * @param shortName   the short name, which may contain a wildcard character
     * @param primaryOnly return only the primary archetypes
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public List<String> getArchetypeShortNames(String shortName, boolean primaryOnly) {
        return service.getArchetypeShortNames(shortName, primaryOnly);
    }

    /**
     * Execute the rule specified by the uri and using the passed in properties and facts.
     *
     * @param ruleUri the rule uri
     * @param props   a set of properties that can be used by the rule engine
     * @param facts   a list of facts that are asserted in to the working memory
     * @return a list objects. May be an empty list.
     * @throws ArchetypeServiceException if it cannot execute the specified rule
     */
    @Override
    public List<Object> executeRule(String ruleUri, Map<String, Object> props, List<Object> facts) {
        return service.executeRule(ruleUri, props, facts);
    }

    /**
     * Adds a listener to receive notification of changes.
     * <p>
     * In a transaction, notifications occur on successful commit.
     *
     * @param shortName the archetype short to receive events for. May contain wildcards.
     * @param listener  the listener to add
     */
    @Override
    public void addListener(String shortName, IArchetypeServiceListener listener) {
        service.addListener(shortName, listener);
    }

    /**
     * Removes a listener.
     *
     * @param shortName the archetype short to remove the listener for. May contain wildcards.
     * @param listener  the listener to remove
     */
    @Override
    public void removeListener(String shortName, IArchetypeServiceListener listener) {
        service.removeListener(shortName, listener);
    }

    /**
     * Returns a bean for an object.
     *
     * @param object the object
     * @return the bean
     */
    @Override
    public IMObjectBean getBean(org.openvpms.component.model.object.IMObject object) {
        // NOTE: has to use this instance, to ensure that any constraints defined by subclasses
        // are not bypassed.
        return new org.openvpms.component.business.service.archetype.helper.IMObjectBean(object, this);
    }

    /**
     * Returns the underlying service.
     *
     * @return the underlying service
     */
    protected IArchetypeService getService() {
        return service;
    }

}
