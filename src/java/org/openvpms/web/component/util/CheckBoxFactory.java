package org.openvpms.web.component.util;

import nextapp.echo2.app.CheckBox;


/**
 * Factory for {@link CheckBox}es.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class CheckBoxFactory extends ComponentFactory {

    /**
     * Create a new check box.
     *
     * @return a new check box
     */
    public static CheckBox create() {
        return new CheckBox();
    }

    /**
     * Create a new check box with localised label.
     *
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new check box
     */
    public static CheckBox create(String key) {
        CheckBox box = create();
        if (key != null) {
            box.setText(getString("label", key, false));
        }
        return box;
    }

    /**
     * Create a new check box with localised label and initial value.
     *
     * @param key   the resource bundle key. May be <code>null</code>
     * @param value the initial value
     * @return a new check box
     */
    public static CheckBox create(String key, boolean value) {
        CheckBox box = create(key);
        box.setSelected(value);
        return box;
    }


}
