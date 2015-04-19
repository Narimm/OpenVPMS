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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * Loads data from an XML stream.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class DataLoader {

    /**
     * The load context.
     */
    private LoadContext context;

    /**
     * The load cache.
     */
    private final LoadCache cache;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The batch size.
     */
    private int batchSize;

    /**
     * Objects to be saved, keyed on their references.
     */
    private Map<IMObjectReference, LoadState> queue
            = new LinkedHashMap<IMObjectReference, LoadState>();

    /**
     * If <tt>true</tt> perform verbose logging.
     */
    private final boolean verbose;

    /**
     * Determines if only validation should be performed, i.e no saving of data
     */
    private final boolean validateOnly;

    /**
     * Maintains a map of archetypes and object count to indicate the number of
     * each saved or validated.
     */
    private final Map<String, Long> statistics;

    /**
     * Object's whose save has been deferred as they aren't complete.
     */
    private List<LoadState> deferred = new ArrayList<LoadState>();

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(DataLoader.class);


    /**
     * Creates a new <tt>DataLoader</tt>.
     *
     * @param batchSize the batch size
     */
    public DataLoader(int batchSize) {
        this(new LoadCache(), ArchetypeServiceHelper.getArchetypeService(),
             false, false, batchSize, new HashMap<String, Long>());
    }

    /**
     * Creates a new <tt>DataLoader</tt>.
     *
     * @param cache        the load cache
     * @param service      the archetype service
     * @param verbose      if <tt>true</tt> perform verbose logging
     * @param validateOnly if <tt>true</tt> only validate, don't save
     * @param batchSize    the batch size
     * @param statistics   the statistucs
     */
    public DataLoader(LoadCache cache, IArchetypeService service,
                      boolean verbose, boolean validateOnly, int batchSize,
                      Map<String, Long> statistics) {
        this.cache = cache;
        this.service = service;
        this.verbose = verbose;
        this.validateOnly = validateOnly;
        this.batchSize = batchSize;
        context = new LoadContext(service, cache, validateOnly);
        this.statistics = statistics;
    }

    /**
     * Loads data from a stream.
     *
     * @param reader the stream reader
     * @param path   a path representing the stream source, for logging purposes
     * @throws XMLStreamException for any stream error
     */
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
                                ErrorInStartElement);
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

    /**
     * Attempts to save any unsaved objects.
     */
    public void flush() {
        save();
        while (processDeferred()) {
            save();
        }
    }

    /**
     * Closes the loader, flusing any pending objects.
     */
    public void close() {
        flush();
        for (LoadState state : queue.values()) {
            Set<IMObjectReference> unsaved = state.getUnsaved();
            if (!unsaved.isEmpty()) {
                for (IMObjectReference ref : unsaved) {
                    if (!queue.containsKey(ref)) {
                        String id = cache.getId(ref);
                        if (id == null) {
                            id = "<unset>";
                        }
                        log.error("Cannot save object, archetype="
                                + state.getArchetypeId().getShortName()
                                + " from path=" + state.getPath()
                                + ", line=" + state.getLineNumber()
                                + ": requires " + unsaved + ", id=" + id);
                    }
                }
            }
        }
        for (LoadState state : deferred) {
            for (DeferredUpdater deferred : state.getDeferred()) {
                log.error("Cannot save object, archetype="
                        + state.getArchetypeId().getShortName()
                        + " from path=" + state.getPath()
                        + ", line=" + state.getLineNumber()
                        + ": requires id=" + deferred.getId());
            }
        }
    }

    /**
     * Returns the statistics.
     * <p/>
     * This is a map of archetypes to their object count. to indicate the number
     * of each saved or validated.
     *
     * @return the statistics
     */
    public Map<String, Long> getStatistics() {
        return statistics;
    }

    /**
     * Returns the load cache.
     *
     * @return the load cache
     */
    public LoadCache getLoadCache() {
        return cache;
    }

    /**
     * Starts a data element, pushing it on the stack.
     *
     * @param reader the reader
     * @param stack  the stack of load states
     * @param path   a path representing the stream source, for logging purposes
     */
    private void startData(XMLStreamReader reader, Stack<LoadState> stack,
                           String path) {
        LoadState current;
        Data data = new Data(reader);
        if (verbose) {
            String archetypeId = stack.isEmpty() ? "none"
                    : stack.peek().getArchetypeId().toString();
            log.info("[START PROCESSING element, parent="
                    + archetypeId + "]" + data);
        }

        try {
            if (!stack.isEmpty()) {
                current = processData(stack.peek(), data, path);
            } else {
                current = processData(null, data, path);
            }
            stack.push(current);
        } catch (Exception exception) {
            Location location = reader.getLocation();
            log.error("Error in start element, line "
                    + location.getLineNumber()
                    + ", column " + location.getColumnNumber() +
                    "" + data + "", exception);
        }
    }

    /**
     * Process the specified data. If the parent object is specified then the
     * specified element is in a parent-child relationship.
     *
     * @param parent the parent object. May be <tt>null</tt>
     * @param data   the data to process
     * @param path   a path representing the data source, for logging purposes
     * @return the load state corresponding to the data
     */
    private LoadState processData(LoadState parent, Data data, String path) {
        LoadState result;

        Location location = data.getLocation();
        String collectionNode = data.getCollection();

        // if a childId node is defined then we can skip the create object
        // process since the object already exists
        String childId = data.getChildId();
        if (StringUtils.isEmpty(childId)) {
            result = create(data, parent, path);
            for (Map.Entry<String, String> attribute
                    : data.getAttributes().entrySet()) {
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

    /**
     * Creates a new load state for the specified data.
     *
     * @param data   the data
     * @param parent the parent state. May be <tt>null</tt>
     * @param path   a path representing the data source, for logging purposes
     * @return a new load state
     */
    private LoadState create(Data data, LoadState parent,
                             String path) {
        String shortName = data.getShortName();
        ArchetypeDescriptor descriptor
                = service.getArchetypeDescriptor(shortName);
        Location location = data.getLocation();
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
        cache.add(object, data.getId());
        return new LoadState(parent, object, descriptor, path,
                             data.getLocation().getLineNumber());
    }

    /**
     * Load the specified object.
     *
     * @param state the state to load
     */
    private void load(LoadState state) {
        if (!state.isComplete()) {
            deferred.add(state);
        } else {
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
                log.info("[CREATED]" + object);
            }
        }
    }

    /**
     * This method will queue the state and save it only when the size of
     * the queue reaches the batch size.
     *
     * @param state the object to save
     */
    private void queue(LoadState state) {
        IMObject object = state.getObject();
        service.deriveValues(object);
        if (validateOnly) {
            service.validateObject(object);
        } else {
            queue.put(object.getObjectReference(), state);
            if (queue.size() >= batchSize) {
                save();
                processDeferred();
            }
        }
    }

    /**
     * Saves all queued objects that have all their dependencies met.
     */
    private void save() {
        LoadState[] objects = queue.values().toArray(new LoadState[0]);
        Map<IMObjectReference, IMObject> batch
                = new HashMap<IMObjectReference, IMObject>();

        // collect all unsaved objects whose dependencies are present
        for (LoadState state : objects) {
            IMObject object = state.getObject();
            IMObjectReference ref = object.getObjectReference();
            if (!batch.containsKey(ref)) {
                Map<IMObjectReference, IMObject> attempt
                        = new HashMap<IMObjectReference, IMObject>(batch);
                if (getPending(ref, attempt)) {
                    batch = attempt;
                }
            }
        }
        if (!batch.isEmpty()) {
            try {
                save(batch);
            } catch (OpenVPMSException exception) {
                // One of the batch failed so nothing saved.
                // Try again one by one logging each error.
                for (LoadState state : objects) {
                    IMObject object = state.getObject();
                    IMObjectReference ref = object.getObjectReference();
                    batch = new HashMap<IMObjectReference, IMObject>();
                    if (getPending(ref, batch)) {
                        try {
                            save(batch);
                        } catch (OpenVPMSException e) {
                            Set<IMObjectReference> unsaved = batch.keySet();
                            queue.keySet().removeAll(unsaved);
                            log.error("Failed to save object, archetype="
                                    + object.getArchetypeId().getShortName()
                                    + " from path=" + state.getPath()
                                    + ", line=" + state.getLineNumber(), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes any deferred objects.
     *
     * @return <tt>true</tt> if any objects were processed
     */
    private boolean processDeferred() {
        boolean result = false;
        while (true) {
            boolean processed = false;
            LoadState[] states = deferred.toArray(new LoadState[0]);
            for (LoadState state : states) {
                Collection<DeferredUpdater> updaters = state.getDeferred();
                if (!updaters.isEmpty()) {
                    for (DeferredUpdater updater
                            : updaters.toArray(new DeferredUpdater[0])) {
                        String id = updater.getId();
                        IMObjectReference ref = context.getReference(id);
                        if (ref != null && updater.update(ref, context)) {
                            processed = true;
                            result = true;
                        }
                    }
                } else {
                    deferred.remove(state);
                    queue(state);
                    result = true;
                }
            }
            if (!processed) {
                break;
            }
        }
        return result;
    }

    /**
     * Collects all queued objects required by the specified reference,
     * adding them to <tt>objects</tt>.
     *
     * @param ref     the reference
     * @param objects the unsaved objects
     * @return <tt>true</tt> if all unsaved objects could be resolved
     */
    private boolean getPending(IMObjectReference ref,
                               Map<IMObjectReference, IMObject> objects) {
        boolean resolved = true;
        LoadState state = queue.get(ref);
        if (state != null) {
            objects.put(ref, state.getObject());
            for (IMObjectReference unsaved : state.getUnsaved()) {
                if (!objects.containsKey(unsaved)) {
                    if (!getPending(unsaved, objects)) {
                        resolved = false;
                        break;
                    }
                }
            }
        } else {
            resolved = false;
        }
        return resolved;
    }

    /**
     * Saves a collection of objects, keyed on their references.
     *
     * @param objects the objects to save
     */
    private void save(Map<IMObjectReference, IMObject> objects) {
        Set<IMObjectReference> saved = objects.keySet();
        service.save(objects.values());
        this.queue.keySet().removeAll(saved);

        // now update the references of any queued object that need the
        // saved references.
        for (IMObject object : objects.values()) {
            IMObjectReference reference = object.getObjectReference();
            cache.update(reference);
            for (LoadState state : queue.values()) {
                if (state.getUnsaved().contains(reference)) {
                    state.update(reference);
                }
            }
        }
    }

}
