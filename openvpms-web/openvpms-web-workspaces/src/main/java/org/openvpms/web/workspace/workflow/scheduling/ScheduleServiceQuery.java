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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.workflow.ScheduleService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.query.DateSelector;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel.Highlight;


/**
 * Queries the {@link ScheduleService} for events.
 *
 * @author Tim Anderson
 */
public abstract class ScheduleServiceQuery extends BaseScheduleQuery {

    /**
     * Schedule service.
     */
    private ScheduleService service;

    /**
     * The date selector.
     */
    private DateSelector date;

    /**
     * Highlight selector, to change colour of display items.
     */
    private SelectField highlightSelector;

    /**
     * Clinician selector.
     */
    private SelectField clinicianSelector;


    /**
     * Constructs a {@link ScheduleServiceQuery}.
     *
     * @param service   the schedule service
     * @param schedules the schedules
     */
    public ScheduleServiceQuery(ScheduleService service, Schedules schedules) {
        super(schedules);
        this.service = service;
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
     * Sets the date.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        this.date.setDate(date);
    }

    /**
     * Performs the query, returning a list of events keyed on schedule.
     *
     * @return the query result set. May be {@code null}
     */
    public Map<Entity, List<PropertySet>> query() {
        getComponent();
        return getEvents();
    }

    /**
     * Returns the selected clinician.
     *
     * @return the selected clinician, or {@code null} if no clinician is
     *         selected
     */
    public User getClinician() {
        return (User) clinicianSelector.getSelectedItem();
    }

    /**
     * Returns the selected highlight.
     *
     * @return the selected highlight
     */
    public Highlight getHighlight() {
        Highlight result = null;
        Object selected = highlightSelector.getSelectedItem();
        if (selected != null) {
            result = Highlight.valueOf(selected.toString());
        }
        if (result == null) {
            result = Highlight.EVENT_TYPE;
        }
        return result;
    }

    /**
     * Returns the events for a schedule and date.
     *
     * @param schedule the schedule
     * @param date     the date
     * @return the events
     */
    protected List<PropertySet> getEvents(Entity schedule, Date date) {
        return service.getEvents(schedule, date);
    }

    /**
     * Creates a new highlight selector.
     *
     * @return a new highlight selector
     */
    protected SelectField createHighlightSelector() {
        NodeLookupQuery lookups = new NodeLookupQuery(getSchedules().getScheduleViewShortName(), "highlight");
        LookupListModel model = new LookupListModel(lookups);
        SelectField result = SelectFieldFactory.create(model);
        result.setCellRenderer(LookupListCellRenderer.INSTANCE);
        result.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        return result;
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        super.doLayout(container);
        FocusGroup focus = getFocusGroup();

        clinicianSelector = createClinicianSelector();
        container.add(LabelFactory.create("clinician"));
        container.add(clinicianSelector);
        focus.add(clinicianSelector);

        date = new DateSelector();
        date.setListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onDateChanged();
            }
        });
        highlightSelector = createHighlightSelector();
        updateHighlightField();

        container.add(LabelFactory.create("workflow.scheduling.query.date"));
        container.add(date.getComponent());
        container.add(LabelFactory.create("workflow.scheduling.highlight"));
        container.add(highlightSelector);

        focus.add(date.getFocusGroup());
        focus.add(highlightSelector);
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
     * Invoked when the schedule view changes.
     * <p/>
     * Notifies any listener to perform a query.
     */
    @Override
    protected void onViewChanged() {
        updateHighlightField();
        super.onViewChanged();
    }

    /**
     * Returns the events, keyed on schedule.
     *
     * @return the events
     */
    private Map<Entity, List<PropertySet>> getEvents() {
        Date date = getDate();
        Map<Entity, List<PropertySet>> result
                = new LinkedHashMap<Entity, List<PropertySet>>();
        Entity selected = getSchedule();
        List<Entity> schedules;
        if (selected != null) {
            schedules = Arrays.asList(selected);
        } else {
            schedules = getSelectedSchedules();
        }
        for (Entity schedule : schedules) {
            List<PropertySet> events = getEvents(schedule, date);
            result.put(schedule, events);
        }
        return result;
    }

    /**
     * Creates a new dropdown to select clinicians.
     *
     * @return a new clinician selector
     */
    private SelectField createClinicianSelector() {
        UserRules rules = new UserRules();
        List<IMObject> clinicians = new ArrayList<IMObject>();
        ArchetypeQuery query = new ArchetypeQuery(UserArchetypes.USER, true, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        Iterator<User> iter = new IMObjectQueryIterator<User>(query);
        while (iter.hasNext()) {
            User user = iter.next();
            if (rules.isClinician(user)) {
                clinicians.add(user);
            }
        }
        IMObjectListModel model
                = new IMObjectListModel(clinicians, true, false);
        SelectField result = SelectFieldFactory.create(model);
        result.setCellRenderer(IMObjectListCellRenderer.NAME);

        result.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });

        return result;
    }

    /**
     * Updates the highlight based on the selected schedule view.
     */
    private void updateHighlightField() {
        Entity view = getScheduleView();
        if (view != null) {
            EntityBean bean = new EntityBean(view);
            String code = bean.getString("highlight", Highlight.EVENT_TYPE.toString());
            highlightSelector.setSelectedItem(code);
        }
    }

}
