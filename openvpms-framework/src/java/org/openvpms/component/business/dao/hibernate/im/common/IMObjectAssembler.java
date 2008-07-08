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

    private static final DetailsMapConverter DETAILS = new DetailsMapConverter();

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

    public IMObjectDO assemble(IMObject source, Context context) {
        DO target;
        T object = type.cast(source);
        if (source.isNew()) {
            target = create(object);

            // pre-cache just in case the graph is cyclic
            context.add(source, target);
            // now assemble
            assembleDO(target, object, context);
        } else {
            target = typeDO.cast(context.getCached(source));
            if (target == null) {
                // target not yet assembled from the source
                target = get(source.getObjectReference(), typeDO, context);

                // pre-cache just in case the graph is cyclic
                context.add(source, target);
                // now assemble
                assembleDO(target, object, context);
            }
        }
        return target;
    }

    public IMObjectDO assemble(IMObjectDO target, IMObject source,
                               Context context) {
        assembleDO(typeDO.cast(target), type.cast(source), context);
        return target;
    }

    public Class<? extends IMObject> getType() {
        return type;
    }

    public Class<? extends IMObjectDO> getDOType() {
        return typeDO;
    }

    protected void assembleDO(DO result, T source, Context context) {
        result.setId(source.getId());
        result.setLinkId(source.getLinkId());
        result.setArchetypeId(source.getArchetypeId());
        result.setVersion(source.getVersion());

        result.setActive(source.isActive());
        result.setDescription(source.getDescription());
        result.setLastModified(source.getLastModified());
        result.setName(source.getName());

        DETAILS.convert(result.getDetails(), source.getDetails());
    }


    protected void assembleObject(T result, DO source, Context context) {
        result.setId(source.getId());
        result.setLinkId(source.getLinkId());
        result.setArchetypeId(source.getArchetypeId());
        result.setVersion(source.getVersion());

        result.setActive(source.isActive());
        result.setDescription(source.getDescription());
        result.setLastModified(source.getLastModified());
        result.setName(source.getName());

        result.setDetails(source.getDetails());
    }

    protected abstract T create(DO object);

    protected abstract DO create(T object);

}
