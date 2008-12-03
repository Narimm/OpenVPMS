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
 * Provides support to asemble sets of {@link IMObject}s from sets of
 * {@link IMObjectDO}s, and vice-versa.
 * *
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SetAssembler<T extends IMObject, DO extends IMObjectDO>
        extends AbstractAssembler {

    /**
     * The object type.
     */
    private final Class<T> type;

    /**
     * The data object implementation type.
     */
    private final Class<? extends DO> typeDO;

    /**
     * Determines if objects are referenced, or owned by the set.
     */
    private final boolean referenced;


    /**
     * Creates a new <tt>SetAssembler</tt>.
     *
     * @param type       the object type
     * @param typeDO     the data object implementation type
     * @param referenced determines if objects are referenced or owned by the
     *                   set
     */
    @SuppressWarnings("unchecked")
    public SetAssembler(Class<T> type, Class typeDO, boolean referenced) {
        this.type = type;
        this.typeDO = typeDO;
        this.referenced = referenced;
    }

    /**
     * Creates a new assembler.
     * <p/>
     * Use this when the set owns the objects it contains.
     *
     * @param type   the object type
     * @param typeDO the data object implementation type
     * @return a new assembler
     */
    public static <T extends IMObject, DO extends IMObjectDO>
    SetAssembler<T, DO> create(Class<T> type, Class typeDO) {
        return create(type, typeDO, false);
    }

    /**
     * Creates a new assembler.
     *
     * @param type       the object type
     * @param typeDO     the data object implementation type
     * @param referenced if <t>true</tt>, objects are referenced by the set,
     *                   otherwise they are owned by it
     * @return a new assembler
     */
    public static <T extends IMObject, DO extends IMObjectDO>
    SetAssembler<T, DO> create(Class<T> type, Class typeDO,
                               boolean referenced) {
        return new SetAssembler<T, DO>(type, typeDO, referenced);
    }

    /**
     * Assembles a set containing <tt>IMObjectDO</tt>s from a set containing
     * <tt>IMObject</tt>s.
     *
     * @param target  the set to assemble
     * @param source  the set to assemble from
     * @param state   the parent data object state
     * @param context the assembly context
     */
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
                if (!referenced) {
                    context.remove(removed);
                }
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

    /**
     * Assembles a set containing <tt>IMObject</tt>s from a set containing
     * <tt>IMObjectDO</tt>s.
     *
     * @param target  the set to assemble
     * @param source  the set to assemble from
     * @param context the assembly context
     */
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

    /**
     * Helper to return a map of objects keyed on reference.
     *
     * @param set the set of objects
     * @return the objects keyed on their references
     */
    private Map<IMObjectReference, T> get(Set<T> set) {
        Map<IMObjectReference, T> result = new HashMap<IMObjectReference, T>();
        for (T object : set) {
            result.put(object.getObjectReference(), object);
        }
        return result;
    }

    /**
     * Helper to return a map of data objects keyed on reference.
     *
     * @param set the set of data objects
     * @return the data objects keyed on their references
     */
    private Map<IMObjectReference, DO> getDO(Set<DO> set) {
        Map<IMObjectReference, DO> result
                = new HashMap<IMObjectReference, DO>();
        for (DO object : set) {
            result.put(object.getObjectReference(), object);
        }
        return result;
    }

    /**
     * Help to return the data objects that have been removed from the source
     * map, and need to be removed from the target.
     *
     * @param target   the target map
     * @param retained the retained objects
     * @return the removed data objects
     */
    private Collection<DO> getRemoved(Map<IMObjectReference, DO> target,
                                      Map<IMObjectReference, DO> retained) {
        Map<IMObjectReference, DO> result = new HashMap<IMObjectReference, DO>(
                target);
        result.keySet().removeAll(retained.keySet());
        return result.values();
    }

    /**
     * Helper to return the objects that are present in both the source and
     * target map.
     *
     * @param target the target map
     * @param source the source map
     * @return the objects in the target that have keys in the source
     */
    private Map<IMObjectReference, DO> getRetained(
            Map<IMObjectReference, DO> target,
            Map<IMObjectReference, T> source) {
        Map<IMObjectReference, DO> result
                = new HashMap<IMObjectReference, DO>(target);
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
    private Collection<T> getAdded(Map<IMObjectReference, DO> retained,
                                   Map<IMObjectReference, T> source) {
        Map<IMObjectReference, T> result
                = new HashMap<IMObjectReference, T>(source);
        result.keySet().removeAll(retained.keySet());
        return result.values();
    }

}
