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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.supplier.order;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.esci.adapter.dispatcher.DefaultESCIDispatcher;
import org.openvpms.web.app.supplier.SupplierActCRUDWindow;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;

import java.util.Date;


/**
 * Supplier CRUD window that adds support to check ESCI inboxes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ESCISupplierCRUDWindow extends SupplierActCRUDWindow<FinancialAct> {

    /**
     * Check inbox button identifier.
     */
    protected static final String CHECK_INBOX_ID = "checkInbox";

    /**
     * Creates a new <tt>ESCISupplierCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public ESCISupplierCRUDWindow(Archetypes<FinancialAct> archetypes) {
        super(archetypes);
    }

    /**
     * Creates a button that invokes {@link #checkInbox} when pressed.
     *
     * @return a new button
     */
    protected Button createCheckInboxButton() {
        return ButtonFactory.create(CHECK_INBOX_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                checkInbox();
            }
        });
    }

    /**
     * Check the ESCI inboxes for messages from the suppliers.
     */
    protected void checkInbox() {
        try {
            DefaultESCIDispatcher dispatcher
                    = (DefaultESCIDispatcher) ServiceHelper.getContext().getBean("esciDispatcher");
            dispatcher.dispatch(true);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Schedule a poll of the ESCI inboxes to pick up messages.
     *
     * @param delay if <tt>true</tt> add a 30 second delay
     */
    protected void scheduleCheckInbox(boolean delay) {
        try {
            String triggerName = "ESCIDispatcherAdhocTrigger";
            Scheduler scheduler = (Scheduler) ServiceHelper.getContext().getBean("scheduler");
            scheduler.unscheduleJob(triggerName, null); // remove the existing trigger, if any
            SimpleTrigger trigger = new SimpleTrigger(triggerName);
            trigger.setJobName("esciDispatcherJob");
            if (delay) {
                Date in30secs = new Date(System.currentTimeMillis() + 30 * 1000);
                trigger.setStartTime(in30secs);
            }
            scheduler.scheduleJob(trigger);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }
}
