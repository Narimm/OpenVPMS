/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * This interface defines the services that are provided by the archetype
 * service. The client is able to return a archetype by name or by archetype
 * idenity.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IArchetypeService {

    /**
     * Returns the {@link ArchetypeDescriptor} with the specified short name.
     *
     * @param shortName the short name
     * @return the descriptor corresponding to the short name, or
     *         <code>null</code> if none is found
     * @throws ArchetypeServiceException for any error
     */
    public ArchetypeDescriptor getArchetypeDescriptor(String shortName);

    /**
     * Returns the {@link ArchetypeDescriptor} for the specified
     * {@link ArchetypeId}.
     *
     * @param id the archetype identifier
     * @return the archetype descriptor corresponding to <code>id</code> or
     *         <code>null</code> if none is found
     * @throws ArchetypeServiceException for any error
     */
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id);

    /**
     * Create a default domain object given a short name. The short name is
     * a reference to an {@link ArchetypeDescriptor}. If the short name is not
     * known then return a null object.
     *
     * @param shortName the short name
     * @return a new object, or <code>null</code> if there is no corresponding
     *         archetype descriptor for <code>shortName</code>
     * @throws ArchetypeServiceException if the object can't be created
     */
    public IMObject create(String shortName);

    /**
     * Create a default domain object given an {@link ArchetypeId}. If the
     * archetype id is not defined then return null.
     *
     * @param id the archetype id
     * @return a new object, or <code>null</code> if there is no corresponding
     *         archetype descriptor for <code>shortName</code>
     * @throws ArchetypeServiceException if the object can't be created
     */
    public IMObject create(ArchetypeId id);

    /**
     * Validate the specified {@link IMObject}. To validate the object it will
     * retrieve the archetype and iterate through the assertions
     *
     * @param object the object to validate
     * @throws ValidationException if there are validation errors
     */
    public void validateObject(IMObject object);

    /**
     * Derived values for the specified {@link IMObject}, based on its
     * corresponding {@link ArchetypeDescriptor}.
     *
     * @param object the object to derived values for
     * @throws ArchetypeServiceException if values cannot be derived
     */
    public void deriveValues(IMObject object);

    /**
     * Derive the value for the {@link NodeDescriptor} with the specified
     * name.
     *
     * @param object the object to operate on.
     * @param node   the name of the {@link NodeDescriptor}, which will be used
     *               to derive the value
     * @throws ArchetypeServiceException if the value cannot be derived
     */
    public void deriveValue(IMObject object, String node);

    /**
     * Returns all the {@link ArchetypeDescriptor} managed by this service.
     *
     * @return the archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors();

    /**
     * Return all the {@link ArchetypeDescriptor} instances that match the
     * specified shortName.
     *
     * @param shortName the short name. May contain wildcards
     * @return a list of matching archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName);

    /**
     * Return all the {@link ArchetypeDescriptor} instance with the specified
     * reference model name.
     *
     * @param rmName the reference model name
     * @return a list of matching archetype descriptors
     * @throws ArchetypeServiceException for any error
     * @deprecated no replacement
     */
    @Deprecated
    public List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(
            String rmName);

    /**
     * Return the {@link AssertionTypeDescriptor} with the specified name.
     *
     * @param name the name of the assertion type
     * @return the assertion type descriptor corresponding to <Code>name</code>
     *         or <code>null</code> if none is found
     * @throws ArchetypeServiceException for any error
     */
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name);

    /**
     * Return all the {@link AssertionTypeDescriptor} instances supported by
     * this service.
     *
     * @return the assertion type descriptors
     * @throws ArchetypeServiceException for any error
     */
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors();

    /**
     * Remove the specified entity.
     *
     * @param entity the entity to remove
     * @throws ArchetypeServiceException if the entity cannot be removed
     */
    public void remove(IMObject entity);

    /**
     * Save or update the specified entity.
     *
     * @param entity the entity to insert or update
     * @throws ArchetypeServiceException if the service cannot save the
     *                                   specified entity
     * @throws ValidationException       if the specified entity cannot be
     *                                   validated
     */
    public void save(IMObject entity);

    /**
     * Save or update the specified entity.
     *
     * @param entity   the entity to insert or update
     * @param validate whether to validate or not
     * @throws ArchetypeServiceException if the service cannot save the
     *                                   specified entity
     * @throws ValidationException       if the specified entity cannot be
     *                                   validated
     */
    @Deprecated
    public void save(IMObject entity, boolean validate);

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects the objects to insert or update
     * @throws ArchetypeServiceException if an object can't be saved
     * @throws ValidationException       if an object can't be validated
     */
    void save(Collection<? extends IMObject> objects);

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects  the objects to insert or update
     * @param validate whether to validate or not
     * @throws ArchetypeServiceException if an object can't be saved
     * @throws ValidationException       if an object can't be validated
     */
    @Deprecated
    void save(Collection<? extends IMObject> objects, boolean validate);

    /**
     * Retrieves an object given its reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException if the query fails
     */
    IMObject get(IMObjectReference reference);

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    IPage<IMObject> get(IArchetypeQuery query);

    /**
     * Retrieves partially populated objects that match the query.
     * This may be used to selectively load parts of object graphs to improve
     * performance.
     * <p/>
     * All simple properties of the returned objects are populated - the
     * <code>nodes</code> argument is used to specify which collection nodes to
     * populate. If empty, no collections will be loaded, and the behaviour of
     * accessing them is undefined.
     *
     * @param query the archetype query
     * @param nodes the collection node names
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    IPage<IMObject> get(IArchetypeQuery query, Collection<String> nodes);

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    IPage<ObjectSet> getObjects(IArchetypeQuery query);

    /**
     * Retrieves the nodes from the objects that match the query criteria.
     *
     * @param query the archetype query
     * @param nodes the node names
     * @return the nodes for each object that matches the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    IPage<NodeSet> getNodes(IArchetypeQuery query, Collection<String> nodes);

    /**
     * Return a list of archtype short names given the specified criteria.
     *
     * @param rmName      the reference model name
     * @param entityName  the entity name
     * @param conceptName the concept name
     * @param primaryOnly indicates whether to return primary objects only.
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     * @see #getArchetypeShortNames(String entityName, String conceptName,
     *      boolean primaryOnly)
     * @deprecated
     */
    @Deprecated
    public List<String> getArchetypeShortNames(String rmName, String entityName,
                                               String conceptName,
                                               boolean primaryOnly);

    /**
     * Return a list of archtype short names given the specified criteria.
     *
     * @param entityName  the entity name
     * @param conceptName the concept name
     * @param primaryOnly indicates whether to return primary objects only.
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public List<String> getArchetypeShortNames(String entityName,
                                               String conceptName,
                                               boolean primaryOnly);

    /**
     * Return all the archetype short names.
     *
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public List<String> getArchetypeShortNames();

    /**
     * Return all the archetypes which match the specified short name.
     *
     * @param shortName   the short name, which may contain a wildcard character
     * @param primaryOnly return only the primary archetypes
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public List<String> getArchetypeShortNames(String shortName,
                                               boolean primaryOnly);

    /**
     * Execute the rule specified by the uri and using the passed in
     * properties and facts.
     *
     * @param ruleUri the rule uri
     * @param props   a set of properties that can be used by the rule engine
     * @param facts   a list of facts that are asserted in to the working memory
     * @return a list objects. May be an empty list.
     * @throws ArchetypeServiceException if it cannot execute the specified rule
     */
    public List<Object> executeRule(String ruleUri, Map<String, Object> props,
                                    List<Object> facts);

    /**
     * Adds a listener to receive notification of changes.
     * <p/>
     * In a transaction, notifications occur on successful commit.
     *
     * @param shortName the archetype short to receive events for. May contain
     *                  wildcards.
     * @param listener  the listener to add
     */
    void addListener(String shortName, IArchetypeServiceListener listener);

    /**
     * Removes a listener.
     *
     * @param shortName the archetype short to remove the listener for. May
     *                  contain wildcards.
     * @param listener  the listener to remove
     */
    void removeListener(String shortName, IArchetypeServiceListener listener);
}
