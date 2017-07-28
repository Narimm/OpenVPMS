package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

import java.util.Date;

/**
 * Iterates over reminders returned by {@link ReminderQueueQueryFactory}.
 *
 * @author Tim Anderson
 */
public class ReminderQueueIterator extends UpdatableQueryIterator<Act> {

    /**
     * Constructs a {@link ReminderQueueIterator}.
     * <p>
     * This returns all {@code IN_PROGRESS} reminders with a {@code startTime} less than the specified date,
     * and have no items that have {@code PENDING} or {@code ERROR} status.
     *
     * @param date     the date
     * @param pageSize the page size
     * @param service  the archetype service
     */
    public ReminderQueueIterator(Date date, int pageSize, IArchetypeService service) {
        this(new ReminderQueueQueryFactory(), date, pageSize, service);
    }

    /**
     * Constructs a {@link ReminderQueueIterator}.
     * <p>
     * This returns all {@code IN_PROGRESS} reminders with a {@code startTime} less than the specified date,
     * and have no items that have {@code PENDING} or {@code ERROR} status.
     *
     * @param factory  the query factory
     * @param date     the date
     * @param pageSize the page size
     * @param service  the archetype service
     */
    protected ReminderQueueIterator(ReminderQueueQueryFactory factory, Date date, int pageSize,
                                    IArchetypeService service) {
        super(factory.createQuery(date), pageSize, service);
    }

    /**
     * Returns the next page.
     *
     * @param query   the query
     * @param service the archetype service
     * @return the next page
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IPage<Act> getNext(ArchetypeQuery query, IArchetypeService service) {
        return (IPage) service.get(query);
    }

    /**
     * Returns a unique identifier for the object.
     *
     * @param object the object
     * @return a unique identifier for the object
     */
    @Override
    protected long getId(Act object) {
        return object.getId();
    }
}
