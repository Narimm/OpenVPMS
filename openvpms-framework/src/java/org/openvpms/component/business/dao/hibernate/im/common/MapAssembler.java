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

import java.util.Map;
import java.util.Set;


/**
 * Provides support to asemble maps of {@link IMObject}s from maps of
 * {@link IMObjectDO}s, and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MapAssembler<K, T extends IMObject, DO extends IMObjectDO>
        extends AbstractAssembler {

    /**
     * The object type.
     */
    private final Class<T> type;


    /**
     * Creates a new <tt>MapAssembler</tt>.
     *
     * @param type the object type
     */
    public MapAssembler(Class<T> type) {
        this.type = type;
    }

    /**
     * Creates a new assembler.
     *
     * @param type the object type
     * @return a new assembler
     */
    public static <K, T extends IMObject, DO extends IMObjectDO>
    MapAssembler<K, T, DO> create(Class<T> type) {
        return new MapAssembler<K, T, DO>(type);
    }


    /**
     * Assembles a map containing <tt>IMObjectDO</tt>s from a map containing
     * <tt>IMObject</tt>s.
     *
     * @param target  the map to assemble
     * @param source  the map to assemble from
     * @param state   the parent data object state
     * @param context the assembly context
     */
    public void assembleDO(Map<K, DO> target, Map<K, T> source,
                           DOState state, Context context) {
        DOMapConverter converter = new DOMapConverter(state, context);
        converter.convert(target, source);
    }

    /**
     * Assembles a map containing <tt>IMObject</tt>s from a map containing
     * <tt>IMObjectDO</tt>s.
     *
     * @param target  the map to assemble
     * @param source  the map to assemble from
     * @param context the assembly context
     */
    public void assembleObject(Map<K, T> target, Map<K, DO> source,
                               Context context) {
        if (!target.isEmpty()) {
            target.clear();
        }
        if (!source.isEmpty()) {
            for (Map.Entry<K, DO> entry : source.entrySet()) {
                T result = getObject(entry.getValue(), type, context);
                target.put(entry.getKey(), result);
            }
        }
    }

    /**
     * Converts maps containing IMObjects to ones containing IMObjectDOs, and
     * vice-versa.
     */
    private class DOMapConverter extends MapConverter<K, T, DO> {

        /**
         * The parent data object state.
         */
        private final DOState state;

        /**
         * The assembly context.
         */
        private final Context context;

        /**
         * Creates a new <tt>DOMapConverter</tt>.
         *
         * @param state   the parent data object state
         * @param context the assembly context
         */
        public DOMapConverter(DOState state, Context context) {
            this.state = state;
            this.context = context;
        }

        /**
         * Assembles an <tt>IMObjectDO</tt> from an <tt>IMObject</tt>.
         *
         * @param map    the map that owns the target object
         * @param key    the key
         * @param target the value to assemble
         * @param source the value to assemble from
         */
        @Override
        protected void convert(Map<K, DO> map, K key, DO target, T source) {
            Assembler assembler = context.getAssembler();
            DOState child = assembler.assemble(target, source, context);
            state.addState(child);
        }

        /**
         * Converts a value from the source to the target type.
         *
         * @param value the value to convert
         * @return the converted value
         */
        @SuppressWarnings("unchecked")
        protected DO convert(T value) {
            DOState child = getDO(value, context);
            state.addState(child);
            return (DO) child.getObject();
        }

        /**
         * Removes a set of values from a map.
         * <p/>
         * This implementation removes the object from the context.
         *
         * @param target the target map
         * @param set    the keys to remove
         */
        @Override
        protected void remove(Map<K, DO> target, Set<K> set) {
            for (K key : set) {
                DO object = target.get(key);
                state.removeState(object);
                context.remove(object);
            }
            super.remove(target, set);
        }
    }

}
