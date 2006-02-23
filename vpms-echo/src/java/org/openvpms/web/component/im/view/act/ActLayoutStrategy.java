package org.openvpms.web.component.im.view.act;

import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Row;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Act layout strategy. Hides the items and participants nodes.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActLayoutStrategy extends ExpandableLayoutStrategy {

    /**
     * The act items.
     */
    private final Component _items;


    /**
     * Construct a new <code>ActLayoutStrategy</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     * @param toggleLayout if <code>true</code> include a button to enable the
     *                     layout to be switched
     */
    public ActLayoutStrategy(boolean showOptional, boolean toggleLayout) {
        this(null, showOptional, toggleLayout);
    }

    /**
     * Construct a new <code>ActLayoutStrategy</code>.
     *
     * @param items        the component representing the act items. May be
     *                     <code>null</code>.
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     * @param toggleLayout if <code>true</code> include a button to enable the
     *                     layout to be switched
     */
    public ActLayoutStrategy(Component items, boolean showOptional,
                             boolean toggleLayout) {
        super(showOptional, toggleLayout);
        ChainedNodeFilter filter = new ChainedNodeFilter();
        filter.add(new BasicNodeFilter(showOptional, false));
        filter.add(new NamedNodeFilter("items", "participants"));
        setNodeFilter(filter);

        _items = items;
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
        Grid grid = GridFactory.create(2);
        for (NodeDescriptor descriptor : descriptors) {
            Component component = factory.create(object, descriptor);
            add(grid, descriptor.getDisplayName(), component);
        }

        if (getButton() == null && showButton()) {
            Row group = RowFactory.create(grid, getButtonRow());
            container.add(group);
        } else {
            container.add(grid);
        }

        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(object);
        NodeDescriptor items = archetype.getNodeDescriptor("items");
        GroupBox box = new GroupBox();
        box.setTitle(items.getDisplayName());

        if (_items != null) {
            box.add(_items);
        } else {
            IMObjectLayoutStrategy strategy = new ActRelationshipTableLayoutStrategy();
            Component child = strategy.apply(object, factory);
            box.add(child);
        }

        if (getButton() == null && showButton()) {
            Row group = RowFactory.create(box, getButtonRow());
            container.add(group);
        } else {
            container.add(box);
        }
    }
}
