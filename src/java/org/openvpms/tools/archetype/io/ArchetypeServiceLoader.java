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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.tools.archetype.io;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads archetype descriptors from the archetype service.
 *
 * @author Tim Anderson
 */
public class ArchetypeServiceLoader implements DescriptorLoader {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an {@link ArchetypeServiceLoader}.
     *
     * @param service the archetype service
     */
    public ArchetypeServiceLoader(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Loads descriptors.
     *
     * @return the descriptors
     * @throws IOException for any I/O error
     */
    @Override
    public Map<String, ArchetypeDescriptor> getDescriptors() throws IOException {
        Map<String, ArchetypeDescriptor> result = new HashMap<String, ArchetypeDescriptor>();
        for (ArchetypeDescriptor descriptor : service.getArchetypeDescriptors()) {
            result.put(descriptor.getShortName(), descriptor);
        }
        return result;
    }

    /**
     * Determines if the returned descriptors are all available descriptors, or a subset.
     *
     * @return true
     */
    @Override
    public boolean isAll() {
        return true;
    }

    /**
     * Returns a string representation of the descriptor source.
     *
     * @return a string representation of the descriptor source
     */
    @Override
    public String toString() {
        return "database";
    }
}
