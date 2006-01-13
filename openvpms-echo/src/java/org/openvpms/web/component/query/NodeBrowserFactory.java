package org.openvpms.web.component.query;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.text.TextComponent;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.TextComponentFactory;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class NodeBrowserFactory {

    public static Component create(IMObject object, NodeDescriptor descriptor) {
        Component result;
        if (descriptor.isLookup()) {
            result = getLabel(object, descriptor);
        } else if (descriptor.isBoolean()) {
            result = getCheckBox(object, descriptor);
        } else if (descriptor.isString()) {
            result = getTextField(object, descriptor);
        } else if (descriptor.isNumeric()) {
            result = getLabel(object, descriptor);
        } else if (descriptor.isDate()) {
            result = getLabel(object, descriptor);
        } else {
            Label label = LabelFactory.create();
            label.setText("No browser for type " + descriptor.getType());
            result = label;
        }
        result.setEnabled(false);
        return result;
    }


    private static Component getLabel(IMObject object, NodeDescriptor descriptor) {
        Pointer pointer = object.pathToObject(descriptor.getPath());
        Label label = LabelFactory.create();
        Object value = pointer.getValue();
        if (value != null) {
            label.setText(value.toString());
        }
        return label;
    }

    private static Component getCheckBox(IMObject object, NodeDescriptor descriptor) {
        Pointer pointer = object.pathToObject(descriptor.getPath());
        Boolean value = (Boolean) pointer.getValue();
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(value);
        return checkBox;
    }


    private static Component getTextField(IMObject object,
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

}
