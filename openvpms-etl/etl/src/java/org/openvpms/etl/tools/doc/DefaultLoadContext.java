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
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

/**
 * Default implementation of the {@link LoadContext}.
 * <p/>
 * Supports notifying a {@link LoaderListener}.
 *
 * @author Tim Anderson
 */
public class DefaultLoadContext implements LoadContext {

    /**
     * The strategy for moving files on success/failure.
     */
    private final FileStrategy strategy;

    /**
     * The listener.
     */
    private final LoaderListener listener;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DefaultLoadContext.class);

    /**
     * Constructs a {@link DefaultLoadContext}.
     *
     * @param strategy the file strategy
     * @param listener the listener
     */
    public DefaultLoadContext(FileStrategy strategy, LoaderListener listener) {
        this.strategy = strategy;
        this.listener = listener;
    }

    /**
     * Returns the listener.
     *
     * @return the listener
     */
    public LoaderListener getListener() {
        return listener;
    }

    /**
     * Invoked when a file is successfully loaded.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     * @return {@code true} if the file was successfully moved
     */
    @Override
    public boolean loaded(File file, long id) {
        boolean result = false;
        try {
            File target = strategy.loaded(file);
            result = true;
            notifyLoaded(file, id, target);
        } catch (IOException exception) {
            log.warn("File " + file + " was loaded, but could not be moved to the target directory. "
                     + "Attempting to move it to the error directory.", exception);
            try {
                File target = strategy.error(file);
                notifyError(file, exception, target);
            } catch (IOException nested) {
                log.warn("File " + file + " was loaded, and could not be moved to the target or error directory. ",
                         nested);
                notifyError(file, exception, file); // notify of original error
            }
        }
        return result;
    }

    /**
     * Invoked when a file can't be loaded as it has already been processed.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    @Override
    public void alreadyLoaded(File file, long id) {
        try {
            File target = strategy.error(file);
            notifyAlreadyLoaded(file, id, target);
        } catch (IOException exception) {
            log.warn("File " + file + " already loaded, but cannot be moved to the error directory");
            notifyError(file, exception, file);
        }
    }

    /**
     * Invoked when a file can't be loaded as a corresponding act cannot be found.
     *
     * @param file the file
     */
    @Override
    public void missingAct(File file) {
        try {
            File target = strategy.error(file);
            notifyMissingAct(file, target);
        } catch (IOException exception) {
            log.warn("File " + file + " missing act but cannot be moved to the error directory");
            notifyError(file, exception, file);
        }
    }

    /**
     * Invoked when a file can't be loaded as a corresponding act cannot be found.
     *
     * @param file the file
     * @param id   the corresponding act identifier
     */
    @Override
    public void missingAct(File file, long id) {
        try {
            File target = strategy.error(file);
            notifyMissingAct(file, id, target);
        } catch (IOException exception) {
            log.warn("File " + file + " missing act: " + id + " but cannot be moved to the error directory");
            notifyError(file, exception, file);
        }
    }

    /**
     * Invoked when a file can't be loaded due to an error.
     *
     * @param file  the file
     * @param error the error
     */
    @Override
    public void error(File file, Throwable error) {
        try {
            if (file.exists()) {
                // try and move the file
                File target = strategy.error(file);
                notifyError(file, error, target);
            } else {
                notifyError(file, error, file);
            }
        } catch (IOException nested) {
            log.warn("Failed to move " + file + " to the error directory", nested);
            // notify the original error
            notifyError(file, error, file);
        }
    }

    /**
     * Invoked when a file can't be loaded due to an error.
     *
     * @param file    the file
     * @param message the error message
     */
    @Override
    public void error(File file, String message) {
        try {
            if (file.exists()) {
                // try and move the file
                File target = strategy.error(file);
                notifyError(file, message, target);
            } else {
                notifyError(file, message, file);
            }
        } catch (IOException nested) {
            log.warn("Failed to move " + file + " to the error directory", nested);
            // notify the original error
            notifyError(file, message, file);
        }
    }

    /**
     * Notifies any registered listener that a file has been loaded.
     *
     * @param file   the original location of the file
     * @param id     the corresponding act identifier
     * @param target the new location of the file
     */
    protected void notifyLoaded(File file, long id, File target) {
        if (listener != null) {
            listener.loaded(file, id, target);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded as it has already been processed.
     *
     * @param file   the original location of the file
     * @param id     the corresponding act identifier
     * @param target the new location of the file
     */
    protected void notifyAlreadyLoaded(File file, long id, File target) {
        if (listener != null) {
            listener.alreadyLoaded(file, id, target);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded as a corresponding act cannot be found.
     *
     * @param file   the original location of the file
     * @param target the new location of the file
     */
    protected void notifyMissingAct(File file, File target) {
        if (listener != null) {
            listener.missingAct(file, target);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded as a corresponding act cannot be found.
     *
     * @param file   the original location of the file
     * @param id     the corresponding act identifier
     * @param target the new location of the file
     */
    protected void notifyMissingAct(File file, long id, File target) {
        if (listener != null) {
            listener.missingAct(file, id, target);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded due to an error.
     *
     * @param file   the file
     * @param error  the error
     * @param target the new location of the file
     */
    protected void notifyError(File file, Throwable error, File target) {
        if (listener != null) {
            listener.error(file, error, target);
        }
    }

    /**
     * Notifies any registered listener that a file can't be loaded due to an error.
     *
     * @param file    the file
     * @param message the error message
     * @param target  the new location of the file
     */
    protected void notifyError(File file, String message, File target) {
        if (listener != null) {
            listener.error(file, message, target);
        }
    }

}
