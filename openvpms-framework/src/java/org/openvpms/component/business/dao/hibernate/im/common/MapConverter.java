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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.apache.commons.lang.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Converts maps with values of different types.
 * <p/>
 * <em>NOTE</em>. Need to be careful about calling Map methods that
 * update the map. For maps backed by hibernate, hibernate marks
 * them as dirty, even if no change is required, and triggers an update
 * (e.g,  calling removeAll(set) when none of set are present in the map).
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class MapConverter<K, VS, VT> {

    /**
     * Converts the source map to the target map.
     *
     * @param target the target map
     * @param source the source map
     */
    public void convert(Map<K, VT> target, Map<K, VS> source) {
        if (target.isEmpty()) {
            for (Map.Entry<K, VS> entry : source.entrySet()) {
                VT value = convert(entry.getValue());
                target.put(entry.getKey(), value);
            }
        } else if (source.isEmpty()) {
            if (!target.isEmpty()) {
                target.clear();
            }
        } else {
            Map<K, VT> retained = getRetained(target, source);

            Set<K> removed = getRemoved(target, retained);
            remove(target, removed);

            for (Map.Entry<K, VT> entry : retained.entrySet()) {
                VT value = entry.getValue();
                VS src = source.get(entry.getKey());
                convert(target, entry.getKey(), value, src);
            }
            Map<K, VS> added = getAdded(retained, source);
            for (Map.Entry<K, VS> entry : added.entrySet()) {
                VT value = convert(entry.getValue());
                target.put(entry.getKey(), value);
            }
        }
    }

    /**
     * Converts the source value to the target, adding it to the map if it
     * is different to any existing value.
     *
     * @param map    the target map
     * @param key    the key
     * @param target the target value
     * @param source the source value
     */
    protected void convert(Map<K, VT> map, K key, VT target, VS source) {
        VT newValue = convert(source);
        if (!ObjectUtils.equals(map.get(key), newValue)) {
            map.put(key, newValue);
        }
    }

    /**
     * Converts a value from the source to the target type.
     *
     * @param value the value to convert
     * @return the converted value
     */
    protected abstract VT convert(VS value);

    /**
     * Removes a set of values from a map.
     *
     * @param target the target map
     * @param set the keys to remove
     */
    protected void remove(Map<K, VT> target, Set<K> set) {
        if (!set.isEmpty()) {
            target.keySet().removeAll(set);
        }
    }

    /**
     * Help to return the keys of the objects that have been removed.
     *
     * @param target   the target map
     * @param retained the retained objects
     * @return the removed keys
     */
    protected Set<K> getRemoved(Map<K, VT> target, Map<K, VT> retained) {
        Map<K, VT> result = new HashMap<K, VT>(target);
        result.keySet().removeAll(retained.keySet());
        return result.keySet();
    }

    /**
     * Helper to return the objects that have keys in both the source and target
     * map.
     *
     * @param target the target map
     * @param source the source map
     * @return the objects in the target that have keys in the source
     */
    protected Map<K, VT> getRetained(Map<K, VT> target, Map<K, VS> source) {
        Map<K, VT> result = new HashMap<K, VT>(target);
        result.keySet().retainAll(source.keySet());
        return result;
    }

    /**
     * Helper to return the objects added to the source map.
     *
     * @param retained the objects that have keys in both source and target map
     * @param source   the source map
     * @return the the added objects
     */
    protected Map<K, VS> getAdded(Map<K, VT> retained, Map<K, VS> source) {
        Map<K, VS> result = new HashMap<K, VS>(source);
        result.keySet().removeAll(retained.keySet());
        return result;
    }

}
