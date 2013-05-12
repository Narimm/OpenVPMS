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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.patient.mr;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.patient.history.PatientHistoryBrowser;
import org.openvpms.web.app.patient.history.PatientHistoryQuery;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.query.BrowserState;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.tabpane.TabPaneModel;
import org.openvpms.web.resource.i18n.Messages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


/**
 * Patient record browser.
 * <p/>
 * TODO - refactor to use TabbedBrowser.
 *
 * @author Tim Anderson
 */
public class RecordBrowser implements Browser<Act> {

    /**
     * The container.
     */
    private Column container;

    /**
     * The tabbed pane.
     */
    private TabbedPane tab;

    /**
     * The summary browser.
     */
    private Browser<Act> summary;

    /**
     * The problems browser.
     */
    private Browser<Act> problems;

    /**
     * The reminders/alerts browser.
     */
    private Browser<Act> reminderAlert;

    /**
     * The documents browser.
     */
    private Browser<Act> document;

    /**
     * The charges browser.
     */
    private Browser<Act> charges;

    /**
     * The set of registered listeners.
     */
    private List<BrowserListener<Act>> listeners = new ArrayList<BrowserListener<Act>>();

    /**
     * The event listener.
     */
    private TabbedBrowserListener listener;

    /**
     * The selected tab.
     */
    private int selected = 0;

    /**
     * The focus group.
     */
    private FocusGroup focusGroup = new FocusGroup(getClass().getName());


    /**
     * The browser view.
     */
    public enum View {

        SUMMARY, PROBLEMS, REMINDER_ALERT, DOCUMENTS, CHARGES
    }


    /**
     * Construct a new <code>RecordBrowser</code> that queries IMObjects using
     * the specified queries.
     *
     * @param history       query for summary
     * @param problems      query for problems
     * @param reminderAlert query for reminders/alerts
     * @param document      query for documents
     * @param charges       query for charges
     * @param help          the help context
     */
    public RecordBrowser(PatientHistoryQuery history, Query<Act> problems,
                         Query<Act> reminderAlert, Query<Act> document,
                         Query<Act> charges, Context context, HelpContext help) {
        LayoutContext layout = new DefaultLayoutContext(context, help);
        this.summary = new PatientHistoryBrowser(history, layout);
        this.problems = BrowserFactory.create(problems, layout);

        // todo - should be able to register ReminderActTableModel in
        // IMObjectTableFactory.properties for act.patientReminder and
        // act.patientAlert
        IMObjectTableModel<Act> model = new ReminderActTableModel(reminderAlert.getShortNames(), layout);
        this.reminderAlert = new DefaultIMObjectTableBrowser<Act>(reminderAlert, model, layout);
        this.document = BrowserFactory.create(document, layout);
        IMObjectTableModel<Act> chargeModel = new ChargesActTableModel(charges.getShortNames(), layout);
        this.charges = new DefaultIMObjectTableBrowser<Act>(charges, chargeModel, layout);
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (container == null) {
            container = ColumnFactory.create();
            TabPaneModel model = new TabPaneModel(container);
            addTab(1, "button.summary", model, summary);
            addTab(2, "button.problem", model, problems);
            addTab(3, "button.reminder", model, reminderAlert);
            addTab(4, "button.document", model, document);
            addTab(5, "button.charges", model, charges);
            tab = TabbedPaneFactory.create(model);
            tab.setSelectedIndex(selected);

            tab.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    int index = tab.getSelectedIndex();
                    if (index != selected) {
                        selected = index;
                        listener.onBrowserChanged();
                    }
                }
            });
            container.add(tab);
            focusGroup.add(tab);
        }
        return container;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public Act getSelected() {
        return getCurrent().getSelected();
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(Act object) {
        getCurrent().setSelected(object);
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<Act> getObjects() {
        return getCurrent().getObjects();
    }

    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    public void addBrowserListener(BrowserListener<Act> listener) {
        listeners.add(listener);
        summary.addBrowserListener(listener);
        problems.addBrowserListener(listener);
        reminderAlert.addBrowserListener(listener);
        document.addBrowserListener(listener);
        charges.addBrowserListener(listener);
    }

    /**
     * Removes a listener to stop receive notification of selection and query actions.
     *
     * @param listener the listener to remove
     */
    public void removeBrowserListener(BrowserListener<Act> listener) {
        listeners.remove(listener);
        summary.removeBrowserListener(listener);
        problems.removeBrowserListener(listener);
        reminderAlert.removeBrowserListener(listener);
        document.removeBrowserListener(listener);
        charges.removeBrowserListener(listener);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        query(summary);
        query(problems);
        query(reminderAlert);
        query(document);
        query(charges);
    }

    /**
     * Returns the browser for the specified view.
     *
     * @param view the view
     * @return the browser for the view
     */
    public Browser<Act> getBrowser(View view) {
        Browser<Act> result;
        switch (view) {
            case PROBLEMS:
                result = problems;
                break;
            case REMINDER_ALERT:
                result = reminderAlert;
                break;
            case DOCUMENTS:
                result = document;
                break;
            case CHARGES:
                result = charges;
                break;
            default:
                result = summary;
        }
        return result;
    }

    /**
     * Determines the current view.
     *
     * @return the current view
     */
    public View getView() {
        View result;
        switch (selected) {
            case 1:
                result = View.PROBLEMS;
                break;
            case 2:
                result = View.REMINDER_ALERT;
                break;
            case 3:
                result = View.DOCUMENTS;
                break;
            case 4:
                result = View.CHARGES;
                break;
            default:
                result = View.SUMMARY;
        }
        return result;
    }

    /**
     * Sets the browser listener.
     *
     * @param listener the listener. May be <code>null</code>
     */
    public void setListener(TabbedBrowserListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the browser state.
     * <p/>
     * This implementation always returns <tt>null</tt>.
     *
     * @return <tt>null</tt>
     */
    public BrowserState getBrowserState() {
        return null;
    }

    /**
     * Sets the browser state.
     * <p/>
     * This implementation is a bo-op.
     *
     * @param state the state
     */
    public void setBrowserState(BrowserState state) {
        // do nothing
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Sets focus on the results.
     */
    public void setFocusOnResults() {
        getCurrent().setFocusOnResults();
    }

    /**
     * Returns the selected browser.
     *
     * @return the selected browser
     */
    private Browser<Act> getCurrent() {
        return getBrowser(getView());
    }

    /**
     * Queries a browser, preserving the selected act (if possible).
     * <p/>
     * Note that this supresses events for all but the current browser, to avoid events from one browser triggering
     * behaviour in another. TODO - ideally each tab would be treated independently, and refreshed when displayed.
     *
     * @param browser the browser
     */
    private void query(Browser<Act> browser) {
        boolean supressEvents = getCurrent() != browser;
        if (supressEvents) {
            for (BrowserListener<Act> l : listeners) {
                browser.removeBrowserListener(l);
            }
        }
        try {
            Act selected = browser.getSelected();
            browser.query();
            browser.setSelected(selected);
        } finally {
            if (supressEvents) {
                for (BrowserListener<Act> l : listeners) {
                    browser.addBrowserListener(l);
                }
            }
        }
    }

    /**
     * Helper to add a browser to the tab pane.
     *
     * @param shortcut the tab button shortcut no.
     * @param button   the button key
     * @param model    the tab model
     * @param browser  the browser to add
     */
    private void addTab(int shortcut, String button, DefaultTabModel model,
                        Browser<Act> browser) {
        String text = "&" + shortcut + " " + Messages.get(button);
        Component component = browser.getComponent();
        component = ColumnFactory.create("Inset", component);
        model.addTab(text, component);

        // select the first available act, if any
        if (browser.getSelected() == null) {
            List<Act> objects = browser.getObjects();
            if (!objects.isEmpty()) {
                Act current = objects.get(0);
                browser.setSelected(current);
            }
        }
    }
}
