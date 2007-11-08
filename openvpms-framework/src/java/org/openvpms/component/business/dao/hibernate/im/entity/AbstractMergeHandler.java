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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Abstract implementation of the {@link MergeHandler} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
abstract class AbstractMergeHandler implements MergeHandler {

    /**
     * Merges an object.
     *
     * @param object  the object to merge
     * @param session the session to use
     * @return the result of <tt>Session.merge(object)</tt>
     */
    public IMObject merge(IMObject object, Session session) {
        return (IMObject) session.merge(object);
    }

    /**
     * Updates the target object with the identifier and version of the source.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    public void update(IMObject target, IMObject source) {
        updateId(target, source);
    }

    /**
     * Helper to save any transient instances.
     *
     * @param objects the objects to check
     * @param session the session
     */
    protected <T extends IMObject> void save(Collection<T> objects,
                                             Session session) {
        for (T object : objects) {
            if (object.isNew()) {
                session.save(object);
            }
        }
    }

    /**
     * Updates the target objects with the identifier and version of the their
     * corresponding sources.
     *
     * @param targets the targets to update
     * @param sources the sources to update from
     */
    protected <T extends IMObject> void update(Collection<T> targets,
                                               Collection<T> sources) {
        if (!targets.isEmpty()) {
            Map<IMObjectReference, T> map = new HashMap<IMObjectReference, T>();
            for (T source : sources) {
                map.put(source.getObjectReference(), source);
            }
            for (T target : targets) {
                T source = map.get(target.getObjectReference());
                if (source != null) {
                    updateId(target, source);
                }
            }
        }
    }

    /**
     * Updates the target object with the identifier and version of the source.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    private void updateId(IMObject target, IMObject source) {
        if (target.getUid() != source.getUid()) {
            target.setUid(source.getUid());
        }
        if (target.getVersion() != source.getVersion()) {
            target.setVersion(source.getVersion());
        }
    }

}
