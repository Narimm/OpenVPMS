package org.openvpms.web.component.im;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.text.TextComponent;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.TextComponentFactory;
import org.openvpms.web.component.bound.BoundCheckBox;


/**
 * Abstract implementation of the {@link IMObjectComponentFactory} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectComponentFactory
        implements IMObjectComponentFactory {

    /**
     * Returns a label to display a node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a label to display the node
     */
    protected Label getLabel(IMObject object, NodeDescriptor descriptor) {
        Pointer pointer = getPointer(object, descriptor);
        Label label = LabelFactory.create();
        Object value = pointer.getValue();
        if (value != null) {
            label.setText(value.toString());
        }
        return label;
    }

    /**
     * Returns a check box to display a node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a check box to display the node
     */
    protected Component getCheckBox(IMObject object,
                                    NodeDescriptor descriptor) {
        Pointer pointer = getPointer(object, descriptor);
        return new BoundCheckBox(pointer);
    }

    /**
     * Returns a text component to display a node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a text field to display the node, or a text area if it is large
     */
    protected TextComponent getTextComponent(IMObject object,
                                             NodeDescriptor descriptor) {
        final int maxColumns = 32;
        TextComponent result;
        int columns = descriptor.getMaxLength();
        if (columns > maxColumns) {
            columns = maxColumns;
        }
        Pointer pointer = getPointer(object, descriptor);
        if (descriptor.isLarge()) {
            result = TextComponentFactory.createTextArea(pointer);
        } else {
            result = TextComponentFactory.create(pointer, columns);
        }
        return result;
    }

    /**
     * Helper to return a pointer to an attribute given its descriptor.
     *
     * @param object     the object that owne the attribute
     * @param descriptor the attribute's descriptor
     * @return a pointer to the attribute identified by <code>descriptor</code>.
     */
    protected Pointer getPointer(IMObject object, NodeDescriptor descriptor) {
        String path = descriptor.getPath();
        Pointer pointer = object.pathToObject(path);
        if (pointer == null) {
            throw new IllegalArgumentException(
                    "No path to " + path + " for object of type  "
                            + object.getClass().getName());
        }
        return pointer;
    }

}
