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

package org.openvpms.plugin.service.internal.config;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.plugin.service.config.ConfigurableService;
import org.openvpms.plugin.service.internal.archetype.AbstractIMObjectListenerNotifier;

/**
 * Monitors {@link ConfigurableService}s, providing them with configuration.
 *
 * @author Tim Anderson
 */
public class PluginConfigurationService extends AbstractIMObjectListenerNotifier<ConfigurableService> {

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
        IArchetypeServiceListener delegate = new AbstractArchetypeServiceListener() {
            /**
             * Invoked after an object has been saved and the transaction committed.
             *
             * @param object the saved object
             */
            @Override
            public void saved(IMObject object) {
                service.updated(object);
            }

            /**
             * Invoked after an object has been removed and the transaction committed.
             *
             * @param object the removed object
             */
            @Override
            public void removed(IMObject object) {
                service.removed(object);
            }
        };
        String archetype = service.getArchetype();
        if (archetype.contains("*")) {
            throw new IllegalStateException(ConfigurableService.class.getSimpleName()
                                            + " archetypes cannot contain wildcards: " + archetype);
        }
        IMObject config = getConfig(archetype);
        service.updated(config);
        String[] archetypes = new String[]{archetype};
        return new Listener(archetypes, delegate);
    }

    private IMObject getConfig(String archetype) {
        ArchetypeQuery query = new ArchetypeQuery(archetype, false);
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<IMObject> iterator = new IMObjectQueryIterator<>(getService(), query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }
}
