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
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ModifiableSet implements Modifiable {

    /**
     * Caches the modified status.
     */
    private boolean _modified;


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
     * Returns all modified objects.
     *
     * @return a list of modified objects
     */
    public Set<IMObject> getModified() {
        HashSet<IMObject> result = new HashSet<IMObject>();
        for (Map.Entry<IMObject, HashSet<Modifiable>> entry :
                _objects.entrySet()) {
            IMObject object = entry.getKey();
            if (object.isNew()) {
                result.add(object);
            } else {
                for (Modifiable modifiable : entry.getValue()) {
                    if (modifiable.isModified()) {
                        result.add(entry.getKey());
                        break;
                    }
                }
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
                IMObject object = entry.getKey();
                if (object.isNew()) {
                    _modified = true;
                    return _modified;
                } else {
                    for (Modifiable modifiable : entry.getValue()) {
                        if (modifiable.isModified()) {
                            _modified = true;
                            return _modified;
                        }
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
     * Clears the modified status of an object.
     *
     * @param object the object
     */
    public void clearModified(IMObject object) {
        Set<Modifiable> set = _objects.get(object);
        for (Modifiable modifiable : set) {
            modifiable.clearModified();
        }
    }
}
