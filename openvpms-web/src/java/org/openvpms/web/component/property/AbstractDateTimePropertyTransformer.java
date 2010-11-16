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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.property;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.resource.util.Messages;

import java.text.ParseException;
import java.util.Date;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractDateTimePropertyTransformer extends AbstractPropertyTransformer {

    /**
     * Constructs a <tt>AbstractDateTimePropertyTransformer</tt>.
     *
     * @param property the property
     */
    public AbstractDateTimePropertyTransformer(Property property) {
        super(property);
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <tt>object</tt> if no
     *         transformation is required
     * @throws org.openvpms.web.component.property.PropertyException
     *          if the object is invalid
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
                    result = getDateTime(value);
                }
            } else if (object instanceof Date) {
                result = getDateTime((Date) object);
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
     * Adds the supplied time to the date.
     *
     * @param value the time string
     * @return the date/time
     * @throws ParseException if the value can't be parsed as a time
     */
    protected Date getDateTime(String value) throws ParseException {
        Date result;
        Date time = DateHelper.parseTime(value);
        Date date = getDate();
        if (date != null) {
            result = DateHelper.addDateTime(date, time);
        } else {
            result = time;
        }
        return result;
    }

    /**
     * Returns the date.
     *
     * @return the date, or <tt>null</tt> if there is no date
     */
    protected abstract Date getDate();

    /**
     * Returns the supplied value as a date/time.
     *
     * @param value a date, time, or date/time
     * @return the date/time
     */
    protected abstract Date getDateTime(Date value);

    /**
     * Helper to create a new property exception.
     *
     * @param cause the cause. May be <tt>null</tt>
     * @return a new property exception
     */
    protected PropertyException getException(Throwable cause) {
        String message = Messages.get("property.error.invalidtime", getProperty().getDisplayName());
        return new PropertyException(getProperty(), message, cause);
    }
}
