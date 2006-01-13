package org.openvpms.web.component;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;


/**
 * Factory for {@link ContentPane}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class ContentPaneFactory extends ComponentFactory {

    /**
     * Create a new content pane, with a specific style.
     *
     * @param style the style to use
     * @return a new content pane.
     */
    public static ContentPane create(String style) {
        ContentPane pane = new ContentPane();
        pane.setStyleName(style);
        return pane;
    }

    /**
     * Create a new content pane, with a specific style and child component
     *
     * @param style the style to use
     * @param child the child component
     * @return a new content pane.
     */
    public static ContentPane create(String style, Component child) {
        ContentPane pane = create(style);
        pane.add(child);
        return pane;
    }

}
