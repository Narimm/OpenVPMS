/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Abstract implementation of the {@link Assembler} interface.
 *
 * @author Tim Anderson
 */
public abstract class IMObjectAssembler<T extends IMObject, DO extends IMObjectDO>
        extends AbstractAssembler implements Assembler {

    /**
     * The object type, or {@code null} if the type has no corresponding interface.
     */
    private final Class<? extends org.openvpms.component.model.object.IMObject> type;

    /**
     * The object type implementation.
     */
    private final Class<T> typeImpl;

    /**
     * The data object interface type.
     */
    private final Class<DO> typeDO;

    /**
     * The data object implementation type.
     */
    private final Class<? extends IMObjectDOImpl> implDO;

    /**
     * Converter for the {@link IMObject#getDetails()}  and
     * {@link IMObjectDO#getDetails()} maps.
     */
    private static final MapConverter<String, Object, Object> DETAILS
            = new MapConverter<String, Object, Object>() {
        protected Object convert(Object value) {
            return value;
        }
    };

    /**
     * Constructs an {@link IMObjectAssembler}.
     *
     * @param type     the object type, or {@code null} if the implementation type has no corresponding interface
     * @param typeImpl the object type implementation
     * @param typeDO   the data object interface type
     * @param implDO   the data object implementation type
     */
    public IMObjectAssembler(Class<? extends org.openvpms.component.model.object.IMObject> type, Class<T> typeImpl,
                             Class<DO> typeDO, Class<? extends IMObjectDOImpl> implDO) {
        this.type = type;
        this.typeImpl = typeImpl;
        this.typeDO = typeDO;
        this.implDO = implDO;
    }

    /**
     * Assembles an {@link IMObjectDO} from an {@link IMObject}.
     *
     * @param source  the object to assemble from
     * @param context the assembly context
     * @return the assembled object
     */
    public DOState assemble(IMObject source, Context context) {
        DOState state;
        DO target = null;
        boolean assembling;
        T object = typeImpl.cast(source);
        state = context.getCached(source);
        if (state == null) {
            // target not yet assembled from the source
            if (source.isNew()) {
                target = create(object);
                // need to populate the identifiers before adding to any HashMaps as when there is a hash collision,
                // the IMObjectDOImpl.equals() method is invoked.
                target.setId(source.getId());
                target.setLinkId(source.getLinkId());
                target.setArchetypeId(source.getArchetypeId());
            } else {
                target = load(source.getObjectReference(), typeDO, implDO, context);
                target = deproxy(target);
            }
            state = new DOState(target, source);
            assembling = false;
        } else {
            assembling = context.isAssembling(state);
            if (!assembling) {
                // updating assembled object
                target = typeDO.cast(state.getObject());
                state.update(source);
            }
        }

        if (!assembling) {
            // pre-cache just in case the graph is cyclic
            context.add(state, source);

            try {
                context.addAssembling(state);
                assembleDO(target, object, state, context);
            } finally {
                context.removeAssembling(state);
            }
        }
        return state;
    }

    /**
     * Assembles an {@link IMObjectDO} from an {@link IMObject}.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     * @return the assembled object
     */
    public DOState assemble(IMObjectDO target, IMObject source,
                            Context context) {
        DOState state = context.getCached(source);
        boolean assembling;
        if (state == null) {
            state = new DOState(target, source);
            assembling = false;
        } else {
            // updating assembled object
            assembling = context.isAssembling(state);
            target = typeDO.cast(state.getObject());
            state.update(source);
        }
        if (!assembling) {
            // pre-cache just in case the graph is cyclic
            context.add(state, source);

            try {
                context.addAssembling(state);
                assembleDO(typeDO.cast(target), typeImpl.cast(source), state, context);
            } finally {
                context.removeAssembling(state);
            }
        }
        return state;
    }

    /**
     * Assembles an {@link IMObject} from an {@link IMObjectDO}.
     *
     * @param source  the object to assemble from
     * @param context the assembly context
     * @return the assembled object
     */
    public IMObject assemble(IMObjectDO source, Context context) {
        DO object = typeDO.cast(source);
        T target = typeImpl.cast(context.getCached(source));
        if (target == null) {
            target = create(object);
            // pre-cache just in case the graph is cyclic
            context.add(target, source);

            try {
                context.addAssembling(target);
                assembleObject(target, object, context);
            } finally {
                context.removeAssembling(target);
            }
        }
        return target;
    }

    /**
     * Assembles an {@link IMObject} from an {@link IMObjectDO}.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     * @return the assembled object
     */
    public IMObject assemble(IMObject target, IMObjectDO source,
                             Context context) {
        if (!context.isAssembling(target)) {
            try {
                context.addAssembling(target);
                assembleObject(typeImpl.cast(target), typeDO.cast(source), context);
            } finally {
                context.removeAssembling(target);
            }
        }
        return target;
    }

    /**
     * Returns the object type.
     *
     * @return the object type, or {@code null} if the type has no corresponding interface
     */
    public Class<? extends org.openvpms.component.model.object.IMObject> getType() {
        return type;
    }

    /**
     * Returns the object type implementation.
     *
     * @return the object type implementation
     */
    public Class<? extends IMObject> getTypeImpl() {
        return typeImpl;
    }

    /**
     * Returns the data object interface type.
     *
     * @return the data object interface type
     */
    public Class<? extends IMObjectDO> getDOType() {
        return typeDO;
    }

    /**
     * Returns the data object implementation type.
     *
     * @return the data object implementation type
     */
    public Class<? extends IMObjectDOImpl> getDOImplType() {
        return implDO;
    }

    /**
     * Assembles a data object from an object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param state   the data object state
     * @param context the assembly context
     */
    protected void assembleDO(DO target, T source, DOState state,
                              Context context) {
        if (target.getId() != source.getId()) {
            target.setId(source.getId());
        }
        if (!ObjectUtils.equals(target.getLinkId(), source.getLinkId())) {
            target.setLinkId(source.getLinkId());
        }
        if (!ObjectUtils.equals(target.getArchetypeId(),
                                source.getArchetypeId())) {
            target.setArchetypeId(source.getArchetypeId());
        }
        if (target.getVersion() != source.getVersion()) {
            target.setVersion(source.getVersion());
        }

        if (target.isActive() != source.isActive()) {
            target.setActive(source.isActive());
        }
        if (!ObjectUtils.equals(target.getDescription(),
                                source.getDescription())) {
            target.setDescription(source.getDescription());
        }
        if (!ObjectUtils.equals(target.getLastModified(),
                                source.getLastModified())) {
            target.setLastModified(source.getLastModified());
        }
        if (!ObjectUtils.equals(target.getName(), source.getName())) {
            target.setName(source.getName());
        }

        DETAILS.convert(target.getDetails(), source.getDetails());
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    protected void assembleObject(T target, DO source, Context context) {
        target.setId(source.getId());
        target.setLinkId(source.getLinkId());
        target.setArchetypeId(source.getArchetypeId());
        target.setVersion(source.getVersion());

        target.setActive(source.isActive());
        target.setDescription(source.getDescription());
        target.setLastModified(source.getLastModified());
        target.setName(source.getName());

        DETAILS.convert(target.getDetails(), source.getDetails());
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected abstract T create(DO object);

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected abstract DO create(T object);

}
