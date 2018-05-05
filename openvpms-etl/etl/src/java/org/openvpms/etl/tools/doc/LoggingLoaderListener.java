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

import org.apache.commons.logging.Log;

import java.io.File;


/**
 * Implementation of {@link LoaderListener} that logs events to a {@code Log}.
 *
 * @author Tim Anderson
 */
public class LoggingLoaderListener extends DelegatingLoaderListener {

    /**
     * The log.
     */
    private final Log log;


    /**
     * Constructs a {@link LoggingLoaderListener} that delegates to a {@link DefaultLoaderListener}.
     *
     * @param log the log
     */
    public LoggingLoaderListener(Log log) {
        this(log, new DefaultLoaderListener());
    }

    /**
     * Constructs a {@link LoggingLoaderListener}.
     *
     * @param log      the log
     * @param listener the listener to delegate to
     */
    public LoggingLoaderListener(Log log, LoaderListener listener) {
        super(listener);
        this.log = log;
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
        log.info("Loaded " + target.getPath() + ", identifier=" + id);
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
        log.info("Skipping " + target.getPath() + ", identifier=" + id);
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
        log.info("Missing act for " + target.getPath() + ", identifier=" + id);
    }

    /**
     * Notifies that a file couldn't be loaded as there was no corresponding act.
     *
     * @param file   the original location of the file
     * @param target the new location of the file
     */
    @Override
    public void missingAct(File file, File target) {
        super.missingAct(file, target);
        log.info("Missing act for " + target.getPath());
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
        log.info("Error " + target.getPath(), exception);
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
        log.info("Error " + target.getPath() + ": " + message);
    }
}
