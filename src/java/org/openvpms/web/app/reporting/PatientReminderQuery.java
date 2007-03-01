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

package org.openvpms.web.app.reporting;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderQuery;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.AbstractArchetypeServiceResultSet;
import org.openvpms.web.component.im.query.AbstractQuery;
import org.openvpms.web.component.im.query.ActDateRange;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Patient reminder query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientReminderQuery extends AbstractQuery<ObjectSet> {

    /**
     * Reminder type filter.
     */
    private SelectField reminderType;

    /**
     * The date range.
     */
    private ActDateRange dateRange;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(PatientReminderQuery.class);


    /**
     * Constructs a new <tt>ReminderQuery</tt>.
     *
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public PatientReminderQuery() {
        super(new String[]{"act.patientReminder"});
    }

    /**
     * Creates an {@link ReminderQuery} from this.
     *
     * @return a new query
     */
    public ReminderQuery createReminderQuery() {
        IMObject obj = (IMObject) reminderType.getSelectedItem();
        Entity entity = (obj instanceof Entity) ? (Entity) obj : null;
        Date from = dateRange.getFrom();
        Date to = dateRange.getTo();
        ReminderQuery query = new ReminderQuery();
        query.setReminderType(entity);
        query.setDueDateRange(from, to);
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
        reminderType.setCellRenderer(new IMObjectListCellRenderer());

        Row reminderTypeRow = RowFactory.create(reminderType);
        // wrap the list in a row as a workaround for render bug in firefox.
        // See OVPMS-239

        container.add(reminderTypeRow);

        dateRange = new ActDateRange(getFocusGroup());
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
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new AbstractArchetypeServiceResultSet<ObjectSet>(
                getMaxResults(), sort) {

            protected ArchetypeQuery createQuery() {
                return createReminderQuery().createQuery();
            }

            @Override
            protected IPage<ObjectSet> getPage(int firstResult,
                                               int maxResults) {
                IPage<ObjectSet> result = null;
                try {
                    IArchetypeService service
                            = ArchetypeServiceHelper.getArchetypeService();
                    ArchetypeQuery query = createQuery(firstResult, maxResults);
                    query.setDistinct(true);
                    result = service.getObjects(query);
                } catch (OpenVPMSException exception) {
                    log.error(exception, exception);
                }
                return result;
            }
        };
    }

    /**
     * Creates the list model of reminder types.
     *
     * @return a new list model
     */
    private ListModel createReminderTypeModel() {
        IPage<IMObject> page = ArchetypeQueryHelper.get(
                ArchetypeServiceHelper.getArchetypeService(),
                new String[]{"entity.reminderType"}, true, 0,
                ArchetypeQuery.ALL_RESULTS);
        List<IMObject> rows = page.getResults();
        return new IMObjectListModel(rows, true, false);
    }

}
