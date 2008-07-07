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

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MapAssembler<K, T extends IMObject, DO extends IMObjectDO>
        extends AbstractAssembler {

    private final Class<T> type;

    private final Class<DO> typeDO;

    public MapAssembler(Class<T> type, Class<DO> typeDO) {
        this.type = type;
        this.typeDO = typeDO;
    }

    public static <K, T extends IMObject, DO extends IMObjectDO>
    MapAssembler<K, T, DO> create(Class<T> type, Class<DO> typeDO) {
        return new MapAssembler<K, T, DO>(type, typeDO);
    }

    public void assemble(Map<K, DO> target, Map<K, T> source, Context context
    ) {
        if (target.isEmpty()) {
            for (Map.Entry<K, T> entry : source.entrySet()) {
                DO result = get(entry.getValue(), typeDO, context);
                target.put(entry.getKey(), result);
            }
        } else if (source.isEmpty()) {
            target.clear();
        } else {
            Assembler assembler = context.getAssembler();
            Map<K, DO> retained = getRetained(target, source);

            Set<K> removed = getRemoved(target, retained);
            target.keySet().removeAll(removed);

            for (Map.Entry<K, DO> entry : retained.entrySet()) {
                DO result = entry.getValue();
                T src = source.get(entry.getKey());
                assembler.assemble(result, src, context);
            }
            Map<K, T> added = getAdded(retained, source);
            for (Map.Entry<K, T> entry : added.entrySet()) {
                DO result = get(entry.getValue(), typeDO, context);
                target.put(entry.getKey(), result);
            }
        }
    }

    private Set<K> getRemoved(Map<K, DO> target, Map<K, DO> retained) {
        Map<K, DO> result = new HashMap<K, DO>(target);
        result.keySet().removeAll(retained.keySet());
        return result.keySet();
    }

    private Map<K, DO> getRetained(Map<K, DO> target, Map<K, T> source) {
        Map<K, DO> result = new HashMap<K, DO>(target);
        result.keySet().retainAll(source.keySet());
        return result;
    }

    private Map<K, T> getAdded(Map<K, DO> retained, Map<K, T> source) {
        Map<K, T> result = new HashMap<K, T>(source);
        result.keySet().removeAll(retained.keySet());
        return result;
    }

}
