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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.DateNavigator;
import org.openvpms.web.component.im.query.DateSelector;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.openvpms.web.echo.style.Styles.WIDE_CELL_SPACING;

/**
 * Roster query.
 *
 * @author Tim Anderson
 */
public abstract class RosterQuery<T extends Entity> extends AbstractEntityQuery<T> {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The title.
     */
    private Label title;

    /**
     * The date selector.
     */
    private DateSelector date;

    /**
     * Constructs a {@link RosterQuery}.
     *
     * @param archetypes    the archetypes being queried
     * @param checkIdentity if {@code true}, automatically check the identity search box if the value contains numerics
     * @param type          the type that this query returns
     * @param context       the context
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public RosterQuery(String[] archetypes, boolean checkIdentity, Class type, Context context) {
        super(archetypes, checkIdentity, type);
        this.context = context;
        date = new DateSelector();
        date.setNavigator(new WeekNavigator());
        date.setListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onDateChanged();
            }
        });
    }

    /**
     * Sets the start date.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        this.date.setDate(date);
    }

    /**
     * Returns the start date.
     *
     * @return the start date
     */
    public Date getDate() {
        return date.getDate();
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return {@code true} if the query should be run automatically;
     * otherwise {@code false}
     */
    @Override
    public boolean isAuto() {
        return true;
    }

    /**
     * Returns the preferred height of the query when rendered.
     *
     * @return the preferred height, or {@code null} if it has no preferred height
     */
    @Override
    public Extent getHeight() {
        return getHeight(2);
    }

    /**
     * Returns all results matching the query.
     *
     * @return the results
     */
    public List<T> getResults() {
        return QueryHelper.query(this);
    }

    /**
     * Invoked when the date changes.
     * <p/>
     * Updates the title and invokes {@link #onQuery()}.
     */
    protected void onDateChanged() {
        updateTitle();
        onQuery();
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Creates a container component to lay out the query component in.
     * This implementation returns a new {@code Row}.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    @Override
    protected Component createContainer() {
        return ColumnFactory.create(WIDE_CELL_SPACING);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        title = LabelFactory.create(null, Styles.BOLD);
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(Alignment.ALIGN_CENTER);
        title.setLayoutData(layout);
        updateTitle();

        Row row = RowFactory.create(Styles.CELL_SPACING);
        doQueryLayout(row);

        container.add(title);
        container.add(row);
    }

    /**
     * Adds the date to a container.
     *
     * @param container the container
     */
    protected void addDate(Component container) {
        container.add(date.getComponent());
        getFocusGroup().add(date.getFocusGroup());
    }

    /**
     * Lays out the query components in a container.
     *
     * @param container the container
     */
    protected abstract void doQueryLayout(Component container);

    /**
     * Updates the title based on the selected date.
     */
    private void updateTitle() {
        Date from = getDate();
        Date to = DateRules.getDate(from, 6, DateUnits.DAYS);
        boolean sameMonth = DateRules.getMonthStart(from).equals(DateRules.getMonthStart(to));
        String text;
        if (sameMonth) {
            text = Messages.format("workflow.rostering.week.samemonth", from, to);
        } else {
            text = Messages.format("workflow.rostering.week.diffmonth", from, to);
        }
        title.setText(text);
    }

    private static class WeekNavigator extends DateNavigator {

        /**
         * Returns the current date.
         *
         * @return the current date
         */
        @Override
        public Date getCurrent() {
            Date now = new Date();
            return getFirstDayOfWeek(now);
        }

        /**
         * Returns the date to display, given a date.
         *
         * @param date the date
         * @return the first day of the week relative to the supplied date
         */
        @Override
        public Date getDate(Date date) {
            return getFirstDayOfWeek(date);
        }

        /**
         * Returns the next date.
         *
         * @param date the starting date
         * @return the date after {@code date}
         */
        @Override
        public Date getNext(Date date) {
            return DateRules.getDate(getFirstDayOfWeek(date), 1, DateUnits.WEEKS);
        }

        /**
         * Returns the previous date.
         *
         * @param date the starting date
         * @return the date before {@code date}
         */
        @Override
        public Date getPrevious(Date date) {
            return DateRules.getDate(getFirstDayOfWeek(date), -1, DateUnits.WEEKS);
        }

        /**
         * Returns the next term.
         * <p/>
         * This implementation returns a week after the specified date.
         *
         * @param date the starting date
         * @return a week after {@code date}
         */
        @Override
        public Date getNextTerm(Date date) {
            return DateRules.getDate(getFirstDayOfWeek(date), 2, DateUnits.WEEKS);
        }

        /**
         * Returns the next term.
         * <p/>
         * This implementation returns a week before the specified date.
         *
         * @param date the starting date
         * @return a week before {@code date}
         */
        @Override
        public Date getPreviousTerm(Date date) {
            return DateRules.getDate(getFirstDayOfWeek(date), -2, DateUnits.WEEKS);
        }

        private Date getFirstDayOfWeek(Date date) {
            LocalDate localDate = DateRules.toLocalDate(date);
            LocalDate firstDayOfWeek = localDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
            return DateRules.toDate(firstDayOfWeek);
        }
    }

}
