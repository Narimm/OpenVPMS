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
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.AbstractBrowser;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;

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

    private ScheduleQuery query;

    private Map<Party, List<ObjectSet>> results;

    private Component component;

    private TableEx table;

    private AppointmentTableModel model;

    private ObjectSet selected;

    private Date selectedTime;

    private Party selectedSchedule;


    /**
     * Construct a new <tt>AppointmentBrowser</tt> that queries ObjectSets using
     * the specified query.
     */
    public AppointmentBrowser() {
        query = new ScheduleQuery();
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

    public Date getDate() {
        return query.getDate();
    }

    public void setScheduleView(Entity view) {
        query.setScheduleView(view);
    }

    public Entity getScheduleView() {
        return query.getScheduleView();
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

    private void onQuery() {
        query();
        notifyQueryListeners();
    }

    private void doLayout() {
        selectedDate = LabelFactory.create(null, "bold");
/*
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(Alignment.ALIGN_CENTER);
        selectedDate.setLayoutData(layout);

*/
        component = ColumnFactory.create("WideCellSpacing", selectedDate,
                                         query.getComponent(), table);
    }

    private void onSelected(ActionEvent event) {
        TableActionEventEx action = (TableActionEventEx) event;
        if (action.getColumn() != 0) {
            selected = model.getAppointment(action.getColumn(),
                                            action.getRow());
            selectedTime = model.getStartTime(action.getRow());
            selectedSchedule = model.getSchedule(action.getColumn());
            notifySelected(selected);
        }
    }

}
