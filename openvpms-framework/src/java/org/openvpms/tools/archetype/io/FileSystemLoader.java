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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptorReader;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Loads archetype descriptors from the file system.
 *
 * @author Tim Anderson
 */
public class FileSystemLoader implements DescriptorLoader {

    /**
     * The path to load from. May be a file or directory.
     */
    private final File path;

    /**
     * If {@code true}, recurse sub-directories.
     */
    private boolean recurse;

    /**
     * The archetype descriptor reader.
     */
    private final ArchetypeDescriptorReader reader;


    /**
     * Constructs an {@link FileSystemLoader}.
     *
     * @param path    the path to load from. May be a file or directory
     * @param recurse if {@code true}, recurse sub-directories
     */
    public FileSystemLoader(String path, boolean recurse) {
        this.path = new File(path);
        this.recurse = recurse;
        reader = new ArchetypeDescriptorReader();
    }

    /**
     * Loads descriptors.
     *
     * @return the descriptors
     * @throws IOException for any I/O error
     */
    public Map<String, ArchetypeDescriptor> getDescriptors() throws IOException {
        Map<String, ArchetypeDescriptor> result = new HashMap<String, ArchetypeDescriptor>();
        if (path.isDirectory()) {
            Collection<File> files = ArchetypeIOHelper.getArchetypeFiles(path, recurse);
            for (File file : files) {
                read(file, result);
            }
        } else {
            read(path, result);
        }
        return result;
    }

    /**
     * Determines if the returned descriptors are all available descriptors, or a subset.
     *
     * @return {@code true} if the returned descriptors are all available descriptors, {@code false} if they are
     *         a subset
     */
    @Override
    public boolean isAll() {
        return path.isDirectory();
    }

    /**
     * Returns a string representation of the descriptor source.
     *
     * @return a string representation of the descriptor source
     */
    public String toString() {
        return path.toString();
    }

    /**
     * Reads archetype descriptors from a file.
     *
     * @param file   the file to read from
     * @param result the map to add to
     * @throws FileNotFoundException if the file is not found
     */
    private void read(File file, Map<String, ArchetypeDescriptor> result) throws FileNotFoundException {
        ArchetypeDescriptors descriptors = reader.read(new FileInputStream(file));
        for (ArchetypeDescriptor descriptor : descriptors.getArchetypeDescriptorsAsArray()) {
            result.put(descriptor.getShortName(), descriptor);
        }
    }

}
