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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of the {@link Listeners} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractListeners<E> implements Listeners<E> {

    /**
     * The listeners.
     */
    private List<Listener<E>> listeners = new ArrayList<>();

    /**
     * Adds a listener.
     *
     * @param listener the listener to add
     */
    @Override
    public synchronized void addListener(Listener<E> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public synchronized void removeListener(Listener<E> listener) {
        listeners.remove(listener);
    }

    /**
     * Removes all listeners.
     */
    @Override
    public synchronized void clear() {
        listeners.clear();
    }

    /**
     * Handles an event.
     *
     * @param event the event
     */
    @Override
    public void onEvent(E event) {
        Listener<E>[] list = getListeners();
        for (Listener<E> listener : list) {
            notify(listener, event);
        }
    }

    /**
     * Returns the listeners.
     *
     * @return the listeners
     */
    @SuppressWarnings("unchecked")
    protected synchronized Listener<E>[] getListeners() {
        return (Listener<E>[]) listeners.toArray(new Listener[listeners.size()]);
    }

    /**
     * Notifies a listener.
     *
     * @param listener the listener
     * @param event    the event
     */
    protected abstract void notify(Listener<E> listener, E event);

}
