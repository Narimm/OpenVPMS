package org.openvpms.web.component.bound;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.jxpath.Pointer;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
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
        setSelected(value);

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pointer.setValue(isSelected());
            }
        });
    }

}
