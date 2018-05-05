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
 * {@link Loader} context.
 *
 * @author Tim Anderson
 */
public interface LoadContext {

    /**
     * Invoked when a file is successfully loaded.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     * @return {@code true} if the file was successfully moved
     */
    boolean loaded(File file, long id);

    /**
     * Invoked when a file can't be loaded as it has already been processed.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    void alreadyLoaded(File file, long id);

    /**
     * Invoked when a file can't be loaded as a corresponding act cannot be found.
     *
     * @param file the file
     */
    void missingAct(File file);

    /**
     * Invoked when a file can't be loaded as a corresponding act cannot be found.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    void missingAct(File file, long id);

    /**
     * Invoked when a file can't be loaded due to an error.
     *
     * @param file  the file
     * @param error the error
     */
    void error(File file, Throwable error);

    /**
     * Invoked when a file can't be loaded due to an error.
     *
     * @param file    the file
     * @param message the error message
     */
    void error(File file, String message);

}
