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
 *  Copyright 2008-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;


/**
 * Abstract implementation of the {@link LoaderListener} interface.
 *
 * @author Tim Anderson
 */
abstract class AbstractLoaderListener implements LoaderListener {

    /**
     * The directory to move loaded files to. May be <tt>null</tt>
     */
    private final File dir;

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
     * Creates a new <tt>AbstractLoaderListener</tt>.
     */
    public AbstractLoaderListener() {
        this(null);
    }

    /**
     * Creates a new <tt>AbstractLoaderListener</tt>.
     *
     * @param dir if non-null, files will be moved here on successful load
     */
    public AbstractLoaderListener(File dir) {
        this.dir = dir;
    }

    /**
     * Notifies when a file is loaded.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    public void loaded(File file, long id) {
        doLoaded(file);
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
     * Notifies that a file couldn't be loaded as it or another file had
     * already been processed.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    public void alreadyLoaded(File file, long id) {
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
     * Notifies that a file couldn't be loaded as there was no corresponding
     * act.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    public void missingAct(File file, long id) {
        ++missing;
        ++errors;
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
     * @param file      the file
     * @param exception the error
     */
    public void error(File file, Throwable exception) {
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
     * Invoked when a file is loaded.
     * <p/>
     * If a target directory is configured, the file will be moved to it.
     *
     * @param file the file
     * @return <tt>true</tt> if the file doesn't need to be moved, or was moved
     *         successfully, otherwise <tt>false</tt>
     */
    protected boolean doLoaded(File file) {
        boolean result = true;
        if (dir != null) {
            try {
                File target = new File(dir, file.getName());
                if (target.exists()) {
                    throw new IOException("Cannot copy " + file.getPath()
                                          + " to " + dir.getPath() + ": file exists");
                }
                FileUtils.copyFile(file, target);
                if (!file.delete()) {
                    throw new IOException("Failed to delete " + file.getPath());
                }
                ++loaded;
            } catch (IOException exception) {
                result = false;
                error(file, exception);
            }
        } else {
            ++loaded;
        }
        return result;
    }

}
