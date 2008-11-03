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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import static org.openvpms.etl.load.LoaderException.ErrorCode.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Automatically generates lookups for lookup nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LookupHandler {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * A mapping of lookup nodes to their corresponding lookup descriptors.
     */
    Map<NodeDescriptor, LookupDescriptor> lookups
            = new HashMap<NodeDescriptor, LookupDescriptor>();

    /**
     * A mapping of target lookup nodes to their corresponding lookup
     * relationship descriptors.
     */
    Map<NodeDescriptor, LookupRelationshipDescriptor> relationships
            = new HashMap<NodeDescriptor, LookupRelationshipDescriptor>();

    /**
     * The error listener, to notify of processing errors. May be <tt>null</tt>
     */
    private ErrorListener listener;

    /**
     * The lookup cache.
     */
    private LookupCache cache;

    /**
     * Exception messages.
     */
    private final ExceptionHelper messages;


    /**
     * Creates a new <tt>LookupHandler</tt>.
     *
     * @param mappings the mappings
     * @param service  the archetype service
     */
    public LookupHandler(Mappings mappings, IArchetypeService service) {
        this.service = service;
        ILookupService lookupService = LookupServiceHelper.getLookupService();
        cache = new LookupCache(service, lookupService);
        messages = new ExceptionHelper(service);

        // cache of lookups of type 'targetLookup'
        Map<NodeDescriptor, ArchetypeDescriptor> targets
                = new HashMap<NodeDescriptor, ArchetypeDescriptor>();

        // for each node in the mapping, determine which of those nodes are
        // lookups. Lookups of type 'lookup' are processed first.
        for (Mapping mapping : mappings.getMapping()) {
            String target = mapping.getTarget();
            Node node = NodeParser.parse(target);
            while (node != null) {
                ArchetypeDescriptor archetype = service.getArchetypeDescriptor(
                        node.getArchetype());
                if (archetype != null) {
                    NodeDescriptor descriptor = archetype.getNodeDescriptor(
                            node.getName());
                    if (descriptor != null && descriptor.isLookup()) {
                        if (isLookupType(descriptor)) {
                            processLookupDescriptor(descriptor);
                        }
                        if (isTargetLookupType(descriptor)) {
                            targets.put(descriptor, archetype);
                        }
                    }
                }
                node = node.getChild();
            }
        }

        // process target lookups. This needs to be done last as the
        // source lookups need to have been registered first
        for (Map.Entry<NodeDescriptor, ArchetypeDescriptor> entry :
                targets.entrySet()) {
            processTargetLookupDescriptor(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Determines if a node descriptor refers to a lookup that is automatically
     * generated.
     *
     * @param descriptor the node descriptor
     * @return <tt>true</tt> if the descriptro refers to an automatically
     *         generated lookup
     */
    public boolean isGeneratedLookup(NodeDescriptor descriptor) {
        return lookups.containsKey(descriptor);
    }

    /**
     * Adds a set of lookup code/name pairs for a row.
     *
     * @param descriptors the lookup descriptors
     */
    public void add(Map<NodeDescriptor, CodeName> descriptors) {
        for (Map.Entry<NodeDescriptor, CodeName> entry
                : descriptors.entrySet()) {
            NodeDescriptor descriptor = entry.getKey();
            CodeName pair = entry.getValue();
            LookupDescriptor lookup = lookups.get(descriptor);
            lookup.add(pair);
            LookupRelationshipDescriptor relationship
                    = relationships.get(descriptor);
            if (relationship != null) {
                CodeName source = descriptors.get(
                        relationship.getSource().getDescriptor());
                if (source != null) {
                    relationship.add(source.getCode(), pair.getCode());
                }
            }
        }
    }

    /**
     * Generates a code for a lopokup name.
     * This capitalises the lookup name, and replaces all non-alphanumeric
     * characters with underscores.
     *
     * @param name the lookup name
     * @return the lookup code
     */
    public String getCode(String name) {
        String result = null;
        if (name != null) {
            result = name.toUpperCase();
            result = result.replaceAll("[^A-Z0-9]+", "_"); // NON-NLS
        }
        return result;
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setErrorListener(ErrorListener listener) {
        this.listener = listener;
    }

    /**
     * Commits all generated lookups.
     *
     * @throws ArchetypeServiceException for any archetype service exception
     * @throws LoaderException           for any loader error
     */
    public void commit() {
        if (!lookups.isEmpty() || !relationships.isEmpty()) {
            List<IMObject> objects = new ArrayList<IMObject>();
            for (LookupDescriptor descriptor : lookups.values()) {
                for (CodeName pair : descriptor.getLookups()) {
                    if (!exists(descriptor.getArchetype(), pair.getCode())) {
                        Lookup lookup = (Lookup) service.create(
                                descriptor.getArchetype());
                        if (lookup == null) {
                            throw new LoaderException(
                                    ArchetypeNotFound,
                                    descriptor.getArchetype());
                        }
                        lookup.setCode(pair.getCode());
                        lookup.setName(pair.getName());
                        objects.add(lookup);
                        cache.add(lookup);
                    }
                }
                descriptor.clear();
            }
            if (!objects.isEmpty()) {
                save(objects);
            }

            for (LookupRelationshipDescriptor descriptor
                    : relationships.values()) {
                List<IMObject> relationships = createRelationships(descriptor);
                if (!relationships.isEmpty()) {
                    save(relationships);
                }
                descriptor.clear();
            }
        }
    }

    /**
     * Closes the handler, releasing resources.
     */
    public void close() {
        lookups.clear();
        relationships.clear();
    }

    /**
     * Saves a collection of objects.
     *
     * @param objects the objects to save
     */
    protected void save(Collection<IMObject> objects) {
        try {
            service.save(objects);
        } catch (OpenVPMSException exception) {
            // can't process as a batch. Process individual objects.
            for (IMObject object : objects) {
                save(object);
            }
        }
    }

    /**
     * Saves an object.
     *
     * @param object the object to save
     */
    protected void save(IMObject object) {
        try {
            service.save(object);
        } catch (OpenVPMSException exception) {
            notifyListener(exception);
        }
    }

    /**
     * Determines if a lookup exists.
     *
     * @param archetype the lookup archetype short name
     * @param code      the lookup code
     * @return <tt>true</tt> if it exists, otherwise <tt>false</tt>
     */
    protected boolean exists(String archetype, String code) {
        return cache.exists(archetype, code);
    }

    /**
     * Determines if a lookup relationship exists.
     *
     * @param archetype the relationship archetype short name
     * @param source    the source lookup
     * @param target    the target lookup
     * @return <tt>true</tt> if it exists, otherwise <tt>false</tt>
     */
    protected boolean exists(String archetype, Lookup source,
                             Lookup target) {
        return cache.exists(archetype, source.getObjectReference(),
                            target.getObjectReference());
    }

    /**
     * Processes a lookup node descriptor with type 'lookup'.
     * This creates and registers a {@link LookupDescriptor}.
     *
     * @param descriptor the lookup node descriptor
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private void processLookupDescriptor(NodeDescriptor descriptor) {
        AssertionDescriptor assertion
                = descriptor.getAssertionDescriptor("lookup");
        String archetype = getValue(assertion, "source");
        lookups.put(descriptor,
                    new LookupDescriptor(descriptor, archetype));
    }

    /**
     * Processes a lookup node descriptor with type 'targetLookup'.
     * This creates and registers a {@link LookupDescriptor},
     * and adds a {@link LookupRelationshipDescriptor} if the source is also
     * mapped.
     *
     * @param archetype the archetype that the node descriptor belongs to
     * @param target    the target lookup node descriptor
     * @throws LoaderException if the lookup definition is invalid
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private void processTargetLookupDescriptor(ArchetypeDescriptor archetype,
                                               NodeDescriptor target) {
        AssertionDescriptor assertion
                = target.getAssertionDescriptor("lookup");
        String relationship = getValue(assertion, "relationship");
        if (relationship == null) {
            throw new LoaderException(LookupRelationshipNotFound,
                                      archetype.getType().getShortName(),
                                      target.getName());
        }
        String value = getValue(assertion, "value");
        NodeDescriptor source = getNodeByPath(archetype, value);
        if (source == null) {
            String shortName = archetype.getType().getShortName();
            throw new LoaderException(
                    LoaderException.ErrorCode.LookupSourceNodeNotFound,
                    shortName, target.getName(), value);
        }
        String targetShortName = getTargetShortName(relationship);
        LookupDescriptor targetLookup
                = new LookupDescriptor(target, targetShortName);
        lookups.put(target, targetLookup);

        LookupDescriptor sourceLookup = lookups.get(source);
        if (sourceLookup != null) {
            LookupRelationshipDescriptor descriptor
                    = new LookupRelationshipDescriptor(relationship,
                                                       sourceLookup,
                                                       targetLookup);
            relationships.put(target, descriptor);
        }
    }

    /**
     * Returns the target node archetype short name from a lookup relationship.
     *
     * @param relationship the lookup relationship short name
     * @return the target node archetype short name
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private String getTargetShortName(String relationship) {
        ArchetypeDescriptor archetype
                = service.getArchetypeDescriptor(relationship);
        if (archetype == null) {
            throw new LoaderException(ArchetypeNotFound, relationship);
        }
        NodeDescriptor node = archetype.getNodeDescriptor("target");
        String result = null;
        if (node != null) {
            String[] shortNames = DescriptorHelper.getShortNames(node, service);
            if (shortNames.length > 0) {
                // NOTE: don't support multiple lookup archetypes, but its
                // difficult to see how these could be used
                result = shortNames[0];
            }
        }
        if (result == null) {
            throw new LoaderException(LookupRelationshipTargetNotFound,
                                      relationship);
        }
        return result;
    }

    /**
     * Creates lookup relationships for a lookup relationship descriptor.
     *
     * @param descriptor the lookup relationship descriptor
     * @return the lookup relationships
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LoaderException           for any loader error
     */
    private List<IMObject> createRelationships(
            LookupRelationshipDescriptor descriptor) {
        List<IMObject> result = new ArrayList<IMObject>();
        for (Pair pair : descriptor.getPairs()) {
            String sourceCode = pair.getValue1();
            String targetCode = pair.getValue2();
            Lookup source = getLookup(descriptor.getSource(), sourceCode);
            Lookup target = getLookup(descriptor.getTarget(), targetCode);

            if (source != null && target != null) {
                if (!exists(descriptor.getArchetype(), source, target)) {
                    LookupRelationship relationship
                            = (LookupRelationship) service.create(
                            descriptor.getArchetype());
                    if (relationship == null) {
                        throw new LoaderException(ArchetypeNotFound,
                                                  descriptor.getArchetype());
                    }
                    relationship.setSource(source.getObjectReference());
                    relationship.setTarget(target.getObjectReference());
                    result.add(relationship);
                    cache.add(relationship);
                }
            } else if (source == null) {
                throw new LoaderException(LookupNotFound,
                                          descriptor.getSource().getArchetype(),
                                          sourceCode);
            } else {
                throw new LoaderException(LookupNotFound,
                                          descriptor.getTarget().getArchetype(),
                                          targetCode);
            }
        }
        return result;
    }

    /**
     * Helper to get a lookup with the specified code from a cache of lookups.
     *
     * @param descriptor the lookup descriptor
     * @param code       the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Lookup getLookup(LookupDescriptor descriptor, String code) {
        return cache.get(descriptor.getArchetype(), code);
    }

    /**
     * Returns the node with matching jxpath.
     *
     * @param archetype the archetype descriptor
     * @param path      the node jxpath
     * @return the node with corresponding path, or <tt>null</tt> if none
     *         is found
     */
    private NodeDescriptor getNodeByPath(ArchetypeDescriptor archetype,
                                         String path) {
        for (NodeDescriptor node : archetype.getAllNodeDescriptors()) {
            if (node.getPath().equals(path)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Determines if a lookup node has type 'lookup'.
     *
     * @param descriptor the node descriptor
     * @return <tt>true</tt> if the node has type 'lookup'
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private boolean isLookupType(NodeDescriptor descriptor) {
        AssertionDescriptor assertion
                = descriptor.getAssertionDescriptor("lookup");
        if (assertion != null) {
            String type = getValue(assertion, "type");
            return "lookup".equals(type);
        }
        return false;
    }

    /**
     * Determines if a lookup node has type 'targetLookup'.
     *
     * @param descriptor the node descriptor
     * @return <tt>true</tt> if the node has type 'targetLookup'
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private boolean isTargetLookupType(NodeDescriptor descriptor) {
        AssertionDescriptor assertion
                = descriptor.getAssertionDescriptor("lookup");
        if (assertion != null) {
            String type = getValue(assertion, "type");
            return "targetLookup".equals(type);
        }
        return false;
    }

    /**
     * Returns the value of the named property from an assertion descriptor.
     *
     * @param assertion the assertion descriptor
     * @param name      the property name
     * @return the property value, or <code>null</code> if it doesn't exist
     */
    private String getValue(AssertionDescriptor assertion, String name) {
        NamedProperty property = assertion.getProperty(name);
        return (property != null) ? (String) property.getValue() : null;
    }

    /**
     * Notifies any registered listener of an error.
     *
     * @param exception the exception
     */
    private void notifyListener(Throwable exception) {
        if (listener != null) {
            listener.error(messages.getMessage(exception), exception);
        }
    }
}
