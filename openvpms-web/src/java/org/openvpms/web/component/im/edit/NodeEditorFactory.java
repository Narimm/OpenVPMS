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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.bound.BoundPalette;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertyComponentEditor;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.view.AbstractIMObjectComponentFactory;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.palette.Palette;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.spring.ServiceHelper;

import java.text.Format;
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
    private Editors _editors;

    /**
     * Component factory for read-only/derived properties.
     */
    private IMObjectComponentFactory _readOnly;


    /**
     * Construct a new <code>NodeEditorFactory</code>.
     *
     * @param editors the editors
     * @param context the layout context
     */
    public NodeEditorFactory(Editors editors, LayoutContext context) {
        super(context);
        _editors = editors;
    }

    /**
     * Create a component to display a property.
     *
     * @param property the property to display
     * @param context  the context object
     * @return a component to display <code>object</code>
     */
    public ComponentState create(Property property, IMObject context) {
        ComponentState result;
        NodeDescriptor descriptor = property.getDescriptor();
        if (descriptor.isReadOnly() || descriptor.isDerived()) {
            result = getReadOnlyFactory().create(property, context);
        } else {
            Editor editor = null;
            if (descriptor.isLookup()) {
                editor = getSelectEditor(property, context);
            } else if (descriptor.isBoolean()) {
                editor = getBooleanEditor(property);
            } else if (descriptor.isString()) {
                editor = getTextEditor(property);
            } else if (descriptor.isNumeric()) {
                editor = getNumericEditor(property);
            } else if (descriptor.isDate()) {
                editor = getDateEditor(property);
            } else if (descriptor.isCollection()) {
                editor = getCollectionEditor((CollectionProperty) property,
                                             context);
            } else if (descriptor.isObjectReference()) {
                editor = getObjectReferenceEditor(property);
            }
            if (editor != null) {
                result = new ComponentState(editor.getComponent(), property,
                                            editor.getFocusGroup());
            } else {
                Label label = LabelFactory.create();
                label.setText("No editor for type " + descriptor.getType());
                result = new ComponentState(label);
            }
        }
        return result;
    }

    /**
     * Create a component to display an object.
     *
     * @param object     the object to display
     * @param context    the object's parent. May be <code>null</code>
     * @param descriptor the parent object's descriptor. May be
     */
    public ComponentState create(IMObject object, IMObject context,
                                 NodeDescriptor descriptor) {
        Editor editor = getObjectEditor(object, context);
        _editors.add(editor);
        return new ComponentState(editor.getComponent(),
                                  editor.getFocusGroup());
    }

    /**
     * Creates an editor for an {@link IMObject}.
     *
     * @param object  the object to edit
     * @param context the object's parent. May be <code>null</code>
     * @return a new editor for <code>object</code>
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
     * @return a new editor for <code>property</code>
     */
    protected Editor getNumericEditor(Property property) {
        int maxColumns = 10;
        NodeDescriptor descriptor = property.getDescriptor();
        boolean edit = !descriptor.isReadOnly() || descriptor.isDerived();
        Format format = NumberFormatter.getFormat(descriptor, edit);
        TextField text = TextComponentFactory.create(property, maxColumns,
                                                     format);
        if (!edit) {
            Alignment align = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
            text.setAlignment(align);
        }
        return createPropertyEditor(property, text);
    }


    /**
     * Returns an editor for a date property.
     *
     * @param property the date property
     * @return a new editor for <code>property</code>
     */
    protected Editor getDateEditor(Property property) {
        Component date = DateFieldFactory.create(property);
        return createPropertyEditor(property, date);
    }

    /**
     * Returns an editor for a lookup property.
     *
     * @param property the lookup property
     * @param context  the parent object
     * @return a new editor for <code>property</code>
     */
    protected Editor getSelectEditor(Property property, IMObject context) {
        ListModel model = new LookupListModel(context,
                                              property.getDescriptor());
        SelectField field = SelectFieldFactory.create(property, model);
        field.setCellRenderer(new LookupListCellRenderer());
        return createPropertyEditor(property, field);
    }

    /**
     * Returns an editor for a boolean property.
     *
     * @param property the boolean property
     * @return a new editor for <code>property</code>
     */
    protected Editor getBooleanEditor(Property property) {
        Component component = getCheckBox(property);
        return createPropertyEditor(property, component);
    }

    /**
     * Returns an editor for a text property.
     *
     * @param property the boolean property
     * @return a new editor for <code>property</code>
     */
    protected Editor getTextEditor(Property property) {
        Component component = getTextComponent(property);
        return createPropertyEditor(property, component);
    }

    /**
     * Returns an editor for a collection property.
     *
     * @param property the collection property
     * @param object   the parent object
     * @return a new editor for <code>property</code>
     */
    protected Editor getCollectionEditor(CollectionProperty property,
                                         IMObject object) {
        Editor editor = null;
        NodeDescriptor descriptor = property.getDescriptor();
        if (descriptor.isParentChild()) {
            if (descriptor.getMinCardinality() == 1
                    && descriptor.getMaxCardinality() == 1) {
                // handle the special case of a collection of one element.
                // This can be edited inline
                String[] range = DescriptorHelper.getShortNames(descriptor);
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
                        _editors.add(editor, property);
                    }
                }
            }
            if (editor == null) {
                editor = IMObjectCollectionEditorFactory.create(
                        property, object, getLayoutContext());
                _editors.add(editor);
            }
        } else {
            List<IMObject> identifiers;
            identifiers = ArchetypeQueryHelper.getCandidateChildren(
                    ServiceHelper.getArchetypeService(),
                    descriptor, object);
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
     * @return a new editor for <code>property</code>
     */
    protected Editor getObjectReferenceEditor(Property property) {
        String[] range = DescriptorHelper.getShortNames(
                property.getDescriptor());
        Editor editor;
        if (TypeHelper.matches(range, "document.*")) {
            editor = new DocumentEditor(property);
        } else {
            editor = IMObjectReferenceEditorFactory.create(property,
                                                           getLayoutContext());
        }
        _editors.add(editor);
        return editor;
    }

    /**
     * Returns a factory for creating read-only components.
     *
     * @return a factory for creating read-only components
     */
    private IMObjectComponentFactory getReadOnlyFactory() {
        if (_readOnly == null) {
            _readOnly = new ReadOnlyComponentFactory(getLayoutContext());
        }
        return _readOnly;
    }

    /**
     * Helper to create a {@link PropertyEditor} for a component, and register
     * with the set of editors.
     */
    private PropertyEditor createPropertyEditor(Property property,
                                                Component component) {
        PropertyComponentEditor editor
                = new PropertyComponentEditor(property, component);
        _editors.add(editor);
        return editor;
    }

}
