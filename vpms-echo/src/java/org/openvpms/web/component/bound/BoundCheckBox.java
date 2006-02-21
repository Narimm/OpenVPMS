package org.openvpms.web.component.bound;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.jxpath.Pointer;


/**
 * Binds a <code>Pointer</code> to a <code>CheckBox</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BoundCheckBox extends CheckBox {

    /**
     * The bound field.
     */
    private final Pointer _pointer;


    /**
     * Construct a new <code>BoundCheckBox</code>.
     *
     * @param pointer the field to bind
     */
    public BoundCheckBox(Pointer pointer) {
        _pointer = pointer;

        Boolean value = (Boolean) _pointer.getValue();
        if (value != null) {
            setSelected(value);
        }

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pointer.setValue(isSelected());
            }
        });
    }

}
