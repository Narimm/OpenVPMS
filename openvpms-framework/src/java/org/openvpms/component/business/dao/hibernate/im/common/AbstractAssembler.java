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
 * Base class for assemblers.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractAssembler {

    /**
     * Returns the data object for the specified source.
     * If the object isn't already cached in the context, it will be assembled.
     *
     * @param source  the object to assemble from. May be <tt>null</tt>
     * @param context the assembly context
     * @return the assembled object, or <tt>null</tt> if <tt>source</tt> is null
     */
    protected <T extends IMObject> DOState getDO(T source, Context context) {
        if (source == null) {
            return null;
        }
        DOState state = context.getCached(source);
        if (state == null) {
            Assembler assembler = context.getAssembler();
            state = assembler.assemble(source, context);
        }
        return state;
    }

    /**
     * Returns the object for the specified data object.
     * If the object isn't already cached in the context, it will be assembled.
     *
     * @param source  the object to assemble from. May be <tt>null</tt>
     * @param type    the object type
     * @param context the assembly context
     * @return the assembled object, or <tt>null</tt> if <tt>source</tt> is null
     */
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
     * Retrieves a data object given its reference.
     *
     * @param reference the object reference. May be <tt>null</tt>
     * @param type      the data object interface type
     * @param impl      the data object implementation type
     * @param context   the assembly context
     * @return the data object, or <tt>null</tt> if it doesn't exist or
     *         <tt>reference</tt> is null
     */
    protected <DO extends IMObjectDO, Impl extends IMObjectDOImpl> DOState
            get(IMObjectReference reference, Class<DO> type, Class<Impl> impl,
                Context context) {
        if (reference == null) {
            return null;
        }
        DOState state = context.getCached(reference);
        if (state != null) {
            return state;
        }
        if (reference.getId() != -1) {
            DO result = context.get(reference, type, impl);
            if (result != null) {
                return new DOState(result);
            }
        }
        return null;
    }

    /**
     * Retrieves a data object given its reference.
     * If the object doesn't exist, an exception will be thrown.
     *
     * @param reference the object reference
     * @param type      the data object interface type
     * @param impl      the data object implementation type
     * @param context   the assembly context
     * @return the corresponding data object
     * @throws IMObjectDAOException if the object doesn't exist
     */
    protected <DO extends IMObjectDO, Impl extends IMObjectDOImpl> DO
            load(IMObjectReference reference, Class<DO> type, Class<Impl> impl,
                 Context context) {
        DOState result = get(reference, type, impl, context);
        if (result == null) {
            throw new IMObjectDAOException(ObjectNotFound, reference);
        }
        return type.cast(result.getObject());
    }

    /**
     * Helper to deproxy an object if required.
     *
     * @param object the potentially proxied object
     * @return the deproxied object, or <tt>object</tt> if it wasn't proxied
     */
    @SuppressWarnings("unchecked")
    protected <DO extends IMObjectDO> DO deproxy(DO object) {
        return (DO) HibernateHelper.deproxy(object);
    }

}
