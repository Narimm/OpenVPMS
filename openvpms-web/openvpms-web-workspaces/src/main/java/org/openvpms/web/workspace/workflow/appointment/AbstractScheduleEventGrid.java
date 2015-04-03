package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Abstract implementation of the {@link ScheduleEventGrid} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractScheduleEventGrid implements ScheduleEventGrid {

    /**
     * The schedule view.
     */
    private final Entity scheduleView;

    /**
     * The schedules.
     */
    private List<Schedule> schedules = Collections.emptyList();

    /**
     * The grid start date.
     */
    private Date startDate;

    /**
     * The grid end date.
     */
    private Date endDate;


    /**
     * Constructs an {@link AbstractScheduleEventGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the grid start and end date
     */
    public AbstractScheduleEventGrid(Entity scheduleView, Date date) {
        this(scheduleView, date, date);
    }

    /**
     * Constructs an {@link AbstractScheduleEventGrid}.
     *
     * @param scheduleView the schedule view
     * @param startDate    the grid start date
     * @param endDate      the grid end date
     */
    public AbstractScheduleEventGrid(Entity scheduleView, Date startDate, Date endDate) {
        this.scheduleView = scheduleView;
        setStartDate(startDate);
        setEndDate(endDate);
    }

    /**
     * Returns the schedule view associated with this grid.
     *
     * @return the schedule view
     */
    @Override
    public Entity getScheduleView() {
        return scheduleView;
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    @Override
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * Returns the schedule start date.
     *
     * @return the start date, excluding any time
     */
    @Override
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the schedule start date.
     * <p/>
     * Any time is removed.
     *
     * @param startDate the schedule start date
     */
    public void setStartDate(Date startDate) {
        this.startDate = DateRules.getDate(startDate);
    }

    /**
     * Returns the schedule end date.
     *
     * @return the end date, excluding any time
     */
    @Override
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the schedule end date.
     * <p/>
     * Any time is removed.
     *
     * @param endDate the end date
     */
    public void setEndDate(Date endDate) {
        this.endDate = DateRules.getDate(endDate);
    }

    /**
     * Sets the schedules.
     *
     * @param schedules the schedules
     */
    protected void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

}
