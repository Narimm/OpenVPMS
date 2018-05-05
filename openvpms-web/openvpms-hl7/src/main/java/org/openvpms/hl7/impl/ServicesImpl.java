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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.MonitoringIMObjectCache;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.service.Services;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of the {@link Services} interface.
 *
 * @author Tim Anderson
 */
public abstract class ServicesImpl extends MonitoringIMObjectCache<Entity> implements Services {

    /**
     * The connectors.
     */
    private final Connectors connectors;

    /**
     * Listeners to notify when a pharmacy changes.
     */
    private final List<Services.Listener> listeners = new ArrayList<Services.Listener>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ServicesImpl.class);

    /**
     * Constructs a {@link ServicesImpl}.
     *
     * @param service    the archetype service
     * @param shortName  the short name to cache
     * @param type       the object types
     * @param prefetch   if {@code true}, pre-load objects from the archetype service
     * @param connectors the connectors
     */
    public ServicesImpl(IArchetypeService service, String shortName, Class<Entity> type, boolean prefetch,
                        Connectors connectors) {
        super(service, shortName, type, prefetch);
        this.connectors = connectors;
    }

    /**
     * Returns the active services.
     *
     * @return the service configurations
     */
    @Override
    public List<Entity> getServices() {
        return getObjects();
    }

    /**
     * Returns a service given its reference.
     *
     * @param reference the service reference
     * @return the service configuration, or {@code null} if none is found
     */
    @Override
    public Entity getService(IMObjectReference reference) {
        return getObject(reference);
    }

    /**
     * Returns the service for a practice location, given the service group.
     *
     * @param group    the service group
     * @param location the practice location
     * @return the service configuration, or {@code null} if none is found
     */
    @Override
    public Entity getService(Entity group, IMObjectReference location) {
        EntityBean bean = new EntityBean(group, getService());
        for (IMObjectReference ref : bean.getNodeTargetEntityRefs("services")) {
            Entity service = getService(ref);
            if (service != null && hasLocation(service, location)) {
                return service;
            }
        }
        return null;
    }

    /**
     * Returns the connector to send messages to the service.
     *
     * @param service the service
     * @return the corresponding sender, or {@code null} if none is found
     */
    @Override
    public Connector getSender(Entity service) {
        EntityBean bean = new EntityBean(service, getService());
        IMObjectReference ref = bean.getNodeTargetObjectRef("sender");
        return (ref != null) ? connectors.getConnector(ref) : null;
    }

    /**
     * Adds a listener to be notified of pharmacy updates.
     *
     * @param listener the listener to add
     */
    public void addListener(Services.Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(Services.Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Invoked when an object is added to the cache.
     *
     * @param object the added object
     */
    @Override
    protected void added(Entity object) {
        for (Services.Listener listener : getListeners()) {
            try {
                listener.added(object);
            } catch (Throwable exception) {
                log.error(exception, exception);
            }
        }
    }

    /**
     * Invoked when an object is removed from the cache.
     *
     * @param object the removed object
     */
    @Override
    protected void removed(Entity object) {
        for (Services.Listener listener : getListeners()) {
            try {
                listener.removed(object);
            } catch (Throwable exception) {
                log.error(exception, exception);
            }
        }
    }

    /**
     * Returns the listeners.
     *
     * @return the listeners
     */
    protected Services.Listener[] getListeners() {
        Services.Listener[] result;
        synchronized (listeners) {
            result = listeners.toArray(new Services.Listener[listeners.size()]);
        }
        return result;
    }

    /**
     * Determines if a service is used for a particular practice location.
     *
     * @param service  the service
     * @param location the location
     * @return {@code true} if the service is used to for the location
     */
    private boolean hasLocation(Entity service, IMObjectReference location) {
        EntityBean bean = new EntityBean(service, getService());
        return ObjectUtils.equals(location, bean.getNodeTargetObjectRef("location"));
    }
}
