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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.Row;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.TimePropertyTransformer;
import org.openvpms.web.component.util.TimeFieldFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Bound date/time field.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundDateTimeField extends Row {

    /**
     * The date field.
     */
    private final BoundDateField date;

    /**
     * The time field.
     */
    private final BoundTimeField time;


    /**
     * Constructs a new <tt>BoundDateTimeField</tt>.
     *
     * @param property the property to bind
     */
    public BoundDateTimeField(Property property) {
        setStyleName("CellSpacing");
        TimePropertyTransformer transformer
                = new TimePropertyTransformer(property);
        Date current = (Date) property.getValue();
        if (current != null) {
            transformer.setDate(current);
        }
        property.setTransformer(transformer);

        date = new DateField(property, transformer);
        time = TimeFieldFactory.create(property);
        add(date);
        add(time);
    }

    /**
     * Returns the date field,
     *
     * @return the date field
     */
    public BoundDateField getDate() {
        return date;
    }

    /**
     * Returns the time field.
     *
     * @return the time field
     */
    public BoundTimeField getTime() {
        return time;
    }

    private static class DateField extends BoundDateField {

        private final TimePropertyTransformer transformer;

        public DateField(Property property,
                         TimePropertyTransformer transformer) {
            super(property);
            this.transformer = transformer;
        }

        @Override
        protected DateBinder createBinder(Property property) {
            return new DateBinder(this, property) {

                /**
                 * Updates the property from the field.
                 *
                 * @param property the propery to update
                 */
                @Override
                protected void setProperty(Property property) {
                    Object fieldValue = getFieldValue();
                    Date date = (Date) fieldValue;
                    Date currentDate = (Date) property.getValue();
                    if (currentDate != null) {
                        Calendar current = new GregorianCalendar();
                        current.setTime(currentDate);

                        Calendar calendar = new GregorianCalendar();
                        calendar.setTime(date);
                        calendar.set(Calendar.HOUR_OF_DAY,
                                     current.get(Calendar.HOUR_OF_DAY));
                        calendar.set(Calendar.MINUTE,
                                     current.get(Calendar.MINUTE));
                        calendar.set(Calendar.SECOND,
                                     current.get(Calendar.SECOND));
                        date = calendar.getTime();
                    }
                    if (property.setValue(date)) {
                        transformer.setDate(date);
                        Object propertyValue = property.getValue();
                        if (!ObjectUtils.equals(date, propertyValue)) {
                            setField();
                        }
                    }
                }
            };
        }
    }

}
