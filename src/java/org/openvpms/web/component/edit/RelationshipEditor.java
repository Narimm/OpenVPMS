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
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.DefaultQuery;
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

        IMObject source = getObject(_relationship.getSource(), sourceDesc);
        IMObject target = getObject(_relationship.getTarget(), targetDesc);

        IMObject edited = Context.getInstance().getEdited();
        boolean srcReadOnly = true;
        if (source == null || !source.equals(edited)) {
            srcReadOnly = false;
        }
        if (source != null && _relationship.getSource() == null) {
            _relationship.setSource(new IMObjectReference(source));
        }
        _source = create(source, sourceDesc, srcReadOnly);

        boolean targetReadOnly = true;
        if (target == null || !target.equals(edited) || target.equals(source)) {
            targetReadOnly = false;
        }
        if (target != null && _relationship.getTarget() == null) {
            _relationship.setTarget(new IMObjectReference(target));
        }
        _target = create(target, targetDesc, targetReadOnly);
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

    /**
     * Pops up a dialog to select an entity.
     *
     * @param entity the entity wrapper
     */
    protected void onSelect(final Entity entity) {
        NodeDescriptor descriptor = entity.getDescriptor();
        Query query = new DefaultQuery(descriptor.getArchetypeRange());
        final Browser browser = new Browser(query);
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

    /**
     * Invoked when an entity object is selected.
     *
     * @param entity the entity wrapper
     * @param object the entity object
     */
    protected void onSelected(Entity entity, IMObject object) {
        entity.setEntity(object);
        IMObjectReference reference = new IMObjectReference(object);
        if (entity == _source) {
            _relationship.setSource(reference);
        } else {
            _relationship.setTarget(reference);
        }
    }

    /**
     * Returns an object given its reference and descriptor. If the reference is
     * null, determines if the descriptor matches that of the current object
     * being edited and returns that instead.
     *
     * @param reference  the object reference. May be <code>null</code>
     * @param descriptor the node descriptor
     * @return the object matching <code>reference</code>, or
     *         <code>descriptor</code>, or <code>null</code> if there is no
     *         match
     */
    private IMObject getObject(IMObjectReference reference,
                               NodeDescriptor descriptor) {
        IMObject result = null;
        if (reference == null) {
            result = match(descriptor);
        } else {
            IMObject edit = Context.getInstance().getEdited();
            if (edit != null) {
                if (edit.getArchetypeId().equals(reference.getArchetypeId())
                        && edit.getUid() == reference.getUid()) {
                    result = edit;
                }
            }
            if (result == null) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                result = service.get(reference);
            }
        }
        return result;
    }

    /**
     * Creates a new entity representing one side of the relationship.
     *
     * @param object     the object. May be <code>null</code>
     * @param descriptor the entity's descriptor
     * @param readOnly   if <code>true</code>, the enity cannot be changed
     * @return a new <code>Entity</code>
     */
    private Entity create(IMObject object, NodeDescriptor descriptor,
                          boolean readOnly) {
        final Entity result;
        result = new Entity(object, descriptor, readOnly);
        if (!readOnly) {
            result.getSelect().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSelect(result);
                }
            });
        }
        return result;
    }

    /**
     * Determines if the current object being edited matches archetype range of
     * the specified descriptor.
     *
     * @param descriptor the node descriptor
     * @return the current object being edited, or <code>null</code> if its type
     *         doesn't match the specified descriptor's archetype range
     */
    private IMObject match(NodeDescriptor descriptor) {
        IMObject result = null;
        String[] range = descriptor.getArchetypeRange();
        IMObject object = Context.getInstance().getEdited();
        if (object != null) {
            String shortName = object.getArchetypeId().getShortName();
            for (int i = 0; i < range.length; ++i) {
                if (range[i].equals(shortName)) {
                    result = object;
                    break;
                }
            }
        }
        return result;
    }


    /**
     * EntityRelationship layout strategy. Displays the source and target nodes
     * before any others.
     */
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

    /**
     * Wrapper for a source/target entity in a relationship.
     */
    private class Entity {

        /**
         * The entity's descriptor.
         */
        private NodeDescriptor _descriptor;

        /**
         * Displays a summary of the entity.
         */
        private TextComponent _label;

        /**
         * Button to change the entity/
         */
        private Button _select;

        /**
         * The rendered component.
         */
        private Row _component;


        /**
         * Construct a new <code>Entity</code>.
         *
         * @param entity     the entity. May be <code>null</code>
         * @param descriptor the entity descriptor
         * @param readOnly   if <code>true<code> don't render the select button
         */
        public Entity(IMObject entity, NodeDescriptor descriptor,
                      boolean readOnly) {
            _descriptor = descriptor;
            doLayout(readOnly);
            setEntity(entity);
        }

        /**
         * Returns the entity's descriptor.
         *
         * @return the entity's descriptor
         */
        public NodeDescriptor getDescriptor() {
            return _descriptor;
        }

        /**
         * Returns the select button.
         *
         * @return the select button, or <code>null</code> if the entity is read
         *         only
         */
        public Button getSelect() {
            return _select;
        }

        /**
         * Returns the rendered component.
         *
         * @return the rendered component
         */
        public Component getComponent() {
            return _component;
        }

        /**
         * Sets the entity
         *
         * @param entity the entity
         */
        public void setEntity(IMObject entity) {
            if (entity != null) {
                String key = "relationship.entity.summary";
                String summary = Messages.get(key, entity.getName(),
                        entity.getDescription());
                _label.setText(summary);
            } else {
                _label.setText(Messages.get("relationship.select"));
            }
        }

        /**
         * Lays out the component.
         *
         * @param readOnly if <code>true<code> don't render the select button
         */
        protected void doLayout(boolean readOnly) {
            final int columns = 32; // @todo
            _label = TextComponentFactory.create();
            _label.setWidth(new Extent(columns, Extent.EX));
            _label.setEnabled(false);
            _component = RowFactory.create("RelationshipEditor.EntityRow", _label);
            if (!readOnly) {
                _select = ButtonFactory.create("select");
                _component.add(_select);
            }
        }

    }


}
