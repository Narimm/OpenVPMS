package org.openvpms.web.component;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;


/**
 * Factory for {@link SplitPane}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public final class SplitPaneFactory extends ComponentFactory {

    /**
     * Create a new split pane.
     *
     * @param orientation the orientation
     * @return a new split pane
     */
    public static SplitPane create(int orientation) {
        SplitPane pane = new SplitPane(orientation);
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

}
