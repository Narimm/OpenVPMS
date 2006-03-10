package org.openvpms.web.component.im.layout;

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
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;


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
     * The tab index.
     */
    private int _tabIndex;


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
        Column column = ColumnFactory.create("CellSpacingColumn");
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
        List<NodeDescriptor> simple;
        List<NodeDescriptor> complex;

        simple = FilterHelper.filter(_filter,
                                     descriptor.getSimpleNodeDescriptors());
        complex = FilterHelper.filter(_filter,
                                      descriptor.getComplexNodeDescriptors());

        doSimpleLayout(object, simple, container, factory);
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
    protected void doSimpleLayout(IMObject object,
                                  List<NodeDescriptor> descriptors,
                                  Component container,
                                  IMObjectComponentFactory factory) {
        if (!descriptors.isEmpty()) {
            Grid grid = GridFactory.create(4);
            doGridLayout(object, descriptors, grid, factory);
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
    protected void doComplexLayout(IMObject object,
                                   List<NodeDescriptor> descriptors,
                                   Component container,
                                   IMObjectComponentFactory factory) {
        if (!descriptors.isEmpty()) {
            DefaultTabModel model = new DefaultTabModel();
            for (NodeDescriptor nodeDesc : descriptors) {
                Component child = factory.create(object, nodeDesc);
                setTabIndex(child);
                model.addTab(nodeDesc.getDisplayName(), child);
            }
            TabbedPane pane = new TabbedPane();
            pane.setModel(model);
            pane.setSelectedIndex(0);
            container.add(pane);
        }
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
     * Lays out child components in a 2x2 grid.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param grid        the grid to use
     * @param factory     the component factory
     */
    protected void doGridLayout(IMObject object,
                                List<NodeDescriptor> descriptors, Grid grid,
                                IMObjectComponentFactory factory) {
        int size = descriptors.size();
        int rows = (size / 2) + (size % 2);
        for (int i = 0; i < rows; ++i) {
            NodeDescriptor leftNode = descriptors.get(i);
            Component left = factory.create(object, leftNode);
            add(grid, leftNode.getDisplayName(), left);

            if ((rows + i) < size) {
                NodeDescriptor rightNode = descriptors.get(rows + i);
                Component right = factory.create(object, rightNode);
                add(grid, rightNode.getDisplayName(), right);
            }
        }
    }

    /**
     * Helper to add a node to a grid, setting its tab index if required.
     *
     * @param grid      the grid
     * @param name      the node display name
     * @param component the component representing the node
     */
    protected void add(Grid grid, String name, Component component) {
        Label label = LabelFactory.create();
        label.setText(name);
        setTabIndex(component);
        grid.add(label);
        grid.add(component);
    }

    /**
     * Sets the tab index for a componen, if it is a focus traversal
     * participant.
     *
     * @param component the component
     */
    protected void setTabIndex(Component component) {
        if (component.isFocusTraversalParticipant()) {
            // @todo -- need to determine tab index for the entire screen...
            // component.setFocusTraversalIndex(++_tabIndex);
        }
    }

}
