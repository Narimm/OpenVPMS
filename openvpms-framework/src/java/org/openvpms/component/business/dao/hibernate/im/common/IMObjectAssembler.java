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
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class IMObjectAssembler<T extends IMObject,
        DO extends IMObjectDO> extends AbstractAssembler implements Assembler {

    private final Class<T> type;

    private final Class<DO> typeDO;
    private final Class<? extends IMObjectDOImpl> impl;

    private static final DetailsMapConverter DETAILS
            = new DetailsMapConverter();

    public IMObjectAssembler(Class<T> type, Class<DO> typeDO,
                             Class<? extends IMObjectDOImpl> impl) {
        this.type = type;
        this.typeDO = typeDO;
        this.impl = impl;
    }

    public IMObject assemble(IMObjectDO source, Context context) {
        DO object = typeDO.cast(source);
        T target = type.cast(context.getCached(source));
        if (target == null) {
            target = create(object);
            // pre-cache just in case the graph is cyclic
            context.add(source, target);

            try {
                context.addAssembling(target);
                assembleObject(target, object, context);
            } finally {
                context.removeAssembling(target);
            }
        }
        return target;
    }

    public IMObject assemble(IMObject target, IMObjectDO source,
                             Context context) {
        if (!context.isAssembling(target)) {
            try {
                context.addAssembling(target);
                assembleObject(type.cast(target), typeDO.cast(source), context);
            } finally {
                context.removeAssembling(target);
            }
        }
        return target;
    }

    public DOState assemble(IMObject source, Context context) {
        DOState state;
        DO target = null;
        boolean assembling;
        T object = type.cast(source);
        state = context.getCached(source);
        if (state == null) {
            // target not yet assembled from the source
            if (source.isNew()) {
                target = create(object);
            } else {
                target = load(source.getObjectReference(), typeDO, impl,
                              context);
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
            context.add(source, state);

            try {
                context.addAssembling(state);
                assembleDO(target, object, state, context);
            } finally {
                context.removeAssembling(state);
            }
        }
        return state;
    }

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
            context.add(source, state);

            try {
                context.addAssembling(state);
                assembleDO(typeDO.cast(target), type.cast(source), state,
                           context);
            } finally {
                context.removeAssembling(state);
            }
        }
        return state;
    }

    public Class<? extends IMObject> getType() {
        return type;
    }

    public Class<? extends IMObjectDO> getDOType() {
        return typeDO;
    }

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

    protected abstract T create(DO object);

    protected abstract DO create(T object);

}
