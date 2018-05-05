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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.product.Product;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextArea;
import org.openvpms.web.system.ServiceHelper;

/**
 * Medication dispensing notes.
 * <p>
 * This renders a component when a product is a medication that has dispensing notes, and hides it if the product
 * doesn't.
 *
 * @author Tim Anderson
 */
public class DispensingNotes {

    /**
     * Usage notes text.
     */
    private SimpleProperty notes;

    /**
     * The component.
     */
    private ComponentState component;

    /**
     * Medication product usage notes node name.
     */
    private static final String USAGE_NOTES = "usageNotes";


    public DispensingNotes() {
        // create a property to hold the medication usage notes, if any.
        NodeDescriptor node = DescriptorHelper.getNode(ProductArchetypes.MEDICATION, USAGE_NOTES,
                                                       ServiceHelper.getArchetypeService());
        notes = new SimpleProperty(node.getName(), null, String.class, node.getDisplayName());
        notes.setMaxLength(node.getMaxLength());
        notes.setReadOnly(true);
    }

    /**
     * Sets the product.
     *
     * @param product the product. May be {@code null}
     */
    public void setProduct(Product product) {
        String text = null;
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            IMObjectBean bean = new IMObjectBean(product);
            text = bean.getString(USAGE_NOTES);
        }
        notes.setValue(text);
        if (component != null) {
            component.setVisible(text != null);
        }
    }

    /**
     * Determines if the product has dispensing notes.
     *
     * @return the product
     */
    public boolean hasNotes() {
        return notes.getString() != null;
    }

    /**
     * Creates a component to display the notes.
     * <br/>
     * If there are no notes, the component will not be visible.
     *
     * @param context the layout context
     * @return a new component
     */
    public ComponentState getComponent(LayoutContext context) {
        component = new ComponentState(context.getComponentFactory().create(notes), notes);
        Component field = component.getComponent();
        if (field instanceof TextArea) {
            TextArea text = (TextArea) field;
            text.setWidth(Styles.FULL_WIDTH);
        }
        component.setVisible(hasNotes());
        return component;
    }
}
