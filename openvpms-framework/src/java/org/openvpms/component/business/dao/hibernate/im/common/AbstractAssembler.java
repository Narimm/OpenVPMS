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

import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.ObjectNotFound;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractAssembler {

    protected <T extends IMObject, DO extends IMObjectDO> DO
            getDO(T source, Class<DO> type, Context context) {
        if (source == null) {
            return null;
        }
        IMObjectDO object = context.getCached(source);
        if (object == null) {
            Assembler assembler = context.getAssembler();
            object = assembler.assemble(source, context);
        }
        return type.cast(object);
    }

    protected <DO extends IMObjectDO, T extends IMObject> T
            getObject(DO source, Class<T> type, Context context) {
        if (source == null) {
            return null;
        }
        IMObject object = context.getCached(source);
        if (object == null) {
            Assembler assembler = context.getAssembler();
            object = assembler.assemble(source, context);
        }
        return type.cast(object);
    }

    /**
     * Retrieves an object given its reference.
     *
     * @param reference the reference
     * @param type      the object type
     * @param context   the context
     * @return the corresponding object, or <tt>null</tt> if the reference is
     *         null
     * @throws IMObjectDAOException if the object doesn't exist
     */
    protected <DO extends IMObjectDO> DO get(IMObjectReference reference,
                                             Class<DO> type, Context context) {
        if (reference == null) {
            return null;
        }
        IMObjectDO object = context.getCached(reference);
        if (object != null) {
            return type.cast(object);
        }
        DO result = context.get(reference, type);
        if (result == null) {
            throw new IMObjectDAOException(ObjectNotFound, reference);
        }
        return result;
    }
}
