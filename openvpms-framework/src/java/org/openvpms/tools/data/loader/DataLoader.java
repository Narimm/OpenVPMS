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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.tools.data.loader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import static org.openvpms.tools.data.loader.ArchetypeDataLoaderException.ErrorCode.*;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class DataLoader {

    private final XMLStreamReader reader;

    private final Log log = LogFactory.getLog(DataLoader.class);

    private LoadContext context;
    private final IdRefCache cache;

    private final IArchetypeService service;

    private int batchSize;
    private List<IMObject> batch = new ArrayList<IMObject>();
    private final boolean verbose;

    private final boolean validateOnly;

    /**
     * Maintains a list of archetypes and count to indicate the number of
     * each saved or validated
     */
    private final Map<String, Long> statistics;


    private List<IMObjectState> deferred = new ArrayList<IMObjectState>();

    public DataLoader(XMLStreamReader reader) {
        this(reader, 0);
    }

    public DataLoader(XMLStreamReader reader, int batchSize) {
        this(reader, new IdRefCache(),
             ArchetypeServiceHelper.getArchetypeService(),
             false, false, batchSize, new HashMap<String, Long>());
    }

    public DataLoader(XMLStreamReader reader, IdRefCache cache,
                      IArchetypeService service, boolean verbose,
                      boolean validateOnly, int batchSize,
                      Map<String, Long> statistics) {
        this.reader = reader;
        this.cache = cache;
        this.service = service;
        this.verbose = verbose;
        this.validateOnly = validateOnly;
        this.batchSize = batchSize;
        context = new LoadContext(service, cache, validateOnly);
        this.statistics = statistics;
    }

    public void load() throws XMLStreamException {
        Stack<IMObjectState> stack = new Stack<IMObjectState>();
        for (int event = reader.next();
             event != XMLStreamConstants.END_DOCUMENT;
             event = reader.next()) {
            IMObjectState current;
            switch (event) {
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    String elementName = reader.getLocalName();
                    if ("data".equals(elementName)) {
                        startData(stack);
                    } else if (!"archetype".equals(elementName)) {
                        throw new ArchetypeDataLoaderException(
                                ArchetypeDataLoaderException.ErrorCode.ErrorInStartElement);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (!stack.isEmpty()) {
                        current = stack.pop();
                        load(current);
                    }

                    if (verbose) {
                        log.info("\n[END PROCESSING element="
                                + reader.getLocalName() + "]\n");
                    }
                    break;

                default:
                    break;
            }
        }
        saveBatch();
    }

    private void startData(Stack<IMObjectState> stack) {
        IMObjectState current;
        Element element = new Element(reader);
        if (verbose) {
            String archetypeId = stack.isEmpty() ? "none"
                    : stack.peek().getArchetypeId().toString();
            log.info("\n[START PROCESSING element, parent="
                    + archetypeId + "]\n" + element);
        }

        try {
            if (!stack.isEmpty()) {
                current = processElement(stack.peek(), element);
            } else {
                current = processElement(null, element);
            }
            stack.push(current);
        } catch (Exception exception) {
            Location location = reader.getLocation();
            log.error("Error in start element, line "
                    + location.getLineNumber()
                    + ", column " + location.getColumnNumber() +
                    "\n" + element + "\n", exception);
        }
    }

    public Map<String, Long> getStatistics() {
        return statistics;
    }

    public IdRefCache getRefCache() {
        return cache;
    }

    /**
     * Process the specified element and all nested elements. If the parent
     * object is specified then then the specified element is in a parent child
     * relationship.
     * <p/>
     * The archetype attribute determines the archetype we need to us to create
     * from the element. Iterate through all the element attributes and attempt
     * to set the specified value using the archetype's node descriptors. The
     * attribute name must correspond to a valid node descriptor
     * <p/>
     *
     * @param parent the parent of this element if it exists
     * @return IMObject the associated IMObject
     */
    private IMObjectState processElement(IMObjectState parent,
                                         Element element) {
        IMObjectState result;

        Location location = element.getLocation();
        String collectionNode = element.getCollection();

        // if a childId node is defined then we can skip the create object
        // process since the object already exists
        String childId = element.getChildId();
        if (StringUtils.isEmpty(childId)) {
            result = create(element, parent);
            for (Map.Entry<String, String> attribute
                    : element.getAttributes().entrySet()) {
                String name = attribute.getKey();
                String value = attribute.getValue();
                result.setValue(name, value, context);
            }
            if (collectionNode != null) {
                parent.addChild(collectionNode, result);
            }
        } else {
            // A childId has been specified. Must have a collection node, and
            // a non-null parent.
            if (parent == null) {
                throw new ArchetypeDataLoaderException(
                        NoParentForChild, location.getLineNumber(),
                        location.getColumnNumber());
            }
            result = parent;
            if (StringUtils.isEmpty(collectionNode)) {
                throw new ArchetypeDataLoaderException(
                        NoCollectionAttribute, location.getLineNumber(),
                        location.getColumnNumber());
            }
            parent.addChild(collectionNode, childId, context);
        }
        return result;
    }

    private IMObjectState create(Element element, IMObjectState parent) {
        String shortName = element.getShortName();
        ArchetypeDescriptor descriptor
                = service.getArchetypeDescriptor(shortName);
        Location location = element.getLocation();
        if (descriptor == null) {
            throw new ArchetypeDataLoaderException(
                    InvalidArchetype, location.getLineNumber(),
                    location.getColumnNumber(), shortName);
        }

        IMObject object = service.create(descriptor.getType());
        if (object == null) {
            throw new ArchetypeDataLoaderException(
                    InvalidArchetype, location.getLineNumber(),
                    location.getColumnNumber(), shortName);
        }
        if (element.getId() != null) {
            cache.add(element.getId(), object.getObjectReference());
        }
        return new IMObjectState(parent, object, descriptor,
                                 reader.getLocation());
    }

    /**
     * Load the specified object.
     */
    private void load(IMObjectState state) {
        if (!state.isComplete()) {
            deferred.add(state);
        } else {
            boolean hasDeferred = !deferred.isEmpty();
            IMObject object = state.getObject();
            queue(state);

            // update the stats
            String shortName = state.getArchetypeId().getShortName();
            Long count = statistics.get(shortName);
            if (count == null) {
                statistics.put(shortName, 1L);
            } else {
                statistics.put(shortName, count + 1);
            }

            if (verbose) {
                log.info("\n[CREATED]\n" + object);
            }

            if (hasDeferred) {
                if (!batch.isEmpty()) {
                    saveBatch();
                }
                List<IMObjectState> deferred = processDeferred();
                for (IMObjectState updated : deferred) {
                    queue(updated);
                }
            }
        }
    }

    private List<IMObjectState> processDeferred() {
        List<IMObjectState> result = new ArrayList<IMObjectState>();
        boolean processed;
        do {
            processed = false;
            IMObjectState[] states = deferred.toArray(
                    new IMObjectState[0]);
            Set<DeferredUpdater> updaters = new HashSet<DeferredUpdater>();
            for (IMObjectState state : states) {
                Set<DeferredUpdater> set = state.getDeferred();
                if (!set.isEmpty()) {
                    updaters.addAll(set);
                } else {
                    deferred.remove(state);
                    result.add(state);
                }
            }
            if (!updaters.isEmpty()) {
                for (DeferredUpdater updater : updaters) {
                    IMObjectReference ref
                            = context.getReference(updater.getId());
                    if (ref != null) {
                        updater.update(ref, context);
                        processed = true;
                    }
                }
            }
        } while (processed);
        return result;
    }

    /**
     * This method will cache the entity and save it only when the size of
     * the cache reaches the configured batch size.
     *
     * @param state the entity to save
     */
    private void queue(IMObjectState state) {
        IMObject object = state.getObject();
        service.deriveValues(object);
        if (validateOnly) {
            service.validateObject(object);
        } else {
            if (state.getParent() == null) {
                batch.add(object);
                if (batch.size() >= batchSize) {
                    saveBatch();
                }
            }
        }
    }

    private void saveBatch() {
        try {
            service.save(batch);
        } catch (OpenVPMSException exception) {
            // One of the batch failed so nothing saved.
            // Try again one by one logging each error.
            for (IMObject batched : batch) {
                try {
                    service.save(batched);
                } catch (OpenVPMSException e) {
                    log.error("Failed to save object\n" +
                            batched.toString(), e);
                }
            }
        }
        for (IMObject saved : batch) {
            cache.update(saved.getObjectReference());
        }
        batch.clear();
    }
}
