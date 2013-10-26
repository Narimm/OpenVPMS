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

import java.io.IOException;
import java.util.Map;

/**
 * Loads descriptors.
 *
 * @author Tim Anderson
 */
public interface DescriptorLoader {

    /**
     * Loads descriptors.
     *
     * @return the descriptors
     * @throws IOException for any I/O error
     */
    Map<String, ArchetypeDescriptor> getDescriptors() throws IOException;

    /**
     * Determines if the returned descriptors are all available descriptors, or a subset.
     *
     * @return {@code true} if the returned descriptors are all available descriptors, {@code false} if they are
     *         a subset
     */
    boolean isAll();

    /**
     * Returns a string representation of the descriptor source.
     *
     * @return a string representation of the descriptor source
     */
    String toString();
}
