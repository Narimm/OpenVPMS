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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Editor for <em>entity.onlineBookingTimesType</em>.
 *
 * @author Tim Anderson
 */
public class OnlineBookingTimesEditor extends AbstractIMObjectEditor {

    /**
     * Constructs an {@link OnlineBookingTimesEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public OnlineBookingTimesEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
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
        return super.doValidation(validator) && validateTimes(validator);
    }

    /**
     * Validates the start and end times.
     *
     * @param validator the validator
     * @return {@code true} if the times are valid
     */
    private boolean validateTimes(Validator validator) {
        boolean valid = true;
        for (String prefix : OnlineBookingTimesLayoutStrategy.DAY_PREFIXES) {
            Property open = getProperty(prefix + "Open");
            if (open.getBoolean()) {
                Property start = getProperty(prefix + "StartTime");
                Property end = getProperty(prefix + "EndTime");
                Date startTime = start.getDate();
                Date endTime = end.getDate();
                if (startTime == null) {
                    valid = reportRequired(start, validator);
                } else if (endTime == null) {
                    valid = reportRequired(end, validator);
                } else if (startTime.compareTo(endTime) >= 0) {
                    String message = Messages.format("workflow.scheduling.onlinebooking.invalidtimes",
                                                     start.getDisplayName(), end.getDisplayName());
                    validator.add(this, new ValidatorError(message));
                    valid = false;
                }
            }
            if (!valid) {
                break;
            }
        }
        return valid;
    }

}