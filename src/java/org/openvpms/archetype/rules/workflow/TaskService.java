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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;


/**
 * Implementation of the {@link ScheduleService} for task events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskService extends AbstractScheduleService {

    /**
     * Creates a new <tt>TaskService</tt>.
     *
     * @param service the archetype service
     * @param cache   the cache
     */
    public TaskService(IArchetypeService service, Cache cache) {
        super("act.customerTask", service, cache);
    }

    /**
     * Adds an event to the cache.
     *
     * @param schedule the event schedule
     * @param act      the event act reference
     * @param set      the <tt>ObjectSet</tt> representation of the event
     */
    @Override
    protected void addEvent(IMObjectReference schedule, IMObjectReference act,
                            ObjectSet set) {
        Date from = set.getDate(ScheduleEvent.ACT_START_TIME);
        Date to = set.getDate(ScheduleEvent.ACT_END_TIME);
        from = DateRules.getDate(from);
        to = DateRules.getDate(to);
        for (Element element : getElements(schedule, from, to)) {
            add(element, act, set);
        }
    }

    /**
     * Removes an event from the cache.
     *
     * @param event    the event to remove
     * @param schedule the schedule to remove the event from
     */
    @Override
    protected void removeEvent(Act event, IMObjectReference schedule) {
        Date from = DateRules.getDate(event.getActivityStartTime());
        Date to = DateRules.getDate(event.getActivityEndTime());
        IMObjectReference act = event.getObjectReference();
        for (Element element : getElements(schedule, from, to)) {
            remove(element, act);
        }
    }

    /**
     * Assembles an {@link ObjectSet ObjectSet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    @Override
    protected void assemble(ObjectSet target, ActBean source) {
        super.assemble(target, source);

        IMObjectReference scheduleRef
                = source.getNodeParticipantRef("worklist");
        String scheduleName = getName(scheduleRef);
        target.set(ScheduleEvent.SCHEDULE_REFERENCE, scheduleRef);
        target.set(ScheduleEvent.SCHEDULE_NAME, scheduleName);

        IMObjectReference typeRef
                = source.getNodeParticipantRef("taskType");
        String typeName = getName(typeRef);
        target.set(ScheduleEvent.SCHEDULE_TYPE_REFERENCE, typeRef);
        target.set(ScheduleEvent.SCHEDULE_TYPE_NAME, typeName);
    }

    /**
     * Returns the schedule reference from an event.
     *
     * @param event the event
     * @return a reference to the schedule. May be <tt>null</tt>
     */
    protected IMObjectReference getSchedule(Act event) {
        ActBean bean = new ActBean(event, getService());
        return bean.getNodeParticipantRef("worklist");
    }

    /**
     * Creates a new query to query events for the specified schedule and date
     * range.
     *
     * @param schedule the schedule
     * @param from     the start time
     * @param to       the end time
     * @return a new query
     */
    protected ScheduleEventQuery createQuery(Entity schedule, Date from,
                                             Date to) {
        return new TaskQuery((Party) schedule, from, to, getService());
    }

}
