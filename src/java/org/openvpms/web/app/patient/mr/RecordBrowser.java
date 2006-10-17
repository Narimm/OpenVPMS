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
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
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
    private TabbedPane _tab;

    /**
     * The summary browser.
     */
    private Browser<Act> _summary;

    /**
     * The visits browser.
     */
    private TableBrowser<Act> _visits;

    /**
     * The problems browser.
     */
    private TableBrowser<Act> _problems;

    /**
     * The medication browser.
     */
    private TableBrowser<Act> _medication;

    /**
     * The reminders/alerts browser.
     */
    private TableBrowser<Act> _reminderAlert;

    /**
     * The event listener.
     */
    private RecordBrowserListener _listener;

    /**
     * The selected tab.
     */
    private int _selected = 0;

    /**
     * The browser view.
     */
    public enum View {
        SUMMARY, VISITS, PROBLEMS, MEDICATION, REMINDER_ALERT
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
                         Query<Act> reminderAlert, SortConstraint[] sort) {
        _summary = new SummaryTableBrowser(summary);
        _visits = new TableBrowser<Act>(visits, sort);
        _problems = new TableBrowser<Act>(problems, sort);
        _medication = new TableBrowser<Act>(medication, sort);

        // todo - should be able to register ReminderActTableModel in
        // IMObjectTableFactory.properties for act.patientReminder and
        // act.patientAlert
        IMObjectTableModel<Act> model = new ReminderActTableModel(
                reminderAlert.getShortNames());
        _reminderAlert = new TableBrowser<Act>(reminderAlert, sort, model);
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (_tab == null) {
            DefaultTabModel model = new DefaultTabModel();
            addTab("button.summary", model, _summary);
            addTab("button.visit", model, _visits);
            addTab("button.problem", model, _problems);
            addTab("button.medication", model, _medication);
            addTab("button.reminder", model, _reminderAlert);
            _tab = TabbedPaneFactory.create(model);
            _tab.setSelectedIndex(_selected);

            _tab.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    int index = _tab.getSelectedIndex();
                    if (index != _selected) {
                        _selected = index;
                        _listener.onViewChanged();
                    }
                }
            });
        }
        return _tab;
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
    public void addQueryListener(QueryBrowserListener listener) {
        _summary.addQueryListener(listener);
        _visits.addQueryListener(listener);
        _problems.addQueryListener(listener);
        _medication.addQueryListener(listener);
        _reminderAlert.addQueryListener(listener);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        query(_summary);
        query(_visits);
        query(_problems);
        query(_medication);
        query(_reminderAlert);
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
                result = _summary;
                break;
            case VISITS:
                result = _visits;
                break;
            case PROBLEMS:
                result = _problems;
                break;
            case MEDICATION:
                result = _medication;
                break;
            default:
                result = _reminderAlert;
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
        switch (_selected) {
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
            default:
                result = View.REMINDER_ALERT;
        }
        return result;
    }

    /**
     * Sets the browser listener.
     *
     * @param listener the listener. May be <code>null</code>
     */
    public void setListener(RecordBrowserListener listener) {
        _listener = listener;
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
