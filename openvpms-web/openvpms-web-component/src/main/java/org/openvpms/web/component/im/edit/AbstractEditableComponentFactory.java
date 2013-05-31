/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.bound.BoundPalette;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.PropertyComponentEditor;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.im.view.AbstractIMObjectComponentFactory;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.palette.Palette;
import org.openvpms.web.echo.style.Styles;

import java.util.List;

/**
 * A factory for editable components.
 *
 * @author Tim Anderson
 */
public class AbstractEditableComponentFactory extends AbstractIMObjectComponentFactory {

    /**
     * Component factory for read-only/derived properties.
     */
    private ReadOnlyComponentFactory readOnly;

    /**
     * Nodes to sort candidate objects on.
     */
    private final String[] IDENTIFIER_SORT_NODES = new String[]{"name", "id"};


    /**
     * Constructs an {@link AbstractEditableComponentFactory}.
     *
     * @param context the layout context.
     * @param style   the style name to use
     */
    public AbstractEditableComponentFactory(LayoutContext context, String style) {
        super(context, style);
    }

    /**
     * Creates components for boolean, string, numeric and date properties.
     *
     * @param property the property
     * @return a new component, or {@code null} if the property isn't supported
     */
    @Override
    public Component create(Property property) {
        Component result;
        if (property.isReadOnly() || property.isDerived()) {
            result = getReadOnlyFactory().create(property);
        } else {
            result = super.create(property);
        }
        return result;
    }

    /**
     * Create a component to display a property.
     *
     * @param property the property to display
     * @param context  the context object
     * @return a component to display {@code object}
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
                editor = createCollectionEditor((CollectionProperty) property, context);
            } else if (property.isObjectReference()) {
                editor = createObjectReferenceEditor(property, context);
            }
            if (editor != null) {
                result = new ComponentState(editor.getComponent(), property, editor.getFocusGroup());
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
     * @param context the object's parent. May be {@code null}
     */
    public ComponentState create(IMObject object, IMObject context) {
        getLayoutContext().setRendered(object);
        IMObjectEditor editor = getObjectEditor(object, context, getLayoutContext());
        return new ComponentState(editor.getComponent(), editor.getFocusGroup());
    }

    /**
     * Creates an editor for an {@link IMObject}.
     *
     * @param object the object to edit
     * @param parent the object's parent. May be {@code null}
     * @return a new editor for {@code object}
     */
    protected IMObjectEditor getObjectEditor(IMObject object, IMObject parent, LayoutContext context) {
        return IMObjectEditorFactory.create(object, parent, context);
    }

    /**
     * Returns an editor for a numeric property.
     *
     * @param property the numeric property
     * @return a new editor for {@code property}
     */
    protected Editor createNumericEditor(Property property) {
        Component component = createNumeric(property);
        return createPropertyEditor(property, component);
    }

    /**
     * Returns an editor for a date property.
     *
     * @param property the date property
     * @return a new editor for {@code property}
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
     * @return a new editor for {@code property}
     */
    protected Editor createLookupEditor(Property property, IMObject context) {
        Component component = LookupFieldFactory.create(property, context);
        return createPropertyEditor(property, component);
    }

    /**
     * Returns an editor for a boolean property.
     *
     * @param property the boolean property
     * @return a new editor for {@code property}
     */
    protected Editor createBooleanEditor(Property property) {
        Component component = createBoolean(property);
        return createPropertyEditor(property, component);
    }

    /**
     * Returns an editor for a text property.
     *
     * @param property the boolean property
     * @return a new editor for {@code property}
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
     * @return a new editor for {@code property}
     */
    protected Editor createCollectionEditor(CollectionProperty property, IMObject object) {
        Editor editor;
        if (property.isParentChild()) {
            LayoutContext context = getLayoutContext();
            HelpContext help = context.getHelpContext().subtopic(property.getName());
            LayoutContext subContext = new DefaultLayoutContext(context, help);

            if (property.getMinCardinality() == 1 && property.getMaxCardinality() == 1) {
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
//                    if (value != null) {
//                        editor = getObjectEditor(value, object, subContext);
//                        editors.add(editor, property);
//                    }
                }
            }
            editor = IMObjectCollectionEditorFactory.create(property, object, subContext);
        } else {
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            List<IMObject> identifiers = ArchetypeQueryHelper.getCandidates(service, property.getDescriptor());
            final String[] nodes = DescriptorHelper.getCommonNodeNames(
                    property.getDescriptor().getArchetypeRange(), IDENTIFIER_SORT_NODES, service);

            Component component;
            if (property.getMaxCardinality() == 1) {
                // render as a dropdown when at most one element can be selected
                IMObjectListModel model = new IMObjectListModel(identifiers, false, false);
                SelectField selectField = BoundSelectFieldFactory.create(property, model);
                selectField.setCellRenderer(IMObjectListCellRenderer.NAME);
                component = selectField;
            } else {
                // render as a palette when more than one object may be selected
                Palette<IMObject> palette = new BoundPalette<IMObject>(identifiers, property) {
                    @Override
                    protected void sort(List<IMObject> values) {
                        IMObjectSorter.sort(values, nodes);
                    }
                };
                palette.setCellRenderer(IMObjectListCellRenderer.NAME);
                component = palette;
            }
            editor = createPropertyEditor(property, component);
        }
        return editor;
    }

    /**
     * Returns an editor for an object reference property.
     *
     * @param property the object reference property
     * @param object   the parent object
     * @return a new editor for {@code property}
     */
    protected Editor createObjectReferenceEditor(Property property, IMObject object) {
        String[] range = property.getArchetypeRange();
        Editor editor;
        LayoutContext context = getLayoutContext();
        if (TypeHelper.matches(range, "document.*")) {
            editor = new DocumentEditor(property, context.getContext(), context.getHelpContext());
        } else {
            editor = IMObjectReferenceEditorFactory.create(property, object, context);
        }
        return editor;
    }

    /**
     * Creates a {@link PropertyEditor} for a component.
     *
     * @param property  the property
     * @param component the component
     * @return a new editor
     */
    protected PropertyEditor createPropertyEditor(Property property, Component component) {
        return new PropertyComponentEditor(property, component);
    }

    /**
     * Returns a factory for creating read-only components.
     *
     * @return a factory for creating read-only components
     */
    private ReadOnlyComponentFactory getReadOnlyFactory() {
        if (readOnly == null) {
            readOnly = new ReadOnlyComponentFactory(getLayoutContext(), Styles.EDIT);
        }
        return readOnly;
    }

}
