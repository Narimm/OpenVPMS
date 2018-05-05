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

import org.apache.commons.collections4.map.ReferenceMap;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * An {@link IMObjectCache} that allows objects to be reclaimed by the garbage collector if they are not referenced by
 * any other object.
 *
 * @author Tim Anderson
 */
public class SoftRefIMObjectCache extends AbstractIMObjectCache {

    /**
     * Constructs a {@link SoftRefIMObjectCache}.
     */
    public SoftRefIMObjectCache(IArchetypeService service) {
        super(new ReferenceMap<>(), service);
    }

}
