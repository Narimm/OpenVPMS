package org.openvpms.web.component.bound;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;

import echopointng.DateField;

import org.openvpms.web.component.edit.Property;

import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.ActionEvent;


/**
 * Binds a {@link Property} to a <code>DateField</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BoundDateField extends DateField {

    /**
     * The bound property.
     */
    private final Binder _binder;

    /**
     * Date change listener.
     */
    private final PropertyChangeListener _listener;


    /**
     * Construct a new <code>BoundDateField</code>.
     *
     * @param property the property to bind
     */
    public BoundDateField(Property property) {
        _listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                _binder.setProperty();
            }
        };

        // Register an action listener to ensure document update events
        // are triggered in a timely fashion
        getTextField().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // no-op.
            }
        });

        _binder = new Binder(property) {
            protected Object getFieldValue() {
                return getDateChooser().getSelectedDate().getTime();
            }

            protected void setFieldValue(Object value) {
                getDateChooser().removePropertyChangeListener(_listener);
                Date date = (Date) value;
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    getDateChooser().setSelectedDate(calendar);
                }
                getDateChooser().addPropertyChangeListener(_listener);
            }
        };
        _binder.setField();

    }
}
