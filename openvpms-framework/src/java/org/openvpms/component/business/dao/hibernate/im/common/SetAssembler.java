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
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SetAssembler<T extends IMObject, DO extends IMObjectDO>
        extends AbstractAssembler {

    private final Class<T> type;

    private final Class<? extends DO> typeDO;

    public SetAssembler(Class<T> type, Class typeDO) {
        this.type = type;
        this.typeDO = typeDO;
    }

    public static <T extends IMObject, DO extends IMObjectDO>
    SetAssembler<T, DO> create(Class<T> type, Class typeDO) {
        return new SetAssembler<T, DO>(type, typeDO);
    }

    public void assembleDO(Set<DO> target, Set<T> source, DOState state,
                           Context context) {
        if (target.isEmpty()) {
            for (T src : source) {
                DOState result = getDO(src, context);
                target.add(typeDO.cast(result.getObject()));
                state.addState(result);
            }
        } else if (source.isEmpty()) {
            if (!target.isEmpty()) {
                target.clear();
            }
        } else {
            Assembler assembler = context.getAssembler();
            Map<IMObjectReference, DO> targetMap = getDO(target);
            Map<IMObjectReference, T> sourceMap = get(source);
            Map<IMObjectReference, DO> retained = getRetained(targetMap,
                                                              sourceMap);
            for (DO removed : getRemoved(targetMap, retained)) {
                target.remove(removed);
                state.removeState(removed);
                context.remove(removed);
            }
            for (Map.Entry<IMObjectReference, DO> entry : retained.entrySet()) {
                DO current = entry.getValue();
                T src = sourceMap.get(entry.getKey());
                DOState result = assembler.assemble(current, src, context);
                state.addState(result);
            }
            for (T added : getAdded(retained, sourceMap)) {
                DOState result = getDO(added, context);
                target.add(typeDO.cast(result.getObject()));
                state.addState(result);
            }
        }
    }

    public void assembleObject(Set<T> target, Set<DO> source, Context context) {
        if (!target.isEmpty()) {
            target.clear();
        }
        if (!source.isEmpty()) {
            for (DO src : source) {
                T result = getObject(src, type, context);
                target.add(result);
            }
        }
    }

    private Map<IMObjectReference, T> get(Set<T> set) {
        Map<IMObjectReference, T> result = new HashMap<IMObjectReference, T>();
        for (T object : set) {
            result.put(object.getObjectReference(), object);
        }
        return result;
    }

    private Map<IMObjectReference, DO> getDO(Set<DO> set) {
        Map<IMObjectReference, DO> result
                = new HashMap<IMObjectReference, DO>();
        for (DO object : set) {
            result.put(object.getObjectReference(), object);
        }
        return result;
    }

    private Collection<DO> getRemoved(Map<IMObjectReference, DO> target,
                                      Map<IMObjectReference, DO> retained) {
        Map<IMObjectReference, DO> result = new HashMap<IMObjectReference, DO>(
                target);
        result.keySet().removeAll(retained.keySet());
        return result.values();
    }

    private Map<IMObjectReference, DO> getRetained(
            Map<IMObjectReference, DO> target,
            Map<IMObjectReference, T> source) {
        Map<IMObjectReference, DO> result
                = new HashMap<IMObjectReference, DO>(target);
        result.keySet().retainAll(source.keySet());
        return result;
    }

    private Collection<T> getAdded(Map<IMObjectReference, DO> retained,
                                   Map<IMObjectReference, T> source) {
        Map<IMObjectReference, T> result
                = new HashMap<IMObjectReference, T>(source);
        result.keySet().removeAll(retained.keySet());
        return result.values();
    }

}
