package org.openvpms.web.component.im.view;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.ReadOnlyProperty;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.LabelFactory;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractReadOnlyComponentFactory
        extends AbstractIMObjectComponentFactory {

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(ReadOnlyComponentFactory.class);

    /**
     * Construct a new <code>AbstractReadOnlyComponentFactory</code>.
     *
     * @param context the layout context.
     */
    public AbstractReadOnlyComponentFactory(LayoutContext context) {
        super(context);
    }

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
            result = getLookup(context, descriptor);
        } else if (descriptor.isBoolean()) {
            result = getBoolean(context, descriptor);
        } else if (descriptor.isString()) {
            result = getTextComponent(context, descriptor);
        } else if (descriptor.isNumeric()) {
            result = getNumber(context, descriptor);
        } else if (descriptor.isDate()) {
            result = getDate(context, descriptor);
        } else if (descriptor.isCollection()) {
            result = getCollectionViewer(context, descriptor);
            // need to enable this otherwise table selection is disabled
            enable = true;
        } else if (descriptor.isObjectReference()) {
            result = getObjectViewer(context, descriptor);
            // need to enable this for hyperlinks to work
            enable = true;
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
     * Helper to return a collection property given its descriptor.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected CollectionProperty getCollectionProperty(
            IMObject object, NodeDescriptor descriptor) {
        return new ReadOnlyProperty(object, descriptor);
    }

    /**
     * Returns a component to display a lookup.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the lookup
     */
    protected abstract Component getLookup(IMObject context,
                                           NodeDescriptor descriptor);

    /**
     * Returns a component to display a boolean.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a componnet to display the boolean
     */
    protected Component getBoolean(IMObject context,
                                   NodeDescriptor descriptor) {
        return getCheckBox(context, descriptor);
    }

    /**
     * Returns a component to display a number.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected abstract Component getNumber(IMObject context,
                                           NodeDescriptor descriptor);

    /**
     * Returns a component to display a date.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected abstract Component getDate(IMObject context,
                                         NodeDescriptor descriptor);

    /**
     * Returns a viewer for an object.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     * @return an component to display the object.
     */
    protected Component getObjectViewer(IMObject parent,
                                        NodeDescriptor descriptor) {
        Property property = getProperty(parent, descriptor);
        IMObjectReference ref = (IMObjectReference) property.getValue();
        boolean link = true;
        if (getLayoutContext().isEdit()) {
            // disable hyperlinks if an edit is in progress.
            link = false;
        }
        return new IMObjectReferenceViewer(ref, link).getComponent();
    }

    /**
     * Returns a component to display a collection.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     * @return a collection to display the node
     */
    protected Component getCollectionViewer(IMObject parent,
                                            NodeDescriptor descriptor) {
        return new CollectionViewer(parent, descriptor);
    }

    /**
     * Helper to convert a numeric property to string.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return the string value of the property associated with
     *         <code>descriptor</code>
     */
    protected String getNumericValue(IMObject context,
                                     NodeDescriptor descriptor) {
        String result;
        Property property = getProperty(context, descriptor);
        Object object = property.getValue();
        Number value;
        if (object instanceof Number) {
            value = (Number) object;
        } else if (object instanceof String) {
            // @todo workaround for OVPMS-228
            try {
                value = new BigDecimal((String) object);
            } catch (NumberFormatException exception) {
                _log.error("Invalid number for " + descriptor.getName() + ": "
                           + object);
                value = new BigDecimal(0);
            }
        } else {
            if (object != null) {
                _log.error("Invalid number for " + descriptor.getName() + ": "
                           + object);
            }
            value = new BigDecimal(0);
        }

        try {
            // @todo - potential loss of precision here as NumberFormat converts
            // BigDecimal to double before formatting
            Locale locale = ApplicationInstance.getActive().getLocale();
            NumberFormat format = NumberFormat.getInstance(locale);
            result = format.format(value);
        } catch (IllegalArgumentException exception) {
            result = value.toString();
        }
        return result;
    }

    /**
     * Helper to convert a date value to a string.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected String getDateValue(IMObject context,
                                  NodeDescriptor descriptor) {
        String result;
        Property property = getProperty(context, descriptor);
        Date value = (Date) property.getValue();
        if (value != null) {
            Locale locale = ApplicationInstance.getActive().getLocale();
            DateFormat format = DateFormat.getDateInstance(
                    DateFormat.DEFAULT, locale);
            result = format.format(value);
        } else {
            result = null;
        }
        return result;
    }


}
