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
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.DateSelector;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ScheduleQuery {

    private SelectField viewField;
    private SelectField scheduleField;
    private List<Party> schedules;
    private DateSelector date;
    private Component component;
    private AppointmentService service;
    private QueryListener listener;
    private FocusGroup focus;

    public ScheduleQuery() {
        service = ServiceHelper.getAppointmentService();
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

    public Entity getScheduleView() {
        return (Entity) viewField.getSelectedItem();
    }

    public void setScheduleView(Entity view) {
        viewField.setSelectedItem(view);
        schedules = null;
    }

    public List<Party> getSchedules() {
        if (schedules == null) {
            Entity view = getScheduleView();
            if (view != null) {
                schedules = new ArrayList<Party>();
                EntityBean bean = new EntityBean(view);
                for (Entity entity : bean.getNodeTargetEntities("schedules")) {
                    Party schedule = (Party) entity;
                    schedules.add(schedule);
                }
            }
        }
        return (schedules != null) ? schedules : Collections.<Party>emptyList();
    }

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

    public void setListener(QueryListener listener) {
        this.listener = listener;
    }

    private Map<Party, List<ObjectSet>> getAppointments() {
        Date date = getDate();
        Map<Party, List<ObjectSet>> result
                = new HashMap<Party, List<ObjectSet>>();
        for (Party schedule : getSchedules()) {
            List<ObjectSet> appointments = service.getAppointments(
                    schedule, date);
            result.put(schedule, appointments);
        }
        return result;
    }

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
        container.add(scheduleField);

        focus = new FocusGroup("ScheduleQuery");
        focus.add(this.viewField);
        focus.add(date.getFocusGroup());
        focus.add(scheduleField);
    }

    private SelectField createScheduleViewField() {
        SelectField result;
        List<IMObject> views = getViews();
        IMObjectListModel model = new IMObjectListModel(views, false, false);
        result = SelectFieldFactory.create(model);
        result.setCellRenderer(IMObjectListCellRenderer.INSTANCE);
        if (!views.isEmpty()) {
            result.setSelectedIndex(0);
        }
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onViewChanged();
            }
        });
        return result;
    }

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

    private void onViewChanged() {
        schedules = null;
        onQuery();
    }

    private void onScheduleChanged() {
        onQuery();
    }

    private void onQuery() {
        if (listener != null) {
            listener.query();
        }
    }

    /**
     * Returns the schedule views.
     */
    private List<IMObject> getViews() {
        ArchetypeQuery query = new ArchetypeQuery("entity.scheduleViewType",
                                                  false, true);
        return ServiceHelper.getArchetypeService().get(query).getResults();
    }


}
