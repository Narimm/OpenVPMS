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

    private static final DetailsMapConverter DETAILS
            = new DetailsMapConverter();

    public IMObjectAssembler(Class<T> type, Class<DO> typeDO) {
        this.type = type;
        this.typeDO = typeDO;
    }

    public IMObject assemble(IMObjectDO source, Context context) {
        DO object = typeDO.cast(source);
        T target = type.cast(context.getCached(source));
        if (target == null) {
            target = create(object);
            // pre-cache just in case the graph is cyclic
            context.add(source, target);

            assembleObject(target, object, context);
        }
        return target;
    }

    public IMObject assemble(IMObject target, IMObjectDO source,
                             Context context) {
        assembleObject(type.cast(target), typeDO.cast(source), context);
        return target;
    }

    public DOState assemble(IMObject source, Context context) {
        DOState state;
        DO target;
        T object = type.cast(source);
        state = context.getCached(source);
        if (state == null) {
            // target not yet assembled from the source
            if (source.isNew()) {
                target = create(object);
            } else {
                target = load(source.getObjectReference(), typeDO, context);
            }
            state = new DOState(target, source);
        } else {
            // updating assembled object
            target = typeDO.cast(state.getObject());
            state.update(source);
        }

        // pre-cache just in case the graph is cyclic
        context.add(source, state);
        // now assemble
        assembleDO(target, object, state, context);
        return state;
    }

    public DOState assemble(IMObjectDO target, IMObject source,
                            Context context) {
        DOState state = context.getCached(source);
        if (state == null) {
            state = new DOState(target, source);
        } else {
            // updating assembled object
            target = typeDO.cast(state.getObject());
            state.update(source);
        }
        // pre-cache just in case the graph is cyclic
        context.add(source, state);

        assembleDO(typeDO.cast(target), type.cast(source), state, context);
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
