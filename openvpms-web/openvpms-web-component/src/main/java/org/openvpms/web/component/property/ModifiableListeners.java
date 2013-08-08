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

package org.openvpms.web.component.property;

import org.openvpms.web.component.util.ErrorHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Manages a list of {@link ModifiableListener}s.
 *
 * @author Tim Anderson
 */
public class ModifiableListeners {

    /**
     * The listeners.
     */
    private List<ModifiableListener> listeners;


    /**
     * Add a listener.
     * <p/>
     * If the listener is already registered, it will be moved to the end of the list.
     *
     * @param listener the listener to add
     */
    public void addListener(ModifiableListener listener) {
        addListener(listener, (listeners != null) ? listeners.size() : 0);
    }

    /**
     * Add a listener.
     * <p/>
     * If the listener is already registered, it will be moved to the new index.
     *
     * @param index    the index to add the listener at
     * @param listener the listener to add
     */
    public void addListener(ModifiableListener listener, int index) {
        if (listeners == null) {
            listeners = new ArrayList<ModifiableListener>();
        }
        int existing = listeners.indexOf(listener);
        if (existing != -1) {
            listeners.remove(listener);
            if (index > existing) {
                --index;
            }
        }
        listeners.add(index, listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(ModifiableListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Notify all registered listeners.
     *
     * @param modifiable the modifiable to pass to the listeners
     */
    public void notifyListeners(Modifiable modifiable) {
        try {
            if (listeners != null) {
                ModifiableListener[] list = listeners.toArray(new ModifiableListener[listeners.size()]);
                for (ModifiableListener listener : list) {
                    listener.modified(modifiable);
                }
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Removes all listeners.
     */
    public void removeAll() {
        listeners.clear();
    }
}
