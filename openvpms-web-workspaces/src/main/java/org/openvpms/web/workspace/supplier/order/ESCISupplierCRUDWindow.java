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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.order;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.esci.adapter.dispatcher.DefaultESCIDispatcher;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.supplier.SupplierActCRUDWindow;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;

import java.util.Date;
import java.util.Iterator;

import static org.openvpms.component.system.common.query.Constraints.shortName;


/**
 * Supplier CRUD window that adds support to check ESCI inboxes.
 *
 * @author Tim Anderson
 */
public class ESCISupplierCRUDWindow extends SupplierActCRUDWindow<FinancialAct> {

    /**
     * Check inbox button identifier.
     */
    protected static final String CHECK_INBOX_ID = "checkInbox";

    /**
     * Constructs an {@code ESCISupplierCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param operations determines the operations that may be performed on the selected object
     * @param context    the context
     * @param help       the help context
     */
    public ESCISupplierCRUDWindow(Archetypes<FinancialAct> archetypes, ActActions<FinancialAct> operations,
                                  Context context, HelpContext help) {
        super(archetypes, operations, context, help);
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
            DefaultESCIDispatcher dispatcher = ServiceHelper.getBean(DefaultESCIDispatcher.class);
            dispatcher.dispatch(true);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Schedule a poll of the ESCI inboxes to pick up messages.
     *
     * @param delay if {@code true} add a 30 second delay
     */
    protected void scheduleCheckInbox(boolean delay) {
        try {
            String name = getESCIJobName();
            if (!StringUtils.isEmpty(name)) {
                String triggerName = "ESCIDispatcherAdhocTrigger";
                Scheduler scheduler = ServiceHelper.getBean(Scheduler.class);
                scheduler.unscheduleJob(triggerName, null); // remove the existing trigger, if any
                SimpleTrigger trigger = new SimpleTrigger(triggerName);
                trigger.setJobName(name);
                if (delay) {
                    Date in30secs = new Date(System.currentTimeMillis() + 30 * 1000);
                    trigger.setStartTime(in30secs);
                }
                scheduler.scheduleJob(trigger);
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Returns the ESCI job configuration name.
     *
     * @return the ESCI job configuration name, or {@code null} if none exists
     */
    private String getESCIJobName() {
        String name = null;
        ArchetypeQuery query = new ArchetypeQuery(shortName("job", "entity.jobESCIInboxReader", true));
        query.setMaxResults(1);
        query.add(new NodeSelectConstraint("job.name"));
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(query);
        if (iterator.hasNext()) {
            name = iterator.next().getString("job.name");
        }
        return name;
    }
}
