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

package org.openvpms.web.component.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Collection of {@link Modifiable} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ModifiableSet implements Modifiable {

    /**
     * Caches the modified status.
     */
    private boolean _modified;

    /**
     * The event listeners.
     */
    private ModifiableListeners _listeners = new ModifiableListeners();


    /**
     * Map of objects to their corresponding modifiable fields.
     */
    private final Map<IMObject, HashSet<Modifiable>> _objects
            = new HashMap<IMObject, HashSet<Modifiable>>();


    /**
     * Add a modifiable object.
     *
     * @param object     the parent object
     * @param modifiable the object to add
     */
    public void add(IMObject object, Modifiable modifiable) {
        HashSet<Modifiable> set = _objects.get(object);
        if (set == null) {
            set = new HashSet<Modifiable>();
            _objects.put(object, set);
        }
        set.add(modifiable);
        modifiable.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                notifyListeners(modifiable);
            }
        });
    }

    /**
     * Remove an object.
     *
     * @param object the object to remove
     */
    public void remove(IMObject object) {
        _objects.remove(object);
        _modified = false;
    }

    /**
     * Removes all objects, retaining the modified status.
     */
    public void removeAll() {
        if (!_modified) {
            _modified = isModified();
        }
        for (Set<Modifiable> set : _objects.values()) {
            set.clear();
        }
        _objects.clear();
    }

    /**
     * Returns all modifiable objects.
     *
     * @return a set of all modifiable objects
     */
    public Set<Modifiable> getModifiable() {
        Set<Modifiable> result = new HashSet<Modifiable>();
        for (Map.Entry<IMObject, HashSet<Modifiable>> entry :
                _objects.entrySet()) {
            for (Modifiable modifiable : entry.getValue()) {
                result.add(modifiable);
            }
        }
        return result;
    }

    /**
     * Returns all saveable objects that have been modified.
     *
     * @return a list of modified saveable objects.
     */
    public List<Saveable> getModifiedSaveable() {
        List<Saveable> result = new ArrayList<Saveable>();
        for (Map.Entry<IMObject, HashSet<Modifiable>> entry :
                _objects.entrySet()) {
            for (Modifiable modifiable : entry.getValue()) {
                if ((modifiable instanceof Saveable)
                    && modifiable.isModified()) {
                    result.add((Saveable) modifiable);
                }
            }
        }
        return result;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <code>true</code> if the object has been modified
     */
    public boolean isModified() {
        if (!_modified) {
            for (Map.Entry<IMObject, HashSet<Modifiable>> entry :
                    _objects.entrySet()) {
                for (Modifiable modifiable : entry.getValue()) {
                    if (modifiable.isModified()) {
                        _modified = true;
                        return _modified;
                    }
                }
            }
        }
        return _modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        _modified = false;
        for (Set<Modifiable> set : _objects.values()) {
            for (Modifiable modifiable : set) {
                modifiable.clearModified();
            }
        }
    }

    /**
     * Add a listener to be notified when a this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        _listeners.addListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        _listeners.removeListener(listener);
    }

    /**
     * Invoked when a {@link Modifiable} changes. Forwards the event to any
     * registered listener.
     *
     * @param modified the changed instance
     */
    protected void notifyListeners(Modifiable modified) {
        _listeners.notifyListeners(modified);
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        for (Map.Entry<IMObject, HashSet<Modifiable>> entry :
                _objects.entrySet()) {
            for (Modifiable modifiable : entry.getValue()) {
                if (!modifiable.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

}
