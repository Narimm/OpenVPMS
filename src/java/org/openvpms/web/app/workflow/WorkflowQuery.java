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

package org.openvpms.web.app.workflow;

import echopointng.DateChooser;
import echopointng.DateField;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.list.AbstractListCellRenderer;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.select.IMObjectSelectorListener;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Query for workflow acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class WorkflowQuery<T> extends ActQuery<T> {

    /**
     * The status range selector.
     */
    private SelectField statusRange;

    /**
     * The status range listener.
     */
    private final ActionListener statusRangeListener;

    /**
     * The date.
     */
    private DateField date;

    /**
     * The last date processed by {@link #onDateChanged()}.
     * todo - this is a workaround for a bug/feature of the EPNG date field
     * which seems to generate 2 events for the one update to the text field.
     */
    private Date lastDate;

    /**
     * The clinician selector.
     */
    private IMObjectSelector<User> clinician;

    /**
     * Indicates no valid clinician selected.
     */
    protected static final IMObjectReference INVALID_CLINICIAN
            = new IMObjectReference(new ArchetypeId("security.user"),
                                    "dummylinkId");

    /**
     * All acts.
     */
    private static final String ALL = "ALL";

    /**
     * Incomplete acts.
     */
    private static final String INCOMPLETE = "INCOMPLETE";

    /**
     * Complete acts.
     */
    private static final String COMPLETE = "COMPLETE";


    /**
     * Constructs a new <code>WorkflowQuery</code>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statuses      the act statuses to search on. May be empty
     */
    public WorkflowQuery(Entity entity, String participant,
                         String participation, String[] shortNames,
                         String[] statuses) {
        super(entity, participant, participation, shortNames, statuses);
        statusRangeListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onStatusRangeChanged();
            }
        };
        clinician = new IMObjectSelector<User>(Messages.get("label.clinician"),
                                               new String[]{"security.user"});
        clinician.setListener(new IMObjectSelectorListener<User>() {
            public void selected(User object) {
                onQuery();
            }

            public void create() {
                // no-op
            }
        });
    }

    /**
     * Returns the selected date.
     *
     * @return the selected date
     */
    public Date getDate() {
        Date datetime = date.getDateChooser().getSelectedDate().getTime();
        return DateHelper.getDayMonthYear(datetime);
    }

    /**
     * Returns the selected status range.
     *
     * @return the selected status range
     */
    protected WorkflowStatus.StatusRange getStatusRange() {
        String selected = (String) statusRange.getSelectedItem();
        WorkflowStatus.StatusRange range = WorkflowStatus.StatusRange.ALL;
        if (INCOMPLETE.equals(selected)) {
            range = WorkflowStatus.StatusRange.INCOMPLETE;
        } else if (COMPLETE.equals(selected)) {
            range = WorkflowStatus.StatusRange.COMPLETE;
        }
        return range;
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        statusRange = SelectFieldFactory.create(
                new Object[]{ALL, INCOMPLETE, COMPLETE});
        statusRange.setCellRenderer(new StatusRangeListCellRenderer());
        statusRange.setSelectedItem(INCOMPLETE);
        statusRange.addActionListener(statusRangeListener);

        Button prevWeek = ButtonFactory.create(
                null, "date.previousWeek", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(-7);
            }
        });
        Button prevDay = ButtonFactory.create(
                null, "date.previousDay", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(-1);
            }
        });
        date = DateFieldFactory.create();
        Button currentDay = ButtonFactory.create(
                null, "date.currentDay", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(0);
            }
        });
        Button nextDay = ButtonFactory.create(
                null, "date.nextDay", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(1);
            }
        });
        Button nextWeek = ButtonFactory.create(
                null, "date.nextWeek", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(7);
            }
        });
        date.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onDateChanged();
                    }
                });

        Row row = RowFactory.create("CellSpacing", prevWeek, prevDay, date,
                                    currentDay, nextDay, nextWeek);

        container.add(LabelFactory.create("actquery.status"));
        container.add(statusRange);
        container.add(row);
        container.add(LabelFactory.create("clinician"));
        container.add(clinician.getComponent());

        FocusGroup focus = getFocusGroup();
        focus.add(statusRange);
        focus.add(prevWeek);
        focus.add(prevDay);
        focus.add(date);
        focus.add(currentDay);
        focus.add(nextDay);
        focus.add(nextWeek);
        focus.add(clinician.getFocusGroup());
    }

    /**
     * Returns the 'from' date.
     *
     * @return the 'from' date, or <tt>null</tt> to query all dates
     */
    @Override
    protected Date getFrom() {
        return getDate();
    }

    /**
     * Returns the 'to' date.
     *
     * @return the 'to' date, or <tt>null</tt> to query all dates
     */
    @Override
    protected Date getTo() {
        long end = getFrom().getTime() + DateUtils.MILLIS_IN_DAY
                - DateUtils.MILLIS_IN_SECOND;
        return new Date(end);
    }

    /**
     * Returns the selected clinician.
     *
     * @return the selected clinician, or {@link #INVALID_CLINICIAN} if the
     *         input name is invalid
     */
    protected IMObjectReference getClinician() {
        IMObject object = clinician.getObject();
        if (object != null) {
            return object.getObjectReference();
        } else if (!clinician.isValid()) {
            return INVALID_CLINICIAN;
        }
        return null;
    }

    /**
     * Returns the participant constraints.
     *
     * @return the participant constraints
     */
    protected ParticipantConstraint[] getParticipantConstraints() {
        ParticipantConstraint[] participants;
        ParticipantConstraint participation = getParticipantConstraint();

        if (getClinician() != null) {
            ParticipantConstraint clinician = new ParticipantConstraint(
                    "clinician", "participation.clinician", getClinician());
            participants = new ParticipantConstraint[]{participation,
                                                       clinician};
        } else {
            participants = new ParticipantConstraint[]{participation};
        }
        return participants;
    }

    /**
     * Invoked to change the selected date.
     *
     * @param days the no. of days to add. <code>0</code> indicates current date
     */
    private void addDays(int days) {
        DateChooser dateChooser = date.getDateChooser();
        Calendar calendar;
        if (days == 0) {
            calendar = new GregorianCalendar();
            calendar.setTime(new Date());
        } else {
            calendar = dateChooser.getSelectedDate();
            calendar.add(Calendar.DAY_OF_MONTH, days);
        }
        date.getDateChooser().setSelectedDate(calendar);
    }

    /**
     * Invoked when a status range is selected.
     */
    protected void onStatusRangeChanged() {
        onQuery();
    }

    /**
     * Invoked when the date is updated.
     * Updates the status range selector to:
     * <ul>
     * <li>select INCOMPLETE appointments for the current date; or</li>
     * <li>ALL appointments for any other date</li>
     * </ul>
     */
    protected void onDateChanged() {
        Date date = getDate();
        if (!date.equals(lastDate)) {
            Date today = DateHelper.getDayMonthYear(new Date());
            statusRange.removeActionListener(statusRangeListener);
            if (date.equals(today)) {
                statusRange.setSelectedItem(INCOMPLETE);
            } else {
                statusRange.setSelectedItem(ALL);
            }
            statusRange.addActionListener(statusRangeListener);
            onQuery();
        }
    }

    /**
     * Notify listnerss to perform a query.
     */
    @Override
    protected void onQuery() {
        lastDate = getDate();
        super.onQuery();
    }

    /**
     * Cell renderer for the status range combo.
     */
    private static class StatusRangeListCellRenderer
            extends AbstractListCellRenderer<String> {


        /**
         * Constructs a new <tt>StatusRangeListCellRenderer</tt>.
         */
        public StatusRangeListCellRenderer() {
            super(String.class);
        }

        /**
         * Renders an object.
         *
         * @param list   the list component
         * @param object the object to render. May be <tt>null</tt>
         * @param index  the object index
         * @return the rendered object
         */
        protected Object getComponent(Component list, String object,
                                      int index) {
            return Messages.get("workflow.scheduling.statusrange." + object);
        }

        /**
         * Determines if an object represents 'All'.
         *
         * @param list   the list component
         * @param object the object. May be <tt>null</tt>
         * @param index  the object index
         * @return <code>true</code> if the object represents 'All'.
         */
        protected boolean isAll(Component list, String object, int index) {
            return ALL.equals(object);
        }

        /**
         * Determines if an object represents 'None'.
         *
         * @param list   the list component
         * @param object the object. May be <tt>null</tt>
         * @param index  the object index
         * @return <code>true</code> if the object represents 'None'.
         */
        protected boolean isNone(Component list, String object, int index) {
            return false;
        }
    }
}
