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
 */

package org.openvpms.web.app.reporting.reminder;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.i18n.Messages;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;


/**
 * Sends reminder emails, updating a progress bar as it goes.
 *
 * @author Tim Anderson
 */
class ReminderEmailProgressBarProcessor extends ReminderProgressBarProcessor {

    /**
     * The email processor.
     */
    private final ReminderEmailProcessor processor;


    /**
     * Constructs a new <tt>ReminderEmailProgressBarProcessor</tt>.
     *
     * @param reminders     the reminders
     * @param sender        the mail sender
     * @param practice      the practice
     * @param groupTemplate the grouped reminder document template
     * @param statistics    the statistics
     * @param context       the context
     */
    public ReminderEmailProgressBarProcessor(List<List<ReminderEvent>> reminders, JavaMailSender sender,
                                             Party practice, DocumentTemplate groupTemplate,
                                             Statistics statistics, Context context) {
        super(reminders, statistics, Messages.get("reporting.reminder.run.email"));
        processor = new ReminderEmailProcessor(sender, practice, groupTemplate, context);
    }

    /**
     * Processes an object.
     *
     * @param events the reminder events to process
     */
    protected void process(List<ReminderEvent> events) {
        super.process(events);
        try {
            processor.process(events);
            processCompleted(events);
        } catch (Throwable exception) {
/*            if (exception.getCause() instanceof AddressException) {
                ReminderEvent event = events.get(0);
                Contact contact = event.getContact();
                String to = null;

                Party party = contact.getParty();
                String customer = null;
                if (party != null) {
                    customer = "id=" + party.getId() + ", name=" + party.getName();
                }

                log.warn("Invalid email address for customer " + customer + ": " + to, exception.getCause());
            }*/
            processError(exception, events);
        }
    }

}
