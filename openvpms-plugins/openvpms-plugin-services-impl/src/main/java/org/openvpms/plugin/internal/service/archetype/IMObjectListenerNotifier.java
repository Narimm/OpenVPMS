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

package org.openvpms.plugin.internal.service.archetype;


import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.plugin.service.archetype.IMObjectListener;

/**
 * Notifies {@link IMObjectListener} when objects are updated or removed.
 *
 * @author Tim Anderson
 */
public class IMObjectListenerNotifier extends IMObjectUpdateNotifier<IMObjectListener> {

    /**
     * Constructs an {@link IMObjectListenerNotifier}.
     *
     * @param service the archetype service
     * @param manager the plugin manager
     */
    public IMObjectListenerNotifier(IArchetypeService service, PluginManager manager) {
        super(IMObjectListener.class, service, manager);
    }

    /**
     * Creates a listener.
     *
     * @param service the service to register a listener for
     * @return the listener
     */
    @Override
    protected Listener createListener(final IMObjectListener service) {
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
        return new Listener(service.getArchetypes(), delegate);
    }
}
