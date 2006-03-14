package org.openvpms.web.component.im.layout;

import echopointng.DateField;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;

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
     * The tab index.
     */
    private int _tabIndex;

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
        _tabIndex = context.getTabIndex();
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
     * Returns the tab index.
     *
     * @return the tab index
     */
    public int getTabIndex() {
        return _tabIndex;
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
