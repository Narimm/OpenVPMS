/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.domain.im.archetype.descriptor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Archetype descriptors.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class ArchetypeDescriptors implements Serializable {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A map of descriptors, keyed on name.
     */
    private HashMap<String, ArchetypeDescriptor> archetypeDescriptors = new HashMap<String, ArchetypeDescriptor>();

    /**
     * Default constructor.
     */
    public ArchetypeDescriptors() {
    }

    /**
     * Returns the descriptors as a map, keyed on short name.
     *
     * @return the descriptors
     */
    public Map<String, ArchetypeDescriptor> getArchetypeDescriptors() {
        return archetypeDescriptors;
    }

    /**
     * Returns the descriptors.
     *
     * @return the descriptors
     */
    public ArchetypeDescriptor[] getArchetypeDescriptorsAsArray() {
        return archetypeDescriptors.values().toArray(new ArchetypeDescriptor[archetypeDescriptors.size()]);
    }

    /**
     * Sets the descriptors.
     *
     * @param descriptors the descriptors to set
     */
    public void setArchetypeDescriptorsAsArray(ArchetypeDescriptor[] descriptors) {
        archetypeDescriptors = new HashMap<String, ArchetypeDescriptor>();
        for (ArchetypeDescriptor descriptor : descriptors) {
            archetypeDescriptors.put(descriptor.getShortName(), descriptor);
        }
    }

    /**
     * Sets the descriptors.
     *
     * @param descriptors the descriptors
     */
    public void setArchetypeDescriptors(HashMap<String, ArchetypeDescriptor> descriptors) {
        this.archetypeDescriptors = descriptors;
    }

    /**
     * Reads descriptors from a stream.
     *
     * @param stream the stream to read from
     * @return the read descriptors
     * @throws DescriptorException if the descriptors cannot be read
     */
    public static ArchetypeDescriptors read(InputStream stream) {
        return new ArchetypeDescriptorReader().read(stream);
    }

    /**
     * Write descriptors to a stream.
     *
     * @param descriptors the descriptors to write
     * @param stream      the stream to write to
     * @throws DescriptorException if the descriptors cannot be written
     */
    public static void write(ArchetypeDescriptors descriptors, OutputStream stream) {
        new ArchetypeDescriptorWriter().write(descriptors, stream);
    }

}
