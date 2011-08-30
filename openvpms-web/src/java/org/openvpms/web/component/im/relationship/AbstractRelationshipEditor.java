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

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditorFactory;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCache;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;

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
     * The source node name.
     */
    private static final String SOURCE = "source";

    /**
     * The target node name.
     */
    private static final String TARGET = "target";


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
        IMObjectCache cache = layoutContext.getCache();
        Property sourceProp = getSource();
        Property targetProp = getTarget();

        IMObjectReference sourceRef = (IMObjectReference) sourceProp.getValue();
        IMObjectReference targetRef = (IMObjectReference) targetProp.getValue();

        IMObject source = getObject(sourceRef, parent, sourceProp.getArchetypeRange(), cache);
        IMObject target = getObject(targetRef, parent, targetProp.getArchetypeRange(), cache);

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
        return getProperty(SOURCE);
    }

    /**
     * Returns the target property.
     *
     * @return the target property
     */
    protected Property getTarget() {
        return getProperty(TARGET);
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
         * Creates a component for a property.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display <code>property</code>
         */
        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            String name = property.getName();
            if (sourceEditor != null && name.equals(sourceEditor.getProperty().getName())) {
                return new ComponentState(sourceEditor);
            } else if (targetEditor != null && name.equals(targetEditor.getProperty().getName())) {
                return new ComponentState(targetEditor);
            }
            return super.createComponent(property, parent, context);
        }

        /**
         * Returns the 'simple' nodes. These will be rendered in a grid.
         *
         * @param archetype the archetype
         * @return the simple nodes
         */
        @Override
        protected List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype) {
            List<NodeDescriptor> result = super.getSimpleNodes(archetype);
            if (targetEditor != null) {
                result.add(0, targetEditor.getProperty().getDescriptor());
            }
            if (sourceEditor != null) {
                result.add(0, sourceEditor.getProperty().getDescriptor());
            }
            return result;
        }

        /**
         * Returns the 'complex' nodes. This filters the 'source' and 'target' nodes which are treated as
         * 'simple' nodes by {@link #getSimpleNodes}.
         *
         * @param archetype the archetype
         * @return the 'complex' nodes
         */
        @Override
        protected List<NodeDescriptor> getComplexNodes(ArchetypeDescriptor archetype) {
            return filter(null, archetype.getComplexNodeDescriptors(), new NamedNodeFilter(SOURCE, TARGET));
        }
    }

    /**
     * Returns the object associated with the reference, defaulting it to the parent if the reference is unset,
     * and the parent is of the correct archetype.
     *
     * @param reference      the reference
     * @param parent         the parent object
     * @param archetypeRange the archetypes that the reference may refer to
     * @param cache          the cache
     * @return the object, or <tt>null</tt> if the reference is set but refers to an invalid object
     */
    private IMObject getObject(IMObjectReference reference, IMObject parent, String[] archetypeRange,
                               IMObjectCache cache) {
        IMObject result = null;
        if (reference == null) {
            if (TypeHelper.isA(parent, archetypeRange)) {
                result = parent;
            }
        } else {
            result = cache.get(reference);
        }
        return result;
    }
}
