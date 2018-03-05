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
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
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
        Component result = null;
        Tab tab = (Tab) getSelected();
        Act reminder = (tab != null) ? tab.getWindow().getReminder() : null;
        if (reminder != null) {
            CustomerPatientSummaryFactory factory = ServiceHelper.getBean(CustomerPatientSummaryFactory.class);
            CustomerPatientSummary summary = factory.createCustomerPatientSummary(getContext(), getHelpContext(),
                                                                                  preferences);
            result = summary.getSummary(reminder);
        }
        return result;
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

    /**
     * Adds a browser for pending reminder items .
     *
     * @param model the tab model
     */
    private void addSendBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("send");
        LayoutContext context = new DefaultLayoutContext(getContext(), help);
        ReminderItemQuery query = new ReminderItemQuery(new ReminderItemDateObjectSetQuery(ReminderItemStatus.PENDING,
                                                                                           context.getContext()));
        PendingReminderItemBrowser browser = new PendingReminderItemBrowser(query, context);
        ReminderItemCRUDWindow window = new PendingReminderItemCRUDWindow(browser, context.getContext(),
                                                                          context.getHelpContext());
        addTab("reporting.reminder.send", model, new Tab(browser, window));
    }

    /**
     * Adds a browser for reminder item errors.
     * <p/>
     * This provides a date filter, and an option to view all errors.
     *
     * @param model the tab model
     */
    private void addErrorBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("error");
        LayoutContext context = new DefaultLayoutContext(getContext(), help);
        ReminderItemQuery query = new ReminderItemQuery(
                new ReminderItemDateObjectSetQuery(ReminderItemStatus.ERROR, true, context.getContext()));
        ReminderItemBrowser browser = new ReminderItemBrowser(query, context);
        ErrorReminderItemCRUDWindow window = new ErrorReminderItemCRUDWindow(browser, context.getContext(),
                                                                             context.getHelpContext());
        addTab("reporting.reminder.error", model, new Tab(browser, window));
    }

    /**
     * Adds a browser to resend reminder items.
     *
     * @param model the tab model
     */
    private void addResendBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("resend");
        LayoutContext context = new DefaultLayoutContext(getContext(), help);
        ReminderItemDateRangeObjectSetQuery dateRangeQuery = new ReminderItemDateRangeObjectSetQuery(
                null, context.getContext(), ReminderItemStatus.COMPLETED, ReminderItemStatus.CANCELLED);
        ReminderItemQuery query = new ReminderItemQuery(dateRangeQuery);
        ReminderItemBrowser browser = new ReminderItemBrowser(query, context);
        ReminderItemCRUDWindow window = new ResendReminderItemCRUDWindow(browser, context.getContext(),
                                                                         context.getHelpContext());
        addTab("reporting.reminder.resend", model, new Tab(browser, window));
    }

    private class Tab extends BrowserCRUDWindowTab<Act> {

        /**
         * Constructs a {@link BrowserCRUDWindowTab}.
         *
         * @param browser the browser
         * @param window  the window
         */
        public Tab(Browser<Act> browser, ReminderItemCRUDWindow window) {
            super(browser, window);
        }

        /**
         * Constructs a {@link BrowserCRUDWindowTab}.
         *
         * @param browser       the browser
         * @param window        the window
         * @param refreshOnShow determines if the browser should be refreshed when the tab is displayed.
         */
        public Tab(Browser<Act> browser, ReminderItemCRUDWindow window, boolean refreshOnShow) {
            super(browser, window, refreshOnShow);
        }

        /**
         * Invoked when the tab is displayed.
         */
        @Override
        public void show() {
            super.show();
            firePropertyChange(SUMMARY_PROPERTY, null, null);
        }

        /**
         * Returns the CRUD window.
         *
         * @return the window
         */
        @Override
        public ReminderItemCRUDWindow getWindow() {
            return (ReminderItemCRUDWindow) super.getWindow();
        }

        /**
         * Selects the current object.
         *
         * @param object the selected object
         */
        @Override
        protected void select(Act object) {
            Act current = getWindow().getObject();
            super.select(object);
            if (getSelected() == this && current != object) {
                firePropertyChange(SUMMARY_PROPERTY, null, null);
            }
        }
    }
}

