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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Factory for {@link ReminderGenerator} instances.
 *
 * @author Tim Anderson
 */
public class ReminderGeneratorFactory {

    /**
     * Constructs a {@link ReminderGenerator} to process a single reminder item.
     *
     * @param item     the reminder item
     * @param location the practice location
     * @param practice the practice
     * @param help     the help context
     */
    public ReminderGenerator create(Act item, Party location, Party practice, HelpContext help) {
        ActBean bean = new ActBean(item);
        Act reminder = (Act) bean.getNodeSourceObject("reminder");
        if (reminder == null) {
            throw new IllegalArgumentException("Argument 'item' is not associated with any reminder");
        }
        return create(item, reminder, location, practice, help);
    }

    /**
     * Constructs a {@link ReminderGenerator} to process a single reminder item.
     *
     * @param item     the reminder item
     * @param reminder the reminder
     * @param location the practice location
     * @param practice the practice
     * @param help     the help context
     */
    public ReminderGenerator create(Act item, Act reminder, Party location, Party practice, HelpContext help) {
        return new ReminderGenerator(item, reminder, help, createFactory(location, practice, help));
    }

    /**
     * Constructs a {@link ReminderGenerator} for reminders returned by a query.
     *
     * @param factory  the query factory
     * @param location the practice location
     * @param practice the practice
     * @param help     the help context
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException for any error
     */
    public ReminderGenerator create(ReminderItemQueryFactory factory, Party location, Party practice, HelpContext help) {
        return new ReminderGenerator(factory, help, createFactory(location, practice, help));
    }


    public PatientReminderProcessorFactory createFactory(Party location, Party practice, HelpContext help) {
        return new PatientReminderProcessorFactory(location, practice, help);
    }

    public PatientReminderPreviewer createPreviewer(PatientReminderProcessor processor, HelpContext help) {
        if (processor instanceof ReminderEmailProcessor) {
            return new ReminderEmailPreviewer((ReminderEmailProcessor) processor, help);
        } else if (processor instanceof ReminderSMSProcessor) {
            return new ReminderSMSPreviewer((ReminderSMSProcessor) processor, help);
        } else if (processor instanceof ReminderPrintProcessor) {
            return new ReminderPrintPreviewer((ReminderPrintProcessor) processor);
        }
        throw new IllegalArgumentException("Unsupported processor: " + processor.getClass().getName());
    }

}
