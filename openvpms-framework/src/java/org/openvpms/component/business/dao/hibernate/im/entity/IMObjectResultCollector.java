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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of {@link ResultCollector} that collects {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectResultCollector
        extends HibernateResultCollector<IMObject> {

    /**
     * The results.
     */
    private List<IMObject> result = new ArrayList<IMObject>();

    public IMObjectResultCollector() {
    }

    /**
     * Collects an object.
     *
     * @param object the object to collect. Must be an instance of
     *               <code>IMObject</code>
     */
    public void collect(Object object) {
        if (!(object instanceof IMObjectDO)) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.CannotCollectObject,
                    object.getClass().getName());
        }
        Context context = getContext();
        Assembler assembler = context.getAssembler();
        result.add(assembler.assemble((IMObjectDO) object, context));
    }

    /**
     * Returns the results.
     *
     * @return the results
     */
    protected List<IMObject> getResults() {
        return result;
    }
}
