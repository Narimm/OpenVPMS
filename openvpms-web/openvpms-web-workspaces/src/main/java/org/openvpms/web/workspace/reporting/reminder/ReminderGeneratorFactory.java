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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.DueReminderQuery;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Factory for {@link ReminderGenerator} instances.
 *
 * @author Tim Anderson
 */
public class ReminderGeneratorFactory {

    /**
     * Constructs a {@link ReminderGenerator} to process a single reminder.
     *
     * @param event       the reminder event
     * @param context     the context
     * @param mailContext the mail context, used when printing reminders interactively. May be {@code null}
     * @param help        the help context
     */
    public ReminderGenerator create(ReminderEvent event, Context context, MailContext mailContext, HelpContext help) {
        return new ReminderGenerator(event, context, mailContext, help);
    }

    /**
     * Constructs a {@link ReminderGenerator} for reminders returned by a query.
     *
     * @param query       the query
     * @param context     the context
     * @param mailContext the mail context, used when printing reminders interactively. May be {@code null}
     * @param help        the help context
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException for any error
     */
    public ReminderGenerator create(DueReminderQuery query, Context context, MailContext mailContext,
                                    HelpContext help) {
        return new ReminderGenerator(query, context, mailContext, help);
    }

}
