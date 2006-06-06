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

package org.openvpms.web.component.im.edit.act;

import java.math.BigDecimal;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerEstimationItem</em>, <em>act.customerAccountInvoiceItem</em>,
 * or <em>act.supplierOrderItem</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ActItemEditor extends AbstractIMObjectEditor {

    /**
     * Current node filter. May be <code>null</code>
     */
    private NodeFilter _filter;


    /**
     * Construct a new <code>ActItemEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent act.
     * @param context the layout context. May be <code>null</code>
     */
    public ActItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Returns a reference to the product.
     *
     * @return a reference to the product, or <code>null</code> if the act has
     *         no product
     */
    public IMObjectReference getProduct() {
        Editor product = getEditor("product");
        if (product instanceof ProductParticipationEditor) {
            ProductParticipationEditor editor
                    = (ProductParticipationEditor) product;
            return (IMObjectReference) editor.getEntity().getValue();
        }
        return null;
    }

    /**
     * Sets the product.
     *
     * @param product a reference to the product.
     */
    public void setProduct(IMObjectReference product) {
        ProductParticipationEditor editor = getProductEditor();
        if (editor != null) {
            Property entity = editor.getEntity();
            entity.setValue(product);
        }
    }

    /**
     * Sets the product quantity.
     *
     * @param quantity the product quantity
     */
    public abstract void setQuantity(BigDecimal quantity);

    /**
     * Invoked when the participation product is changed, to update prices.
     *
     * @param participation the product participation instance
     */
    protected abstract void productModified(Participation participation);

    /**
     * Helper to return a product price from a product.
     *
     * @param shortName the price short name
     * @param product   the product
     * @return the price corresponding to  <code>shortName</code> or
     *         <code>null</code> if none exists
     */
    protected ProductPrice getPrice(String shortName, Product product) {
        return IMObjectHelper.getObject(shortName, product.getProductPrices());
    }

    /**
     * Returns the product editor.
     *
     * @return the product editor, or <code>null</code> if none exists
     */
    protected ProductParticipationEditor getProductEditor() {
        Editor product = getEditor("product");
        if (product instanceof ProductParticipationEditor) {
            return (ProductParticipationEditor) product;
        }
        return null;
    }

    /**
     * Returns the patient editor.
     *
     * @return the patient editor, or <code>null</code>  if none exists
     */
    protected PatientParticipationEditor getPatientEditor() {
        Editor product = getEditor("patient");
        if (product instanceof PatientParticipationEditor) {
            return (PatientParticipationEditor) product;
        }
        return null;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Change the layout of the act.
     *
     * @param filter the node filter to use
     */
    protected void changeLayout(NodeFilter filter) {
        _filter = filter;
        onLayout();
    }

    /**
     * Returns the current node filter, used to lay out the act.
     *
     * @return the current node filter. May be <code>null</code>
     */
    protected NodeFilter getFilter() {
        return _filter;
    }

    /**
     * Invoked when layout has completed.
     */
    protected void onLayoutCompleted() {
        final ProductParticipationEditor product = getProductEditor();
        PatientParticipationEditor patient = getPatientEditor();
        if (product != null) {
            final Participation participant = product.getParticipation();
            product.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    productModified(participant);
                }
            });
        }
        if (product != null && patient != null) {
            product.setPatient(patient.getEntity());
        }
    }

    /**
     * Act item layout strategy.
     */
    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Apply the layout strategy.
         * <p/>
         * This renders an object in a <code>Component</code>, using a factory
         * to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param context    the layout context
         * @return the component containing the rendered <code>object</code>
         */
        @Override
        public Component apply(IMObject object, PropertySet properties,
                               LayoutContext context) {
            Component component = super.apply(object, properties, context);
            onLayoutCompleted();
            return component;
        }

        /**
         * Creates a component for a property.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display <code>property</code>
         */
        @Override
        protected Component createComponent(Property property, IMObject parent,
                                            LayoutContext context) {
            Component component = super.createComponent(property, parent,
                                                        context);
            String name = property.getDescriptor().getName();
            if (name.equals("lowTotal") || name.equals("highTotal")
                || name.equals("total")) {
                // @todo - workaround for OVPMS-211
                component.setEnabled(false);
                component.setFocusTraversalParticipant(false);
                if (component instanceof TextComponent) {
                    Alignment align = new Alignment(Alignment.RIGHT,
                                                    Alignment.DEFAULT);
                    ((TextComponent) component).setAlignment(align);
                }
            }
            return component;
        }

    }
}
