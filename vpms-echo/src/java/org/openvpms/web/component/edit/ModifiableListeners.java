package org.openvpms.web.component.edit;

import java.util.ArrayList;
import java.util.List;


/**
 * Manages a list of {@link ModifiableListener}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
            for (ModifiableListener listener : _listeners) {
                listener.modified(modifiable);
            }
        }
    }
}
