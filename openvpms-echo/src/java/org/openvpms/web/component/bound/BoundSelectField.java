package org.openvpms.web.component.bound;

import org.apache.commons.jxpath.Pointer;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class BoundSelectField extends SelectField {

    /**
     * The bound field.
     */
    private final Pointer _pointer;


    /**
     * Construct a new <code>BoundSelectField</code>.
     *
     * @param pointer the field to bind
     */
    public BoundSelectField(Pointer pointer, ListModel model) {
        super(model);
        
        _pointer = pointer;
        Object value = pointer.getValue();
        for (int i = 0; i < model.size(); ++i) {
            if (model.get(i).equals(value)) {
                setSelectedIndex(i);
                break;
            }
        }
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pointer.setValue(getSelectedItem());
            }
        });
    }

}
