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

import java.io.File;


/**
 * Implementation of {@link LoaderListener} that logs events to a {@code Log}.
 *
 * @author Tim Anderson
 */
public class LoggingLoaderListener extends AbstractLoaderListener {

    /**
     * The log.
     */
    private final Log log;


    /**
     * Constructs a {@link LoggingLoaderListener}.
     *
     * @param log the log
     * @param dir if non-null, files will be moved here on successful load
     */
    public LoggingLoaderListener(Log log, File dir) {
        this(log, dir, null, false);
    }

    /**
     * Constructs a {@link LoggingLoaderListener}.
     *
     * @param log              the log
     * @param dir              the directory to move files to on successful load. May be {@code null}
     * @param errorDir         the directory to move files to on error. May be {@code null}
     * @param renameDuplicates if {@code true}, rename files on move, if a file exists with the same name
     */
    public LoggingLoaderListener(Log log, File dir, File errorDir, boolean renameDuplicates) {
        super(dir, errorDir, renameDuplicates);
        this.log = log;
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
        int loaded = getLoaded();
        File target = doLoaded(file);
        if (target != null) {
            file = target;
        }
        if (getLoaded() > loaded) {
            log.info("Loaded " + file.getPath() + ", identifier=" + id);
        }
        return target;
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
        File target = super.alreadyLoaded(file, id);
        if (target != null) {
            file = target;
        }
        log.info("Skipping " + file.getPath() + ", identifier=" + id);
        return target;
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
        File target = super.missingAct(file, id);
        if (target != null) {
            file = target;
        }
        log.info("Missing act for " + file.getPath() + ", identifier=" + id);
        return target;
    }

    /**
     * Notifies that a file couldn't be loaded as there was no corresponding act.
     *
     * @param file the file
     * @return the new location of the file. May be {@code null}
     */
    @Override
    public File missingAct(File file) {
        File target = super.missingAct(file);
        if (target != null) {
            file = target;
        }
        log.info("Missing act for " + file.getPath());
        return target;
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
        File target = super.error(file, exception);
        if (target != null) {
            file = target;
        }
        log.info("Error " + file.getPath(), exception);
        return target;
    }

    /**
     * Handles a file in error.
     *
     * @param file the file
     * @return the new location of the file. May be {@code null}
     */
    @Override
    protected File handleError(File file) {
        return moveFileToErrorDir(file, log);
    }
}
