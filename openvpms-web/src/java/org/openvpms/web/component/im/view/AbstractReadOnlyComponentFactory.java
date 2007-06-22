/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.view;

import echopointng.RichTextArea;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.doc.DocumentViewer;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.LabelFactory;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractReadOnlyComponentFactory
        extends AbstractIMObjectComponentFactory {

    /**
     * The layout strategy factory.
     */
    private final IMObjectLayoutStrategyFactory _strategies;


    /**
     * Construct a new <tt>AbstractReadOnlyComponentFactory</tt>.
     *
     * @param context    the layout context
     * @param strategies the layout strategy factory
     */
    public AbstractReadOnlyComponentFactory(
            LayoutContext context, IMObjectLayoutStrategyFactory strategies) {
        super(context);
        _strategies = strategies;
    }

    /**
     * Create a component to display a property.
     *
     * @param property the property to display
     * @param context  the context object
     * @return a component to display <tt>object</tt>
     */
    public ComponentState create(Property property, IMObject context) {
        Component component = null;
        boolean enable = false;
        if (!property.isLookup()) {
            component = create(property); // isString() returns true for lookups
        }
        if (component == null) {
            // not a simple property
            if (property.isLookup()) {
                component = createLookup(property, context);
            } else if (property.isCollection()) {
                component = getCollectionViewer((CollectionProperty) property,
                                                context);
                // need to enable this otherwise table selection is disabled
                enable = true;
            } else if (property.isObjectReference()) {
                component = getObjectViewer(property, context);
                // need to enable this for hyperlinks to work
                enable = true;
            } else {
                Label label = LabelFactory.create();
                label.setText("No viewer for type " + property.getType());
                component = label;
            }
        }
        if (component instanceof RichTextArea) {
            ((RichTextArea) component).setEditable(enable);
        }
        component.setEnabled(enable);
        component.setFocusTraversalParticipant(false);
        return new ComponentState(component, property);
    }

    /**
     * Create a component to display an object.
     *
     * @param object  the object to display
     * @param context the object's parent. May be <tt>null</tt>
     * @return a component to display <tt>object</tt>
     */
    public ComponentState create(IMObject object, IMObject context) {
        IMObjectLayoutStrategy strategy = _strategies.create(object, context);
        IMObjectViewer viewer = new IMObjectViewer(object, context, strategy,
                                                   getLayoutContext());
        return new ComponentState(viewer.getComponent(),
                                  viewer.getFocusGroup());
    }

    /**
     * Returns a component to display a lookup property.
     *
     * @param property the lookup property
     * @param context  the context object
     * @return a component to display the property
     */
    protected abstract Component createLookup(Property property,
                                              IMObject context);

    /**
     * Returns a viewer for an object reference.
     *
     * @param property the object reference property
     * @param context  the context object
     * @return an component to display the object reference.
     */
    protected Component getObjectViewer(Property property, IMObject context) {
        IMObjectReference ref = (IMObjectReference) property.getValue();
        boolean link = true;
        if (getLayoutContext().isEdit()) {
            // disable hyperlinks if an edit is in progress.
            link = false;
        }
        String[] range = property.getArchetypeRange();
        if (TypeHelper.matches(range, "document.*")) {
            return new DocumentViewer(ref, context, link).getComponent();
        }

        return new IMObjectReferenceViewer(ref, link).getComponent();
    }

    /**
     * Returns a component to display a collection.
     *
     * @param parent the parent object
     * @return a collection to display the node
     */
    protected Component getCollectionViewer(CollectionProperty property,
                                            IMObject parent) {
        Component result = null;
        if (property.getMaxCardinality() == 1) {
            // handle the special case of a collection of one element.
            // This can be viewed inline
            String[] shortNames = property.getArchetypeRange();
            if (shortNames.length == 1) {
                Object[] values = property.getValues().toArray();
                IMObject value;
                if (values.length > 0) {
                    value = (IMObject) values[0];
                    result = create(value, parent).getComponent();
                } else {
                    // nothing to display, so return an empty label
                    result = LabelFactory.create();
                }
            }
        }
        if (result == null) {
            IMObjectCollectionViewer viewer
                    = IMObjectCollectionViewerFactory.create(
                    property, parent, getLayoutContext());
            result = viewer.getComponent();
        }
        return result;
    }

    /**
     * Helper to return a lookup name, given its code.
     *
     * @param property the property to use
     * @param context  the context object
     * @return the lookup name, or <tt>null</tt> if it can't be found
     * @throws OpenVPMSException for any error
     */
    protected String getLookupName(Property property, IMObject context) {
        NodeDescriptor descriptor
                = ((IMObjectProperty) property).getDescriptor();

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        return LookupHelper.getName(service, descriptor, context);
    }


}
