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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workspace.BrowserCRUDWindowTab;
import org.openvpms.web.component.workspace.TabComponent;
import org.openvpms.web.component.workspace.TabbedWorkspace;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.CustomerPatientSummary;
import org.openvpms.web.workspace.patient.summary.CustomerPatientSummaryFactory;


/**
 * Reminder generation workspace.
 *
 * @author Tim Anderson
 */
public class ReminderWorkspace extends TabbedWorkspace<Act> {

    /**
     * The preferences.
     */
    private final Preferences preferences;

    /**
     * Constructs a {@link ReminderWorkspace}.
     *
     * @param context     the context
     * @param mailContext the mail context
     * @param preferences the preferences
     */
    public ReminderWorkspace(Context context, MailContext mailContext, Preferences preferences) {
        super("reporting.reminder", context);
        setMailContext(mailContext);
        this.preferences = preferences;
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or {@code null} if there is no summary
     */
    @Override
    public Component getSummary() {
        CustomerPatientSummaryFactory factory = ServiceHelper.getBean(CustomerPatientSummaryFactory.class);
        CustomerPatientSummary summary = factory.createCustomerPatientSummary(getContext(), getHelpContext(),
                                                                              preferences);
        return summary.getSummary(getObject());
    }

    /**
     * Returns the class type that this operates on.
     *
     * @return the class type that this operates on
     */
    @Override
    protected Class<Act> getType() {
        return Act.class;
    }

    /**
     * Adds tabs to the tabbed pane.
     *
     * @param model the tabbed pane model
     */
    @Override
    protected void addTabs(ObjectTabPaneModel<TabComponent> model) {
        addSendBrowser(model);
        addErrorBrowser(model);
        addResendBrowser(model);
    }

    private void addSendBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("send");
        ReminderItemQuery query = new ReminderItemQuery();
        LayoutContext context = new DefaultLayoutContext(getContext(), help);
        ReminderItemBrowser browser = new ReminderItemBrowser(query, context);
        ReminderItemCRUDWindow window = new ReminderItemCRUDWindow(browser, context.getContext(),
                                                                   context.getHelpContext());
        addTab("reporting.reminder.send", model, new BrowserCRUDWindowTab<>(browser, window));
    }

    private void addErrorBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("error");
        ReminderItemQuery query = new ReminderItemQuery(new ReminderItemObjectSetQuery(true, true));
        LayoutContext context = new DefaultLayoutContext(getContext(), help);
        ReminderItemBrowser browser = new ReminderItemBrowser(query, context);
        ReminderItemCRUDWindow window = new ReminderItemCRUDWindow(browser, context.getContext(),
                                                                   context.getHelpContext());
        addTab("reporting.reminder.error", model, new BrowserCRUDWindowTab<>(browser, window));
    }

    private void addResendBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("resend");
        ReminderItemQuery query = new ReminderItemQuery(new ReminderItemObjectSetQuery(true, true));
        LayoutContext context = new DefaultLayoutContext(getContext(), help);
        ReminderItemBrowser browser = new ReminderItemBrowser(query, context);
        ReminderItemCRUDWindow window = new ReminderItemCRUDWindow(browser, context.getContext(),
                                                                   context.getHelpContext());
        addTab("reporting.reminder.resend", model, new BrowserCRUDWindowTab<>(browser, window, false));
    }

}

