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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.web.component.processor.BatchProcessorComponent;


/**
 * Reminder batch processor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
interface ReminderBatchProcessor extends BatchProcessorComponent {

    /**
     * Determines if reminders should be updated on completion.
     * <p/>
     * If set, the <tt>reminderCount</tt> is incremented the <tt>lastSent</tt> timestamp set on completed reminders.
     *
     * @param update if <tt>true</tt> update reminders on completion
     */
    void setUpdateOnCompletion(boolean update);

}
