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
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientParticipationEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccount*Item</em>, <em>act.customerEstimationItem</em>,
 * <em><em>act.supplierAccount*Item</em> or <em>act.supplierOrderItem</em>.
 *
 * @author Tim Anderson
 */
public abstract class ActItemEditor extends AbstractActEditor {

    /**
     * Current nodes to display. May be {@code null}.
     */
    private ArchetypeNodes nodes;

    /**
     * The product listener. May be {@code null}.
     */
    private ProductListener listener;

    /**
     * Constructs an {@link ActItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act.
     * @param context the layout context. May be {@code null}
     */
    public ActItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);

        if (act.isNew() && parent != null) {
            // default the act start time to that of the parent
            act.setActivityStartTime(parent.getActivityStartTime());
        }
    }

    /**
     * Returns a reference to the customer, obtained from the parent act.
     *
     * @return a reference to the customer or {@code null} if the act
     *         has no parent
     */
    public IMObjectReference getCustomerRef() {
        Act act = (Act) getParent();
        if (act != null) {
            ActBean bean = new ActBean(act);
            return bean.getParticipantRef("participation.customer");
        }
        return null;
    }

    /**
     * Returns a reference to the customer, obtained from the parent act.
     *
     * @return a reference to the customer or {@code null} if the act
     *         has no parent
     */
    public Party getCustomer() {
        return (Party) getObject(getCustomerRef());
    }

    /**
     * Returns a reference to the product.
     *
     * @return a reference to the product, or {@code null} if the act has
     *         no product
     */
    public IMObjectReference getProductRef() {
        return getParticipantRef("product");
    }

    /**
     * Returns the product.
     *
     * @return the product, or {@code null} if the act has no product
     */
    public Product getProduct() {
        return (Product) getObject(getProductRef());
    }

    /**
     * Sets the product.
     *
     * @param product the product. May be {@code null}
     */
    public void setProduct(Product product) {
        setProductRef(product != null ? product.getObjectReference() : null);
    }

    /**
     * Sets the product.
     *
     * @param product a reference to the product. May be {@code null}
     */
    public void setProductRef(IMObjectReference product) {
        setParticipant("product", product);
    }

    /**
     * Returns a reference to the patient.
     *
     * @return a reference to the patient, or {@code null} if the act has no patient
     */
    public Party getPatient() {
        return (Party) getObject(getPatientRef());
    }

    /**
     * Returns a reference to the patient.
     *
     * @return a reference to the patient, or {@code null} if the act
     *         has no patient
     */
    public IMObjectReference getPatientRef() {
        return getParticipantRef("patient");
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        setPatientRef(patient != null ? patient.getObjectReference() : null);
    }

    /**
     * Sets the patient.
     *
     * @param patient a reference to the patient. May be {@code null}
     */
    public void setPatientRef(IMObjectReference patient) {
        setParticipant("patient", patient);
    }

    /**
     * Returns a reference to the clinician.
     *
     * @return a reference to the clinician, or {@code null} if the act has
     *         no clinician
     */
    public User getClinician() {
        return (User) getObject(getClinicianRef());
    }

    /**
     * Returns a reference to the clinician.
     *
     * @return a reference to the clinician, or {@code null} if the act has
     *         no clinician
     */
    public IMObjectReference getClinicianRef() {
        return getParticipantRef("clinician");
    }

    /**
     * Sets the clinician.
     *
     * @param clinician a reference to the clinician. May be {@code null}
     */
    public void setClinician(User clinician) {
        setClinicianRef(clinician != null ? clinician.getObjectReference() : null);
    }

    /**
     * Sets the clinician.
     *
     * @param clinician a reference to the clinician. May be {@code null}
     */
    public void setClinicianRef(IMObjectReference clinician) {
        setParticipant("clinician", clinician);
    }

    /**
     * Sets the product quantity.
     *
     * @param quantity the product quantity
     */
    public void setQuantity(BigDecimal quantity) {
        getProperty("quantity").setValue(quantity);
    }

    /**
     * Returns the product quantity.
     *
     * @return the product quantity
     */
    public BigDecimal getQuantity() {
        BigDecimal value = (BigDecimal) getProperty("quantity").getValue();
        return (value != null) ? value : BigDecimal.ZERO;
    }

    /**
     * Sets the fixed price.
     *
     * @param fixedPrice the fixed price
     */
    public void setFixedPrice(BigDecimal fixedPrice) {
        getProperty("fixedPrice").setValue(fixedPrice);
    }

    /**
     * Sets the unit price.
     *
     * @param unitPrice the unit price
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        getProperty("unitPrice").setValue(unitPrice);
    }

    /**
     * Returns the unit price.
     *
     * @return the unit price
     */
    public BigDecimal getUnitPrice() {
        BigDecimal value = (BigDecimal) getProperty("unitPrice").getValue();
        return (value != null) ? value : BigDecimal.ZERO;
    }

    /**
     * Sets the discount.
     *
     * @param discount the discount
     */
    public void setDiscount(BigDecimal discount) {
        getProperty("discount").setValue(discount);
    }

    /**
     * Sets the listener for product change events.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setProductListener(ProductListener listener) {
        this.listener = listener;
    }

    /**
     * Invoked when the participation product is changed.
     * <p/>
     * This delegates to {@link #productModified(Product)}.
     *
     * @param participation the product participation instance
     */
    protected void productModified(Participation participation) {
        Product product = (Product) getObject(participation.getEntity());
        productModified(product);
    }

    /**
     * Invoked when the product is changed.
     * <p/>
     * This implementation is a no-op.
     *
     * @param product the product. May be {@code null}
     */
    protected void productModified(Product product) {
    }

    /**
     * Notify any registered {@link ProductListener} of a change in product.
     *
     * @param product the product. May be {@code null}
     */
    protected void notifyProductListener(Product product) {
        if (listener != null) {
            listener.productChanged(this, product);
        }
    }

    /**
     * Returns the first price with the specified short name.
     *
     * @param shortName the price short name
     * @param product   the product
     * @return the corresponding product price, or {@code null} if none exists
     */
    protected ProductPrice getProductPrice(String shortName, Product product) {
        ProductPriceRules rules = new ProductPriceRules();
        return rules.getProductPrice(product, shortName, getStartTime());
    }

    /**
     * Returns the product editor.
     *
     * @return the product editor, or {@code null} if none exists
     */
    protected ProductParticipationEditor getProductEditor() {
        return getProductEditor(true);
    }

    /**
     * Returns the product editor.
     *
     * @param create if {@code true} force creation of the edit components if
     *               it hasn't already been done
     * @return the product editor, or {@code null} if none exists
     */
    protected ProductParticipationEditor getProductEditor(boolean create) {
        ParticipationEditor<Product> editor = getParticipationEditor("product",
                                                                     create);
        return (ProductParticipationEditor) editor;
    }

    /**
     * Returns the patient editor.
     *
     * @return the patient editor, or {@code null}  if none exists
     */
    protected PatientParticipationEditor getPatientEditor() {
        return getPatientEditor(true);
    }

    /**
     * Returns the patient editor.
     *
     * @param create if {@code true} force creation of the edit components if
     *               it hasn't already been done
     * @return the patient editor, or {@code null} if none exists
     */
    protected PatientParticipationEditor getPatientEditor(boolean create) {
        ParticipationEditor<Party> editor
                = getParticipationEditor("patient", create);
        return (PatientParticipationEditor) editor;
    }

    /**
     * Returns the clinician editor.
     *
     * @return the clinician editor, or {@code null}  if none exists
     */
    protected ClinicianParticipationEditor getClinicianEditor() {
        return getClinicianEditor(true);
    }

    /**
     * Returns the clinician editor.
     *
     * @param create if {@code true} force creation of the edit components if
     *               it hasn't already been done
     * @return the clinician editor, or {@code null}  if none exists
     */
    protected ClinicianParticipationEditor getClinicianEditor(boolean create) {
        ParticipationEditor<User> editor = getParticipationEditor("clinician",
                                                                  create);
        return (ClinicianParticipationEditor) editor;
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
     * @param nodes the nodes to display
     */
    protected void changeLayout(ArchetypeNodes nodes) {
        setArchetypeNodes(nodes);
        onLayout();
    }

    /**
     * Sets the nodes to display.
     *
     * @param nodes the nodes. May be {@code null}
     */
    protected void setArchetypeNodes(ArchetypeNodes nodes) {
        this.nodes = nodes;
    }

    /**
     * Returns the nodes to display.
     *
     * @return the nodes. May be {@code null}
     */
    protected ArchetypeNodes getArchetypeNodes() {
        return nodes;
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        final ProductParticipationEditor product = getProductEditor();
        final PatientParticipationEditor patient = getPatientEditor();
        if (product != null) {
            final Participation participant = product.getParticipation();
            product.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    productModified(participant);
                }
            });
        }
        if (patient != null && product != null) {
            product.setPatient(patient.getEntity());
            patient.getEditor().addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    product.setPatient(patient.getEntity());
                }
            });
        }
    }

    /**
     * Act item layout strategy.
     */
    protected class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
         *
         * @return the archetype nodes
         */
        @Override
        protected ArchetypeNodes getArchetypeNodes() {
            return nodes != null ? nodes : super.getArchetypeNodes();
        }
    }
}
