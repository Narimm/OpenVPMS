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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.customer.charge;

import org.openvpms.web.component.im.edit.IMObjectEditor;

/**
 * Queues editors so that they can be displayed in a dialog, one at a time.
 *
 * @author Tim Anderson
 */
public interface EditorQueue {

    /**
     * Queues an editor for display.
     *
     * @param editor   the editor to queue
     * @param skip     if {@code true}, indicates that the edit can be skipped
     * @param listener the listener to notify on completion
     */
    void queue(IMObjectEditor editor, boolean skip, Listener listener);

    /**
     * Determines if editing is complete.
     *
     * @return {@code true} if there are no more editors
     */
    boolean isComplete();

    /**
     * Listener to notify completion of the edit.
     */
    public interface Listener {

        /**
         * Invoked when the edit is complete.
         *
         * @param skipped if {@code true} indicates that the edit was skipped
         */
        void completed(boolean skipped);
    }
}