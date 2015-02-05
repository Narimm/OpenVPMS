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

package org.openvpms.web.jobs.docload;

import org.openvpms.etl.tools.doc.LoaderListener;

import java.io.File;

/**
 * An implementation of {@link LoaderListener} that delegates to another instance.
 *
 * @author Tim Anderson
 */
class DelegatingLoaderListener implements LoaderListener {

    /**
     * The listener to delegate to.
     */
    private LoaderListener listener;

    /**
     * Constructs a {@link DelegatingLoaderListener}.
     *
     * @param listener the listener to delegate to
     */
    public DelegatingLoaderListener(LoaderListener listener) {
        this.listener = listener;
    }

    /**
     * Notifies when a file is loaded.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    @Override
    public void loaded(File file, long id) {
        listener.loaded(file, id);
    }

    /**
     * Returns the no. of files loaded.
     *
     * @return the no. of files loaded
     */
    @Override
    public int getLoaded() {
        return listener.getLoaded();
    }

    /**
     * Notifies that a file couldn't be loaded as it or another file had already been processed.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    @Override
    public void alreadyLoaded(File file, long id) {
        listener.alreadyLoaded(file, id);
    }

    /**
     * Returns the no. of files that weren't loaded as the corresponding act was already associated with a document.
     *
     * @return the no. of files that were skipped
     */
    @Override
    public int getAlreadyLoaded() {
        return listener.getAlreadyLoaded();
    }

    /**
     * Notifies that a file couldn't be loaded as there was no corresponding act.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    @Override
    public void missingAct(File file, long id) {
        listener.missingAct(file, id);
    }

    /**
     * Returns the no. of files that don't have a corresponding act.
     *
     * @return the no. of files with no corresponding act
     */
    @Override
    public int getMissingAct() {
        return listener.getMissingAct();
    }

    /**
     * Notifies that a file couldn't be loaded due to error.
     *
     * @param file      the file
     * @param exception the error
     */
    @Override
    public void error(File file, Throwable exception) {
        listener.error(file, exception);
    }

    /**
     * Returns the no. of files that failed load due to error.
     *
     * @return the no. of errors
     */
    @Override
    public int getErrors() {
        return listener.getErrors();
    }

    /**
     * Returns the no. of files processed.
     *
     * @return the no. of files processed
     */
    @Override
    public int getProcessed() {
        return listener.getProcessed();
    }
}
