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
     * The current batch of unsaved objects, keyed on their references.
     */
    private Map<IMObjectReference, LoadState> batch
            = new LinkedHashMap<IMObjectReference, LoadState>();

    /**
     * The child objects of objects in the batch, keyed on their references.
     */
    private Map<IMObjectReference, IMObject> children
            = new LinkedHashMap<IMObjectReference, IMObject>();

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

    /**
     * Processes any deferred objects.
     */
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

    /**
     * Attempts to save the object associated with the specified reference.
     *
     * @param reference the object reference
     * @return the updated reference, if the object saved successfully, or
     *         <tt>reference</tt> if the save failed
     */
    private IMObjectReference attemptSave(IMObjectReference reference) {

        LoadState state = batch.get(reference);
        if (state != null && state.isComplete()) {
            try {
                IMObject object = state.getObject();
                service.save(state.getObjects());
                batch.remove(reference);
                saved(object.getObjectReference()); // ref now contains id
            } catch (OpenVPMSException ignore) {
                // ignore
            }
        }
        return reference;
    }

    /**
     * Invoked when an object is saved. Updates the references of any batched
     * states that depend on the reference.
     *
     * @param reference the saved reference
     */
    private void saved(IMObjectReference reference) {
        cache.update(reference);
        for (LoadState state : batch.values()) {
            state.update(reference);
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
            batch.put(object.getObjectReference(), state);
            if (batch.size() >= batchSize && isComplete()) {
                saveBatch();
            }
        }
    }

    /**
     * Determines if the batch can be saved.
     *
     * @return <tt>true</tt> if the batch can be saved
     */
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

    /**
     * Saves the batch.
     */
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
