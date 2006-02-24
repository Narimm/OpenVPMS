package org.openvpms.web.component.util;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;


/**
 * Factory for {@link GroupBox}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class GroupBoxFactory extends ComponentFactory {

    /**
     * Create a new group box.
     *
     * @return a new group box
     */
    public static GroupBox create() {
        return new GroupBox();
    }

    /**
     * Create a new group box with localised title.
     *
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new group box
     */
    public static GroupBox create(String key) {
        GroupBox box = create();
        if (key != null) {
            box.setTitle(getString("label", key, false));
        }
        return box;
    }

    /**
     * Create a new group box, containing a set of components.
     *
     * @param components the components to add
     * @return a new group box
     */
    public static GroupBox create(Component ... components) {
        GroupBox box = create();
        add(box, components);
        return box;
    }

    /**
     * Create a new group box with a localised title, and containing a set of
     * components.
     *
     * @param key        the resource bundle key. May be <code>null</code>
     * @param components the components to add
     * @return a new group box
     */
    public static GroupBox create(String key, Component ... components) {
        GroupBox box = create(key);
        add(box, components);
        return box;
    }

}
