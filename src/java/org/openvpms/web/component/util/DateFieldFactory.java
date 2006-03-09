package org.openvpms.web.component.util;

import echopointng.DateField;

import org.openvpms.web.component.bound.BoundDateField;
import org.openvpms.web.component.edit.Property;


/**
 * Factory for <code>DateFields</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DateFieldFactory extends ComponentFactory {

    /**
     * Creates a new date field with the default style.
     *
     * @return a new date field
     */
    public static DateField create() {
        DateField date = new DateField();
        setDefaults(date.getDateChooser());
        return date;
    }

    /**
     * Creates a new bound date field with the default style.
     *
     * @param property the property to bind
     * @return a new bound date field
     */
    public static DateField create(Property property) {
        DateField date = new BoundDateField(property);
        setDefaults(date.getDateChooser());
        return date;
    }

}

