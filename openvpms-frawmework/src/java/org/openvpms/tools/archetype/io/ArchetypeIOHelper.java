package org.openvpms.tools.archetype.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.Collection;

/**
 * Archetype I/O helper methods.
 *
 * @author Tim Anderson
 */
public class ArchetypeIOHelper {

    /**
     * Returns all .adl files in a directory.
     *
     * @param dir     the directory to search
     * @param recurse if {@code true}, recurse subdirectories
     * @return the archetype files
     */
    @SuppressWarnings("unchecked")
    public static Collection<File> getArchetypeFiles(File dir, boolean recurse) {
        IOFileFilter fileFilter = FileFilterUtils.suffixFileFilter("adl");
        IOFileFilter dirFilter = (recurse) ? TrueFileFilter.INSTANCE : null;
        Collection files = FileUtils.listFiles(dir, fileFilter, dirFilter);
        return (Collection<File>) files;
    }
}
