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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class MapConverter<K, VS, VT> {

    public void convert(Map<K, VT> target, Map<K, VS> source) {
        if (target.isEmpty()) {
            for (Map.Entry<K, VS> entry : source.entrySet()) {
                VT value = convert(entry.getValue());
                target.put(entry.getKey(), value);
            }
        } else if (source.isEmpty()) {
            target.clear();
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

    protected void convert(Map<K, VT> map, K key, VT target, VS source) {
        map.put(key, convert(source));
    }

    protected abstract VT convert(VS value);

    protected void remove(Map<K, VT> target, Set<K> removed) {
        target.keySet().removeAll(removed);
    }


    protected Set<K> getRemoved(Map<K, VT> target, Map<K, VT> retained) {
        Map<K, VT> result = new HashMap<K, VT>(target);
        result.keySet().removeAll(retained.keySet());
        return result.keySet();
    }

    protected Map<K, VT> getRetained(Map<K, VT> target, Map<K, VS> source) {
        Map<K, VT> result = new HashMap<K, VT>(target);
        result.keySet().retainAll(source.keySet());
        return result;
    }

    protected Map<K, VS> getAdded(Map<K, VT> retained, Map<K, VS> source) {
        Map<K, VS> result = new HashMap<K, VS>(source);
        result.keySet().removeAll(retained.keySet());
        return result;
    }

}
