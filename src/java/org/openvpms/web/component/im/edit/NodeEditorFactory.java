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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.bound.BoundPalette;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.edit.PropertyComponentEditor;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.AbstractIMObjectComponentFactory;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.palette.Palette;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;

import java.util.List;


/**
 * Factory for editors of {@link IMObject} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NodeEditorFactory extends AbstractIMObjectComponentFactory {

    /**
     * Collects the editors created by this factory.
     */
    private Editors editors;

    /**
     * Component factory for read-only/derived properties.
     */
    private IMObjectComponentFactory readOnly;


    /**
     * Construct a new <tt>NodeEditorFactory</tt>.
     *
     * @param editors the editors
     * @param context the layout context
     */
    public NodeEditorFactory(Editors editors, LayoutContext context) {
        super(context);
        this.editors = editors;
    }

    /**
     * Create a component to display a property.
     *
     * @param property the property to display
     * @param context  the context object
     * @return a component to display <tt>object</tt>
     */
    public ComponentState create(Property property, IMObject context) {
        ComponentState result;
        if (property.isReadOnly() || property.isDerived()) {
            result = getReadOnlyFactory().create(property, context);
        } else {
            Editor editor = null;
            if (property.isLookup()) {
                editor = createLookupEditor(property, context);
            } else if (property.isBoolean()) {
                editor = createBooleanEditor(property);
            } else if (property.isString()) {
                editor = createStringEditor(property);
            } else if (property.isNumeric()) {
                editor = createNumericEditor(property);
            } else if (property.isDate()) {
                editor = createDateEditor(property);
            } else if (property.isCollection()) {
                editor = createCollectionEditor((CollectionProperty) property,
                                                context);
            } else if (property.isObjectReference()) {
                editor = createObjectReferenceEditor(property, context);
            }
            if (editor != null) {
                result = new ComponentState(editor.getComponent(), property,
                                            editor.getFocusGroup());
            } else {
                Label label = LabelFactory.create();
                label.setText("No editor for type " + property.getType());
                result = new ComponentState(label);
            }
        }
        return result;
    }

    /**
     * Create a component to display an object.
     *
     * @param object  the object to display
     * @param context the object's parent. May be <tt>null</tt>
     */
    public ComponentState create(IMObject object, IMObject context) {
        Editor editor = getObjectEditor(object, context);
        editors.add(editor);
        return new ComponentState(editor.getComponent(),
                                  editor.getFocusGroup());
    }

    /**
     * Creates an editor for an {@link IMObject}.
     *
     * @param object  the object to edit
     * @param context the object's parent. May be <tt>null</tt>
     * @return a new editor for <tt>object</tt>
     */
    protected IMObjectEditor getObjectEditor(IMObject object,
                                             IMObject context) {
        return IMObjectEditorFactory.create(object, context,
                                            getLayoutContext());
    }

    /**
     * Returns an editor for a numeric property.
     *
     * @param property the numeric property
     * @return a new editor for <tt>property</tt>
     */
    protected Editor createNumericEditor(Property property) {
        Component component = createNumeric(property);
        return createPropertyEditor(property, component);
    }

    /**
     * Returns an editor for a date property.
     *
     * @param property the date property
     * @return a new editor for <tt>property</tt>
     */
    protected Editor createDateEditor(Property property) {
        Component date = createDate(property);
        return createPropertyEditor(property, date);
    }

    /**
     * Returns an editor for a lookup property.
     *
     * @param property the lookup property
     * @param context  the parent object
     * @return a new editor for <tt>property</tt>
     */
    protected Editor createLookupEditor(Property property, IMObject context) {
        IMObjectProperty p = (IMObjectProperty) property;
        ListModel model = new LookupListModel(context, p.getDescriptor());
        SelectField field = SelectFieldFactory.create(property, model);
        field.setCellRenderer(new LookupListCellRenderer());
        return createPropertyEditor(property, field);
    }

    /**
     * Returns an editor for a boolean property.
     *
     * @param property the boolean property
     * @return a new editor for <tt>property</tt>
     */
    protected Editor createBooleanEditor(Property property) {
        Component component = createBoolean(property);
        return createPropertyEditor(property, component);
    }

    /**
     * Returns an editor for a text property.
     *
     * @param property the boolean property
     * @return a new editor for <tt>property</tt>
     */
    protected Editor createStringEditor(Property property) {
        Component component = createString(property);
        return createPropertyEditor(property, component);
    }

    /**
     * Returns an editor for a collection property.
     *
     * @param property the collection property
     * @param object   the parent object
     * @return a new editor for <tt>property</tt>
     */
    protected Editor createCollectionEditor(CollectionProperty property,
                                            IMObject object) {
        Editor editor = null;
        IMObjectProperty p = (IMObjectProperty) property;
        NodeDescriptor descriptor = p.getDescriptor();
        if (descriptor.isParentChild()) {
            if (descriptor.getMinCardinality() == 1
                    && descriptor.getMaxCardinality() == 1) {
                // handle the special case of a collection of one element.
                // This can be edited inline
                String[] range = property.getArchetypeRange();
                if (range.length == 1) {
                    Object[] values = property.getValues().toArray();
                    IMObject value;
                    if (values.length > 0) {
                        value = (IMObject) values[0];
                    } else {
                        value = IMObjectCreator.create(range[0]);
                        if (value != null) {
                            property.add(value);
                        }
                    }
                    if (value != null) {
                        editor = getObjectEditor(value, object);
                        editors.add(editor, property);
                    }
                }
            }
            if (editor == null) {
                editor = IMObjectCollectionEditorFactory.create(
                        property, object, getLayoutContext());
                editors.add(editor);
            }
        } else {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            List<IMObject> identifiers
                    = ArchetypeQueryHelper.getCandidates(service, descriptor);
            Palette palette = new BoundPalette(identifiers, property);
            palette.setCellRenderer(new IMObjectListCellRenderer());
            editor = createPropertyEditor(property, palette);
        }
        return editor;
    }

    /**
     * Returns an editor for an object reference property.
     *
     * @param property the object reference properrty
     * @param object   the parent object
     * @return a new editor for <tt>property</tt>
     */
    protected Editor createObjectReferenceEditor(Property property,
                                                 IMObject object) {
        String[] range = property.getArchetypeRange();
        Editor editor;
        if (TypeHelper.matches(range, "document.*")) {
            editor = new DocumentEditor(property);
        } else {
            editor = IMObjectReferenceEditorFactory.create(property, object,
                                                           getLayoutContext());
        }
        editors.add(editor);
        return editor;
    }

    /**
     * Returns a factory for creating read-only components.
     *
     * @return a factory for creating read-only components
     */
    private IMObjectComponentFactory getReadOnlyFactory() {
        if (readOnly == null) {
            readOnly = new ReadOnlyComponentFactory(getLayoutContext());
        }
        return readOnly;
    }

    /**
     * Helper to create a {@link PropertyEditor} for a component, and register
     * with the set of editors.
     */
    private PropertyEditor createPropertyEditor(Property property,
                                                Component component) {
        PropertyComponentEditor editor
                = new PropertyComponentEditor(property, component);
        editors.add(editor);
        return editor;
    }

}
