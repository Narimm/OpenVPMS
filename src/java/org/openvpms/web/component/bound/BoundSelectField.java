package org.openvpms.web.component.bound;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.lang.ObjectUtils;

import org.openvpms.web.component.edit.Property;


/**
 * Binds a {@link Property} to a <code>SelectField</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BoundSelectField extends SelectField {

    /**
     * The bound property.
     */
    private final Property _property;


    /**
     * Construct a new <code>BoundSelectField</code>.
     *
     * @param property the property to bind
     * @param model    the list model
     */
    public BoundSelectField(Property property, ListModel model) {
        super(model);
        _property = property;

        Object value = property.getValue();
        int index = setSelected(value);
        if (index == -1 && model.size() != 0) {
            // current value not in the list, so default it to the first
            // list value.
            setSelectedIndex(0);
            update();
        }

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                update();
            }
        });
    }

    /**
     * Updates the bound object from the list.
     */
    private void update() {
        _property.setValue(getSelectedItem());
    }

    /**
     * Sets the selected object based on the supplied value.
     *
     * @param value the value
     * @return the selected index, or <code>-1</code> if the value wasn't found
     *         in the list
     */
    private int setSelected(Object value) {
        int result = -1;
        ListModel model = getModel();
        for (int i = 0; i < model.size(); ++i) {
            if (ObjectUtils.equals(model.get(i), value)) {
                setSelectedIndex(i);
                result = i;
                break;
            }
        }
        return result;
    }

}
