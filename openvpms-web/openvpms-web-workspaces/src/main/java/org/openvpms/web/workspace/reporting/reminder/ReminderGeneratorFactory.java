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

import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
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
     * @param item        the reminder item
     * @param context     the context
     * @param mailContext the mail context, used when printing reminders interactively. May be {@code null}
     * @param help        the help context
     */
    public ReminderGenerator create(Act item, Context context, MailContext mailContext, HelpContext help) {
        ActBean bean = new ActBean(item);
        Act reminder = (Act) bean.getNodeSourceObject("reminder");
        if (reminder == null) {
            throw new IllegalArgumentException("Argument 'item' is not associated with any reminder");
        }
        return create(item, reminder, context, mailContext, help);
    }

    /**
     * Constructs a {@link ReminderGenerator} to process a single reminder.
     *
     * @param item        the reminder item
     * @param reminder    the reminder
     * @param context     the context
     * @param mailContext the mail context, used when printing reminders interactively. May be {@code null}
     * @param help        the help context
     */
    public ReminderGenerator create(Act item, Act reminder, Context context, MailContext mailContext,
                                    HelpContext help) {
        return new ReminderGenerator(item, reminder, context, mailContext, help);
    }

    /**
     * Constructs a {@link ReminderGenerator} for reminders returned by a query.
     *
     * @param factory     the query factory
     * @param context     the context
     * @param mailContext the mail context, used when printing reminders interactively. May be {@code null}
     * @param help        the help context
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException for any error
     */
    public ReminderGenerator create(ReminderItemQueryFactory factory, Context context, MailContext mailContext,
                                    HelpContext help) {
        return new ReminderGenerator(factory, context, mailContext, help);
    }

}
