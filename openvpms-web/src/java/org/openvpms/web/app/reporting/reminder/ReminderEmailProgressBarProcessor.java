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
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.reminder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.resource.util.Messages;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.AddressException;
import java.util.List;


/**
 * Sends reminder emails, updating a progress bar as it goes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderEmailProgressBarProcessor extends ReminderProgressBarProcessor {

    /**
     * The email processor.
     */
    private final ReminderEmailProcessor processor;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReminderEmailProgressBarProcessor.class);


    /**
     * Constructs a new <tt>ReminderEmailProgressBarProcessor</tt>.
     *
     * @param reminders     the reminders
     * @param sender        the mail sender
     * @param practice      the practice
     * @param groupTemplate the grouped reminder document template
     * @param statistics    the statistics
     */
    public ReminderEmailProgressBarProcessor(List<List<ReminderEvent>> reminders, JavaMailSender sender,
                                             Party practice, Entity groupTemplate,
                                             Statistics statistics) {
        super(reminders, statistics, Messages.get("reporting.reminder.run.email"));
        processor = new ReminderEmailProcessor(sender, practice, groupTemplate);
    }

    /**
     * Processes an object.
     *
     * @param events the reminder events to process
     * @throws OpenVPMSException if the object cannot be processed
     */
    protected void process(List<ReminderEvent> events) {
        try {
            processor.process(events);
            processCompleted(events);
        } catch (ReminderProcessorException exception) {
            if (exception.getCause() instanceof AddressException) {
                ReminderEvent event = events.get(0);
                Contact contact = event.getContact();
                String to = null;

                Party party = contact.getParty();
                String customer = null;
                if (party != null) {
                    customer = "id=" + party.getId() + ", name=" + party.getName();
                }

                log.warn("Invalid email address for customer " + customer + ": " + to, exception.getCause());
                skip(events);
            } else {
                throw exception;
            }
        }
    }

}
