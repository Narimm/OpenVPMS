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

package org.openvpms.plugin.internal.service.config;

import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.plugin.internal.service.archetype.IMObjectUpdateNotifier;
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.plugin.service.config.ConfigurableService;

/**
 * Monitors {@link ConfigurableService}s, providing them with configuration.
 *
 * @author Tim Anderson
 */
public class PluginConfigurationService extends IMObjectUpdateNotifier<ConfigurableService> {

    /**
     * Constructs a {@link PluginConfigurationService}.
     *
     * @param service the archetype service
     * @param manager the plugin manager
     */
    public PluginConfigurationService(IArchetypeService service, PluginManager manager) {
        super(ConfigurableService.class, service, manager);
    }

    /**
     * Creates a listener.
     *
     * @param service the service to register a listener for
     * @return the listener
     */
    @Override
    protected Listener createListener(final ConfigurableService service) {
        final String archetype = service.getArchetype();
        if (archetype.contains("*")) {
            throw new IllegalStateException(ConfigurableService.class.getSimpleName()
                                            + " archetypes cannot contain wildcards: " + archetype);
        }
        IArchetypeServiceListener delegate = new AbstractArchetypeServiceListener() {
            /**
             * Invoked after an object has been saved and the transaction committed.
             *
             * @param object the saved object
             */
            @Override
            public void saved(org.openvpms.component.business.domain.im.common.IMObject object) {
                onSaved(object, service);
            }

            /**
             * Invoked after an object has been removed and the transaction committed.
             *
             * @param object the removed object
             */
            @Override
            public void removed(org.openvpms.component.business.domain.im.common.IMObject object) {
                onRemoved(object, service);
            }
        };
        IMObject config = getConfig(archetype);
        service.setConfiguration(config);
        String[] archetypes = new String[]{archetype};
        return new Listener(archetypes, delegate);
    }

    /**
     * Invoked when a configuration is saved.
     *
     * @param object  the object
     * @param service the configurable service
     */
    private void onSaved(IMObject object, ConfigurableService service) {
        boolean inactive = false;
        synchronized (service) {
            IMObject current = service.getConfiguration();
            if (current == null || (current.getId() == object.getId()) && current.getVersion() < object.getVersion()) {
                if (object.isActive()) {
                    service.setConfiguration(object);
                } else {
                    inactive = true;
                }
            }
        }
        if (inactive) {
            // the configuration is inactive. Get an active configuration, if one is available, and the service
            // hasn't updated subsequently
            IMObject config = getConfig(object.getArchetype());
            synchronized (service) {
                IMObject current = service.getConfiguration();
                if (current == null || current.getId() == object.getId()) {
                    service.setConfiguration(config);
                }
            }
        }
    }

    /**
     * Invoked when a configuration is removed.
     *
     * @param object  the object
     * @param service the configurable service
     */
    private void onRemoved(IMObject object, ConfigurableService service) {
        synchronized (service) {
            IMObject current = service.getConfiguration();
            if (current != null && current.getId() == object.getId()) {
                service.setConfiguration(getConfig(object.getArchetype()));
            }
        }
    }

    /**
     * Returns a configuration object of the specified archetype.
     *
     * @param archetype the archetype
     * @return the configuration object, or {@code null} if no active instance exists
     */
    private IMObject getConfig(String archetype) {
        ArchetypeQuery query = new ArchetypeQuery(archetype, true);
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<org.openvpms.component.business.domain.im.common.IMObject> iterator
                = new IMObjectQueryIterator<>(getService(), query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }
}
