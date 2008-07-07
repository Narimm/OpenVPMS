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

import java.util.HashMap;
import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class CompoundAssembler implements Assembler {

    private Map<Class, Assembler> assemblers
            = new HashMap<Class, Assembler>();

    public CompoundAssembler() {
    }

    public void addAssembler(IMObjectAssembler<? extends IMObject,
                             ? extends IMObjectDO> assembler) {
        assemblers.put(assembler.getType(), assembler);
    }

    public IMObjectDO assemble(IMObject source, Context context
    ) {
        Assembler assembler = assemblers.get(source.getClass());
        if (assembler == null) {
            throw new IllegalArgumentException(
                    "Unsupported type: " + source.getClass().getName());
        }
        return assembler.assemble(source, context);
    }

    public IMObject assemble(IMObjectDO source, Context context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IMObjectDO assemble(IMObjectDO target, IMObject source,
                               Context context) {
        Assembler assembler = assemblers.get(source.getClass());
        if (assembler == null) {
            throw new IllegalArgumentException(
                    "Unsupported type: " + source.getClass().getName());
        }
        return assembler.assemble(target, source, context);
    }

    public IMObject assemble(IMObject target, IMObjectDO source,
                             Context context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

