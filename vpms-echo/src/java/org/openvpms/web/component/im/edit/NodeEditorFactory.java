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

import java.text.Format;
import java.util.List;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.list.ListModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.bound.BoundPalette;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.ModifiableSet;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.view.AbstractIMObjectComponentFactory;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.palette.Palette;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Factory for editors for {@link IMObject} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NodeEditorFactory extends AbstractIMObjectComponentFactory {

    /**
     * The modification tracker;
     */
    private ModifiableSet _modifiable;

    /**
     * The lookup service.
     */
    private ILookupService _lookup;

    /**
     * Component factory for read-only/derived properties.
     */
    private IMObjectComponentFactory _readOnly;


    /**
     * Construct a new <code>NodeEditorFactory</code>.
     *
     * @param context    the layout context
     * @param modifiable the modification tracker
     */
    public NodeEditorFactory(LayoutContext context,
                             ModifiableSet modifiable) {
        super(context);
        _modifiable = modifiable;
    }

    /**
     * Create a component to display a property.
     *
     * @param property the property to display
     * @param context  the context object
     * @return a component to display <code>object</code>
     */
    public Component create(Property property, IMObject context) {
        Component result;
        NodeDescriptor descriptor = property.getDescriptor();
        if (descriptor.isReadOnly() || descriptor.isDerived()) {
            result = getReadOnlyFactory().create(property, context);
        } else {
            if (descriptor.isLookup()) {
                result = getSelectEditor(property, context);
            } else if (descriptor.isBoolean()) {
                result = getCheckBox(property);
            } else if (descriptor.isString()) {
                result = getTextComponent(property);
            } else if (descriptor.isNumeric()) {
                result = getNumericEditor(property);
            } else if (descriptor.isDate()) {
                result = getDateEditor(property);
            } else if (descriptor.isCollection()) {
                result = getCollectionEditor((CollectionProperty) property,
                                             context);
            } else if (descriptor.isObjectReference()) {
                result = getObjectReferenceEditor(property);
            } else {
                Label label = LabelFactory.create();
                label.setText("No editor for type " + descriptor.getType());
                result = label;
            }
            result.setFocusTraversalParticipant(true);
        }
        return result;
    }

    /**
     * Create a component to display an object.
     *
     * @param object     the object to display
     * @param context    the object's parent. May be <code>null</code>
     * @param descriptor the parent object's descriptor. May be
     *                   <code>null</code>
     */
    public Component create(IMObject object, IMObject context,
                            NodeDescriptor descriptor) {
        IMObjectEditor editor = IMObjectEditorFactory.create(object, context,
                                                             descriptor,
                                                             getLayoutContext());
        _modifiable.add(object, editor);
        return editor.getComponent();
    }

    /**
     * Returns a component to edit a numeric property.
     *
     * @param property the numeric property
     * @return a component to edit the property
     */
    protected Component getNumericEditor(Property property) {
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
        return text;
    }


    /**
     * Returns a component to edit a date property.
     *
     * @param property the date property
     * @return a component to edit the node
     */
    protected Component getDateEditor(Property property) {
        return DateFieldFactory.create(property);
    }

    /**
     * Returns a component to edit a lookup property.
     *
     * @param property the lookup property
     * @param context  the parent object
     * @return a component to edit the property
     */
    protected Component getSelectEditor(Property property, IMObject context) {
        ListModel model = new LookupListModel(context, property.getDescriptor(),
                                              getLookupService());
        SelectField field = SelectFieldFactory.create(property, model);
        field.setCellRenderer(new LookupListCellRenderer());
        return field;
    }

    /**
     * Returns a component to edit a collection property.
     *
     * @param property the collection property
     * @param object   the parent object
     * @return a component to edit the property
     */
    protected Component getCollectionEditor(CollectionProperty property,
                                            IMObject object) {
        Component result;
        NodeDescriptor descriptor = property.getDescriptor();
        if (descriptor.isParentChild()) {
            CollectionEditor editor = new CollectionEditor(property, object,
                                                           getLayoutContext());
            result = editor.getComponent();
        } else {
            List<IMObject> identifiers = ArchetypeServiceHelper.getCandidateChildren(
                    ServiceHelper.getArchetypeService(),
                    descriptor, object);
            Palette palette = new BoundPalette(identifiers, property);
            palette.setCellRenderer(new IMObjectListCellRenderer());
            result = palette;
        }
        return result;
    }

    /**
     * Returns a component to edit an object reference.
     *
     * @param property the object reference properrty
     * @return a component to edit the property
     */
    protected Component getObjectReferenceEditor(Property property) {
        ObjectReferenceEditor editor = new ObjectReferenceEditor(
                property, getLayoutContext());
        return editor.getComponent();
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
     * Helper to return the lookup service.
     *
     * @return the lookup service
     */
    private ILookupService getLookupService() {
        if (_lookup == null) {
            _lookup = ServiceHelper.getLookupService();
        }
        return _lookup;
    }

}
