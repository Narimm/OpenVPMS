package org.openvpms.web.component.query;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.im.AbstractIMObjectComponentFactory;


/**
 * Component factory that returns read-only components.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class NodeBrowserFactory extends AbstractIMObjectComponentFactory {

    /**
     * Create a component to display the supplied object.
     *
     * @param object     the object to display
     * @param descriptor the object's descriptor
     * @return a component to display <code>object</code>
     */
    public Component create(IMObject object, NodeDescriptor descriptor) {
        Component result;
        boolean enable = false;
        if (descriptor.isLookup()) {
            result = getLabel(object, descriptor);
        } else if (descriptor.isBoolean()) {
            result = getCheckBox(object, descriptor);
        } else if (descriptor.isString()) {
            result = getTextComponent(object, descriptor);
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

    /**
     * Returns a component to display a collection.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a collection to display the node
     */
    private Component getCollectionBrowser(IMObject object, NodeDescriptor descriptor) {
        return new CollectionBrowser(object, descriptor);
    }

}
