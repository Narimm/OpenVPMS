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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.payment;

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.till.CashDrawer;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Opens a cash drawer if a payment is finalised, and contains Cash, Cheque or EFT with cash-out items.
 *
 * @author Tim Anderson
 */
public class OpenDrawerTask extends SynchronousTask {

    /**
     * Executes the task.
     *
     * @throws OpenVPMSException for any error
     */
    @Override
    public void execute(TaskContext context) {
        Act act = (Act) context.getObject(CustomerAccountArchetypes.PAYMENT);
        if (act != null) {
            open(act);
        } else {
            act = (Act) context.getObject(CustomerAccountArchetypes.REFUND);
            if (act != null) {
                open(act);
            }
        }
    }

    /**
     * Opens the cash drawer for a payment or refund if:
     * <ul>
     * <li> the act is POSTED</li>
     * <li>the till has a drawer command</li>
     * </ul>
     *
     * @param act the payment or refund act
     */
    public void open(Act act) {
        if (ActStatus.POSTED.equals(act.getStatus())) {
            ActBean bean = new ActBean(act);
            Entity till = bean.getNodeParticipant("till");
            if (till != null) {
                CashDrawer drawer = new CashDrawer(till);
                if (drawer.canOpen() && drawer.needsOpen(act)) {
                    final InformationDialog dialog = new InformationDialog(Messages.get("till.opendrawer"));
                    dialog.setStyleName("InformationDialog.Compact");
                    dialog.show((int) DateUtils.MILLIS_PER_SECOND); // close the dialog after 1 second
                    try {
                        drawer.open();
                    } catch (Throwable exception) {
                        dialog.close();
                        ErrorHelper.show(exception);
                    }
                }
            }
        }
    }

}

