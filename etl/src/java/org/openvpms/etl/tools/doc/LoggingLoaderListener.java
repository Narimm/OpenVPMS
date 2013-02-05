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

import org.apache.commons.logging.Log;

import java.io.File;


/**
 * Implementation of {@link LoaderListener} that logs events to a {@code Log}.
 *
 * @author Tim Anderson
 */
class LoggingLoaderListener extends AbstractLoaderListener {

    /**
     * The log.
     */
    private final Log log;


    /**
     * Constructs a {@code LoggingLoaderListener}.
     *
     * @param log the log
     * @param dir if non-null, files will be moved here on successful load
     */
    public LoggingLoaderListener(Log log, File dir) {
        super(dir);
        this.log = log;
    }

    /**
     * Notifies when a file is loaded.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    @Override
    public void loaded(File file, long id) {
        if (doLoaded(file)) {
            log.info("Loaded " + file.getPath() + ", identifier=" + id);
        }
    }

    /**
     * Notifies that a file couldn't be loaded as it or another file had already been processed.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    @Override
    public void alreadyLoaded(File file, long id) {
        super.alreadyLoaded(file, id);
        log.info("Skipping " + file.getPath() + ", identifier=" + id);
    }

    /**
     * Notifies that a file couldn't be loaded as there was no corresponding act.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    @Override
    public void missingAct(File file, long id) {
        super.missingAct(file, id);
        log.info("Missing act for " + file.getPath() + ", identifier=" + id);
    }

    /**
     * Notifies that a file couldn't be loaded due to error.
     *
     * @param file      the file
     * @param exception the error
     */
    @Override
    public void error(File file, Throwable exception) {
        super.error(file, exception);
        log.info("Error " + file.getPath(), exception);
    }
}
