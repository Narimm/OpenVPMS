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

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListModel;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.FreeSlotQuery;
import org.openvpms.archetype.rules.workflow.Slot;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.bound.BoundTimeField;
import org.openvpms.web.component.bound.BoundTimeFieldFactory;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.TimePropertyTransformer;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.BaseScheduleQuery;

import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * Query to find free appointment slots.
 *
 * @author Tim Anderson
 */
public class FreeAppointmentSlotQuery extends BaseScheduleQuery {

    /**
     * The initial date, used to initialise the date range.
     */
    private final Date date;

    /**
     * The date range.
     */
    private DateRange dateRange;

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
     * Constructs a {@link FreeAppointmentSlotQuery}.
     *
     * @param location the current location. May be {@code null}
     * @param view     the current schedule view. May be {@code null}
     * @param schedule the current schedule. May be {@code null}
     * @param date     the current schedule date. May be {@code null}
     */
    public FreeAppointmentSlotQuery(Party location, Entity view, Party schedule, Date date) {
        super(new AppointmentSchedules(location));
        this.date = date;
        fromTime = createTime("fromTime", Messages.get("workflow.scheduling.appointment.find.fromTime"));
        toTime = createTime("toTime", Messages.get("workflow.scheduling.appointment.find.toTime"));
        duration = new SimpleProperty("duration", null, Integer.class,
                                      Messages.get("workflow.scheduling.appointment.find.duration"));
        durationUnits = new SimpleProperty("duration", null, String.class);
        setScheduleView(view);
        setSchedule(schedule);
    }

    /**
     * Sets the selected schedule.
     *
     * @param schedule the schedule. May be {@code null}
     */
    @Override
    public void setSchedule(Party schedule) {
        super.setSchedule(schedule);
        if (schedule != null) {
            IMObjectBean bean = new IMObjectBean(schedule);
            duration.setValue(bean.getInt("slotSize"));
            durationUnits.setValue(bean.getString("slotUnits"));
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
            durationUnits.setValue(minSlotUnits);
        }
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
        query.setFromDate(dateRange.getFrom());
        query.setToDate(dateRange.getTo());
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
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(final Component container) {
        super.doLayout(container);
        dateRange = new DateRange(getFocusGroup(), false) {
            @Override
            protected Component getContainer() {
                // will lay the date range out in the supplied container instead of creating its own
                return container;
            }
        };
        if (date != null) {
            dateRange.setFrom(date);
        } else {
            dateRange.setFrom(new Date());
        }
        dateRange.setTo(DateRules.getDate(dateRange.getFrom(), 1, DateUnits.MONTHS));
        dateRange.getComponent();
        addTime(fromTime, container);
        addTime(toTime, container);
        Label durationLabel = LabelFactory.create();
        durationLabel.setText(duration.getDisplayName());
        container.add(durationLabel);

        container.add(RowFactory.create(Styles.CELL_SPACING, BoundTextComponentFactory.createNumeric(duration, 5),
                                        createDurationUnits()));
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
}
