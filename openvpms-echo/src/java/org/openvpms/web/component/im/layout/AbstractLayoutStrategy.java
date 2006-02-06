package org.openvpms.web.component.im.layout;

import java.util.ArrayList;
import java.util.List;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.GridFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.im.IMObjectComponentFactory;
import org.openvpms.web.component.im.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.NodeFilter;
import org.openvpms.web.util.DescriptorHelper;


/**
 * Abstract implementation of the {@link IMObjectLayoutStrategy} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * The node filter. May be <code>null</code>.
     */
    private NodeFilter _filter;

    /**
     * Construct a new <code>AbstractLayoutStrategy</code>.
     */
    public AbstractLayoutStrategy() {
        this(null);
    }

    /**
     * Construct a new <code>AbstractLayoutStrategy</code>.
     *
     * @param filter the node filter. May be <code>null</code>.
     */
    public AbstractLayoutStrategy(NodeFilter filter) {
        _filter = filter;
    }

    /**
     * Apply the layout strategy.
     *
     * @param object  the object to apply
     * @param factory the component factory
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, IMObjectComponentFactory factory) {
        Column column = new Column();
        doLayout(object, column, factory);
        return column;
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object    the object to lay out
     * @param container the container to use
     * @param factory   the component factory
     */
    protected void doLayout(IMObject object, Component container,
                            IMObjectComponentFactory factory) {
        ArchetypeDescriptor descriptor
                = DescriptorHelper.getArchetypeDescriptor(object);
        List<NodeDescriptor> simple
                = filter(descriptor.getSimpleNodeDescriptors());
        doSimpleLayout(object, simple, container, factory);
        List<NodeDescriptor> complex
                = filter(descriptor.getComplexNodeDescriptors());
        doComplexLayout(object, complex, container, factory);
    }

    /**
     * Lays out child components in a 2x2 grid.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param factory     the component factory
     */
    protected void doSimpleLayout(IMObject object, List<NodeDescriptor> descriptors,
                                  Component container, IMObjectComponentFactory factory) {
        if (!descriptors.isEmpty()) {
            Grid grid = GridFactory.create(2);
            for (NodeDescriptor descriptor : descriptors) {
                Component child = factory.create(object, descriptor);
                add(grid, descriptor.getDisplayName(), child);
            }
            container.add(grid);
        }
    }

    /**
     * Lays out each child component in a tabbed pane.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param factory     the component factory
     */
    protected void doComplexLayout(IMObject object, List<NodeDescriptor> descriptors,
                                   Component container, IMObjectComponentFactory factory) {
        if (!descriptors.isEmpty()) {
            DefaultTabModel model = new DefaultTabModel();
            for (NodeDescriptor nodeDesc : descriptors) {
                Component child = factory.create(object, nodeDesc);
                model.addTab(nodeDesc.getDisplayName(), child);
            }
            TabbedPane pane = new TabbedPane();
            pane.setModel(model);
            pane.setSelectedIndex(0);
            container.add(pane);
        }
    }

    /**
     * Filters a list of descriptors returning only those that must be
     * displayed.
     *
     * @param descriptors the descriptors to filter
     * @return a list of descriptors returning only those that must be
     *         displayed
     */
    protected List<NodeDescriptor> filter(List<NodeDescriptor> descriptors) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        for (NodeDescriptor descriptor : descriptors) {
            if (_filter != null && _filter.include(descriptor)) {
                result.add(descriptor);
            }
        }
        return result;
    }

    /**
     * Sets the node filter.
     *
     * @param filter the node filter
     */
    protected void setNodeFilter(NodeFilter filter) {
        _filter = filter;
    }

    /**
     * Returns the node filter.
     *
     * @return the node filter
     */
    protected NodeFilter getNodeFilter() {
        return _filter;
    }

    /**
     * Helper to add a node to a grid
     *
     * @param grid      the grid
     * @param name      the node display name
     * @param component the component representing the node
     */
    protected void add(Grid grid, String name, Component component) {
        Label label = LabelFactory.create();
        label.setText(name);
        grid.add(label);
        grid.add(component);
    }

}
