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

package org.openvpms.web.app.workflow.scheduling;

import echopointng.TableEx;
import echopointng.table.TableActionEventEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import static org.openvpms.web.app.workflow.scheduling.AppointmentTableModel.Availability.UNAVAILABLE;
import static org.openvpms.web.app.workflow.scheduling.AppointmentTableModel.Highlight;
import org.openvpms.web.component.im.query.AbstractBrowser;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Appointment browser. Renders blocks of appointments in different hours a
 * different colour.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentBrowser extends AbstractBrowser<ObjectSet> {

    /**
     * Displays the selected date above the appointments.
     */
    private Label selectedDate;

    /**
     * The query.
     */
    private AppointmentQuery query;

    /**
     * The appointments, keyed on schedule.
     */
    private Map<Party, List<ObjectSet>> results;

    /**
     * The browser component.
     */
    private Component component;

    /**
     * The appointment table.
     */
    private TableEx table;

    /**
     * The appointment table model.
     */
    private AppointmentTableModel model;

    /**
     * The selected appointment.
     */
    private ObjectSet selected;

    /**
     * The selected time. May be <tt>null</tt>.
     */
    private Date selectedTime;

    /**
     * The selected schedule. May be <tt>null</tt>
     */
    private Party selectedSchedule;

    /**
     * Highlight selector, to change colour of display items.
     */
    private SelectField highlightSelector;


    /**
     * Creates a new <tt>AppointmentBrowser</tt>.
     */
    public AppointmentBrowser() {
        query = new AppointmentQuery();
        query.setListener(new QueryListener() {
            public void query() {
                onQuery();
            }
        });
        model = new AppointmentTableModel();
        table = new TableEx(model, model.getColumnModel());
        table.setStyleName("default");
        //table.setResizeable(true);
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                if (event.getType() == TableModelEvent.STRUCTURE_CHANGED) {
                    table.setColumnModel(model.getColumnModel());
                }
            }
        });
        table.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelected(event);
            }
        });
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        getComponent();
        results = query.query();
        List<Party> schedules;
        selectedSchedule = query.getSchedule();
        if (selectedSchedule != null) {
            schedules = Arrays.asList(selectedSchedule);
        } else {
            schedules = query.getSchedules();
        }
        if (!schedules.equals(model.getSchedules())) {
            model.setSchedules(schedules);
        }
        model.setAppointments(query.getDate(), results);
        DateFormat format = DateHelper.getFullDateFormat();
        selectedDate.setText(format.format(query.getDate()));
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public ObjectSet getSelected() {
        return selected;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(ObjectSet object) {
        selected = object;
    }

    /**
     * Returns the query date.
     *
     * @return the query date
     */
    public Date getDate() {
        return query.getDate();
    }

    /**
     * Sets the schedule view.
     *
     * @param view the schedule view. May be <tt>null</tt>
     */
    public void setScheduleView(Entity view) {
        query.setScheduleView(view);
    }

    /**
     * Returns the schedule view.
     *
     * @return the schedule view. May be <tt>null</tt>
     */
    public Entity getScheduleView() {
        return query.getScheduleView();
    }

    /**
     * Returns the selected schedule.
     *
     * @return the selected schedule. May be <tt>null</tt>
     */
    public Party getSelectedSchedule() {
        return selectedSchedule;
    }

    /**
     * Returns the selected time.
     *
     * @return the selected time. May be <tt>null</tt>
     */
    public Date getSelectedTime() {
        return selectedTime;
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query
     */
    public List<ObjectSet> getObjects() {
        if (results == null) {
            query();
        }
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        for (List<ObjectSet> list : results.values()) {
            for (ObjectSet set : list) {
                result.add(set);
            }
        }
        return result;
    }

    /**
     * Returns the browser component.
     *
     * @return the browser component
     */
    public Component getComponent() {
        if (component == null) {
            doLayout();
        }
        return component;
    }

    /**
     * Adds an appointment listener.
     *
     * @param listener the listener to add
     */
    public void addAppointmentListener(AppointmentListener listener) {
        addQueryListener(listener);
    }

    /**
     * Performs a query and notifies registered listeners.
     */
    private void onQuery() {
        query();
        notifyQueryListeners();
    }

    /**
     * Lays out the component.
     */
    private void doLayout() {
        selectedDate = LabelFactory.create(null, "bold");
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(Alignment.ALIGN_CENTER);
        selectedDate.setLayoutData(layout);

        String[] highlightSelectorItems = {
                Messages.get("workflow.scheduling.highlight.appointment"),
                Messages.get("workflow.scheduling.highlight.clinician"),
                Messages.get("workflow.scheduling.highlight.status")};

        highlightSelector = SelectFieldFactory.create(highlightSelectorItems);
        highlightSelector.setSelectedItem(highlightSelectorItems[0]);
        highlightSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onHighlightChanged();
            }
        });

        Label viewLabel = LabelFactory.create("workflow.scheduling.highlight");
        Row row = RowFactory.create("CellSpacing", query.getComponent(),
                                    viewLabel, highlightSelector);
        component = ColumnFactory.create("WideCellSpacing", selectedDate, row,
                                         table);
    }

    /**
     * Invoked when the view changes.
     */
    private void onHighlightChanged() {
        int index = highlightSelector.getSelectedIndex();
        switch (index) {
            case 0:
                model.setHighlight(Highlight.APPOINTMENT);
                break;
            case 1:
                model.setHighlight(Highlight.CLINICIAN);
                break;
            default:
                model.setHighlight(Highlight.STATUS);

        }
    }

    /**
     * Invoked when a cell is selected.
     * <p/>
     * Notifies listeners of the selection.
     *
     * @param event the event
     */
    private void onSelected(ActionEvent event) {
        TableActionEventEx action = (TableActionEventEx) event;
        int column = action.getColumn();
        int row = action.getRow();
        boolean doubleClick = false;
        if (model.isSelectedCell(column, row)) {
            doubleClick = true;
        }
        model.setSelectedCell(column, row);
        selected = model.getAppointment(column, row);
        if (model.getAvailability(column, row) != UNAVAILABLE) {
            selectedTime = model.getStartTime(row);
            selectedSchedule = model.getSchedule(column);
        } else {
            selectedTime = null;
            selectedSchedule = null;
        }
        if (doubleClick) {
            if (selected == null) {
                for (QueryBrowserListener<ObjectSet> listener
                        : getQueryListeners()) {
                    if (listener instanceof AppointmentListener) {
                        ((AppointmentListener) listener).create();
                    }
                }
            } else {
                for (QueryBrowserListener<ObjectSet> listener
                        : getQueryListeners()) {
                    if (listener instanceof AppointmentListener) {
                        ((AppointmentListener) listener).edit(selected);
                    }
                }
            }
        } else {
            notifySelected(selected);
        }
    }

}
