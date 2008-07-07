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
import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractAssembler {

    protected <A extends IMObject, B extends IMObjectDO> B
            get(A source, Class<B> type, Context context) {
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

    protected <A extends IMObjectDO> A get(IMObjectReference reference,
                                           Class<A> type, Context context) {
        if (reference == null) {
            return null;
        }
        IMObjectDO object = context.getCached(reference);
        if (object != null) {
            return type.cast(object);
        }
        return context.get(reference, type);
    }
}
