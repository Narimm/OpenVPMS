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
     * The data object implementation type.
     */
    private final Class<? extends IMObjectDOImpl> typeDO;

    /**
     * Creates a new <tt>MapAssembler</tt>.
     *
     * @param type the object type
     * @param typeDO the data object implementation type
     */
    public MapAssembler(Class<T> type, Class<? extends IMObjectDOImpl> typeDO) {
        this.type = type;
        this.typeDO = typeDO;
    }

    public static <K, T extends IMObject, DO extends IMObjectDO>
    MapAssembler<K, T, DO> create(Class<T> type,
                                  Class<? extends IMObjectDOImpl> typeDO) {
        return new MapAssembler<K, T, DO>(type, typeDO);
    }

    public void assembleDO(Map<K, DO> target, Map<K, T> source,
                           DOState state, Context context) {
        DOMapConverter converter = new DOMapConverter(state, context);
        converter.convert(target, source);
    }

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

    private class DOMapConverter extends MapConverter<K, T, DO> {

        private final DOState state;
        private final Context context;

        public DOMapConverter(DOState state, Context context) {
            this.state = state;
            this.context = context;
        }

        @Override
        protected void convert(Map<K, DO> map, K key, DO target, T source) {
            Assembler assembler = context.getAssembler();
            DOState child = assembler.assemble(target, source, context);
            state.addState(child);
        }

        protected DO convert(T value) {
            DOState child = getDO(value, context);
            state.addState(child);
            return (DO) child.getObject();
        }

        @Override
        protected void remove(Map<K, DO> target, Set<K> removed) {
            for (K key : removed) {
                DO object = target.get(key);
                state.removeState(object);
                context.remove(object);
            }
            super.remove(target, removed);
        }
    }

}
