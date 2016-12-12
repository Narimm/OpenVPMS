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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Handles moving files from to target or error directories on load success or failure.
 *
 * @author Tim Anderson
 */
public class FileStrategy {

    /**
     * The target directory.
     */
    private final File target;

    /**
     * The error directory.
     */
    private final File error;

    /**
     * Determines the behaviour if a file exists.
     */
    private final boolean renameDuplicates;

    /**
     * Constructs a {@link FileStrategy}.
     *
     * @param target           the directory to move files to, on success. May be {@code null}
     * @param error            the directory to move files to, on error. May be {@code null}
     * @param renameDuplicates if {@code true}, rename duplicates if a file exists on move
     */
    public FileStrategy(File target, File error, boolean renameDuplicates) {
        this.target = target;
        this.error = error;
        this.renameDuplicates = renameDuplicates;
    }

    /**
     * Returns the target directory.
     *
     * @return the target directory. May be {@code null}
     */
    public File getTarget() {
        return target;
    }

    /**
     * Returns the error directory.
     *
     * @return the target directory. May be {@code null}
     */
    public File getError() {
        return error;
    }

    /**
     * Determines if duplicates are being renamed.
     *
     * @return {@code true} if duplicates are being renamed
     */
    public boolean getRenameDuplicates() {
        return renameDuplicates;
    }

    /**
     * Invoked when a file is loaded.
     * <p/>
     * If a target directory is configured, the file will be moved to it.
     *
     * @param file the file
     * @return the new location of the file
     * @throws IOException if the file cannot be moved
     */
    public File loaded(File file) throws IOException {
        File result;
        if (target != null) {
            result = move(file, target);
        } else {
            result = file;
        }
        return result;
    }

    /**
     * Moves a file to the error directory, if one is configured.
     *
     * @param file the file to move
     * @return the new location of the file
     */
    public File error(File file) throws IOException {
        File result;
        if (error != null) {
            result = move(file, error);
        } else {
            result = file;
        }
        return result;
    }

    /**
     * Moves a file to a directory.
     *
     * @param file the file
     * @param dir  the directory to move to
     * @return the new location of the file
     * @throws IOException for any I/O error
     */
    private File move(File file, File dir) throws IOException {
        File target = new File(dir, file.getName());
        if (target.exists()) {
            if (renameDuplicates) {
                target = getUniqueFile(target);
            } else {
                throw new IOException("Cannot copy " + file.getPath() + " to " + dir.getPath() + ": file exists");
            }
        }
        Files.move(file.toPath(), target.toPath());
        return target;
    }

    /**
     * Generates a unique file.
     *
     * @param file the file
     * @return a unique file
     */
    private File getUniqueFile(File file) {
        File parent = file.getParentFile();
        String baseName = FilenameUtils.getBaseName(file.getName());
        String ext = FilenameUtils.getExtension(file.getName());
        int id = 1;
        while ((file = new File(parent, getName(baseName, id, ext))).exists()) {
            ++id;
        }
        return file;
    }

    /**
     * Returns a file name given a base name, index and extension.
     *
     * @param baseName the base name
     * @param ext      the extension. May be {@code null}
     * @return a file name
     */
    private String getName(String baseName, int index, String ext) {
        String result = baseName + "(" + index + ")";
        return (!StringUtils.isEmpty(ext)) ? result + "." + ext : result;
    }

}
