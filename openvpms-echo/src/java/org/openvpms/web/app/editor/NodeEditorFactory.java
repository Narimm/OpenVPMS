package org.openvpms.web.app.editor;

import java.util.List;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.text.TextComponent;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.bound.BoundDateField;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.SelectFieldFactory;
import org.openvpms.web.component.TextComponentFactory;
import org.openvpms.web.component.model.LookupListModel;
import org.openvpms.web.app.editor.CollectionEditor;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class NodeEditorFactory {

    public static Component create(IMObject object, NodeDescriptor descriptor,
                                   ILookupService lookup) {
        Component result;
        if (descriptor.isLookup()) {
            result = getSelectEditor(object, descriptor, lookup);
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

    private static Component getBooleanEditor(IMObject object,
                                              NodeDescriptor descriptor) {
        Pointer pointer = object.pathToObject(descriptor.getPath());
        CheckBox checkBox = new BoundCheckBox(pointer);
        return checkBox;
    }

    private static Component getDateEditor(IMObject object,
                                           NodeDescriptor descriptor) {
        Pointer pointer = object.pathToObject(descriptor.getPath());
        BoundDateField result = new BoundDateField(pointer);
        return result;
    }

    private static Component getTextEditor(IMObject object,
                                           NodeDescriptor descriptor) {
        TextComponent result;
        Pointer pointer = object.pathToObject(descriptor.getPath());
        if (descriptor.isLarge()) {
            result = TextComponentFactory.createTextArea(pointer);
        } else {
            result = TextComponentFactory.create(pointer);
        }
        return result;
    }

    private static Component getSelectEditor(IMObject object,
                                             NodeDescriptor descriptor,
                                             ILookupService lookup) {
        Pointer pointer = object.pathToObject(descriptor.getPath());
        List<Lookup> values = lookup.get(descriptor, object);
        LookupListModel list = new LookupListModel(values,
                !descriptor.isRequired());
        SelectField result = SelectFieldFactory.create(pointer, list);
        return result;
    }


    private static Component getCollectionEditor(IMObject object, NodeDescriptor descriptor) {
        return new CollectionEditor(object, descriptor);
    }

}
