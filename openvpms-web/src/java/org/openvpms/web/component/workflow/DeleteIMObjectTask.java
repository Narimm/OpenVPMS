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
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Task to delete an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DeleteIMObjectTask extends SynchronousTask {

    /**
     * The short name of the object to delete.
     */
    private final String shortName;


    /**
     * Constructs a new <tt>DeleteIMObjectTask</tt> to delete an object
     * in the {@link TaskContext}.
     *
     * @param shortName the short name of the object to delete
     */
    public DeleteIMObjectTask(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Executes the task.
     *
     * @throws OpenVPMSException for any error
     */
    public void execute(TaskContext context) {
        IMObject object = context.getObject(shortName);
        if (object != null && !object.isNew()) {
            ArchetypeServiceHelper.getArchetypeService().remove(object);
        }
    }

}
