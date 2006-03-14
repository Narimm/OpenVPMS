package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Component;

import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.filter.NodeFilter;


/**
 * Layout context.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface LayoutContext {

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    IMObjectComponentFactory getComponentFactory();

    /**
     * Sets the component factory.
     *
     * @param factory the component factory
     */
    void setComponentFactory(IMObjectComponentFactory factory);

    /**
     * Returns the tab indexer.
     *
     * @return the tab indexer
     */
    TabIndexer getTabIndexer();

    /**
     * Sets the tab index of a component.
     *
     * @param component the component
     */
    void setTabIndex(Component component);

    /**
     * Returns the default filter.
     *
     * @return the default filter. May be <code>null</code>
     */
    NodeFilter getDefaultNodeFilter();

    /**
     * Sets the default filter.
     *
     * @param filter the default filter. May be <code>null</code>
     */
    void setNodeFilter(NodeFilter filter);

}
