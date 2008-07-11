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

package org.openvpms.component.business.dao.hibernate.im.common;

import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.ClassNameMustBeSpecified;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.ObjectNotFound;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * Abstract implementation of the {@link DeleteHandler} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractDeleteHandler implements DeleteHandler {

    /**
     * The assembler.
     */
    private final CompoundAssembler assembler;


    /**
     * Creates a new <tt>AbstractDeleteHandler<tt>.
     *
     * @param assembler the assembler
     */
    public AbstractDeleteHandler(CompoundAssembler assembler) {
        this.assembler = assembler;
    }

    /**
     * Deletes an object.
     *
     * @param object  the object to delete
     * @param session the session
     * @param context
     */
    public void delete(IMObject object, Session session, Context context) {
        if (!object.isNew()) {
            IMObjectDO target;
            IMObjectReference ref = object.getObjectReference();
            DOState state = context.getCached(ref);
            if (state != null) {
                target = state.getObject();
            } else {
                Class<? extends IMObjectDO> type
                        = assembler.getDOClass(object.getClass());
                if (type == null) {
                    throw new IMObjectDAOException(ClassNameMustBeSpecified);
                }
                target = context.get(ref, type);
            }
            if (target == null) {
                throw new IMObjectDAOException(ObjectNotFound, ref);
            }
            if (target.getVersion() != object.getVersion()) {
                throw new StaleObjectStateException(object.getClass().getName(),
                                                    object.getId());
            }
            delete(target, session);
            session.flush();
        }
    }

    protected void delete(IMObjectDO object, Session session) {
        session.delete(object);
    }

}
