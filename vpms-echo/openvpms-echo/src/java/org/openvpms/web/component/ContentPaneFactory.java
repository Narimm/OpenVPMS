package org.openvpms.web.component;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;


/**
 * Factory for {@link ContentPane}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
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
     * Create a new content pane, with a specific style and child component.
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
