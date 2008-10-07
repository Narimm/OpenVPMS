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
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.archetype.rules.workflow.ScheduleService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.DateSelector;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Schedule query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ScheduleQuery {

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
    private List<Entity> schedules;

    /**
     * The date selector.
     */
    private DateSelector date;

    /**
     * The query component.
     */
    private Component component;

    /**
     * Schedule service.
     */
    private ScheduleService service;

    /**
     * Listener to notify of query events.
     */
    private QueryListener listener;

    /**
     * The focus group.
     */
    private FocusGroup focus;


    /**
     * Creates a new <tt>ScheduleQuery</tt>.
     *
     * @param service the schedule service
     */
    public ScheduleQuery(ScheduleService service) {
        this.service = service;
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            component = RowFactory.create("ControlRow");
            focus = new FocusGroup("ScheduleQuery");
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
        getComponent();
        return (Entity) viewField.getSelectedItem();
    }

    /**
     * Sets the selected schedule view.
     *
     * @param view the schedule view
     */
    public void setScheduleView(Entity view) {
        getComponent();
        viewField.setSelectedItem(view);
        schedules = null;
        updateScheduleField();
    }

    /**
     * Returns the schedules associated with the selected schedule view.
     *
     * @return the schedules
     */
    public List<Entity> getSchedules() {
        if (schedules == null) {
            Entity view = getScheduleView();
            if (view != null) {
                schedules = getSchedules(view);
            }
        }
        return (schedules != null) ? schedules
                : Collections.<Entity>emptyList();
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
     * Performs the query, returning a list of events keyed on schedule.
     *
     * @return the query result set. May be <tt>null</tt>
     */
    public Map<Entity, List<ObjectSet>> query() {
        getComponent();
        return getEvents();
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
     * Returns the schedule views.
     *
     * @return the schedule views
     */
    protected abstract List<Entity> getScheduleViews();

    /**
     * Returns the default schedule view.
     *
     * @return the default schedule view. May be <tt>null</tt>
     */
    protected abstract Entity getDefaultScheduleView();

    /**
     * Returns the schedules for the specified schedule view.
     *
     * @param view the schedule view
     * @return the corresponding schedules
     */
    protected abstract List<Entity> getSchedules(Entity view);

    /**
     * Returns a display name for the schedule selector.
     *
     * @return a display name for the schedule selector
     */
    protected abstract String getScheduleDisplayName();

    /**
     * Returns the events for a schedule and date.
     *
     * @param schedule the schedule
     * @param date     the date
     * @return the events
     */
    protected List<ObjectSet> getEvents(Entity schedule, Date date) {
        return service.getEvents(schedule, date);
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        viewField = createScheduleViewField();
        scheduleField = createScheduleField();

        date = new DateSelector();
        date.setListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onDateChanged();
            }
        });
        container.add(viewField);
        container.add(date.getComponent());
        Label label = LabelFactory.create();
        label.setText(getScheduleDisplayName());
        container.add(label);
        container.add(scheduleField);

        focus.add(viewField);
        focus.add(date.getFocusGroup());
        focus.add(scheduleField);
    }

    /**
     * Notifies any listener to perform a query.
     */
    protected void onQuery() {
        if (listener != null) {
            listener.query();
        }
    }

    /**
     * Invoked when the date changes.
     * <p/>
     * This implementation invokes {@link #onQuery()}.
     */
    protected void onDateChanged() {
        onQuery();
    }

    /**
     * Returns the events, keyed on schedule.
     *
     * @return the events
     */
    private Map<Entity, List<ObjectSet>> getEvents() {
        Date date = getDate();
        Map<Entity, List<ObjectSet>> result
                = new LinkedHashMap<Entity, List<ObjectSet>>();
        Entity selected = (Entity) scheduleField.getSelectedItem();
        List<Entity> schedules;
        if (selected != null) {
            schedules = Arrays.asList(selected);
        } else {
            schedules = getSchedules();
        }
        for (Entity schedule : schedules) {
            List<ObjectSet> events = getEvents(schedule, date);
            result.put(schedule, events);
        }
        return result;
    }

    /**
     * Creates a new field to select a schedule view.
     *
     * @return a new select field
     */
    private SelectField createScheduleViewField() {
        SelectField result;
        List<Entity> views = getScheduleViews();
        Entity defaultView = getDefaultScheduleView();
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
        IMObjectListModel model = createScheduleModel();
        SelectField result = SelectFieldFactory.create(model);
        result.setCellRenderer(IMObjectListCellRenderer.INSTANCE);
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onScheduleChanged();
            }
        });
        return result;
    }

    /**
     * Updates the schedule selector.
     */
    private void updateScheduleField() {
        IMObjectListModel model = createScheduleModel();
        scheduleField.setModel(model);
        if (model.size() > 0) {
            scheduleField.setSelectedIndex(0); // select All
        }
    }

    /**
     * Creates a model containing the schedules.
     *
     * @return a new schedule model
     */
    private IMObjectListModel createScheduleModel() {
        List<Entity> schedules = getSchedules();
        return new IMObjectListModel(schedules, true, false);
    }

    /**
     * Invoked when the schedule view changes.
     * <p/>
     * Notifies any listener to perform a query.
     */
    private void onViewChanged() {
        schedules = null;
        updateScheduleField();
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

}
