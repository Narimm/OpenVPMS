package org.openvpms.web.app.editor;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.text.TextComponent;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.SelectFieldFactory;
import org.openvpms.web.component.TextComponentFactory;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.bound.BoundDateField;
import org.openvpms.web.component.im.IMObjectComponentFactory;
import org.openvpms.web.component.list.LookupListCellRenderer;
import org.openvpms.web.component.list.LookupListModel;
import org.openvpms.web.component.validator.NodeValidator;
import org.openvpms.web.component.validator.ValidatingPointer;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Factory for editors for {@link IMObject} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class NodeEditorFactory implements IMObjectComponentFactory {

    /**
     * The lookup service.
     */
    private ILookupService _lookup;

    /**
     * Create a component to display the supplied object.
     *
     * @param object     the object to display
     * @param descriptor the object's descriptor
     * @return a component to display <code>object</code>
     */
    public Component create(IMObject object, NodeDescriptor descriptor) {
        Component result;
        if (descriptor.isLookup()) {
            result = getSelectEditor(object, descriptor);
        } else if (descriptor.isBoolean()) {
            result = getBooleanEditor(object, descriptor);
        } else if (descriptor.isString()) {
            result = getTextEditor(object, descriptor);
        } else if (descriptor.isNumeric()) {
            // @todo - need validators
            result = getTextEditor(object, descriptor);
        } else if (descriptor.isDate()) {
            result = getDateEditor(object, descriptor);
        } else if (descriptor.isCollection()) {
            result = getCollectionEditor(object, descriptor);
        } else {
            Label label = LabelFactory.create();
            label.setText("No editor for type " + descriptor.getType());
            result = label;
        }
        if (descriptor.isReadOnly()) {
            result.setEnabled(false);
        }
        return result;
    }

    private Component getBooleanEditor(IMObject object,
                                       NodeDescriptor descriptor) {
        Pointer pointer = getPointer(object, descriptor);
        return new BoundCheckBox(pointer);
    }

    private Component getDateEditor(IMObject object,
                                    NodeDescriptor descriptor) {
        Pointer pointer = getPointer(object, descriptor);
        return new BoundDateField(pointer);
    }

    private Component getTextEditor(IMObject object,
                                    NodeDescriptor descriptor) {
        final int maxColumns = 32;
        TextComponent result;
        Pointer pointer = getPointer(object, descriptor);
        if (descriptor.isLarge()) {
            result = TextComponentFactory.createTextArea(pointer);
        } else {
            int columns = descriptor.getMaxLength();
            if (columns > maxColumns) {
                columns = maxColumns;
            }
            result = TextComponentFactory.create(pointer, columns);
        }
        return result;
    }

    private Component getSelectEditor(IMObject object,
                                      NodeDescriptor descriptor) {
        Pointer pointer = getPointer(object, descriptor);
        ListModel model = new LookupListModel(object, descriptor,
                getLookupService());
        SelectField field = SelectFieldFactory.create(pointer, model);
        field.setCellRenderer(new LookupListCellRenderer());
        return field;
    }

    private Component getCollectionEditor(IMObject object, NodeDescriptor descriptor) {
        return new CollectionEditor(object, descriptor);
    }

    private Pointer getPointer(IMObject object, NodeDescriptor descriptor) {
        Pointer pointer = object.pathToObject(descriptor.getPath());
        NodeValidator validator = new NodeValidator(descriptor);
        return new ValidatingPointer(pointer, validator);
    }

    private ILookupService getLookupService() {
        if (_lookup == null) {
            _lookup = ServiceHelper.getLookupService();
        }
        return _lookup;
    }


}
