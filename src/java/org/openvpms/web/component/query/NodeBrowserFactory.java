package org.openvpms.web.component.query;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.app.Context;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.im.AbstractIMObjectComponentFactory;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.Messages;


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
        } else if (descriptor.isObjectReference()) {
            result = getObjectBrowser(object, descriptor);
        } else {
            Label label = LabelFactory.create();
            label.setText("No browser for type " + descriptor.getType());
            result = label;
        }
        result.setEnabled(enable);
        return result;
    }

    /**
     * Returns a browser for an object.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     * @return an component to display the object.
     */
    private Component getObjectBrowser(IMObject parent,
                                       NodeDescriptor descriptor) {
        Pointer pointer = getPointer(parent, descriptor);
        IMObjectReference ref = (IMObjectReference) pointer.getValue();
        IMObject value = null;
        if (ref != null) {
            value = Context.getInstance().getObject(ref);
            if (value == null) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                value = service.getById(ref.getArchetypeId(), ref.getUid());
            }
        }
        Label label = LabelFactory.create();
        if (value != null) {
            String text = Messages.get("imobject.summary",
                    value.getName(), value.getDescription());
            label.setText(text);
        }
        return label;
    }

    /**
     * Returns a component to display a collection.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     * @return a collection to display the node
     */
    private Component getCollectionBrowser(IMObject parent,
                                           NodeDescriptor descriptor) {
        return new CollectionBrowser(parent, descriptor);
    }

}
