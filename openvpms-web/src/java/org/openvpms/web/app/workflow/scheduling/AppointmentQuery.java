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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.DateSelector;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Appointment query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class AppointmentQuery {

    /**
     * The schedule view selector.
     */
    private SelectField viewField;

    /**
     * The schedule selector.
     */
    private SelectField scheduleField;

    /**
     * The list of schedules associated with the selected schedule view.
     */
    private List<Party> schedules;

    /**
     * The date selector.
     */
    private DateSelector date;

    /**
     * The query component.
     */
    private Component component;

    /**
     * Appointment service.
     */
    private AppointmentService service;

    /**
     * Appointment rules.
     */
    private AppointmentRules rules;

    /**
     * Listener to notify of query events.
     */
    private QueryListener listener;

    /**
     * The focus group.
     */
    private FocusGroup focus;


    /**
     * Creates a new <tt>AppointmentQuery</tt>.
     */
    public AppointmentQuery() {
        service = ServiceHelper.getAppointmentService();
        rules = new AppointmentRules();
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            component = RowFactory.create("ControlRow");
            doLayout(component);
        }
        return component;
    }

    /**
     * Returns the date.
     *
     * @return the date
     */
    public Date getDate() {
        return date.getDate();
    }

    /**
     * Returns the selected schedule view.
     *
     * @return the selected schedule view. May be <tt>null</tt>
     */
    public Entity getScheduleView() {
        return (Entity) viewField.getSelectedItem();
    }

    /**
     * Sets the selected schedule view.
     *
     * @param view the schedule view
     */
    public void setScheduleView(Entity view) {
        viewField.setSelectedItem(view);
        schedules = null;
    }

    /**
     * Returns the schedules associated with the selected schedule view.
     *
     * @return the schedules
     */
    public List<Party> getSchedules() {
        if (schedules == null) {
            Entity view = getScheduleView();
            if (view != null) {
                schedules = rules.getSchedules(view);
            }
        }
        return (schedules != null) ? schedules : Collections.<Party>emptyList();
    }

    /**
     * Returns the selected schedule.
     *
     * @return the selected schedule, or <tt>null</tt> if all schedules are
     *         selected
     */
    public Party getSchedule() {
        return (Party) scheduleField.getSelectedItem();
    }

    /**
     * Performs the query, returning a list of appointments keyed on schedule.
     *
     * @return the query result set. May be <tt>null</tt>
     * @throws ArchetypeServiceException if the query fails
     */
    public Map<Party, List<ObjectSet>> query() {
        getComponent();
        return getAppointments();
    }

    /**
     * Returns the focus group for the component.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focus;
    }

    /**
     * Sets the query listener.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setListener(QueryListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the appointments, keyed on schedule.
     *
     * @return the appointments
     */
    private Map<Party, List<ObjectSet>> getAppointments() {
        Date date = getDate();
        Map<Party, List<ObjectSet>> result
                = new HashMap<Party, List<ObjectSet>>();
        Party selected = (Party) scheduleField.getSelectedItem();
        List<Party> schedules;
        if (selected != null) {
            schedules = Arrays.asList(selected);
        } else {
            schedules = getSchedules();
        }
        for (Party schedule : schedules) {
            List<ObjectSet> appointments = service.getAppointments(
                    schedule, date);
            result.put(schedule, appointments);
        }
        return result;
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    private void doLayout(Component container) {
        viewField = createScheduleViewField();
        scheduleField = createScheduleField();

        date = new DateSelector();
        date.setListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuery();
            }
        });
        container.add(viewField);
        container.add(date.getComponent());
        container.add(
                LabelFactory.create("workflow.scheduling.query.schedule"));
        container.add(scheduleField);

        focus = new FocusGroup("ScheduleQuery");
        focus.add(viewField);
        focus.add(date.getFocusGroup());
        focus.add(scheduleField);
    }

    /**
     * Creates a new field to select a schedule view.
     *
     * @return a new select field
     */
    private SelectField createScheduleViewField() {
        SelectField result;
        Party location = GlobalContext.getInstance().getLocation();
        List<Entity> views;
        Entity defaultView = null;
        if (location != null) {
            LocationRules locationRules = new LocationRules();
            views = locationRules.getScheduleViews(location);
            defaultView = locationRules.getDefaultScheduleView(location);
        } else {
            views = Collections.emptyList();
        }
        IMObjectListModel model = new IMObjectListModel(views, false, false);
        result = SelectFieldFactory.create(model);
        result.setCellRenderer(IMObjectListCellRenderer.INSTANCE);
        if (defaultView != null) {
            result.setSelectedItem(defaultView);
        }
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onViewChanged();
            }
        });
        return result;
    }

    /**
     * Creates a new field to select a schedule.
     *
     * @return a new select field
     */
    private SelectField createScheduleField() {
        List<Party> schedules = getSchedules();
        IMObjectListModel model = new IMObjectListModel(schedules, true, false);
        SelectField result = SelectFieldFactory.create(model);
        result.setCellRenderer(IMObjectListCellRenderer.INSTANCE);
        if (model.size() > 0) {
            result.setSelectedIndex(0); // select All
        }
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onScheduleChanged();
            }
        });
        return result;
    }

    /**
     * Invoked when the schedule view changes.
     * <p/>
     * Notifies any listener to perform a query.
     */
    private void onViewChanged() {
        schedules = null;
        onQuery();
    }

    /**
     * Invoked when the schedule changes.
     * <p/>
     * Notifies any listener to perform a query.
     */
    private void onScheduleChanged() {
        onQuery();
    }

    /**
     * Notifies any listener to perform a query.
     */
    private void onQuery() {
        if (listener != null) {
            listener.query();
        }
    }

}
