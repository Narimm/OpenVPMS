package org.openvpms.web.component.im;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import echopointng.GroupBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.web.component.GridFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.util.DescriptorHelper;


/**
 * {@link IMObjectLayoutStrategy} that lays out {@link IMObject} instances on a
 * single page.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class SinglePageLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * Determines if a summary of the object should be displayed.
     */
    private final boolean _summary;


    /**
     * Construct a new <code>IMObjectBrowser</code>.
     *
     * @param summary if <code>true</code>, only display a summary
     */
    public SinglePageLayoutStrategy(boolean summary) {
        _summary = summary;
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
     * @param object the object to lay out
     */
    protected void doLayout(IMObject object, Component container,
                            IMObjectComponentFactory factory) {
        ArchetypeDescriptor descriptor = DescriptorHelper.getArchetypeDescriptor(object);
        doSimpleLayout(object, descriptor.getSimpleNodeDescriptors(), container, factory);
        doComplexLayout(object, descriptor.getComplexNodeDescriptors(), container, factory);
    }

    /**
     * Lays out child components in a 2x2 boxed grid.
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
        Grid grid = GridFactory.create(2);
        for (NodeDescriptor node : descriptors) {
            if (show(node)) {
                Label label = LabelFactory.create();
                label.setText(node.getDisplayName());
                Component browser = factory.create(object, node);
                grid.add(label);
                grid.add(browser);
            }
        }
        container.add(grid);
    }

    /**
     * Lays out each child component in a group box.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param factory     the component factory
     */
    protected void doComplexLayout(IMObject object, List<NodeDescriptor> descriptors,
                                   Component container, IMObjectComponentFactory factory) {
        for (NodeDescriptor node : descriptors) {
            if (show(node)) {
                Collection values = null;
                GroupBox box = new GroupBox(node.getDisplayName());
                Object collection = node.getValue(object);
                if (collection instanceof Collection) {
                    values = (Collection) collection;
                } else if (collection instanceof Map) {
                    values = ((Map) collection).values();
                } else if (collection instanceof PropertyList) {
                    values = ((PropertyList) collection).values();
                }
                for (Object value : values) {
                    doLayout((IMObject) value, box, factory);
                }
                container.add(box);
            }
        }
    }

    /**
     * Determines if a node should be displayed.
     *
     * @param node the node
     * @return <code>true<code> if the node should be displayed
     */
    private boolean show(NodeDescriptor node) {
        boolean show = false;
        if (!node.isHidden()) {
            show = (!_summary) || node.isRequired();
        }
        return show;
    }


}
