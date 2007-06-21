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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerEstimationItem</em>, <em>act.customerAccountInvoiceItem</em>,
 * or <em>act.supplierOrderItem</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ActItemEditor extends AbstractActEditor {

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
     * Returns a reference to the customer, obtained from the parent act.
     *
     * @return a reference to the customer or <code>null</code> if the act
     *         has no parent
     */
    public IMObjectReference getCustomer() {
        Act act = (Act) getParent();
        ActBean bean = new ActBean(act);
        return bean.getParticipantRef("participation.customer");
    }

    /**
     * Returns a reference to the product.
     *
     * @return a reference to the product, or <code>null</code> if the act has
     *         no product
     */
    public IMObjectReference getProduct() {
        return getParticipantRef("product");
    }

    /**
     * Sets the product.
     *
     * @param product a reference to the product.
     */
    public void setProduct(IMObjectReference product) {
        ProductParticipationEditor editor = getProductEditor();
        if (editor != null) {
            Property entity = editor.getProperty();
            entity.setValue(product);
        }
    }

    /**
     * Returns a reference to the patient.
     *
     * @return a reference to the patient, or <code>null</code> if the act
     *         has no patient
     */
    public IMObjectReference getPatient() {
        return getParticipantRef("patient");
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
        Editor patient = getEditor("patient");
        if (patient instanceof PatientParticipationEditor) {
            return (PatientParticipationEditor) patient;
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
        setFilter(filter);
        onLayout();
    }

    /**
     * Sets the node filter, used to lay out the act.
     *
     * @param filter the node filter. May be <code>null</code>
     */
    protected void setFilter(NodeFilter filter) {
        _filter = filter;
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
    @Override
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
            product.setPatient(patient.getProperty());
        }
    }

    /**
     * Act item layout strategy.
     */
    protected class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Returns a node filter to filter nodes.
         *
         * @param context the context
         * @return a node filter to filter nodes, or <code>null</code> if no
         *         filterering is required
         */
        @Override
        protected NodeFilter getNodeFilter(LayoutContext context) {
            return super.getNodeFilter(context, getFilter());
        }

    }
}
