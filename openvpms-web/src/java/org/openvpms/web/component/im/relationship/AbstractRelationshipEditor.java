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
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditorFactory;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.GridFactory;

import java.util.List;


/**
 * An editor for relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-17 01:10:40Z $
 */
public abstract class AbstractRelationshipEditor extends AbstractIMObjectEditor {

    /**
     * Editor for the source of the relationship. Null if the source is the parent.
     */
    private PropertyEditor sourceEditor;

    /**
     * Editor for the target of the relationship. Null if the target is the parent.
     */
    private PropertyEditor targetEditor;


    /**
     * Constructs an <tt>AbstractRelationshipEditor</tt>.
     *
     * @param relationship  the relationship
     * @param parent        the parent object
     * @param layoutContext the layout context
     */
    public AbstractRelationshipEditor(IMObject relationship,
                                      IMObject parent,
                                      LayoutContext layoutContext) {
        super(relationship, parent, layoutContext);
        Context context = layoutContext.getContext();
        Property sourceProp = getSource();
        Property targetProp = getTarget();

        IMObjectReference sourceRef = (IMObjectReference) sourceProp.getValue();
        IMObjectReference targetRef = (IMObjectReference) targetProp.getValue();

        IMObject source = getObject(sourceRef, parent, sourceProp.getArchetypeRange(), context);
        IMObject target = getObject(targetRef, parent, targetProp.getArchetypeRange(), context);

        // initialise the properties if null
        if (sourceRef == null && source != null) {
            sourceProp.setValue(source.getObjectReference());
        }
        if (targetRef == null && target != null) {
            targetProp.setValue(target.getObjectReference());
        }

        if (source == null || !source.equals(parent)) {
            sourceEditor = createReferenceEditor(sourceProp, layoutContext);
        }

        if (target == null || !target.equals(parent) || target.equals(source)) {
            targetEditor = createReferenceEditor(targetProp, layoutContext);
        }
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
     * Creates a new reference editor.
     *
     * @param property the reference property
     * @param context  the layout context
     * @return a new reference editor
     */
    protected IMObjectReferenceEditor<Entity> createReferenceEditor(Property property, LayoutContext context) {
        IMObjectReferenceEditor<Entity> editor
                = IMObjectReferenceEditorFactory.create(property, getObject(), context);
        editor.setAllowCreate(true);
        return editor;
    }

    /**
     * Relationship layout strategy. Displays the source/target nodes before any others.
     */
    protected class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Constructs a <tt>LayoutStrategy</tt>.
         */
        public LayoutStrategy() {
        }

        /**
         * Returns a node filter to filter nodes.
         * <p/>
         * This implementation filters the "source" and "target" nodes.
         *
         * @param object  the object to filter nodes for
         * @param context the context
         * @return a node filter to filter nodes
         */
        @Override
        protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
            NodeFilter filter = new NamedNodeFilter("source", "target");
            return getNodeFilter(context, filter);
        }

        /**
         * Lays out child components in a grid.
         *
         * @param object      the object to lay out
         * @param parent      the parent object. May be <tt>null</tt>
         * @param descriptors the property descriptors
         * @param properties  the properties
         * @param container   the container to use
         * @param context     the layout context
         */
        @Override
        protected void doSimpleLayout(IMObject object, IMObject parent, List<NodeDescriptor> descriptors,
                                      PropertySet properties, Component container, LayoutContext context) {
            Grid grid = createGrid(descriptors);
            if (sourceEditor != null) {
                add(grid, new ComponentState(sourceEditor.getComponent(), sourceEditor.getProperty(),
                        sourceEditor.getFocusGroup()));
            }
            if (targetEditor != null) {
                add(grid, new ComponentState(targetEditor.getComponent(), targetEditor.getProperty(),
                        targetEditor.getFocusGroup()));
            }
            doGridLayout(object, descriptors, properties, grid, context);
            container.add(grid);
        }

        /**
         * Creates a grid with the no. of columns determined by the no. of
         * node descriptors.
         *
         * @param descriptors the node descriptors
         * @return a new grid with <tt>4</tt> columns
         */
        @Override
        protected Grid createGrid(List<NodeDescriptor> descriptors) {
            return GridFactory.create(4);
        }
    }

    /**
     * Returns the object associated with the reference, defaulting it to the parent if the reference is unset,
     * and the parent is of the correct archetype.
     *
     * @param reference      the reference
     * @param parent         the parent object
     * @param archetypeRange the archetypes that the reference may refer to
     * @param context        the current context
     * @return the object, or <tt>null</tt> if the reference is set but refers to an invalid object
     */
    private IMObject getObject(IMObjectReference reference, IMObject parent, String[] archetypeRange, Context context) {
        IMObject result = null;
        if (reference == null) {
            if (TypeHelper.isA(parent, archetypeRange)) {
                result = parent;
            }
        } else {
            result = IMObjectHelper.getObject(reference, context);
        }
        return result;
    }
}
