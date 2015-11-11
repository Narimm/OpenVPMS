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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * Abstract implementation of the {@link LoaderListener} interface.
 *
 * @author Tim Anderson
 */
abstract class AbstractLoaderListener implements LoaderListener {

    /**
     * The directory to move loaded files to. May be {@code null}
     */
    private final File dir;

    /**
     * The directory to move files that failed to load to. May be {@code null}
     */
    private final File errorDir;

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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractLoaderListener.class);

    /**
     * Constructs an {@link AbstractLoaderListener}.
     *
     * @param dir if non-null, files will be moved here on successful load
     */
    public AbstractLoaderListener(File dir) {
        this(dir, null);
    }

    /**
     * Constructs an {@link AbstractLoaderListener}.
     *
     * @param dir      the directory to move files to on successful load. May be {@code null}
     * @param errorDir the directory to move files to on error. May be {@code null}
     */
    public AbstractLoaderListener(File dir, File errorDir) {
        this.dir = dir;
        this.errorDir = errorDir;
    }

    /**
     * Notifies when a file is loaded.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     * @return the new location of the file. May be {@code null}
     */
    @Override
    public File loaded(File file, long id) {
        return doLoaded(file);
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
     * @return the new location of the file. May be {@code null}
     */
    @Override
    public File alreadyLoaded(File file, long id) {
        ++skipped;
        ++errors;
        return handleError(file);
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
     * @param file the file
     * @return the new location of the file. May be {@code null}
     */
    @Override
    public File missingAct(File file) {
        return handleMissingAct(file);
    }

    /**
     * Notifies that a file couldn't be loaded as there was no corresponding act.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     * @return the new location of the file. May be {@code null}
     */
    @Override
    public File missingAct(File file, long id) {
        return handleMissingAct(file);
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
     * @return the new location of the file. May be {@code null}
     */
    @Override
    public File error(File file, Throwable exception) {
        ++errors;
        return handleError(file);
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
     * @return the new location of the file. May be {@code null}
     */
    protected File doLoaded(File file) {
        File result;
        if (dir != null) {
            try {
                result = move(file, dir);
                ++loaded;
            } catch (IOException exception) {
                result = error(file, exception);
            }
        } else {
            ++loaded;
            result = file;
        }
        return result;
    }

    /**
     * Handles a missing act.
     *
     * @param file the file
     * @return the new location of the file. May be {@code null}
     */
    protected File handleMissingAct(File file) {
        ++missing;
        ++errors;
        return handleError(file);
    }

    /**
     * Handles a file in error.
     * <p/>
     * This implementation delegates to {@link #moveFileToErrorDir(File, Log)}.
     *
     * @param file the file
     * @return the new location of the file. May be {@code null}
     */
    protected File handleError(File file) {
        return moveFileToErrorDir(file, log);
    }

    /**
     * Moves a file to the error directory, if one is configured.
     *
     * @param file the file to move
     * @param log  the log to use if the file cannot be moved
     * @return the new location of the file. May be {@code null}
     */
    protected File moveFileToErrorDir(File file, Log log) {
        File result = file;
        if (errorDir != null) {
            try {
                result = move(file, errorDir);
            } catch (IOException exception) {
                log.error("Failed to move " + file.getPath() + " to " + errorDir, exception);
            }
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
            throw new IOException("Cannot copy " + file.getPath()
                                  + " to " + dir.getPath() + ": file exists");
        }
        Files.move(file.toPath(), target.toPath());
        return target;
    }
}
