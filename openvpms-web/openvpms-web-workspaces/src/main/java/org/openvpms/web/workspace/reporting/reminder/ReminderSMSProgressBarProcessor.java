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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.web.resource.i18n.Messages;


/**
 * Sends reminder SMSes, updating a progress bar as it goes.
 *
 * @author Tim Anderson
 */
class ReminderSMSProgressBarProcessor extends ReminderProgressBarProcessor {

    /**
     * Constructs a {@link ReminderSMSProgressBarProcessor}.
     *
     * @param query      the query
     * @param processor  the SMS processor
     * @param statistics the statistics
     */
    public ReminderSMSProgressBarProcessor(ReminderItemSource query, ReminderSMSProcessor processor,
                                           Statistics statistics) {
        super(query, processor, statistics, Messages.get("reporting.reminder.run.sms"));
    }

}
