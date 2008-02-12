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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.hibernate.Session;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.Collection;


/**
 * Implementation of {@link IMObjectSessionHandler} for {@link Entity} instances.
 * x
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class EntitySessionHandler extends AbstractIMObjectSessionHandler {

    /**
     * Default session handler for updating child objects.
     */
    private final IMObjectSessionHandler defaultHandler;


    /**
     * Creates a new <tt>EntitySessionHandler<tt>.
     *
     * @param dao the DAO
     */
    public EntitySessionHandler(IMObjectDAO dao) {
        super(dao);
        defaultHandler = new DefaultIMObjectSessionHandler(dao);
    }

    /**
     * Saves an object.
     *
     * @param object  the object to merge
     * @param session the session to use
     * @return the result of <tt>Session.merge(object)</tt>
     */
    @Override
    public IMObject save(IMObject object, Session session) {
        Entity entity = (Entity) object;
        saveNew(entity.getEntityRelationships(), session);
        saveNew(entity.getIdentities(), session);
        return super.save(object, session);
    }

    /**
     * Updates the target object with the identifier and version of the source.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    @Override
    public void updateIds(IMObject target, IMObject source) {
        Entity targetEntity = (Entity) target;
        Entity sourceEntity = (Entity) source;
        update(targetEntity.getEntityRelationships(),
               sourceEntity.getEntityRelationships(), defaultHandler);
        update(targetEntity.getIdentities(), sourceEntity.getIdentities(),
               defaultHandler);
        super.updateIds(target, source);
    }

    /**
     * Updates the target objects with the identifier and version of the their
     * corresponding sources, using an {@link DefaultIMObjectSessionHandler}.
     *
     * @param targets the targets to update
     * @param sources the sources to update from
     */
    protected <T extends IMObject> void update(Collection<T> targets,
                                               Collection<T> sources) {
        super.update(targets, sources, defaultHandler);
    }

}
