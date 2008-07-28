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
import java.util.LinkedHashMap;
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

    private LoadContext context;
    private final IdRefCache cache;

    private final IArchetypeService service;

    private int batchSize;
    private Map<IMObjectReference, LoadState> batch
            = new LinkedHashMap<IMObjectReference, LoadState>();
    private Map<IMObjectReference, IMObject> children
            = new LinkedHashMap<IMObjectReference, IMObject>();
    private final boolean verbose;

    private final boolean validateOnly;

    /**
     * Maintains a list of archetypes and count to indicate the number of
     * each saved or validated
     */
    private final Map<String, Long> statistics;


    private List<LoadState> deferred = new ArrayList<LoadState>();

    private final Log log = LogFactory.getLog(DataLoader.class);


    public DataLoader(int batchSize) {
        this(new IdRefCache(), ArchetypeServiceHelper.getArchetypeService(),
             false, false, batchSize, new HashMap<String, Long>());
    }

    public DataLoader(IdRefCache cache,
                      IArchetypeService service, boolean verbose,
                      boolean validateOnly, int batchSize,
                      Map<String, Long> statistics) {
        this.cache = cache;
        this.service = service;
        this.verbose = verbose;
        this.validateOnly = validateOnly;
        this.batchSize = batchSize;
        context = new LoadContext(service, cache, validateOnly);
        this.statistics = statistics;
    }

    public void load(XMLStreamReader reader, String path)
            throws XMLStreamException {

        Stack<LoadState> stack = new Stack<LoadState>();
        for (int event = reader.next();
             event != XMLStreamConstants.END_DOCUMENT;
             event = reader.next()) {
            LoadState current;
            switch (event) {
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    String elementName = reader.getLocalName();
                    if ("data".equals(elementName)) {
                        startData(reader, stack, path);
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
                        log.info("[END PROCESSING element="
                                + reader.getLocalName() + "]");
                    }
                    break;

                default:
                    break;
            }
        }
    }

    public void flush() {
        processDeferred();
        for (LoadState state : batch.values()) {
            Set<IMObjectReference> unsaved = state.getUnsaved();
            if (!unsaved.isEmpty()) {
                for (IMObjectReference ref : unsaved) {
                    if (!batch.containsKey(ref) && !children.containsKey(ref)) {
                        String id = cache.getId(ref);
                        if (id == null) {
                            id = "<unset>";
                        }
                        log.warn("Cannot save object, archetype="
                                + state.getArchetypeId().getShortName()
                                + " from path=" + state.getPath()
                                + ", line=" + state.getLineNumber()
                                + ": requires " + unsaved + ", id=" + id);
                    }

                }
            }
        }
        saveBatch();
    }


    private void startData(XMLStreamReader reader, Stack<LoadState> stack,
                           String path) {
        LoadState current;
        Element element = new Element(reader);
        if (verbose) {
            String archetypeId = stack.isEmpty() ? "none"
                    : stack.peek().getArchetypeId().toString();
            log.info("[START PROCESSING element, parent="
                    + archetypeId + "]" + element);
        }

        try {
            if (!stack.isEmpty()) {
                current = processElement(stack.peek(), element, path);
            } else {
                current = processElement(null, element, path);
            }
            stack.push(current);
        } catch (Exception exception) {
            Location location = reader.getLocation();
            log.error("Error in start element, line "
                    + location.getLineNumber()
                    + ", column " + location.getColumnNumber() +
                    "" + element + "", exception);
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
    private LoadState processElement(LoadState parent,
                                         Element element,
                                         String path) {
        LoadState result;

        Location location = element.getLocation();
        String collectionNode = element.getCollection();

        // if a childId node is defined then we can skip the create object
        // process since the object already exists
        String childId = element.getChildId();
        if (StringUtils.isEmpty(childId)) {
            result = create(element, parent, path);
            for (Map.Entry<String, String> attribute
                    : element.getAttributes().entrySet()) {
                String name = attribute.getKey();
                String value = attribute.getValue();
                result.setValue(name, value, context);
            }
            if (collectionNode != null) {
                if (parent == null) {
                    throw new ArchetypeDataLoaderException(
                            NoParentForChild, location.getLineNumber(),
                            location.getColumnNumber());
                }
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

    private LoadState create(Element element, LoadState parent,
                                 String path) {
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
        return new LoadState(parent, object, descriptor, path,
                             element.getLocation().getLineNumber());
    }

    /**
     * Load the specified object.
     */
    private void load(LoadState state) {
        if (!state.isComplete()) {
            deferred.add(state);
        } else {
            boolean updateDeferred = !deferred.isEmpty();
            IMObject object = state.getObject();
            if (state.getParent() == null) {
                queue(state);
            } else {
                children.put(object.getObjectReference(), object);
            }

            // update the stats
            String shortName = state.getArchetypeId().getShortName();
            Long count = statistics.get(shortName);
            if (count == null) {
                statistics.put(shortName, 1L);
            } else {
                statistics.put(shortName, count + 1);
            }

            if (verbose) {
                log.info("[CREATED]" + object);
            }

            if (updateDeferred) {
                processDeferred();
            }
        }
    }

    private void processDeferred() {
        boolean processed;
        do {
            processed = false;
            LoadState[] states = deferred.toArray(new LoadState[0]);
            Set<DeferredUpdater> updaters = new HashSet<DeferredUpdater>();
            for (LoadState state : states) {
                Set<DeferredUpdater> set = state.getDeferred();
                if (!set.isEmpty()) {
                    updaters.addAll(set);
                } else {
                    deferred.remove(state);
                    queue(state);
                }
            }
            if (!updaters.isEmpty()) {
                for (DeferredUpdater updater : updaters) {
                    IMObjectReference ref
                            = context.getReference(updater.getId());
                    if (ref != null) {
                        if (ref.isNew() && batch.containsKey(ref)) {
                            ref = attemptSave(ref);
                        }
                        if (updater.update(ref, context)) {
                            processed = true;
                        }
                    }
                }
            }
        } while (processed);
    }

    private IMObjectReference attemptSave(IMObjectReference ref) {

        LoadState state = batch.get(ref);
        if (state != null && state.isComplete()) {
            try {
                IMObject object = state.getObject();
                service.save(object);
                batch.remove(ref);
                saved(object.getObjectReference());
            } catch (OpenVPMSException exception) {
                // ignore
            }
        }
        return ref;
    }

    private void saved(IMObjectReference ref) {
        cache.update(ref);
        for (LoadState state : batch.values()) {
            state.update(ref);
        }
    }

    /**
     * This method will cache the entity and save it only when the size of
     * the cache reaches the configured batch size.
     *
     * @param state the entity to save
     */
    private void queue(LoadState state) {
        IMObject object = state.getObject();
        service.deriveValues(object);
        if (validateOnly) {
            service.validateObject(object);
        } else {
            batch.put(object.getObjectReference(), state);
            if (batch.size() >= batchSize && isComplete()) {
                saveBatch();
            }
        }
    }

    private boolean isComplete() {
        Set<IMObjectReference> unsaved = new HashSet<IMObjectReference>();
        for (LoadState state : batch.values()) {
            unsaved.addAll(state.getUnsaved());
        }
        for (IMObjectReference ref : unsaved) {
            if (!batch.containsKey(ref) && !children.containsKey(ref)) {
                return false;
            }
        }
        return true;
    }

    private void saveBatch() {
        List<IMObject> objects = new ArrayList<IMObject>();
        for (LoadState state : batch.values()) {
            objects.add(state.getObject());
        }
        try {
            service.save(objects);
        } catch (OpenVPMSException exception) {
            // One of the batch failed so nothing saved.
            // Try again one by one logging each error.
            log.error("Failed to save batch. Attempting to save objects "
                    + "individually", exception);
            for (IMObject object : objects) {
                try {
                    service.save(object);
                } catch (OpenVPMSException e) {
                    LoadState state
                            = batch.get(object.getObjectReference());
                    log.error("Failed to save object, archetype="
                            + object.getArchetypeId().getShortName()
                            + " from path=" + state.getPath()
                            + ", line=" + state.getLineNumber(), e);
                }
            }
        }
        for (IMObject object : objects) {
            saved(object.getObjectReference());
        }
        batch.clear();
        objects.clear();

        for (IMObject child : children.values().toArray(new IMObject[0])) {
            if (!child.isNew()) {
                IMObjectReference ref = child.getObjectReference();
                saved(ref);
                children.remove(ref);
            }
        }
    }
}
