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
import org.openvpms.web.component.im.IMObjectComponentFactory;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class NodeBrowserFactory implements IMObjectComponentFactory {

    public Component create(IMObject object, NodeDescriptor descriptor) {
        Component result;
        boolean enable = false;
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
        } else if (descriptor.isCollection()) {
            result = getCollectionBrowser(object, descriptor);
            // need to enable this otherwise table selection is disabled
            enable = true;
        } else {
            Label label = LabelFactory.create();
            label.setText("No browser for type " + descriptor.getType());
            result = label;
        }
        result.setEnabled(enable);
        return result;
    }


    private Component getLabel(IMObject object, NodeDescriptor descriptor) {
        Pointer pointer = object.pathToObject(descriptor.getPath());
        Label label = LabelFactory.create();
        Object value = pointer.getValue();
        if (value != null) {
            label.setText(value.toString());
        }
        return label;
    }

    private Component getCheckBox(IMObject object, NodeDescriptor descriptor) {
        Pointer pointer = object.pathToObject(descriptor.getPath());
        Boolean value = (Boolean) pointer.getValue();
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(value);
        return checkBox;
    }

    private Component getTextField(IMObject object,
                                   NodeDescriptor descriptor) {
        final int maxColumns = 32;
        TextComponent result;
        Pointer pointer = object.pathToObject(descriptor.getPath());
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

    private Component getCollectionBrowser(IMObject object, NodeDescriptor descriptor) {
        return new CollectionBrowser(object, descriptor);
    }


}
