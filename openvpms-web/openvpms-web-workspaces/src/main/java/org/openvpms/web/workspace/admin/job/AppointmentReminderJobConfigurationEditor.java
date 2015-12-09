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

package org.openvpms.web.workspace.admin.job;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

import static org.openvpms.web.workspace.admin.job.AppointmentReminderJobLayoutStrategy.SMS_FROM;
import static org.openvpms.web.workspace.admin.job.AppointmentReminderJobLayoutStrategy.SMS_FROM_UNITS;
import static org.openvpms.web.workspace.admin.job.AppointmentReminderJobLayoutStrategy.SMS_TO;
import static org.openvpms.web.workspace.admin.job.AppointmentReminderJobLayoutStrategy.SMS_TO_UNITS;

/**
 * An editor for <em>entity.jobAppointmentReminder</em>.
 *
 * @author Tim Anderson
 * @see AppointmentReminderJobLayoutStrategy
 */
public class AppointmentReminderJobConfigurationEditor extends SingletonJobConfigurationEditor {

    /**
     * Constructs an {@link AppointmentReminderJobConfigurationEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public AppointmentReminderJobConfigurationEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validateInterval(validator);
    }

    /**
     * Verifies that the SMS From interval is greater than the SMS To interval.
     *
     * @param validator the validator
     * @return {@code true} if the interval is valid
     */
    private boolean validateInterval(Validator validator) {
        boolean valid = false;
        int from = getProperty(SMS_FROM).getInt();
        DateUnits fromUnits = DateUnits.valueOf(getProperty(SMS_FROM_UNITS).getString());
        int to = getProperty(SMS_TO).getInt();
        DateUnits toUnits = DateUnits.valueOf(getProperty(SMS_TO_UNITS).getString());
        Date now = new Date();

        // this may yield unexpected results if using different units and one of the units is MONTHS. The likelihood
        // of configuring reminders in this way is small however.
        Date fromDate = DateRules.getDate(now, from, fromUnits);
        Date toDate = DateRules.getDate(now, to, toUnits);
        if (DateRules.compareTo(fromDate, toDate) <= 0) {
            validator.add(this, new ValidatorError(Messages.get("sms.appointment.fromGreaterThanToInterval")));
        } else {
            valid = true;
        }
        return valid;
    }
}
