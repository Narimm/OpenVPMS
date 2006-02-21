package org.openvpms.web.component.util;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;

import org.openvpms.web.component.util.ComponentFactory;


/**
 * Factory for {@link SplitPane}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public final class SplitPaneFactory extends ComponentFactory {

    /**
     * Create a new split pane.
     *
     * @param orientation the orientation
     * @return a new split pane
     */
    public static SplitPane create(int orientation) {
        return new SplitPane(orientation);
    }

    /**
     * Create a split pane, with a specific style.
     *
     * @param style the style name
     * @return a new split pane
     */
    public static SplitPane create(String style) {
        SplitPane pane = new SplitPane();
        pane.setStyleName(style);
        return pane;
    }

    /**
     * Create a split pane containing a set of components.
     *
     * @param orientation the orientation
     * @param components  the components to add
     */
    public static SplitPane create(int orientation, Component ... components) {
        SplitPane pane = create(orientation);
        add(pane, components);
        return pane;
    }

    /**
     * Create a split pane, with a specific style and containing a set of
     * components
     *
     * @param orientation the orientation
     * @param style       the style name
     * @param components  the components to add
     * @return a new split pane
     */
    public static SplitPane create(int orientation, String style, Component ... components) {
        SplitPane pane = create(orientation);
        pane.setStyleName(style);
        add(pane, components);
        return pane;
    }

}
