package org.openvpms.web.component.im.view.act;

import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.CollectionEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.util.GridFactory;


/**
 * Act layout strategy. Hides the items and participants nodes.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The act item editor. May be <code>null</code>.
     */
    private final CollectionEditor _editor;


    /**
     * Construct a new <code>ActLayoutStrategy</code>.
     */
    public ActLayoutStrategy() {
        this(null);
    }

    /**
     * Construct a new <code>ActLayoutStrategy</code>.
     *
     * @param editor the act items editor. May be <code>null</code>.
     */
    public ActLayoutStrategy(CollectionEditor editor) {
        _editor = editor;
    }

    /**
     * Lays out each child component in a group box.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param context
     */
    @Override
    protected void doComplexLayout(IMObject object,
                                   List<NodeDescriptor> descriptors,
                                   Component container,
                                   LayoutContext context) {
        Grid grid = GridFactory.create(4);
        IMObjectComponentFactory factory = context.getComponentFactory();
        for (NodeDescriptor descriptor : descriptors) {
            Component component = factory.create(object, descriptor);
            add(grid, descriptor.getDisplayName(), component, context);
        }

        container.add(grid);

        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object);
        NodeDescriptor items = archetype.getNodeDescriptor("items");
        GroupBox box = new GroupBox();
        box.setTitle(items.getDisplayName());

        if (_editor != null) {
            box.add(_editor.getComponent());
        } else {
            IMObjectLayoutStrategy strategy
                    = new ActRelationshipTableLayoutStrategy(items);

            context = new DefaultLayoutContext(context);
            context.setComponentFactory(new TableComponentFactory());

            Component child = strategy.apply(object, context);
            box.add(child);
        }
        container.add(box);
    }

    /**
     * Returns a node filter to filter nodes. This implementation filters the
     * "items" and "participants" nodes.
     *
     * @param context the context
     * @return a node filter to filter nodes
     */
    @Override
    protected NodeFilter getNodeFilter(LayoutContext context) {
        NodeFilter filter = new NamedNodeFilter("items", "participants");
        return getNodeFilter(context, filter);
    }

    /**
     * Helper to create a component for a node desciprotr.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     * @param context    the layout context
     */
    @Override
    protected Component createComponent(IMObject parent,
                                        NodeDescriptor descriptor,
                                        LayoutContext context) {
        Component component = super.createComponent(parent, descriptor,
                                                    context);
        String name = descriptor.getName();
        if (name.equals("lowTotal") || name.equals("highTotal")
            || name.equals("total")) {
            // @todo - workaround for OVPMS-211
            component.setEnabled(false);
            component.setFocusTraversalParticipant(false);
            if (component instanceof TextComponent) {
                Alignment align = new Alignment(Alignment.RIGHT,
                                                Alignment.DEFAULT);
                ((TextComponent) component).setAlignment(align);
            }
        }
        return component;
    }

}
