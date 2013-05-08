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
 */

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.mail.MailContext;


/**
 * Prints an {@link Act}. On successful printing, updates the {@code printed} flag, if the act has one.
 *
 * @author Tim Anderson
 */
public class PrintActTask extends PrintIMObjectTask {

    /**
     * Constructs a {@code PrintActTask}.
     *
     * @param act     the act to print
     * @param context the mail context. May be {@code null}
     */
    public PrintActTask(Act act, MailContext context) {
        super(act, context);
    }

    /**
     * Creates a new {@code PrintActTask}.
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
     * @param object the printed object
     */
    @Override
    protected void onPrinted(IMObject object) {
        try {
            IMObjectBean bean = new IMObjectBean(object);
            if (bean.hasNode("printed") && !bean.getBoolean("printed")) {
                bean.setValue("printed", true);
                bean.save();
            }
            super.onPrinted(object);
        } catch (OpenVPMSException exception) {
            notifyCancelledOnError(exception);
        }
    }
}
