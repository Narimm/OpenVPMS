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

package org.openvpms.component.business.domain.im.archetype.descriptor;

import org.exolab.castor.mapping.Mapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Archetype descriptor reader.
 *
 * @author Tim Anderson
 */
public class ArchetypeDescriptorReader {

    /**
     * The mapping.
     */
    private Mapping mapping;

    /**
     * The mapping resource path.
     */
    static final String MAPPING
            = "org/openvpms/component/business/domain/im/archetype/descriptor/archetype-mapping-file.xml";


    /**
     * Constructs an {@link ArchetypeDescriptorReader}.
     *
     * @throws DescriptorException if the mapping can't be loaded
     */
    public ArchetypeDescriptorReader() {
        mapping = DescriptorIOHelper.getMapping(MAPPING);
    }

    /**
     * Reads descriptors from a stream.
     *
     * @param stream the stream to read from
     * @return the read descriptors
     * @throws DescriptorException if the descriptors cannot be read
     */
    public ArchetypeDescriptors read(InputStream stream) {
        return read(stream, ArchetypeDescriptors.class);
    }

    /**
     * Reads descriptors from a file.
     *
     * @param file the file to read from
     * @return the read descriptors
     * @throws DescriptorException   if the descriptors cannot be read
     * @throws FileNotFoundException if the file cannot be found
     */
    public ArchetypeDescriptors read(File file) throws FileNotFoundException {
        return ArchetypeDescriptors.class.cast(DescriptorIOHelper.read(file, mapping));
    }

    /**
     * Reads a descriptor from a stream.
     *
     * @param stream the stream to read from
     * @param type   the    descriptor type
     * @return the read descriptor
     * @throws DescriptorException if the descriptor cannot be read
     */
    public <T> T read(InputStream stream, Class<T> type) {
        return type.cast(DescriptorIOHelper.read(stream, mapping));
    }

}
