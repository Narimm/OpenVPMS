package org.openvpms.web.component.util;

import echopointng.DateField;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;


/**
 * Tab indexer.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class TabIndexer {

    /**
     * The tab index.
     */
    private int _tabIndex;

    /**
     * Construct a new  <code>TabIndexer</code>.
     */
    public TabIndexer() {
    }

    /**
     * Sets the tab index of a component.
     *
     * @param component the component
     */
    public void setTabIndex(Component component) {
        ++_tabIndex;
        component.setFocusTraversalIndex(_tabIndex);
        if (component instanceof DateField) {
            // @todo workaround for dates.
            TextField text = ((DateField) component).getTextField();
            text.setFocusTraversalIndex(_tabIndex);
        }
    }

}
