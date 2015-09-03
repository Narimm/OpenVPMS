/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.layout.PrintObjectLayoutStrategy;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.ReadOnlyProperty;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextArea;

import java.util.List;


/**
 * Layout strategy that includes a 'Print Label' button to print the act.
 */
public class PatientMedicationActLayoutStrategy extends PrintObjectLayoutStrategy {

    /**
     * Determines if the date node should be displayed read-only.
     */
    private boolean showDateReadOnly;

    /**
     * Determines if the product node should be displayed. False if
     * the parent act has a product. Ignored if {@code showProductReadOnly}
     * is {@code true}
     */
    private boolean showProduct;

    /**
     * Determines if the product node should be displayed read-only.
     */
    private boolean showProductReadOnly;

    /**
     * Determines if the medication was dispensed from a prescription.
     * If so, then the quantity and label node should be displayed read-only.
     */
    private boolean prescription = false;

    /**
     * A component to display usage notes. May be {@code null}.
     */
    private Component usageNotes;

    /**
     * The nodes to display.
     */
    private ArchetypeNodes nodes;

    /**
     * Factory for read-only components.
     */
    private ReadOnlyComponentFactory factory;

    /**
     * The product node.
     */
    static final String PRODUCT = "product";

    /**
     * The quantity node.
     */
    static final String QUANTITY = "quantity";

    /**
     * The notes node.
     */
    static final String LABEL = "label";


    /**
     * Constructs a {@code PatientMedicationActLayoutStrategy}.
     */
    public PatientMedicationActLayoutStrategy() {
        super("button.printlabel");
    }

    /**
     * Determines if the date should be displayed read-only.
     *
     * @param readOnly if {@code true} display the date read-only.
     */
    public void setDateReadOnly(boolean readOnly) {
        showDateReadOnly = readOnly;
    }

    /**
     * Determines if the product should be displayed read-only.
     *
     * @param readOnly if {@code true} display the product read-only.
     */
    public void setProductReadOnly(boolean readOnly) {
        showProduct = true;
        showProductReadOnly = readOnly;
    }

    /**
     * Determines if the medication was dispensed from a prescription.
     * If {@code true}, then the quantity and label should be displayed read-only.
     *
     * @param prescription if {@code true} display the quantity and label read-only
     */
    public void setDispensedFromPrescription(boolean prescription) {
        this.prescription = prescription;
    }

    /**
     * Registers a component to display usage notes.
     * <p/>
     * If set, this is displayed immediately after the simple properties.
     *
     * @param notes the usage notes. May be {@code null}
     */
    public void setUsageNotes(Component notes) {
        usageNotes = notes;
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        ComponentState result;
        try {
            nodes = new ArchetypeNodes().exclude(LABEL);
            if (!showProduct) {
                nodes.exclude(PRODUCT);
            }

            if (!showProductReadOnly) {
                if (parent instanceof Act) {
                    ActBean bean = new ActBean((Act) parent);
                    showProduct = !bean.hasNode(PRODUCT);
                } else {
                    showProduct = true;
                }
            } else {
                addComponent(getReadOnlyComponent(properties.get(PRODUCT), parent, context));
            }

            Property label = properties.get(LABEL);
            if (prescription) {
                addComponent(getReadOnlyComponent(properties.get(QUANTITY), parent, context));
                label = new ReadOnlyProperty(label);
            }
            addComponent(createNotes(parent, label, context));
            result = super.apply(object, properties, parent, context);
        } finally {
            factory = null;
        }
        return result;
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties,
                                  Component container, LayoutContext context) {
        super.doSimpleLayout(object, parent, properties, container, context);
        if (usageNotes != null) {
            container.add(ColumnFactory.create(Styles.INSET_X, usageNotes));
        }
    }

    /**
     * Lays out components in a grid.
     *
     * @param object     the object to lay out
     * @param properties the properties
     * @param context    the layout context
     * @param columns    the no. of columns to use
     */
    @Override
    protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context, int columns) {
        ComponentGrid grid = super.createGrid(object, properties, context, columns);
        grid.add(getComponent(LABEL), columns);
        return grid;
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return nodes;
    }

    /**
     * Creates a component for a note node.
     *
     * @param property the property
     * @param object   the parent object
     * @param context  the layout context
     * @return a new component
     */
    protected ComponentState createNotes(IMObject object, Property property, LayoutContext context) {
        ComponentState notes = createComponent(property, object, context);
        Component component = notes.getComponent();
        if (component instanceof TextArea) {
            TextArea text = (TextArea) component;
            text.setWidth(Styles.FULL_WIDTH);
        }
        return notes;
    }

    /**
     * Helper to return a read-only component. This uses an {@link ReadOnlyComponentFactory} rather than the default
     * factory as it renders differently (fields aren't greyed out).
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a read-only component to display the property
     */
    private ComponentState getReadOnlyComponent(Property property, IMObject parent, LayoutContext context) {
        if (factory == null) {
            factory = new ReadOnlyComponentFactory(context);
        }
        return factory.create(property, parent);
    }

}
