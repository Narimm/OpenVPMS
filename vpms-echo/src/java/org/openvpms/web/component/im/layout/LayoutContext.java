package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Component;

import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.TabIndexer;


/**
 * Layout context.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface LayoutContext {

    /**
     * Determines if this is an edit context.
     *
     * @return <code>true</code> if this is an edit context; <code>false</code>
     *         if it is a view context. Defaults to <code>false</code>
     */
    boolean isEdit();

    /**
     * Sets if this is an edit context.
     *
     * @param edit if <code>true</code> this is an edit context; if
     *             <code>false</code> it is a view context.
     */
    void setEdit(boolean edit);

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
