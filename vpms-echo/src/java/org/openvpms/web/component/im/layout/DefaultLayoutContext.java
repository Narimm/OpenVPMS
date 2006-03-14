package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Component;

import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;

/**
 * Default implmentation of the {@link LayoutContext} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultLayoutContext implements LayoutContext {

    /**
     * The component factory.
     */
    private IMObjectComponentFactory _factory;

    /**
     * The default node filter.
     */
    private NodeFilter _filter;

    /**
     * The tab indexer.
     */
    private TabIndexer _indexer;

    
    /**
     * Construct a new <code>DefaultLayoutContext</code>.
     */
    public DefaultLayoutContext() {
        this((IMObjectComponentFactory) null);
    }

    /**
     * Construct a new <code>DefaultLayoutContext</code>.
     *
     * @param factory the component factory. May  be <code>null</code>
     */
    public DefaultLayoutContext(IMObjectComponentFactory factory) {
        _factory = factory;
        _filter = new BasicNodeFilter(true);
        _indexer = new TabIndexer();
    }

    /**
     * Construct a new  <code>DefaultLayoutContext</code> from an existing
     * layout context.
     *
     * @param context the context
     */
    public DefaultLayoutContext(LayoutContext context) {
        _factory = context.getComponentFactory();
        _filter = context.getDefaultNodeFilter();
        _indexer = context.getTabIndexer();
    }

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    public IMObjectComponentFactory getComponentFactory() {
        return _factory;
    }

    /**
     * Sets the component factory.
     *
     * @param factory the component factory
     */
    public void setComponentFactory(IMObjectComponentFactory factory) {
        _factory = factory;
    }

    /**
     * Returns the tab indexer.
     *
     * @return the tab indexer
     */
    public TabIndexer getTabIndexer() {
        return _indexer;
    }

    /**
     * Sets the tab index of a component.
     *
     * @param component the component
     */
    public void setTabIndex(Component component) {
        _indexer.setTabIndex(component);
    }

    /**
     * Returns the default filter.
     *
     * @return the default filter. May be <code>null</code>
     */
    public NodeFilter getDefaultNodeFilter() {
        return _filter;
    }

    /**
     * Sets the default filter.
     *
     * @param filter the default filter. May be <code>null</code>
     */
    public void setNodeFilter(NodeFilter filter) {
        _filter = filter;
    }
}
