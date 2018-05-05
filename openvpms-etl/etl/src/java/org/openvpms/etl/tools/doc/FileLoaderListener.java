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

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link LoaderListener} that tracks files.
 *
 * @author Tim Anderson
 */
public class FileLoaderListener extends DelegatingLoaderListener {

    /**
     * Map of loaded files, to their corresponding act identifiers.
     */
    private final Map<File, Long> loaded = new LinkedHashMap<>();

    /**
     * Map of already loaded files, to their corresponding act identifiers.
     */
    private final Map<File, Long> alreadyLoaded = new LinkedHashMap<>();

    /**
     * Map of files missing acts, to their corresponding act identifiers.
     */
    private final Map<File, Long> missingAct = new LinkedHashMap<>();

    /**
     * Map of files in error, to their error messages.
     */
    private final Map<File, String> errors = new LinkedHashMap<>();


    /**
     * Constructs a {@link FileLoaderListener}.
     *
     * @param listener the listener to delegate to
     */
    public FileLoaderListener(LoaderListener listener) {
        super(listener);
    }

    /**
     * Returns the loaded files and their corresponding act identifiers, in the order that they were loaded.
     *
     * @return the files
     */
    public Map<File, Long> getLoadedFiles() {
        return loaded;
    }

    /**
     * Returns the already loaded files and their corresponding act identifiers, in the order that they were
     * processed.
     *
     * @return the files
     */
    public Map<File, Long> getAlreadyLoadedFiles() {
        return alreadyLoaded;
    }

    /**
     * Returns the files that have no corresponding acts, and their act identifiers, in the order that they were
     * processed.
     *
     * @return the files
     */
    public Map<File, Long> getMissingActFiles() {
        return missingAct;
    }

    /**
     * Returns the files that are in error, and their corresponding error messages in the order that they were
     * processed.
     *
     * @return the files
     */
    public Map<File, String> getErrorFiles() {
        return errors;
    }

    /**
     * Notifies when a file is loaded.
     *
     * @param file   the original location of the file
     * @param id     the corresponding act identifier
     * @param target the new location of the file
     */
    @Override
    public void loaded(File file, long id, File target) {
        super.loaded(file, id, target);
        loaded.put(target, id);
    }

    /**
     * Notifies that a file couldn't be loaded as it or another file had already been processed.
     *
     * @param file   the original location of the file
     * @param id     the corresponding act identifier
     * @param target the new location of the file
     */
    @Override
    public void alreadyLoaded(File file, long id, File target) {
        super.alreadyLoaded(file, id, target);
        alreadyLoaded.put(target, id);
    }

    /**
     * Notifies that a file couldn't be loaded as there was no corresponding act.
     *
     * @param file   the original location of the file
     * @param id     the corresponding act identifier
     * @param target the new location of the file
     */
    @Override
    public void missingAct(File file, long id, File target) {
        super.missingAct(file, id, target);
        missingAct.put(target, id);
    }

    /**
     * Notifies that a file couldn't be loaded due to error.
     *
     * @param file      the original location of the file
     * @param exception the error
     * @param target    the new location of the file
     */
    @Override
    public void error(File file, Throwable exception, File target) {
        super.error(file, exception, target);
        errors.put(target, exception.getMessage());
    }

    /**
     * Notifies that a file couldn't be loaded due to error.
     *
     * @param file    the original location of the file
     * @param message the error message
     * @param target  the new location of the file
     */
    @Override
    public void error(File file, String message, File target) {
        super.error(file, message, target);
        errors.put(target, message);
    }
}
