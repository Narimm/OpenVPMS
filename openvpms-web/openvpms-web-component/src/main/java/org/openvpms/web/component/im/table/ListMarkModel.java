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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

/**
 * Tracks marks in a list.
 *
 * @author Tim Anderson
 */
public interface ListMarkModel {

    interface Listener {

        /**
         * Invoked when a list index is marked or unmarked.
         *
         * @param index the list index
         * @param marked if {@code true}, the index was marked, otherwise it was unmarked
         */
        void changed(int index, boolean marked);

        /**
         * Invoked when all marks are cleared.
         * <p/>
         * Individual {@code changed} events are not triggered.
         */
        void cleared();

    }

    /**
     * Adds a listener to be notified of changes.
     *
     * @param listener the listener to add
     */
    void addListener(Listener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    void removeListener(Listener listener);

    /**
     * Marks/unmarks a list index.
     *
     * @param index the index
     * @param mark  if {@code true} mark the index, otherwise unmark it
     */
    void setMarked(int index, boolean mark);

    /**
     * Determines if the list index is marked.
     *
     * @param index the list index
     * @return {@code true} if it is marked, {@code false} if it is unmarked
     */
    boolean isMarked(int index);

    /**
     * Removes all marks.
     */
    void clear();

    /**
     * Determines if nothing is marked.
     *
     * @return {@code true} if nothing is marked
     */
    boolean isEmpty();

}
