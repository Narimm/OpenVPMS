package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TextComponentFactory;


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
        Property property = getProperty(object, descriptor);
        Label label = LabelFactory.create();
        Object value = property.getValue();
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
        Property property = getProperty(object, descriptor);
        return new BoundCheckBox(property);
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
        final int maxDisplayLength = 50;
        int length = descriptor.getMaxLength();
        int maxColumns = (length < maxDisplayLength) ? length : maxDisplayLength;
        return getTextComponent(object, descriptor, maxColumns);
    }

    /**
     * Returns a text component to display a node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @param columns    the maximum no, of columns to display
     * @return a text field to display the node, or a text area if it is large
     */
    protected TextComponent getTextComponent(IMObject object,
                                             NodeDescriptor descriptor,
                                             int columns) {
        TextComponent result;
        Property property = getProperty(object, descriptor);
        if (descriptor.isLarge()) {
            result = TextComponentFactory.createTextArea(property, columns);
        } else {
            result = TextComponentFactory.create(property, columns);
        }
        return result;
    }

    /**
     * Helper to return a property given its descriptor.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected abstract Property getProperty(IMObject object,
                                            NodeDescriptor descriptor);

    /**
     * Helper to return a collection property given its descriptor.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected abstract CollectionProperty getCollectionProperty(
            IMObject object, NodeDescriptor descriptor);

}
