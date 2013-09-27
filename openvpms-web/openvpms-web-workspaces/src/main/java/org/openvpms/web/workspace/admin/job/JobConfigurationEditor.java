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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.job;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.regex.Pattern;

/**
 * Editor for <em>entity.job*</em> archetypes.
 *
 * @author Tim Anderson
 */
public class JobConfigurationEditor extends AbstractIMObjectEditor {

    /**
     * Constructs an {@link JobConfigurationEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public JobConfigurationEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
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
        boolean valid = super.doValidation(validator);
        if (valid) {
            valid = validateMinutes(validator) && validateHours(validator) && validateDayOfMonth(validator)
                    && validateMonth(validator) && validateDayOfWeek(validator);
            if (valid) {
                // try and parse the expression
                Property property = getProperty("expression");
                String expression = property.getString();
                try {
                    new CronExpression(expression);
                } catch (ParseException exception) {
                    valid = false;
                    validator.add(property, new ValidatorError(property, exception.getMessage()));
                }
            }
        }
        return valid;
    }

    /**
     * Validates the minutes property.
     *
     * @param validator the validator
     * @return {@code true} if the property is valid
     */
    private boolean validateMinutes(Validator validator) {
        return validateProperty("minutes", CronHelper.MINUTES, validator);
    }

    /**
     * Validates the hours property.
     *
     * @param validator the validator
     * @return {@code true} if the property is valid
     */
    private boolean validateHours(Validator validator) {
        return validateProperty("hours", CronHelper.HOURS, validator);
    }

    /**
     * Validates the dayOfMonth property.
     *
     * @param validator the validator
     * @return {@code true} if the property is valid
     */
    private boolean validateDayOfMonth(Validator validator) {
        return validateProperty("dayOfMonth", CronHelper.DAY_OF_MONTH, validator);
    }

    /**
     * Validates the month property.
     *
     * @param validator the validator
     * @return {@code true} if the property is valid
     */
    private boolean validateMonth(Validator validator) {
        return validateProperty("month", CronHelper.MONTH, validator);
    }

    /**
     * Validates the dayOfWeek property.
     *
     * @param validator the validator
     * @return {@code true} if the property is valid
     */
    private boolean validateDayOfWeek(Validator validator) {
        return validateProperty("dayOfWeek", CronHelper.DAY_OF_WEEK, validator);
    }

    /**
     * Validates a property.
     *
     * @param name      the property name
     * @param pattern   the pattern to validate against
     * @param validator the validator
     * @return {@code true} if the property is valid
     */
    private boolean validateProperty(String name, Pattern pattern, Validator validator) {
        Property property = getProperty(name);
        String value = property.getString();
        if (value == null) {
            value = "";
        }
        boolean valid = pattern.matcher(value).matches();
        if (!valid) {
            validator.add(property, new ValidatorError(property, Messages.format("job.property.invalid", value)));
        }
        return valid;
    }

}
