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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.event.impl;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.smartflow.event.EventDispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages a set of {@link QueueDispatcher} instances.
 * <br/>
 * NOTE: whilst multiple practice locations sharing the one Smart Flow Sheet Clinic API Key is supported, the location
 * selected for treatment events is not guaranteed to be the same between restarts or practice location changes.
 * <br/>
 * E.g. give two locations A, and B, with the same API key, the location that is first registered will be the one
 * selected for use.
 *
 * @author Tim Anderson
 */
class QueueDispatchers {

    /**
     * The queue dispatcher factory.
     */
    private final QueueDispatcherFactory factory;

    /**
     * Practice locations keyed on clinic API key.
     */
    private final Map<String, Set<Party>> locationsByKey = new HashMap<>();

    /**
     * Clinic API key keyed on location.
     */
    private final Map<Party, String> keysByLocation = new HashMap<>();

    /**
     * The queue dispatchers, keyed on clinic API key.
     */
    private final Map<String, QueueDispatcher> dispatchers = new HashMap<>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(QueueDispatchers.class);

    /**
     * Constructs a {@link QueueDispatchers}.
     *
     * @param factory the queue dispatcher factory
     */
    public QueueDispatchers(QueueDispatcherFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns the queue dispatchers.
     *
     * @return the queue dispatchers
     */
    public synchronized List<QueueDispatcher> getDispatchers() {
        return new ArrayList<>(dispatchers.values());
    }

    /**
     * Adds a practice location.
     * <p>
     * If the practice location has an API key, this will create a {@link QueueDispatcher} to read the queue.
     * <br/>
     * If the practice location has no API key, any existing {@link QueueDispatcher} will be removed.
     *
     * @param location the practice location
     * @return the queue dispatcher, if one was added
     */
    public synchronized QueueDispatcher add(Party location) {
        QueueDispatcher reader = null;
        String existingKey = keysByLocation.get(location);
        String newKey = (location.isActive()) ? factory.getClinicAPIKey(location) : null;
        if (!ObjectUtils.equals(existingKey, newKey)) {
            if (existingKey == null) {
                if (!StringUtils.isEmpty(newKey)) {
                    reader = addKey(newKey, location);
                }
            } else {
                removeKey(existingKey, location);
                if (!StringUtils.isEmpty(newKey)) {
                    reader = addKey(newKey, location);
                }
            }
        }
        return reader;
    }

    /**
     * Removes the {@link QueueDispatcher} associated with a practice location, if any.
     *
     * @param location the practice location
     */
    public synchronized void remove(Party location) {
        String existingKey = keysByLocation.get(location);
        if (existingKey != null) {
            removeKey(existingKey, location);
        }
    }

    /**
     * Returns the {@link EventDispatcher} associated with a practice location.
     *
     * @param location the practice location
     * @return the event dispatcher, or {@code null} if none is found
     */
    public synchronized EventDispatcher getEventDispatcher(Party location) {
        EventDispatcher result = null;
        String key = keysByLocation.get(location);
        if (key != null) {
            QueueDispatcher dispatcher = dispatchers.get(key);
            if (dispatcher != null) {
                result = dispatcher.getEventDispatcher();
            }
        }
        return result;
    }

    /**
     * Adds an API key for the specified practice location.
     * <p>
     * If the key is not already registered, this creates an {@link EventDispatcher} to handle messages read from
     * the SFS Azure Service Bus queue.
     *
     * @param key      the clinic API key
     * @param location the practice location
     * @return the queue dispatcher, or {@code null} if none was added
     */
    private QueueDispatcher addKey(String key, Party location) {
        QueueDispatcher reader = null;
        try {
            Set<Party> locations = locationsByKey.get(key);
            if (locations == null) {
                locations = new HashSet<>();
                locationsByKey.put(key, locations);
            }
            if (locations.isEmpty()) {
                reader = addDispatcher(key, location);
            }
            if (!locations.contains(location)) {
                if (reader == null) {
                    StringBuilder names = new StringBuilder();
                    for (Party party : locations) {
                        if (names.length() != 0) {
                            names.append(", ");
                        }
                        names.append('\'').append(party.getName()).append('\'');
                    }
                    log.error("Practice location='" + location.getName() + "' shares a Smart Flow Sheet Clinic API Key"
                              + " with: " + names
                              + ".\nThe location associated with any treatments is non-deterministic");
                }
                locations.add(location);
            }
            keysByLocation.put(location, key);
        } catch (Throwable exception) {
            log.error("Failed initialise Smart Flow Sheet queue for location=" + location.getName(), exception);
        }
        return reader;
    }

    /**
     * Adds a dispatcher for the specified practice API key and practice location.
     * <p>
     * NOTE: if a dispatcher exists, it will be replaced. TODO - event listeners aren't supported at present,
     * but this would mean that any listener will no longer receive an event as they aren't transferred.
     *
     * @param key      the API key
     * @param location the practice location
     * @return the queue dispatcher
     */
    private QueueDispatcher addDispatcher(String key, Party location) {
        QueueDispatcher dispatcher = factory.createQueueDispatcher(location);
        dispatchers.put(key, dispatcher);
        return dispatcher;
    }

    /**
     * Removes an an API key for the specified practice location.
     * <br/>
     * This only will destroy the associated {@link QueueDispatcher} when there are no more locations associated with
     * it.
     *
     * @param key      the API key
     * @param location the practice location
     */
    private void removeKey(String key, Party location) {
        boolean reset = false;
        Set<Party> locations = locationsByKey.get(key);
        if (locations != null) {
            if (locations.remove(location)) {
                if (locations.isEmpty()) {
                    reset = true;
                }
            }
        }
        keysByLocation.remove(location);
        if (reset) {
            // no more locations associated with the key, so remove the dispatcher.
            dispatchers.remove(key);
        } else if (locations != null && !locations.isEmpty()) {
            QueueDispatcher reader = dispatchers.get(key);
            if (reader != null && ObjectUtils.equals(reader.getLocation(), location)) {
                // the dispatcher was shared amongst multiple locations. Add a new dispatcher for one of the remaining
                // locations.
                Party nextLocation = getLocation(locations);
                addDispatcher(key, nextLocation);
            }
        }
    }

    /**
     * Returns the location with the lowest identifier from a set of locations.
     *
     * @param locations the locations
     * @return the location with the lowest identifier
     */
    private Party getLocation(Set<Party> locations) {
        List<Party> list = new ArrayList<>(locations);
        if (list.size() > 1) {
            Collections.sort(list, new Comparator<Party>() {
                @Override
                public int compare(Party o1, Party o2) {
                    return Long.compare(o1.getId(), o2.getId());
                }
            });
        }
        return list.get(0);
    }
}
