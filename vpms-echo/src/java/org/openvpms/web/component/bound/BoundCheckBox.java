package org.openvpms.web.component.bound;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.component.edit.Property;


/**
 * Binds a {@link Property} to a <code>CheckBox</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BoundCheckBox extends CheckBox {

    /**
     * The property binder.
     */
    private final Binder _binder;

    /**
     * Checkbox listener.
     */
    private final ActionListener _listener;


    /**
     * Construct a new <code>BoundCheckBox</code>.
     *
     * @param property the property to bind
     */
    public BoundCheckBox(Property property) {
        _listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _binder.setProperty();
            }
        };

        _binder = new Binder(property) {
            protected Object getFieldValue() {
                return isSelected();
            }

            protected void setFieldValue(Object value) {
                if (value != null) {
                    removeActionListener(_listener);
                    setSelected((Boolean) value);
                    addActionListener(_listener);
                }
            }
        };
        _binder.setField();

    }

}
