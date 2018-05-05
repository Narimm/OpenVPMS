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

/**
 * Default implementation of the {@link LoaderListener} interface.
 *
 * @author Tim Anderson
 */
public class DefaultLoaderListener implements LoaderListener {

    /**
     * The no. of loaded files.
     */
    private int loaded = 0;

    /**
     * The no. of skipped files.
     */
    private int skipped = 0;

    /**
     * The no. of missing acts.
     */
    private int missing = 0;

    /**
     * The no. of errors.
     */
    private int errors = 0;

    /**
     * Constructs a {@link DefaultLoaderListener}.
     */
    public DefaultLoaderListener() {
        super();
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
        ++loaded;
    }

    /**
     * Returns the no. of files loaded.
     *
     * @return the no. of files loaded
     */
    public int getLoaded() {
        return loaded;
    }

    /**
     * Notifies that a file couldn't be loaded as it or another file had already been processed.
     *
     * @param file   the file
     * @param id     the corresponding act identifier
     * @param target the new location of the file
     */
    @Override
    public void alreadyLoaded(File file, long id, File target) {
        ++skipped;
        ++errors;
    }

    /**
     * Returns the no. of files that weren't loaded as the corresponding act was already associated with a document.
     *
     * @return the no. of files that were skipped
     */
    public int getAlreadyLoaded() {
        return skipped;
    }

    /**
     * Notifies that a file couldn't be loaded as there was no corresponding act.
     *
     * @param file   the original location of the file
     * @param target the new location of the file
     */
    @Override
    public void missingAct(File file, File target) {
        handleMissingAct();
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
        handleMissingAct();
    }

    /**
     * Returns the no. of files that don't have a corresponding act.
     *
     * @return the no. of files with no corresponding act
     */
    public int getMissingAct() {
        return missing;
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
        ++errors;
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
        ++errors;
    }

    /**
     * Returns the no. of files that failed load due to error.
     *
     * @return the no. of errors
     */
    public int getErrors() {
        return errors;
    }

    /**
     * Returns the no. of files processed.
     *
     * @return the no. of files processed
     */
    public int getProcessed() {
        return loaded + errors;
    }

    /**
     * Handles a missing act.
     */
    protected void handleMissingAct() {
        ++missing;
        ++errors;
    }

}
