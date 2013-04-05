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

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Date;


/**
 * Abstract implementation of the {@link ProcessorListener} interface
 * for the {@link ReminderProcessor}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractReminderProcessorListener
        implements ProcessorListener<ReminderEvent> {

    /**
     * The rules.
     */
    private final ReminderRules rules;

    /**
     * Constructs a new {@code AbstractReminderProcessorListener}.
     *
     * @param service      the archetype service
     * @param patientRules the patient rules
     */
    public AbstractReminderProcessorListener(IArchetypeService service, PatientRules patientRules) {
        rules = new ReminderRules(service, new ReminderTypeCache(), patientRules);
    }

    /**
     * Updates a reminder that has been successfully sent.
     *
     * @param reminder the reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void update(Act reminder) {
        rules.updateReminder(reminder, new Date());
    }

    /**
     * Returns the reminder rules.
     *
     * @return the reminder rules
     */
    protected ReminderRules getRules() {
        return rules;
    }

}
