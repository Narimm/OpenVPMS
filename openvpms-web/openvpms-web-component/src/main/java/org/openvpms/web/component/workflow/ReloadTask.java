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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.resource.i18n.Messages;

import static org.openvpms.web.component.app.ContextException.ErrorCode.NoObject;


/**
 * Task to reload an object into the context.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReloadTask extends SynchronousTask {

    /**
     * The short name of the object to reload.
     */
    private final String shortName;


    /**
     * Creates a new <tt>ReloadTask</tt>.
     *
     * @param shortName the archetype short name of the object to reload
     */
    public ReloadTask(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Executes the task.
     *
     * @throws OpenVPMSException for any error
     */
    public void execute(TaskContext context) {
        IMObject object = context.getObject(shortName);
        if (object == null) {
            throw new ContextException(NoObject, shortName);
        } else {
            IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
            object = service.get(object.getObjectReference());
            if (object == null) {
                ErrorDialog.show(Messages.get("imobject.noexist", DescriptorHelper.getDisplayName(shortName)));
                notifyCancelled();
            } else {
                context.setObject(shortName, object);
            }
        }
    }

}
