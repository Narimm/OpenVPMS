package org.openvpms.web.component.util;

import nextapp.echo2.app.Component;

import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.resource.util.Styles;


/**
 * Factory for {@link Component}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
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
     * @param type      the component type
     * @param name      the component instance name
     * @param allowNull if <code>true</code> return <code>null</code> if there
     *                  is no text for the give <code>type</code> and
     *                  <code>name</code>
     * @return the localised string corresponding to <code>key</code>
     */
    protected static String getString(String type, String name, boolean allowNull) {
        return Messages.get(type + "." + name, allowNull);
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
