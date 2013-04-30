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

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.util.Iterator;


/**
 * Executes one or more queries, and adds the first matching result (if any) to
 * the task context.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class QueryIMObjectTask extends SynchronousTask {

    /**
     * Creates a new <tt>QueryIMObjectTask</tt>.
     */
    public QueryIMObjectTask() {
    }

    /**
     * Executes the task.
     *
     * @throws OpenVPMSException for any error
     */
    public void execute(TaskContext context) {
        for (ArchetypeQuery query : getQueries(context)) {
            query.setMaxResults(1);
            Iterator<IMObject> iterator
                = new IMObjectQueryIterator<IMObject>(query);
            if (iterator.hasNext()) {
                IMObject object = iterator.next();
                context.addObject(object);
                break;
            }
        }
    }

    /**
     * Returns the queries to execute.
     *
     * @param context the task context
     * @return the queries
     */
    protected abstract ArchetypeQuery[] getQueries(TaskContext context);

}
