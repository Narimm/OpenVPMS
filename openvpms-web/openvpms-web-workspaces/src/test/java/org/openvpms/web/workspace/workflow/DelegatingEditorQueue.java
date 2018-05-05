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

package org.openvpms.web.workspace.workflow;

import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.workspace.customer.charge.EditorQueue;

/**
 * An {@link EditorQueue} that delegates to one returned by an {@link EditorQueueHandle}.
 *
 * @author Tim Anderson
 */
public class DelegatingEditorQueue implements EditorQueue {

    /**
     * Holds the queue to delegate to.
     */
    private final EditorQueueHandle queue;

    /**
     * Constructs a {@link DelegatingEditorQueue}.
     *
     * @param queue the queue to delegate to
     */
    public DelegatingEditorQueue(EditorQueueHandle queue) {
        this.queue = queue;
    }

    /**
     * Queues an editor for display.
     *
     * @param editor   the editor to queue
     * @param skip     if {@code true}, indicates that the edit can be skipped
     * @param cancel   if {@code true}, indicates that the edit can be cancelled
     * @param listener the listener to notify on completion
     */
    public void queue(IMObjectEditor editor, boolean skip, boolean cancel, Listener listener) {
        queue.getQueue().queue(editor, skip, cancel, listener);
    }

    /**
     * Queues a dialog.
     *
     * @param dialog the dialog to queue
     */
    @Override
    public void queue(PopupDialog dialog) {
        queue.getQueue().queue(dialog);
    }

    /**
     * Returns the current popup dialog.
     *
     * @return the current popup dialog. May be {@code null}
     */
    @Override
    public PopupDialog getCurrent() {
        return queue.getQueue().getCurrent();
    }

    /**
     * Determines if editing is complete.
     *
     * @return {@code true} if there are no more editors
     */
    public boolean isComplete() {
        return queue.getQueue().isComplete();
    }

    /**
     * Queues a callback.
     * <p/>
     * These must execute synchronously.
     * <p/>
     * Note that calls to {@link #isComplete()} return {@code true} if the queue is empty but a callback is in progress.
     * <p/>
     * This is required so that callbacks can trigger automatic saves without affecting the valid status of editors.
     *
     * @param runnable the callback
     */
    @Override
    public void queue(Runnable runnable) {
        queue.getQueue().queue(runnable);
    }
}
