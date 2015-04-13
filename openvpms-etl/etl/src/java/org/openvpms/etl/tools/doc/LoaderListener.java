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

import java.io.File;


/**
 * Listener for {@link Loader} events.
 *
 * @author Tim Anderson
 */
public interface LoaderListener {

    /**
     * Notifies when a file is loaded.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    void loaded(File file, long id);

    /**
     * Returns the no. of files loaded.
     *
     * @return the no. of files loaded
     */
    int getLoaded();

    /**
     * Notifies that a file couldn't be loaded as it or another file had
     * already been processed.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    void alreadyLoaded(File file, long id);

    /**
     * Returns the no. of files that weren't loaded as the corresponding act
     * was already associated with a document.
     *
     * @return the no. of files that were skipped
     */
    int getAlreadyLoaded();

    /**
     * Notifies that a file couldn't be loaded as there was no corresponding act.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    void missingAct(File file, long id);

    /**
     * Returns the no. of files that don't have a corresponding act.
     *
     * @return the no. of files with no corresponding act
     */
    int getMissingAct();

    /**
     * Notifies that a file couldn't be loaded due to error.
     *
     * @param file      the file
     * @param exception the error
     */
    void error(File file, Throwable exception);

    /**
     * Returns the no. of files that failed load due to error.
     *
     * @return the no. of errors
     */
    int getErrors();

    /**
     * Returns the no. of files processed.
     *
     * @return the no. of files processed
     */
    int getProcessed();

}
