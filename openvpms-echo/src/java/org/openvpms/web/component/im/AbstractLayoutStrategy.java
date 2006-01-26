package org.openvpms.web.component.im;

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
import org.openvpms.web.util.DescriptorHelper;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * If <code>true</code> show optional fields, as well as mandatory ones.
     */
    private final boolean _showOptional;

    /**
     * If <code>true</code>, show hidden fields.
     */
    private final boolean _showHidden;


    /**
     * Construct a new <code>AbstractLayoutStrategy</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     * @param showHidden   if <code>true</code> show hidden fields
     */
    public AbstractLayoutStrategy(boolean showOptional, boolean showHidden) {
        _showOptional = showOptional;
        _showHidden = showHidden;
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
        if (!simple.isEmpty()) {
            doSimpleLayout(object, simple, container, factory);
        }
        List<NodeDescriptor> complex
                = filter(descriptor.getComplexNodeDescriptors());
        if (!complex.isEmpty()) {
            doComplexLayout(object, complex, container, factory);
        }
    }

    /**
     * Determines if optional fields should be displayed.
     *
     * @return <code>true</code> if optional fields should be displayed
     */
    public boolean showOptional() {
        return _showOptional;
    }

    /**
     * Determines if hidden fields should be displayed.
     *
     * @return <code>true</code> if hidden fields should be displayed
     */
    public boolean showHidden() {
        return _showHidden;
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
        Grid grid = GridFactory.create(2);
        for (NodeDescriptor nodeDesc : descriptors) {
            Label label = LabelFactory.create();
            label.setText(nodeDesc.getDisplayName());
            Component child = factory.create(object, nodeDesc);
            grid.add(label);
            grid.add(child);
        }
        container.add(grid);
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
            if (show(descriptor)) {
                result.add(descriptor);
            }
        }
        return result;
    }

    /**
     * Determines if a node should be displayed.
     *
     * @param node the node
     * @return <code>true<code> if the node should be displayed
     */
    protected boolean show(NodeDescriptor node) {
        boolean result = false;
        if (node.isHidden()) {
            if (showHidden()) {
                result = true;
            }
        } else if (showOptional()) {
            result = true;
        } else {
            result = node.isRequired();
        }
        return result;
    }
}
