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
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.TabbedPaneFactory;
import org.openvpms.web.resource.util.Messages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;


/**
 * Patient record browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RecordBrowser implements Browser<Act> {

    /**
     * The tabbed pane.
     */
    private TabbedPane tab;

    /**
     * The summary browser.
     */
    private Browser<Act> summary;

    /**
     * The visits browser.
     */
    private TableBrowser<Act> visits;

    /**
     * The problems browser.
     */
    private TableBrowser<Act> problems;

    /**
     * The medication browser.
     */
    private TableBrowser<Act> medication;

    /**
     * The reminders/alerts browser.
     */
    private TableBrowser<Act> reminderAlert;

    /**
     * The documents browser.
     */
    private TableBrowser<Act> document;

    /**
     * The investigations browser.
     */
    private TableBrowser<Act> investigation;

    /**
     * The event listener.
     */
    private RecordBrowserListener listener;

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
        SUMMARY, VISITS, PROBLEMS, MEDICATION, REMINDER_ALERT, DOCUMENTS,
        INVESTIGATIONS
    }


    /**
     * Construct a new <code>RecordBrowser</code> that queries IMObjects using
     * the specified queries.
     *
     * @param summary       query for summary
     * @param visits        query for visits
     * @param problems      query for problems
     * @param medication    query for medication
     * @param reminderAlert query for reminders/alerts
     * @param sort          the sort criteria. May be <code>null</code>
     */
    public RecordBrowser(Query<Act> summary, Query<Act> visits,
                         Query<Act> problems, Query<Act> medication,
                         Query<Act> reminderAlert, Query<Act> document,
                         Query<Act> investigation, SortConstraint[] sort) {
        this.summary = new SummaryTableBrowser(summary);
        this.visits = new IMObjectTableBrowser<Act>(visits, sort);
        this.problems = new IMObjectTableBrowser<Act>(problems, sort);
        this.medication = new IMObjectTableBrowser<Act>(medication, sort);

        // todo - should be able to register ReminderActTableModel in
        // IMObjectTableFactory.properties for act.patientReminder and
        // act.patientAlert
        IMObjectTableModel<Act> model = new ReminderActTableModel(
                reminderAlert.getShortNames());
        this.reminderAlert = new TableBrowser<Act>(reminderAlert, sort, model);
        IMObjectTableModel<Act> docModel
                = new ActAmountTableModel<Act>(true, false);
        this.document = new TableBrowser<Act>(document, sort, docModel);
        IMObjectTableModel<Act> invModel
                = new ActAmountTableModel<Act>(true, false);
        this.investigation = new TableBrowser<Act>(investigation, sort,
                                                   invModel);
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (tab == null) {
            DefaultTabModel model = new DefaultTabModel();
            addTab("button.summary", model, summary);
            addTab("button.visit", model, visits);
            addTab("button.problem", model, problems);
            addTab("button.medication", model, medication);
            addTab("button.reminder", model, reminderAlert);
            addTab("button.document", model, document);
            addTab("button.investigation", model, investigation);
            tab = TabbedPaneFactory.create(model);
            tab.setSelectedIndex(selected);

            tab.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    int index = tab.getSelectedIndex();
                    if (index != selected) {
                        selected = index;
                        listener.onViewChanged();
                    }
                }
            });
            focusGroup.add(tab);
        }
        return tab;
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
    public void addQueryListener(QueryBrowserListener<Act> listener) {
        summary.addQueryListener(listener);
        visits.addQueryListener(listener);
        problems.addQueryListener(listener);
        medication.addQueryListener(listener);
        reminderAlert.addQueryListener(listener);
        document.addQueryListener(listener);
        investigation.addQueryListener(listener);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        query(summary);
        query(visits);
        query(problems);
        query(medication);
        query(reminderAlert);
        query(document);
        query(investigation);
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
            case SUMMARY:
                result = summary;
                break;
            case VISITS:
                result = visits;
                break;
            case PROBLEMS:
                result = problems;
                break;
            case MEDICATION:
                result = medication;
                break;
            case REMINDER_ALERT:
                result = reminderAlert;
                break;
            case DOCUMENTS:
                result = document;
                break;
            default:
                result = investigation;
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
            case 0:
                result = View.SUMMARY;
                break;
            case 1:
                result = View.VISITS;
                break;
            case 2:
                result = View.PROBLEMS;
                break;
            case 3:
                result = View.MEDICATION;
                break;
            case 4:
                result = View.REMINDER_ALERT;
                break;
            case 5:
                result = View.DOCUMENTS;
                break;
            default:
                result = View.INVESTIGATIONS;
        }
        return result;
    }

    /**
     * Sets the browser listener.
     *
     * @param listener the listener. May be <code>null</code>
     */
    public void setListener(RecordBrowserListener listener) {
        this.listener = listener;
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
     * Returns the selected browser.
     *
     * @return the selected browser
     */
    private Browser<Act> getCurrent() {
        return getBrowser(getView());
    }

    /**
     * Queries a browser, preserving the selected act (if possible).
     *
     * @param browser the browser
     */
    private void query(Browser<Act> browser) {
        Act selected = browser.getSelected();
        browser.query();
        browser.setSelected(selected);
    }

    /**
     * Helper to add a browser to the tab pane.
     *
     * @param button  the button key
     * @param model   the tab model
     * @param browser the browser to add
     */
    private void addTab(String button, DefaultTabModel model, Browser browser) {
        Component component = browser.getComponent();
        component = ColumnFactory.create("Inset", component);
        model.addTab(Messages.get(button), component);
    }
}
