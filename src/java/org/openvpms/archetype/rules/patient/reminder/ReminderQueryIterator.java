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

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.IPage;

import java.util.Iterator;


/**
 * Reminder query iterator.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderQueryIterator implements Iterator<Act> {

    /**
     * The reminder query.
     */
    private final ReminderQuery query;

    /**
     * The current page no.
     */
    private int page = 0;

    /**
     * The current page.
     */
    private IPage<Act> reminders;

    /**
     * Iterator over the reminders.
     */
    private Iterator<Act> iterator;


    /**
     * Constructs a new <code>ReminderQueryIterator</code>.
     *
     * @param query the query
     */
    public ReminderQueryIterator(ReminderQuery query) {
        this.query = query;
    }

    /**
     * Determines if there are any more reminders to process.
     *
     * @return <code>true</code> if the are more reminders, otherwise
     *         <code>false</code>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean hasNext() {
        final int maxRows = 25;
        if (reminders == null || !iterator.hasNext()) {
            reminders = query.query(page * maxRows, maxRows);
            iterator = reminders.getRows().iterator();
            page++;
        }
        return iterator.hasNext();
    }

    /**
     * Returns the next reminder.
     *
     * @return the next reminder
     */
    public Act next() {
        return iterator.next();
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
