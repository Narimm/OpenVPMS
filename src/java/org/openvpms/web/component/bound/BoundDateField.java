package org.openvpms.web.component.bound;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;

import echopointng.DateField;
import org.apache.commons.jxpath.Pointer;


/**
 * Binds a <code>Pointer</code> to a <code>DateField</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BoundDateField extends DateField {

    /**
     * The bound field.
     */
    private final Pointer _pointer;


    /**
     * Construct a new <code>BoundDateField</code>.
     *
     * @param pointer the field to bind
     */
    public BoundDateField(Pointer pointer) {
        _pointer = pointer;

        Date value = (Date) pointer.getValue();
        if (value != null) {
            Calendar date = Calendar.getInstance();
            date.setTime(value);
            getDateChooser().setSelectedDate(date);
        } else {
            _pointer.setValue(getDateChooser().getSelectedDate().getTime());
        }

        getDateChooser().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                _pointer.setValue(getDateChooser().getSelectedDate().getTime());
            }
        });

    }
}
