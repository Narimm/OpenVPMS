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
import org.openvpms.web.component.edit.ModifiableProperty;
import org.openvpms.web.component.edit.ModifiableSet;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.view.AbstractIMObjectComponentFactory;
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
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
     * Create a component to display an object.
     *
     * @param context    the context object
     * @param descriptor the object's descriptor
     * @return a component to display <code>object</code>
     */
    public Component create(IMObject context, NodeDescriptor descriptor) {
        Component result;
        if (descriptor.isLookup()) {
            result = getSelectEditor(context, descriptor);
        } else if (descriptor.isBoolean()) {
            result = getCheckBox(context, descriptor);
        } else if (descriptor.isString()) {
            result = getTextComponent(context, descriptor);
        } else if (descriptor.isNumeric()) {
            result = getNumericEditor(context, descriptor);
        } else if (descriptor.isDate()) {
            result = getDateEditor(context, descriptor);
        } else if (descriptor.isCollection()) {
            result = getCollectionEditor(context, descriptor);
        } else if (descriptor.isObjectReference()) {
            result = getObjectReferenceEditor(context, descriptor);
        } else {
            Label label = LabelFactory.create();
            label.setText("No editor for type " + descriptor.getType());
            result = label;
        }
        if (descriptor.isReadOnly() || descriptor.isDerived()) {
            result.setEnabled(false);
            result.setFocusTraversalParticipant(false);
        } else {
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
     * Returns a component to edit a numeric node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a component to edit the node
     */
    protected Component getNumericEditor(IMObject object,
                                         NodeDescriptor descriptor) {
        int maxColumns = 10;
        Property property = getProperty(object, descriptor);
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
     * Returns a component to edit a date node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a component to edit the node
     */
    protected Component getDateEditor(IMObject object,
                                      NodeDescriptor descriptor) {
        Property property = getProperty(object, descriptor);
        return DateFieldFactory.create(property);
    }

    /**
     * Returns a component to edit a lookup node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a component to edit the node
     */
    protected Component getSelectEditor(IMObject object,
                                        NodeDescriptor descriptor) {
        Property property = getProperty(object, descriptor);
        ListModel model = new LookupListModel(object, descriptor,
                                              getLookupService());
        SelectField field = SelectFieldFactory.create(property, model);
        field.setCellRenderer(new LookupListCellRenderer());
        return field;
    }

    /**
     * Returns a component to edit a collection node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a component to edit the node
     */
    protected Component getCollectionEditor(IMObject object, NodeDescriptor descriptor) {
        Component result;
        if (descriptor.isParentChild()) {
            CollectionEditor editor = new CollectionEditor(object, descriptor,
                                                           getLayoutContext());
            _modifiable.add(object, editor);
            result = editor.getComponent();
        } else {
            List<IMObject> identifiers = ArchetypeServiceHelper.getCandidateChildren(
                    ServiceHelper.getArchetypeService(),
                    descriptor, object);
            CollectionProperty property = getCollectionProperty(
                    object, descriptor);
            Palette palette = new BoundPalette(identifiers, property);
            palette.setCellRenderer(new IMObjectListCellRenderer());
            result = palette;
        }
        return result;
    }

    /**
     * Returns a component to edit an object reference.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a component to edit the node
     */
    protected Component getObjectReferenceEditor(IMObject object,
                                                 NodeDescriptor descriptor) {
        Property property = getProperty(object, descriptor);
        ObjectReferenceEditor editor
                = new ObjectReferenceEditor(property, descriptor);
        return editor.getComponent();
    }

    /**
     * Helper to return a property given its descriptor. This implementation
     * returns a {@link ModifiableProperty}.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected Property getProperty(IMObject object, NodeDescriptor descriptor) {
        ModifiableProperty property
                = new ModifiableProperty(object, descriptor);
        _modifiable.add(object, property);
        return property;
    }

    /**
     * Helper to return a collection property given its descriptor.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected CollectionProperty getCollectionProperty(
            IMObject object, NodeDescriptor descriptor) {
        return (CollectionProperty) getProperty(object, descriptor);
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
