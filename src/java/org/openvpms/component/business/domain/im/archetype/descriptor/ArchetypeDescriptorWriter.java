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

package org.openvpms.component.business.domain.im.archetype.descriptor;

import org.exolab.castor.mapping.Mapping;

import java.io.OutputStream;

/**
 * Writes {@link ArchetypeDescriptor} and {@link AssertionDescriptor} instances.
 *
 * @author Tim Anderson
 */
public class ArchetypeDescriptorWriter {

    /**
     * The mapping.
     */
    private Mapping mapping;

    /**
     * If {@code true} omit xml declarations.
     */
    private final boolean fragment;

    /**
     * If {@code true}, indent the XML and remove namespace and type information.
     */
    private final boolean prettyPrint;

    /**
     * Constructs an {@link ArchetypeDescriptorWriter}.
     */
    public ArchetypeDescriptorWriter() {
        this(false, false);
    }

    /**
     * Constructs an {@link ArchetypeDescriptorWriter}.
     *
     * @param fragment    if {@code true} omit xml declarations
     * @param prettyPrint if {@code true}, indent the XML and remove namespace and type information
     * @throws DescriptorException if the mapping can't be loaded
     */
    public ArchetypeDescriptorWriter(boolean fragment, boolean prettyPrint) {
        mapping = DescriptorIOHelper.getMapping(ArchetypeDescriptorReader.MAPPING);
        this.fragment = fragment;
        this.prettyPrint = prettyPrint;
    }

    /**
     * Write descriptors to a stream.
     *
     * @param descriptors the descriptors to write
     * @param stream      the stream to write to
     * @throws DescriptorException if the descriptors cannot be written
     */
    public void write(ArchetypeDescriptors descriptors, OutputStream stream) {
        DescriptorIOHelper.write(descriptors, stream, mapping, fragment, prettyPrint);
    }

    /**
     * Writes an assertion descriptor to a stream.
     *
     * @param descriptor the descriptor to write
     * @param stream     the stream to write to
     * @throws DescriptorException if the descriptor cannot be written
     */
    public void write(AssertionDescriptor descriptor, OutputStream stream) {
        DescriptorIOHelper.write(descriptor, stream, mapping, fragment, prettyPrint);
    }
}
