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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;

import java.util.Collections;
import java.util.List;

/**
 * Schedule query.
 *
 * @author Tim Anderson
 */
public abstract class ScheduleQuery {

    /**
     * The schedules.
     */
    private final Schedules schedules;

    /**
     * The available views.
     */
    private final List<Entity> views;

    /**
     * The default schedule view.
     */
    private final Entity defaultView;

    /**
     * The schedule selector.
     */
    private SelectField scheduleField;

    /**
     * Listener to notify of query events.
     */
    private QueryListener listener;

    /**
     * The schedule view selector.
     */
    private SelectField viewField;

    /**
     * The list of schedules associated with the selected schedule view.
     */
    private List<Entity> viewSchedules;

    /**
     * The query component.
     */
    private Component component;

    /**
     * The focus group.
     */
    private FocusGroup focus;

    /**
     * Constructs a {@link ScheduleQuery}.
     *
     * @param schedules the schedules
     */
    public ScheduleQuery(Schedules schedules) {
        this.schedules = schedules;
        views = schedules.getScheduleViews();
        defaultView = schedules.getDefaultScheduleView(views);
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            component = createContainer();
            focus = new FocusGroup(getClass().getSimpleName());
            doLayout(component);
        }
        return component;
    }

    /**
     * Returns the selected schedule view.
     *
     * @return the selected schedule view. May be {@code null}
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
        if (!ObjectUtils.equals(viewField.getSelectedItem(), view)) {
            viewField.setSelectedItem(view);
            updateViewSchedules();
        }
    }

    /**
     * Returns the schedules associated with the selected schedule view.
     *
     * @return the schedules
     */
    public List<Entity> getViewSchedules() {
        if (viewSchedules == null) {
            Entity view = getScheduleView();
            if (view != null) {
                viewSchedules = schedules.getSchedules(view);
            }
        }
        return (viewSchedules != null) ? viewSchedules : Collections.<Entity>emptyList();
    }

    /**
     * Returns the selected schedule.
     *
     * @return the selected schedule, or {@code null} if all schedules are selected
     */
    public Entity getSchedule() {
        return (Entity) scheduleField.getSelectedItem();
    }

    /**
     * Returns the selected schedules.
     *
     * @return the selected schedules
     */
    public List<Entity> getSelectedSchedules() {
        Entity schedule = getSchedule();
        return (schedule != null) ? Collections.singletonList(schedule) : getViewSchedules();
    }

    /**
     * Sets the selected schedule.
     *
     * @param schedule the schedule. If {@code null}, indicates all schedules
     */
    public void setSchedule(Entity schedule) {
        getComponent();
        if (schedule != null) {
            scheduleField.setSelectedItem(schedule);
        } else if (scheduleField.getModel().size() > 0) {
            scheduleField.setSelectedIndex(0); // All
        }
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
     * @param listener the listener. May be {@code null}
     */
    public void setListener(QueryListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    protected Schedules getSchedules() {
        return schedules;
    }

    /**
     * Creates a container to lay out the component.
     *
     * @return a new container
     */
    protected Component createContainer() {
        return GridFactory.create(6);
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        viewField = createScheduleViewField();
        scheduleField = createScheduleField();

        Label scheduleLabel = LabelFactory.create();
        scheduleLabel.setText(schedules.getScheduleDisplayName());

        container.add(LabelFactory.create("workflow.scheduling.query.view"));
        container.add(viewField);
        container.add(scheduleLabel);
        container.add(scheduleField);
        focus.add(viewField);
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
     * Invoked when the schedule view changes.
     * <p/>
     * Notifies any listener to perform a query.
     */
    protected void onViewChanged() {
        updateViewSchedules();
        onQuery();
    }

    /**
     * Invoked to update the view schedules.
     */
    protected void updateViewSchedules() {
        viewSchedules = null;
        updateScheduleField();
    }

    /**
     * Creates a new field to select a schedule view.
     *
     * @return a new select field
     */
    private SelectField createScheduleViewField() {
        SelectField result;
        IMObjectListModel model = new IMObjectListModel(views, false, false);
        result = SelectFieldFactory.create(model);
        result.setCellRenderer(IMObjectListCellRenderer.NAME);
        if (defaultView != null) {
            result.setSelectedItem(defaultView);
        }
        result.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
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
        List<Entity> viewSchedules = getViewSchedules();
        IMObjectListModel model = createScheduleModel(viewSchedules);
        SelectField result = SelectFieldFactory.create(model);
        if (defaultView != null && defaultView.equals(getScheduleView())) {
            Entity schedule = schedules.getDefaultSchedule(defaultView, viewSchedules);
            if (schedule != null) {
                result.setSelectedItem(schedule);
            } else {
                result.setSelectedIndex(model.getAllIndex());
            }
        }

        result.setCellRenderer(IMObjectListCellRenderer.NAME);
        result.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
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
        setSchedule(null); // select All
    }

    /**
     * Creates a model containing the schedules.
     *
     * @return a new schedule model
     */
    private IMObjectListModel createScheduleModel() {
        return createScheduleModel(getViewSchedules());
    }

    /**
     * Creates a model containing the schedules.
     *
     * @param schedules the schedules
     * @return a new schedule model
     */
    private IMObjectListModel createScheduleModel(List<Entity> schedules) {
        return new IMObjectListModel(schedules, true, false);
    }

}
