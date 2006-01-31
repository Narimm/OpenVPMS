package org.openvpms.web.component.edit;

import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.app.Context;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.GridFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.TextComponentFactory;
import org.openvpms.web.component.im.IMObjectComponentFactory;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.query.Browser;
import org.openvpms.web.component.query.BrowserDialog;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.Messages;


/**
 * An editor for {@link EntityRelationship}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class RelationshipEditor extends AbstractIMObjectEditor {

    /**
     * The relationship.
     */
    private final EntityRelationship _relationship;

    /**
     * The entity representing the source of the relationship.
     */
    private Entity _source;

    /**
     * The entity representing the target of the relationship.
     */
    private Entity _target;


    /**
     * Construct a new <code>RelationshipEditor</code>.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param descriptor   the parent descriptor
     */
    public RelationshipEditor(EntityRelationship relationship, IMObject parent,
                              NodeDescriptor descriptor) {
        super(relationship, parent, descriptor, true);
        _relationship = relationship;
        ArchetypeDescriptor archetype = getArchetypeDescriptor();
        NodeDescriptor sourceDesc = archetype.getNodeDescriptor("source");
        NodeDescriptor targetDesc = archetype.getNodeDescriptor("target");

        IMObject source = null;
        IMObject target = null;

        if (relationship.getSource() == null) {
            String[] range = sourceDesc.getArchetypeRange();
            source = Context.getInstance().getObject(range);
            if (source != null) {
                relationship.setSource(new IMObjectReference(source));
            }
        } else {
            source = Context.getInstance().getObject(relationship.getSource());
        }
        boolean srcModifiable = (source == null);
        _source = new Entity(source, _relationship.getSource(), sourceDesc,
                srcModifiable);
        if (srcModifiable) {
            _source.getSelect().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSelect(_source);
                }
            });
        }

        if (relationship.getTarget() == null) {
            String[] range = targetDesc.getArchetypeRange();
            target = Context.getInstance().getObject(range);
            if (target != null) {
                relationship.setTarget(new IMObjectReference(target));
            }
        }
        boolean targetModifiable = (target == null || !srcModifiable);
        _target = new Entity(target, _relationship.getTarget(), targetDesc,
                targetModifiable);
        if (targetModifiable) {
            _target.getSelect().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSelect(_target);
                }
            });
        }
    }

    /**
     * Creates the layout strategy.
     *
     * @param showAll if <code>true</code> show required and optional fields;
     *                otherwise show required fields.
     * @return a new layout strategy
     */
    @Override
    protected ExpandableLayoutStrategy createLayoutStrategy(boolean showAll) {
        return new LayoutStrategy(showAll);
    }

    protected void onSelect(final Entity entity) {
        NodeDescriptor descriptor = entity.getDescriptor();
        final Browser browser = new Browser(descriptor.getArchetypeRange());
        String title = Messages.get("relationship.select",
                descriptor.getDisplayName());
        final BrowserDialog popup = new BrowserDialog(title, browser);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                IMObject object = popup.getSelected();
                if (object != null) {
                    onSelected(entity, object);
                }
            }
        });

        popup.show();
    }

    protected void onSelected(Entity entity, IMObject object) {
        entity.setObject(object);
        IMObjectReference reference = new IMObjectReference(object);
        if (entity == _source) {
            _relationship.setSource(reference);
        } else {
            _relationship.setTarget(reference);
        }
    }

    private class LayoutStrategy extends ExpandableLayoutStrategy {

        /**
         * Construct a new <code>LayoutStrategy</code>.
         *
         * @param showOptional if <code>true</code> show optional fields as well
         *                     as mandatory ones.
         */
        public LayoutStrategy(boolean showOptional) {
            super(showOptional);
            ChainedNodeFilter filter = new ChainedNodeFilter();
            filter.add(new BasicNodeFilter(showOptional, false));
            filter.add(new NamedNodeFilter("source", "target"));
            setNodeFilter(filter);
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
            Grid grid = GridFactory.create(2);
            add(grid, _source.getDescriptor().getDisplayName(),
                    _source.getComponent());
            add(grid, _target.getDescriptor().getDisplayName(),
                    _target.getComponent());
            for (NodeDescriptor descriptor : descriptors) {
                Component component = factory.create(object, descriptor);
                add(grid, descriptor.getDisplayName(), component);
            }

            Row group = RowFactory.create(grid, getButtonRow());
            container.add(group);
        }

    }


    private class Entity {

        private NodeDescriptor _descriptor;
        private TextComponent _label;
        private Button _select;
        private Row _component;

        public Entity(IMObject object, IMObjectReference reference,
                      NodeDescriptor descriptor, boolean modifiable) {
            _descriptor = descriptor;
            doLayout(modifiable);
            if (object != null) {
                setObject(object);
            } else {
                setObject(reference);
            }
        }

        public NodeDescriptor getDescriptor() {
            return _descriptor;
        }

        public Button getSelect() {
            return _select;
        }

        public Component getComponent() {
            return _component;
        }

        public void setObject(IMObjectReference reference) {
            IMObject object = null;
            if (reference != null) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                object = service.getById(reference.getArchetypeId(),
                        reference.getUid());
            }
            setObject(object);
        }

        public void setObject(IMObject object) {
            if (object != null) {
                String key = "relationship.entity.summary";
                String summary = Messages.get(key, object.getName(),
                        object.getDescription());
                _label.setText(summary);
            } else {
                _label.setText(Messages.get("relationship.select"));
            }
        }

        protected void doLayout(boolean modifiable) {
            final int columns = 32; // @todo
            _label = TextComponentFactory.create();
            _label.setWidth(new Extent(columns, Extent.EX));
            _label.setEnabled(false);
            _component = RowFactory.create("RelationshipEditor.EntityRow", _label);
            if (modifiable) {
                _select = ButtonFactory.create("select");
                _component.add(_select);
            }
        }

    }


}
