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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.property;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * Handler for time nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 04:38:04Z $
 */
public class TimePropertyTransformer extends AbstractPropertyTransformer {

    /**
     * The date component of the time. May be <tt>null</tt>.
     */
    private Date date;


    /**
     * Construct a new <tt>TimePropertyTransformer</tt>.
     *
     * @param property the property
     */
    public TimePropertyTransformer(Property property) {
        super(property);
    }

    /**
     * Sets the date part of the time.
     *
     * @param date the date. May be <tt>null</tt>
     */
    public void setDate(Date date) {
        this.date = (date != null) ? DateRules.getDate(date) : null;
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <tt>object</tt> if no
     *         transformation is required
     * @throws PropertyException if the object is invalid
     */
    public Object apply(Object object) throws PropertyException {
        Object result;
        try {
            if (object instanceof String) {
                String value = (String) object;
                if (StringUtils.isEmpty(value)) {
                    Property property = getProperty();
                    if (!property.isRequired()) {
                        result = null;
                    } else {
                        String msg = Messages.get("property.error.required", property.getDisplayName());
                        throw new PropertyException(property, msg);
                    }
                } else {
                    Date time = DateHelper.parseTime((String) object);
                    if (date != null) {
                        result = DateHelper.addDateTime(date, time);
                    } else {
                        result = time;
                    }
                }
            } else if (object instanceof Date) {
                if (date != null) {
                    result = DateHelper.addDateTime(date, (Date) object);
                } else {
                    result = object;
                }
            } else if (object == null) {
                result = null;
            } else {
                throw getException(null);
            }
        } catch (ValidationException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw getException(exception);
        }

        return result;
    }

    /**
     * Helper to create a new property exception.
     *
     * @param cause the cause. May be <tt>null</tt>
     * @return a new property exception
     */
    private PropertyException getException(Throwable cause) {
        String message = Messages.get("property.error.invalidtime",
                                      getProperty().getDisplayName());
        return new PropertyException(getProperty(), message, cause);
    }

}
