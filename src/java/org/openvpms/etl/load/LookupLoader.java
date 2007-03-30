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
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.etl.ETLArchetype;
import org.openvpms.etl.ETLPair;
import org.openvpms.etl.ETLValueDAO;
import static org.openvpms.etl.load.LoaderException.ErrorCode.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Loads {@link Lookup} instances, sourced from a {@link ETLValueDAO}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupLoader {

    /**
     * The source DAO.
     */
    private final ETLValueDAO dao;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup handler.
     */
    private final LookupLoaderHandler handler;


    /**
     * Constructs a new <tt>LookupLoader</tt>.
     *
     * @param dao     the source DAO
     * @param service the archeype service
     * @param handler the lookup handler
     */
    public LookupLoader(ETLValueDAO dao, IArchetypeService service,
                        LookupLoaderHandler handler) {
        this.dao = dao;
        this.service = service;
        this.handler = handler;
    }

    /**
     * Loads all lookups.
     *
     * @throws LoaderException           for any load error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void load() {
        handler.start();
        Collection<ETLArchetype> archetypes = dao.getArchetypes();

        Map<NodeDescriptor, ArchetypeDescriptor> lookups
                = new HashMap<NodeDescriptor, ArchetypeDescriptor>();
        Map<NodeDescriptor, ArchetypeDescriptor> targetLookups
                = new HashMap<NodeDescriptor, ArchetypeDescriptor>();

        for (ETLArchetype archetype : archetypes) {
            ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                    archetype.getArchetype());
            if (descriptor != null) {
                for (String name : archetype.getNames()) {
                    NodeDescriptor node = descriptor.getNodeDescriptor(name);
                    if (node != null && node.isLookup()) {
                        if (isLookupType(node)) {
                            lookups.put(node, descriptor);
                        } else if (isTargetLookupType(node)) {
                            targetLookups.put(node, descriptor);
                        }
                    }
                }
            } else {
                LoaderException exception = new LoaderException(
                        LookupArchetypeNotFound, archetype.getArchetype());
                if (handler.error(null, exception, null)) {
                    throw exception;
                }
            }
        }

        // process all lookups of type 'lookup' first, as these may be needed
        // by lookup relationships
        for (Map.Entry<NodeDescriptor, ArchetypeDescriptor> entry :
                lookups.entrySet()) {
            processLookup(entry.getValue(), entry.getKey());
        }

        // now process all lookups of type 'targetLookup'
        for (Map.Entry<NodeDescriptor, ArchetypeDescriptor> entry :
                targetLookups.entrySet()) {
            processLookup(entry.getValue(), entry.getKey());
        }
        handler.end();
    }

    /**
     * Processes a lookup.
     *
     * @param archetype  the archetype
     * @param descriptor the lookup descriptor
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void processLookup(ArchetypeDescriptor archetype,
                               NodeDescriptor descriptor) {
        AssertionDescriptor assertion
                = descriptor.getAssertionDescriptor("lookup");
        String type = getValue(assertion, "type");
        if ("lookup".equals(type)) {
            processLookup(assertion, archetype, descriptor);
        } else if ("targetLookup".equals(type)) {
            processTargetLookup(assertion, archetype, descriptor);
        }
    }

    /**
     * Determines if a lookup node has type 'lookup'.
     *
     * @param descriptor the node descriptor
     * @return <tt>true</tt> if the node has type 'lookup'
     */
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
     * Processes a lookup of type 'lookup'.
     *
     * @param assertion  the lookup assertion descriptor
     * @param archetype  the archetype
     * @param descriptor the node descriptor
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void processLookup(AssertionDescriptor assertion,
                               ArchetypeDescriptor archetype,
                               NodeDescriptor descriptor) {
        String source = getValue(assertion, "source");
        Collection<String> values
                = dao.getDistinctValues(
                archetype.getType().getShortName(),
                descriptor.getName());
        createLookups(source, values);
    }

    /**
     * Processes a lookup of type 'targetLookup'.
     *
     * @param assertion  the lookup assertion descriptor
     * @param archetype  the archetype
     * @param descriptor the node descriptor
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void processTargetLookup(AssertionDescriptor assertion,
                                     ArchetypeDescriptor archetype,
                                     NodeDescriptor descriptor) {
        String relationship = getValue(assertion, "relationship");
        String target = getTargetShortName(relationship);
        if (target != null) {
            String shortName = archetype.getType().getShortName();
            Collection<String> values = dao.getDistinctValues(
                    shortName, descriptor.getName());
            createLookups(target, values);
            String value = getValue(assertion, "value");
            NodeDescriptor source = getNodeByPath(archetype, value);
            if (source != null) {
                Collection<ETLPair> relationships
                        = dao.getDistinctValuePairs(
                        shortName, source.getName(), descriptor.getName());
                createLookupRelationships(relationship, relationships);
            } else {
                LoaderException exception = new LoaderException(
                        LookupSourceNodeNotFound, shortName,
                        descriptor.getName(), value);
                raise(exception);
            }
        } else {
            LoaderException exception = new LoaderException(
                    LookupRelationshipTargetNotFound, relationship);
            raise(exception);
        }
    }

    /**
     * Returns the target node archetype short name from a lookup relationship.
     *
     * @param relationship the lookup relationship short name
     * @return the target node archetype short name, or <tt>null</tt> if
     *         none is found
     */
    private String getTargetShortName(String relationship) {
        if (relationship != null) {
            ArchetypeDescriptor archetype
                    = service.getArchetypeDescriptor(relationship);
            if (archetype != null) {
                NodeDescriptor node = archetype.getNodeDescriptor("target");
                if (node != null) {
                    return getShortName(node);
                }
            }
        }
        return null;
    }

    /**
     * Returns the archetype short name from a node descriptor.
     *
     * @param node the node descriptor
     * @return the archetype short name, or <tt>null</tt> if none si found
     *         todo currently don't support multiple lookup archetypes, but its
     *         difficult to see how these could be used
     */
    private String getShortName(NodeDescriptor node) {
        String[] shortNames = DescriptorHelper.getShortNames(node, service);
        if (shortNames.length > 0) {
            return shortNames[0];
        }
        return null;
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
     * Creates lookups with the given short name, for each value.
     *
     * @param shortName the lookup short name
     * @param values    the values
     */
    private void createLookups(String shortName, Collection<String> values) {
        for (String value : values) {
            Lookup lookup = (Lookup) service.create(shortName);
            if (lookup != null) {
                lookup.setCode(value);
                handler.add(lookup, null);
            } else {
                LoaderException exception = new LoaderException(
                        LookupArchetypeNotFound, shortName);
                if (handler.error(null, exception, null)) {
                    throw exception;
                } else {
                    break;
                }
            }
        }
    }

    private void createLookupRelationships(String shortName,
                                           Collection<ETLPair> values) {
        handler.flush(); // need to flush any unsaved lookups
        for (ETLPair value : values) {
            LookupRelationshipHelper rel
                    = new LookupRelationshipHelper(shortName, service);
            Lookup source = handler.getLookup(rel.getSource(),
                                              value.getValue1());
            Lookup target = handler.getLookup(rel.getTarget(),
                                              value.getValue2());

            if (source != null && target != null) {
                IMObject relationship = service.create(shortName);
                if (relationship != null) {
                    IMObjectBean bean = new IMObjectBean(relationship, service);
                    bean.setValue("source", source.getObjectReference());
                    bean.setValue("target", target.getObjectReference());
                    handler.add(relationship, null);
                }
            } else if (source == null) {
                raise(new LoaderException(LookupNotFound, rel.getSource(),
                                          value.getValue1()));
            } else {
                raise(new LoaderException(LookupNotFound, rel.getTarget(),
                                          value.getValue2()));
            }
        }
    }

    /**
     * Notifies the registered listeners {@link LoaderListener#error}
     * method of an error, and raises an exception if the listener indicates to
     * terminate.
     *
     * @param exception the exception
     * @throws LoaderException if the listener indicates to terminate
     */
    private void raise(LoaderException exception) {
        if (!handler.error(null, exception, null)) {
            throw exception;
        }
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

}
