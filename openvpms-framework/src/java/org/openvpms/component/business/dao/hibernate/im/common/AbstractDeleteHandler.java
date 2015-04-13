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
import org.openvpms.component.business.domain.im.common.IMObject;


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
     * @param context the assembly context
     */
    public void delete(IMObject object, Session session, Context context) {
        if (!object.isNew()) {
            DOState state = assembler.assemble(object, context);
            delete(state.getObject(), session, context);
            session.flush();
        }
    }

    /**
     * Deletes an object.
     *
     * @param object  the object to delete
     * @param session the hibernate session
     * @param context the assembly context
     */
    protected void delete(IMObjectDO object, Session session, Context context) {
        context.remove(object);
    }

}
