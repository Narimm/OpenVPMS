package org.openvpms.web.component;

import nextapp.echo2.app.Component;

import org.openvpms.web.util.Messages;


/**
 * Factory for {@link Component}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class ComponentFactory {

    /**
     * Helper to set defaults for a component.
     *
     * @param component the component to populate
     */
    public static void setDefaults(Component component) {
        component.setStyleName(Styles.DEFAULT);
    }

    /**
     * Helper to return localised text for a component.
     *
     * @param type the component type
     * @param name the component instance name
     * @return the localised string corresponding to <code>key</code>
     */
    protected static String getString(String type, String name) {
        return Messages.getString(type + "." + name);
    }

    /**
     * Helper to add a set of components to a container.
     *
     * @param container  the container
     * @param components the components to add
     */
    protected static void add(Component container, Component ... components) {
        for (Component component : components) {
            container.add(component);
        }
    }
}
