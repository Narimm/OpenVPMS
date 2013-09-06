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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.system.ServiceHelper;


/**
 * Prints an {@link Act}. On successful printing, updates the {@code printed} flag, if the act has one.
 *
 * @author Tim Anderson
 */
public class PrintActTask extends PrintIMObjectTask {

    /**
     * Constructs a {@link PrintActTask}.
     *
     * @param act         the act to print
     * @param context     the mail context. May be {@code null}
     * @param interactive if {@code true} print interactively, otherwise attempt to print in the background
     */
    public PrintActTask(Act act, MailContext context, boolean interactive) {
        super(act, context, interactive);
    }

    /**
     * Constructs a {@link PrintActTask}.
     *
     * @param shortName the short name of the act to print
     * @param context   the mail context. May be {@code null}
     */
    public PrintActTask(String shortName, MailContext context) {
        super(shortName, context);
    }

    /**
     * Invoked when the object is successfully printed.
     *
     * @param object  the printed object
     * @param context the task context
     */
    @Override
    protected void onPrinted(IMObject object, TaskContext context) {
        try {
            if (setPrintStatus(object)) {
                ServiceHelper.getArchetypeService().save(object);
            }
            super.onPrinted(object, context);
        } catch (OpenVPMSException exception) {
            notifyCancelledOnError(exception);
        }
    }

    /**
     * Sets the print status of an object.
     *
     * @param object the object
     * @return {@code true} if the print status was changed
     */
    protected boolean setPrintStatus(IMObject object) {
        IMObjectBean bean = new IMObjectBean(object);
        if (bean.hasNode("printed") && !bean.getBoolean("printed")) {
            bean.setValue("printed", true);
            return true;
        }
        return false;
    }
}
