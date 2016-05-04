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

package org.openvpms.component.system.common.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * A {@link Listeners} implementation that performs asynchronous notification using a {@code ThreadPoolExecutor}.
 *
 * @author Tim Anderson
 */
public class AsyncListeners<E> extends AbstractListeners<E> {

    /**
     * The thread pool executor.
     */
    private final ThreadPoolExecutor executor;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AsyncListeners.class);

    /**
     * Constructs a {@link AsyncListeners}.
     *
     * @param executor the thread pool executor
     */
    public AsyncListeners(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    /**
     * Notifies a listener.
     *
     * @param listener the listener
     * @param event    the event
     */
    @Override
    protected void notify(final Listener<E> listener, final E event) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onEvent(event);
                } catch (Throwable exception) {
                    onException(listener, exception);
                }
            }
        });
    }

    /**
     * Invoked when a listener throws an exception.
     * <p/>
     * This implementation logs the exception.
     *
     * @param exception the exception
     */
    protected void onException(Listener<E> listener, Throwable exception) {
        log.error("Listener " + listener + " terminated with exception: " + exception.getMessage(), exception);
    }
}
