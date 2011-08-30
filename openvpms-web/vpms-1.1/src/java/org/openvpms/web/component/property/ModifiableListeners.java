/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.property;

import java.util.ArrayList;
import java.util.List;


/**
 * Manages a list of {@link ModifiableListener}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ModifiableListeners {

    /**
     * The listeners.
     */
    private List<ModifiableListener> _listeners;


    /**
     * Add a listener.
     *
     * @param listener the listener to add
     */
    public void addListener(ModifiableListener listener) {
        if (_listeners == null) {
            _listeners = new ArrayList<ModifiableListener>();
        }
        _listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(ModifiableListener listener) {
        if (_listeners != null) {
            _listeners.remove(listener);
        }
    }

    /**
     * Notify all registered listeners.
     *
     * @param modifiable the modifiable to pass to the listeners
     */
    public void notifyListeners(Modifiable modifiable) {
        if (_listeners != null) {
            ModifiableListener[] listeners =
                    _listeners.toArray(new ModifiableListener[0]);
            for (ModifiableListener listener : listeners) {
                listener.modified(modifiable);
            }
        }
    }
}
