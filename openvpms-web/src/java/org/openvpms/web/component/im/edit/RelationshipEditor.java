/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit;

import java.util.List;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.create.IMObjectCreatorListener;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * An editor for {@link EntityRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class RelationshipEditor extends AbstractIMObjectEditor {

    /**
     * Editor for the source of the relationship.
     */
    private IMObjectReferenceEditor _source;

    /**
     * Editor for the target of the relationship.
     */
    private IMObjectReferenceEditor _target;


    /**
     * Construct a new <code>RelationshipEditor</code>.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param context      the layout context
     */
    public RelationshipEditor(EntityRelationship relationship,
                                 IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
        IMObject source;
        IMObject target;

        ArchetypeDescriptor archetype = getArchetypeDescriptor();
        NodeDescriptor sourceDesc = archetype.getNodeDescriptor("source");
        NodeDescriptor targetDesc = archetype.getNodeDescriptor("target");

        source = Entity.getObject(relationship.getSource(), sourceDesc);
        target = Entity.getObject(relationship.getTarget(), targetDesc);

        IMObject edited = Context.getInstance().getCurrent();
        boolean srcReadOnly = true;
        if (source == null || !source.equals(edited)) {
            srcReadOnly = false;
        }

        _source = getEditor(relationship, sourceDesc, srcReadOnly, context);
        if (source != null && relationship.getSource() == null) {
            _source.setObject(source);
        }

        boolean targetReadOnly = true;
        if (target == null || !target.equals(edited) || target.equals(source)) {
            targetReadOnly = false;
        }

        _target = getEditor(relationship, targetDesc, targetReadOnly, context);
        if (target != null && relationship.getTarget() == null) {
            _target.setObject(target);
        }
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        LayoutContext context) {
        IMObjectEditor result = null;
        if (object instanceof EntityRelationship) {
            result = new RelationshipEditor((EntityRelationship) object, parent,
                                            context);
        }
        return result;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Returns an editor for one side of the relationship.
     *
     * @param relationship the relationship
     * @param descriptor   the descriptor of the node to edit
     * @param readOnly     determines if the node is read-only
     * @param context      the layout context
     */
    protected IMObjectReferenceEditor getEditor(EntityRelationship relationship,
                                              NodeDescriptor descriptor,
                                              boolean readOnly,
                                              LayoutContext context) {

        Property property = getProperty(descriptor.getName());
        return new Entity(property, readOnly, context);
    }

    /**
     * Pops up a dialog to select an entity.
     *
     * @param entity the entity wrapper
     */
    protected void onSelect(final Entity entity) {
        NodeDescriptor descriptor = entity.getDescriptor();
        Query query = QueryFactory.create(descriptor.getArchetypeRange());
        final Browser browser = new Browser(query);
        String title = Messages.get("imobject.select.title",
                                    descriptor.getDisplayName());
        final BrowserDialog popup = new BrowserDialog(title, browser, true);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (popup.createNew()) {
                    onCreate(entity);
                } else {
                    IMObject object = popup.getSelected();
                    if (object != null) {
                        entity.setObject(object);
                    }
                }
            }
        });

        popup.show();
    }

    /**
     * Invoked when the 'new' button is pressed.
     *
     * @param entity describes the type of object to create
     */
    protected void onCreate(final Entity entity) {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated(object, entity);
            }
        };

        NodeDescriptor descriptor = entity.getDescriptor();
        IMObjectCreator.create(descriptor.getDisplayName(),
                               descriptor.getArchetypeRange(), listener);
    }


    /**
     * Invoked when an object is created. Pops up an editor to edit it.
     *
     * @param object the object to edit
     * @param entity the entity to associate the object with, on completion of
     *               editing
     */
    private void onCreated(IMObject object, final Entity entity) {
        LayoutContext context = getLayoutContext();
        final IMObjectEditor editor
                = IMObjectEditorFactory.create(object, context);
        final EditDialog dialog = new EditDialog(editor, context);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                onEditCompleted(editor, entity);
            }
        });

        dialog.show();
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param entity the entity to associate the object with
     */
    protected void onEditCompleted(IMObjectEditor editor, Entity entity) {
        if (!editor.isCancelled() && !editor.isDeleted()) {
            entity.setObject(editor.getObject());
        }
    }

    /**
     * EntityRelationship layout strategy. Displays the source and target nodes
     * before any others.
     */
    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Returns a node filter to filter nodes. This implementation filters
         * the "source" and "target" nodes.
         *
         * @param context the context
         * @return a node filter to filter nodes
         */
        @Override
        protected NodeFilter getNodeFilter(LayoutContext context) {
            NodeFilter filter = new NamedNodeFilter("source", "target");
            return getNodeFilter(context, filter);
        }

        /**
         * Lays out child components in a 2x2 grid.
         *
         * @param object      the parent object
         * @param descriptors the property descriptors
         * @param properties  the properties
         * @param container   the container to use
         * @param context     the layout context
         */
        @Override
        protected void doSimpleLayout(IMObject object,
                                      List<NodeDescriptor> descriptors,
                                      PropertySet properties, Component container,
                                      LayoutContext context) {
            Grid grid = GridFactory.create(4);
            add(grid, _source.getProperty(), _source.getComponent(), context);
            add(grid, _target.getProperty(), _target.getComponent(),context);
            doGridLayout(object, descriptors, properties, grid, context);
            container.add(grid);
        }

    }

    /**
     * Editor for a source/target entity in a relationship.
     */
    private class Entity extends IMObjectReferenceEditor {

        /**
         * Construct a new <code>Entity</code>.
         *
         * @param property the reference property
         * @param readOnly if <code>true<code> don't render the select button
         * @param context  the layout context
         */
        public Entity(Property property, boolean readOnly,
                      LayoutContext context) {
            super(property, readOnly, context);
        }

        /**
         * Pops up a dialog to select an object.
         */
        @Override
        protected void onSelect() {
            // override default behaviour to enable creation of objects.
            RelationshipEditor.this.onSelect(this);
        }

    }

}
