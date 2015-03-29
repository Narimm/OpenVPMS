/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.retry.AbstractRetryable;
import org.openvpms.web.component.retry.Retryable;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.system.ServiceHelper;

/**
 * An {@link UpdateIMObjectTask} that will retry the update if it fails.
 *
 * @author Tim Anderson
 * @see Retryer
 */
public class RetryableUpdateIMObjectTask extends UpdateIMObjectTask {

    /**
     * Constructs an {@link RetryableUpdateIMObjectTask}.
     * The object is saved on update.
     *
     * @param shortName  the short name of the object to update
     * @param properties properties to populate the object with
     */
    public RetryableUpdateIMObjectTask(String shortName, TaskProperties properties) {
        super(shortName, properties);
    }

    /**
     * Executes the task.
     *
     * @throws OpenVPMSException for any error
     */
    @Override
    public void execute(final TaskContext context) {
        Retryable action = new AbstractRetryable() {
            @Override
            protected boolean runFirst() {
                return update(true, context);
            }

            @Override
            protected boolean runAction() {
                return update(false, context);
            }
        };
        if (!Retryer.run(action)) {
            notifyCancelled();
        }
    }

    private boolean update(boolean first, TaskContext context) {
        IMObject object = getObject(context);
        if (!first) {
            object = IMObjectHelper.reload(object);
        }
        if (object != null) {
            populate(object, context);
            ServiceHelper.getArchetypeService().save(object);
            return true;
        }
        return false;
    }
}
