package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.PagedReminderItemIterator;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.system.ServiceHelper;

/**
 * An {@link ProgressBarProcessor} for processing reminder items returned by an {@link ReminderItemQueryFactory}.
 *
 * @author Tim Anderson
 */
abstract class ReminderItemProgressBarProcessor extends ProgressBarProcessor<ObjectSet> {

    /**
     * Constructs a {@link ReminderItemProgressBarProcessor }.
     */
    public ReminderItemProgressBarProcessor(ReminderItemQueryFactory factory) {
        super(null);
        IArchetypeRuleService service = ServiceHelper.getArchetypeService();
        ArchetypeQuery query = factory.createQuery();
        query.setMaxResults(0);
        query.setCountResults(true);
        IPage<IMObject> page = service.get(query);
        int size = page.getTotalResults();
        setItems(new PagedReminderItemIterator(factory, 100, service), size);
    }

    /**
     * Processes an object.
     *
     * @param object the object to process
     */
    @Override
    protected void process(ObjectSet object) {
        Act item = (Act) object.get("item");
        Act reminder = (Act) object.get("reminder");
        process(item, reminder);
        processCompleted(object);
    }

    /**
     * Processes a reminder item.
     *
     * @param item     the reminder item
     * @param reminder the reminder
     */
    protected abstract void process(Act item, Act reminder);

    /**
     * Notifies the iterator that the iteration has updated.
     */
    protected void updated() {
        getIterator().updated();
    }

    /**
     * Returns the iterator.
     *
     * @return the iterator
     */
    @Override
    protected PagedReminderItemIterator getIterator() {
        return (PagedReminderItemIterator) super.getIterator();
    }
}
