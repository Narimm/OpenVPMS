package org.openvpms.web.component.im.view;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.ReadOnlyProperty;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ReadOnlyComponentFactory extends AbstractIMObjectComponentFactory {

    /**
     * Create a component to display the an object.
     *
     * @param context    the context object
     * @param descriptor the object's descriptor
     * @return a component to display <code>object</code>
     */
    public Component create(IMObject context, NodeDescriptor descriptor) {
        Component result;
        boolean enable = false;
        if (descriptor.isLookup()) {
            result = getLabel(context, descriptor);
        } else if (descriptor.isBoolean()) {
            result = getCheckBox(context, descriptor);
        } else if (descriptor.isString()) {
            result = getTextComponent(context, descriptor);
        } else if (descriptor.isNumeric()) {
            result = getNumericField(context, descriptor);
        } else if (descriptor.isDate()) {
            result = getDateField(context, descriptor);
        } else if (descriptor.isCollection()) {
            result = getCollectionViewer(context, descriptor);
            // need to enable this otherwise table selection is disabled
            enable = true;
        } else if (descriptor.isObjectReference()) {
            result = getObjectViewer(context, descriptor);
        } else {
            Label label = LabelFactory.create();
            label.setText("No viewer for type " + descriptor.getType());
            result = label;
        }
        result.setEnabled(enable);
        result.setFocusTraversalParticipant(false);
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
        IMObjectViewer viewer = new IMObjectViewer(object);
        return viewer.getComponent();
    }

    /**
     * Helper to return a property given its descriptor.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected Property getProperty(IMObject object, NodeDescriptor descriptor) {
        return new ReadOnlyProperty(object, descriptor);
    }

    /**
     * Returns a component to display a number.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    private Component getNumericField(IMObject context, NodeDescriptor descriptor) {
        Property property = getProperty(context, descriptor);
        Label label = LabelFactory.create();
        Number value = (Number) property.getValue();
        if (value == null) {
            value = new BigDecimal(0);
        }

        try {
            // @todo - potential loss of precision here as NumberFormat converts
            // BigDecimal to double before formatting
            Locale locale = ApplicationInstance.getActive().getLocale();
            NumberFormat format = NumberFormat.getInstance(locale);
            label.setText(format.format(value));
        } catch (IllegalArgumentException exception) {
            label.setText(value.toString());
        }
        return label;
    }

    /**
     * Returns a component to display a date.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    private Component getDateField(IMObject context,
                                   NodeDescriptor descriptor) {
        Property property = getProperty(context, descriptor);
        Label label = LabelFactory.create();
        Date value = (Date) property.getValue();
        if (value != null) {
            Locale locale = ApplicationInstance.getActive().getLocale();
            DateFormat format = DateFormat.getDateInstance(
                    DateFormat.DEFAULT, locale);
            label.setText(format.format(value));
        }
        return label;
    }

    /**
     * Returns a viewer for an object.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     * @return an component to display the object.
     */
    private Component getObjectViewer(IMObject parent,
                                      NodeDescriptor descriptor) {
        Property property = getProperty(parent, descriptor);
        IMObjectReference ref = (IMObjectReference) property.getValue();
        IMObject value = IMObjectHelper.getObject(ref);
        Label label = LabelFactory.create();

        if (value != null) {
            String text = Messages.get("imobject.name", value.getName());
            label.setText(text);
        } else {
            label.setText(Messages.get("imobject.none"));
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
    private Component getCollectionViewer(IMObject parent,
                                          NodeDescriptor descriptor) {
        return new CollectionViewer(parent, descriptor);
    }

}
