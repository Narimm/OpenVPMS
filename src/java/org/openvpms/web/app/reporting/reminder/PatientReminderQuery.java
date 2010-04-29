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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.reminder;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.archetype.rules.patient.reminder.DueReminderQuery;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderQuery;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.AbstractArchetypeQuery;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;


/**
 * Patient reminder query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientReminderQuery extends AbstractArchetypeQuery<Act> {

    /**
     * Reminder type filter.
     */
    private SelectField reminderType;

    /**
     * The date range.
     */
    private DateRange dateRange;


    /**
     * Constructs a new <tt>ReminderQuery</tt>.
     *
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public PatientReminderQuery() {
        super(new String[]{ReminderArchetypes.REMINDER}, Act.class);
        QueryFactory.initialise(this);
    }

    /**
     * Creates an {@link ReminderQuery} from this.
     *
     * @return a new query
     */
    public DueReminderQuery createReminderQuery() {
        IMObject obj = (IMObject) reminderType.getSelectedItem();
        Entity entity = (obj instanceof Entity) ? (Entity) obj : null;
        Date from = dateRange.getFrom();
        Date to = dateRange.getTo();
        DueReminderQuery query = new DueReminderQuery();
        query.setReminderType(entity);
        query.setFrom(from);
        query.setTo(to);
        query.setCancelDate(new Date());
        return query;
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        reminderType = SelectFieldFactory.create(createReminderTypeModel());
        reminderType.setCellRenderer(IMObjectListCellRenderer.NAME);

        Row reminderTypeRow = RowFactory.create(reminderType);
        // wrap the list in a row as a workaround for render bug in firefox.
        // See OVPMS-239

        container.add(reminderTypeRow);

        dateRange = new DateRange(getFocusGroup(), false);
        container.add(dateRange.getComponent());

        // default dueFrom to the 1st of next month
        Calendar calendarFrom = new GregorianCalendar();
        calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
        calendarFrom.add(Calendar.MONTH, 1);
        dateRange.setFrom(calendarFrom.getTime());

        // default dueTo to the last of next month
        Calendar calendarTo = new GregorianCalendar();
        calendarTo.set(Calendar.DAY_OF_MONTH, 1);
        calendarTo.add(Calendar.MONTH, 2);
        calendarTo.add(Calendar.DAY_OF_MONTH, -1);
        dateRange.setTo(calendarTo.getTime());
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    @Override
    protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
        DueReminderQuery query = createReminderQuery();
        Iterator<Act> iterator = query.query().iterator();
        List<Act> due = new ArrayList<Act>();
        while (iterator.hasNext()) {
            due.add(iterator.next());
        }
        return new ListResultSet<Act>(due, getMaxResults());
    }

    /**
     * Creates the list model of reminder types.
     *
     * @return a new list model
     */
    private ListModel createReminderTypeModel() {
        ArchetypeQuery query = new ArchetypeQuery("entity.reminderType", true)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS);
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<IMObject> rows = service.get(query).getResults();
        return new IMObjectListModel(rows, true, false);
    }

}
