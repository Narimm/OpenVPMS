package org.openvpms.web.component.im.view.act;

import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.GridFactory;


/**
 * Act layout strategy. Hides the items and participants nodes.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The act items.
     */
    private final Component _items;


    /**
     * Construct a new <code>ActLayoutStrategy</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     */
    public ActLayoutStrategy(boolean showOptional) {
        this(null, showOptional);
    }

    /**
     * Construct a new <code>ActLayoutStrategy</code>.
     *
     * @param items        the component representing the act items. May be
     *                     <code>null</code>.
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     */
    public ActLayoutStrategy(Component items, boolean showOptional) {
        ChainedNodeFilter filter = new ChainedNodeFilter();
        filter.add(new BasicNodeFilter(showOptional, false));
        filter.add(new NamedNodeFilter("items", "participants"));
        setNodeFilter(filter);

        _items = items;
    }

    /**
     * Lays out child components in a 2x2 grid.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param factory     the component factory
     */
    @Override
    protected void doSimpleLayout(IMObject object,
                                  List<NodeDescriptor> descriptors,
                                  Component container,
                                  IMObjectComponentFactory factory) {
        Grid grid = GridFactory.create(4);
        for (NodeDescriptor descriptor : descriptors) {
            Component child = factory.create(object, descriptor);
            String name = descriptor.getName();
            if (name.equals("lowTotal") || name.equals("highTotal")
                    || name.equals("total")) {
                // @todo - workaround for OVPMS-211
                child.setEnabled(false);
            }
            add(grid, descriptor.getDisplayName(), child);
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
    @Override
    protected void doComplexLayout(IMObject object,
                                   List<NodeDescriptor> descriptors,
                                   Component container,
                                   IMObjectComponentFactory factory) {
        Grid grid = GridFactory.create(4);
        for (NodeDescriptor descriptor : descriptors) {
            Component component = factory.create(object, descriptor);
            add(grid, descriptor.getDisplayName(), component);
        }

        container.add(grid);

        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(object);
        NodeDescriptor items = archetype.getNodeDescriptor("items");
        GroupBox box = new GroupBox();
        box.setTitle(items.getDisplayName());

        if (_items != null) {
            box.add(_items);
        } else {
            IMObjectLayoutStrategy strategy
                    = new ActRelationshipTableLayoutStrategy(items);
            Component child = strategy.apply(object, factory);
            box.add(child);
        }
        container.add(box);
    }
}
