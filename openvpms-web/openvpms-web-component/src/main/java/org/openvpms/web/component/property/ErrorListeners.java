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
 * Manages a list of {@link ErrorListener}s.
 *
 * @author Tim Anderson
 */
public class ErrorListeners {

    /**
     * The listeners.
     */
    private List<ErrorListener> listeners;


    /**
     * Add a listener.
     *
     * @param listener the listener to add
     */
    public void addListener(ErrorListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ErrorListener>();
        }
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(ErrorListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Notify all registered listeners.
     *
     * @param modifiable the modifiable to pass to the listeners
     */
    public void notifyListeners(Modifiable modifiable, String message) {
        try {
            if (listeners != null) {
                ErrorListener[] list = listeners.toArray(new ErrorListener[listeners.size()]);
                for (ErrorListener listener : list) {
                    listener.error(modifiable, message);
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
