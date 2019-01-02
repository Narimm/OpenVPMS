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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Default implementation of {@link ListMarkModel}.
 *
 * @author Tim Anderson
 */
public class DefaultListMarkModel implements ListMarkModel {

    /**
     * The marks.
     */
    private final BitSet marks = new BitSet();

    /**
     * The listeners.
     */
    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Adds a listener to be notified of changes.
     *
     * @param listener the listener to add
     */
    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Marks/unmarks a list index.
     *
     * @param index the index
     * @param mark  if {@code true} mark the index, otherwise unmark it
     */
    @Override
    public void setMarked(int index, boolean mark) {
        if (marks.get(index) != mark) {
            marks.set(index, mark);
            for (Listener listener : getListeners()) {
                listener.changed(index, mark);
            }
        }
    }

    /**
     * Determines if the list index is marked.
     *
     * @param index the list index
     * @return {@code true} if it is marked, {@code false} if it is unmarked
     */
    @Override
    public boolean isMarked(int index) {
        return marks.get(index);
    }

    /**
     * Removes all marks.
     */
    @Override
    public void clear() {
        if (!marks.isEmpty()) {
            marks.clear();
            for (Listener listener : getListeners()) {
                listener.cleared();
            }
        }
    }

    /**
     * Determines if nothing is marked.
     *
     * @return {@code true} if nothing is marked
     */
    @Override
    public boolean isEmpty() {
        return marks.isEmpty();
    }

    /**
     * Determines if a list index can be marked.
     *
     * @param index the list index
     * @return {@code true}
     */
    @Override
    public boolean canMark(int index) {
        return true;
    }

    /**
     * Returns the listeners.
     *
     * @return the listeners, as an array
     */
    private Listener[] getListeners() {
        return this.listeners.toArray(new Listener[0]);
    }
}
