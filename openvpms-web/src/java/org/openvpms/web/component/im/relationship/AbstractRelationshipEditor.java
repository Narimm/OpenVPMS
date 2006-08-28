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

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.create.IMObjectCreatorListener;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * An editor for relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-17 01:10:40Z $
 */
public abstract class AbstractRelationshipEditor
        extends AbstractIMObjectEditor {

    /**
     * Editor for the source of the relationship.
     */
    private Entity _source;

    /**
     * Editor for the target of the relationship.
     */
    private Entity _target;


    /**
     * Construct a new <code>EntityRelationshipEditor</code>.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param context      the layout context
     */
    public AbstractRelationshipEditor(IMObject relationship,
                                      IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
        IMObjectReference sourceRef;
        IMObjectReference targetRef;
        IMObject source;
        IMObject target;

        Property sourceProp = getSource();
        Property targetProp = getTarget();

        sourceRef = (IMObjectReference) sourceProp.getValue();
        targetRef = (IMObjectReference) targetProp.getValue();

        source = IMObjectHelper.getObject(sourceRef,
                                          sourceProp.getDescriptor());
        target = IMObjectHelper.getObject(targetRef,
                                          targetProp.getDescriptor());

        // initialise the properties if null
        if (sourceRef == null && source != null) {
            sourceProp.setValue(source.getObjectReference());
        }
        if (targetRef == null && target != null) {
            targetProp.setValue(target.getObjectReference());
        }

        IMObject edited = Context.getInstance().getCurrent();
        boolean srcReadOnly = true;
        if (source == null || !source.equals(edited)) {
            srcReadOnly = false;
        }

        _source = new Entity(sourceProp, srcReadOnly, context);

        boolean targetReadOnly = true;
        if (target == null || !target.equals(edited) || target.equals(source)) {
            targetReadOnly = false;
        }

        _target = new Entity(targetProp, targetReadOnly, context);
    }

    /**
     * Returns the source property.
     *
     * @return the source property
     */
    protected Property getSource() {
        return getProperty("source");
    }

    /**
     * Returns the target property.
     *
     * @return the target property
     */
    protected Property getTarget() {
        return getProperty("target");
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
     * Pops up a dialog to select an entity.
     *
     * @param entity the entity wrapper
     */
    protected void onSelect(final Entity entity) {
        NodeDescriptor descriptor = entity.getProperty().getDescriptor();
        try {
            Query<IMObject> query = QueryFactory.create(
                    descriptor.getArchetypeRange());
            final Browser<IMObject> browser = new TableBrowser<IMObject>(query);
            String title = Messages.get("imobject.select.title",
                                        descriptor.getDisplayName());
            final BrowserDialog<IMObject> popup
                    = new BrowserDialog<IMObject>(title, browser, true);

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
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
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

        NodeDescriptor descriptor = entity.getProperty().getDescriptor();
        IMObjectCreator.create(descriptor.getDisplayName(),
                               descriptor.getArchetypeRange(), listener);
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param entity the entity to associate the object with
     */
    protected void onEditCompleted(IMObjectEditor editor,
                                   AbstractRelationshipEditor.Entity entity) {
        if (!editor.isCancelled() && !editor.isDeleted()) {
            entity.setObject(editor.getObject());
        }
    }

    /**
     * Invoked when an object is created. Pops up an editor to edit it.
     *
     * @param object the object to edit
     * @param entity the entity to associate the object with, on completion of
     *               editing
     */
    private void onCreated(IMObject object,
                           final AbstractRelationshipEditor.Entity entity) {
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
     * Relationship layout strategy. Displays the source and target nodes
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
                                      PropertySet properties,
                                      Component container,
                                      LayoutContext context) {
            Grid grid = GridFactory.create(4);
            add(grid, _source.getProperty(), _source.getComponent(), context);
            add(grid, _target.getProperty(), _target.getComponent(), context);
            doGridLayout(object, descriptors, properties, grid, context);
            container.add(grid);
        }

    }

    /**
     * Editor for a source/target entity in a relationship.
     */
    private class Entity extends AbstractPropertyEditor {

        /**
         * The viewer.
         */
        private IMObjectReferenceViewer _viewer;

        /**
         * The editor.
         */
        private IMObjectReferenceEditor _editor;

        /**
         * Construct a new <code>Entity</code>.
         *
         * @param property the reference property
         * @param readOnly if <code>true<code> don't render the select button
         * @param context  the layout context
         */
        public Entity(Property property, boolean readOnly,
                      LayoutContext context) {
            super(property);
            if (readOnly) {
                IMObjectReference ref = (IMObjectReference) property.getValue();
                _viewer = new IMObjectReferenceViewer(ref, false);
            } else {
                _editor = new IMObjectReferenceEditor(property, context) {
                    protected void onSelect() {
                        // override default behaviour to enable creation of objects.
                        Entity.this.onSelect();
                    }
                };
            }
        }

        /**
         * Sets the value of the reference to the supplied object.
         *
         * @param object the object. May  be <code>null</code>
         */
        public void setObject(IMObject object) {
            if (_editor != null) {
                _editor.setObject(object);
            }
        }

        /**
         * Returns the edit component.
         *
         * @return the edit component
         */
        public Component getComponent() {
            return (_editor != null) ? _editor.getComponent() :
                    _viewer.getComponent();
        }

        /**
         * Pops up a dialog to select an object.
         */
        protected void onSelect() {
            AbstractRelationshipEditor.this.onSelect(this);
        }

    }

}
