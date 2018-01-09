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

package org.openvpms.component.system.common.cache;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.object.Reference;

import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of the {@link IMObjectCache} interface that is backed by a {@code Map}.
 * <p>
 * This cache grows until cleared.
 *
 * @author Tim Anderson
 */
public class MapIMObjectCache extends AbstractIMObjectCache {

    /**
     * Constructs a {@link MapIMObjectCache}.
     */
    public MapIMObjectCache() {
        super(new HashMap<>(), null);
    }

    /**
     * Constructs a {@link MapIMObjectCache} that will retrieve objects from the archetype service if they are
     * not cached.
     *
     * @param service the archetype service
     */
    public MapIMObjectCache(IArchetypeService service) {
        super(new HashMap<>(), service);
    }

    /**
     * Constructs a {@link MapIMObjectCache} that will retrieve objects from the archetype service if they are
     * not cached.
     *
     * @param map     the underlying map
     * @param service the archetype service
     */
    public MapIMObjectCache(Map<Reference, IMObject> map, IArchetypeService service) {
        super(map, service);
    }
}
