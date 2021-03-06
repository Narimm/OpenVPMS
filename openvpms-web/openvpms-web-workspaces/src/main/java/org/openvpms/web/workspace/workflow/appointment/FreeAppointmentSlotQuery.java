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

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListModel;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.FreeSlotQuery;
import org.openvpms.archetype.rules.workflow.Slot;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.cache.MapIMObjectCache;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.bound.BoundTimeField;
import org.openvpms.web.component.bound.BoundTimeFieldFactory;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.TimePropertyTransformer;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Query to find free appointment slots.
 *
 * @author Tim Anderson
 */
class FreeAppointmentSlotQuery extends ScheduleQuery {

    /**
     * The initial date, used to initialise the date range.
     */
    private final Date date;

    /**
     * The 'from' time, used to restrict free slots to those after a time each day.
     */
    private final Property fromTime;

    /**
     * The 'to' time, used to restrict free slots to those before a time each day.
     */
    private final Property toTime;

    /**
     * The minimum slot duration.
     */
    private final Property duration;

    /**
     * The minimum slot duration units.
     */
    private final Property durationUnits;

    /**
     * The container for the cage type label.
     */
    private final Component cageLabelContainer = new Row();

    /**
     * The container for the cage type selector.
     */
    private final Component cageContainer = new Row();

    /**
     * The date range.
     */
    private DateRange dateRange;

    /**
     * The cage type selector.
     */
    private SelectField cageSelector;


    /**
     * Constructs a {@link FreeAppointmentSlotQuery}.
     *
     * @param location the current location. May be {@code null}
     * @param view     the current schedule view. May be {@code null}
     * @param schedule the current schedule. May be {@code null}
     * @param date     the current schedule date. May be {@code null}
     * @param prefs    the user preferences
     */
    public FreeAppointmentSlotQuery(Party location, Entity view, Entity schedule, Date date, Preferences prefs) {
        super(new AppointmentSchedules(location, prefs));
        this.date = date;
        fromTime = createTime("fromTime", Messages.get("workflow.scheduling.appointment.find.fromTime"));
        toTime = createTime("toTime", Messages.get("workflow.scheduling.appointment.find.toTime"));
        duration = new SimpleProperty("duration", null, Integer.class,
                                      Messages.get("workflow.scheduling.appointment.find.duration"));
        durationUnits = new SimpleProperty("duration", null, DateUnits.class);
        setScheduleView(view);
        setSchedule(schedule);
    }

    /**
     * Sets the selected schedule.
     *
     * @param schedule the schedule. May be {@code null}
     */
    @Override
    public void setSchedule(Entity schedule) {
        super.setSchedule(schedule);
        if (schedule != null) {
            IMObjectBean bean = new IMObjectBean(schedule);
            duration.setValue(bean.getInt("slotSize"));
            durationUnits.setValue(DateUnits.valueOf(bean.getString("slotUnits")));
        } else {
            int minSlotSize = 0;
            String minSlotUnits = DateUnits.MINUTES.toString();
            int minSlotMinutes = 0;
            for (Entity entity : getSelectedSchedules()) {
                IMObjectBean bean = new IMObjectBean(entity);
                int slotSize = bean.getInt("slotSize");
                String units = bean.getString("slotUnits");
                int slotMinutes = DateUnits.HOURS.name().equals(units) ? 60 * slotSize : slotSize;
                if (minSlotSize == 0 || slotMinutes < minSlotMinutes) {
                    minSlotSize = slotSize;
                    minSlotUnits = units;
                    minSlotMinutes = slotMinutes;
                }
            }
            duration.setValue(minSlotSize);
            durationUnits.setValue(DateUnits.valueOf(minSlotUnits));
        }
    }

    /**
     * Sets the selected schedule view.
     *
     * @param view the schedule view
     */
    @Override
    public void setScheduleView(Entity view) {
        super.setScheduleView(view);
    }

    /**
     * Queries free appointment slots.
     *
     * @return an iterator over the free appointment slots
     */
    public Iterator<Slot> query() {
        FreeSlotQuery query = new FreeSlotQuery(ServiceHelper.getArchetypeService());
        List<Entity> schedules = getSelectedSchedules();
        query.setSchedules(schedules.toArray(new Entity[schedules.size()]));
        Entity cageType = null;
        if (cageSelector != null) {
            cageType = (Entity) cageSelector.getSelectedItem();
        }
        query.setCageType(cageType);

        Date from = dateRange.getFrom();
        Date now = new Date();
        if (from == null || DateRules.compareTo(from, now) < 0) {
            from = getNextSlot(now);
        }
        query.setFromDate(from);
        Date to = dateRange.getTo();
        if (to == null || DateRules.compareTo(to, from) < 0) {
            to = from;
        }
        to = DateRules.getDate(to, 1, DateUnits.DAYS); // FreeSlotQuery returns slots < to
        query.setToDate(to);
        query.setFromTime(getPeriod(fromTime));
        query.setToTime(getPeriod(toTime));
        query.setMinSlotSize(duration.getInt(), getDurationUnits());
        return query.query();
    }

    /**
     * Creates a container to lay out the component.
     *
     * @return a new container
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(8);
    }

    /**
     * Invoked to update the view schedules.
     */
    @Override
    protected void updateViewSchedules() {
        super.updateViewSchedules();
        updateCageTypes();
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(final Component container) {
        super.doLayout(container);

        dateRange = new DateRange(false);
        dateRange.setContainer(container);
        if (date != null) {
            dateRange.setFrom(date);
        } else {
            dateRange.setFrom(new Date());
        }
        dateRange.setTo(DateRules.getDate(dateRange.getFrom(), 1, DateUnits.MONTHS));
        FocusGroup group = getFocusGroup();
        group.add(dateRange.getFocusGroup());
        addTime(fromTime, container);
        addTime(toTime, container);
        Label durationLabel = LabelFactory.create();
        durationLabel.setText(duration.getDisplayName());
        container.add(durationLabel);

        TextField durationField = BoundTextComponentFactory.createNumeric(duration, 5);
        SelectField unitsField = createDurationUnits();
        group.add(durationField);
        group.add(unitsField);
        container.add(RowFactory.create(Styles.CELL_SPACING, durationField, unitsField));
        container.add(cageLabelContainer);
        container.add(cageContainer);
    }

    /**
     * Adds a time field to the container.
     *
     * @param property  the time property
     * @param container the container to add the field to
     */
    private void addTime(Property property, Component container) {
        Label label = LabelFactory.create();
        label.setText(property.getDisplayName());
        container.add(label);
        BoundTimeField field = BoundTimeFieldFactory.create(property);
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Creates a time property.
     *
     * @param name        the property name
     * @param displayName the property display anme
     * @return a new time property
     */
    private Property createTime(String name, String displayName) {
        SimpleProperty property = new SimpleProperty(name, null, Date.class, displayName);
        PropertyTransformer transformer = new TimePropertyTransformer(property);
        property.setTransformer(transformer);
        return property;
    }

    /**
     * Returns a period for a time property.
     *
     * @param property the time property
     * @return the period, as the number of milliseconds for the day, or {@code null} if no time has been set.
     */
    private Period getPeriod(Property property) {
        Date date = property.getDate();
        return (date != null) ? new Period(new DateTime(date).getMillisOfDay()) : null;
    }

    /**
     * Returns the selected duration units.
     *
     * @return the duration units
     */
    private DateUnits getDurationUnits() {
        String code = durationUnits.getString();
        return (code != null) ? DateUnits.valueOf(code) : DateUnits.MINUTES;
    }

    /**
     * Creates a field to select the duration units.
     *
     * @return a new field
     */
    private SelectField createDurationUnits() {
        EnumSet<DateUnits> units = EnumSet.range(DateUnits.MINUTES, DateUnits.WEEKS);
        ListModel model = new DefaultListModel(units.toArray());
        SelectField result = BoundSelectFieldFactory.create(durationUnits, model);
        result.setCellRenderer(new ListCellRenderer() {
            @Override
            public Object getListCellRendererComponent(Component component, Object o, int i) {
                String result = null;
                DateUnits unit = (DateUnits) o;
                switch (unit) {
                    case MINUTES:
                        result = Messages.get("workflow.scheduling.appointment.find.minutes");
                        break;
                    case HOURS:
                        result = Messages.get("workflow.scheduling.appointment.find.hours");
                        break;
                    case DAYS:
                        result = Messages.get("workflow.scheduling.appointment.find.days");
                        break;
                    case WEEKS:
                        result = Messages.get("workflow.scheduling.appointment.find.weeks");
                        break;
                }
                return result;
            }
        });
        result.setSelectedIndex(0);
        return result;
    }

    /**
     * Returns the next slot nearest to the specified time.
     *
     * @param time the slot time
     * @return the next nearest slot
     */
    private Date getNextSlot(Date time) {
        Date from;
        int slotSize = 15;
        DateTime dt = new DateTime(time);
        int min = (dt.getMinuteOfHour() / slotSize) * slotSize;
        if (dt.getMinuteOfHour() % slotSize != 0) {
            min += slotSize;
            if (min >= 60) {
                dt = dt.plusHours(1);
                min = 0;
            }
        }
        from = dt.withMinuteOfHour(min).minuteOfDay().roundFloorCopy().toDate();
        return from;
    }

    /**
     * Updates the cage types for the selected view.
     */
    private void updateCageTypes() {
        Map<Reference, IMObject> cageTypes;
        if (AppointmentHelper.isMultiDayView(getScheduleView())) {
            cageTypes = new HashMap<>();
            IArchetypeRuleService service = ServiceHelper.getArchetypeService();
            MapIMObjectCache cache = new MapIMObjectCache(cageTypes, service);
            for (Entity schedule : getSelectedSchedules()) {
                IMObjectBean bean = new IMObjectBean(schedule);
                cache.get(bean.getNodeTargetObjectRef("cageType"));
            }
        } else {
            cageTypes = Collections.emptyMap();
        }
        if (cageTypes.isEmpty()) {
            cageSelector = null;
            cageLabelContainer.removeAll();
            cageContainer.removeAll();
        } else {
            List<IMObject> objects = new ArrayList<>(cageTypes.values());
            Collections.sort(objects, IMObjectSorter.getNameComparator(true));
            IMObjectListModel model = new IMObjectListModel(objects, true, false);
            cageSelector = SelectFieldFactory.create(model);
            cageSelector.setCellRenderer(IMObjectListCellRenderer.NAME);
            cageSelector.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onQuery();
                }
            });
            if (cageLabelContainer.getComponentCount() == 0) {
                cageLabelContainer.add(LabelFactory.create("workflow.scheduling.appointment.find.cagetype"));
            }
            cageContainer.removeAll();
            cageContainer.add(cageSelector);
        }
    }

}
