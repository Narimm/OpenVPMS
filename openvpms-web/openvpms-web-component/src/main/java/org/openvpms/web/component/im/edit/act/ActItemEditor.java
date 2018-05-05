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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.clinician.ClinicianParticipationEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientParticipationEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;

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
     * The default patient node name.
     */
    public static final String PATIENT = "patient";

    /**
     * The default product node name.
     */
    public static final String PRODUCT = "product";

    /**
     * The default template node name.
     */
    public static final String TEMPLATE = "template";

    /**
     * The default quantity node name.
     */
    public static final String QUANTITY = "quantity";

    /**
     * The default clinician node name.
     */
    public static final String CLINICIAN = "clinician";

    /**
     * The default fixed price node name.
     */
    public static final String FIXED_PRICE = "fixedPrice";

    /**
     * The default unit price node name.
     */
    public static final String UNIT_PRICE = "unitPrice";

    /**
     * The default discount node name.
     */
    public static final String DISCOUNT = "discount";

    /**
     * The default location node name.
     */
    public static final String LOCATION = "location";

    /**
     * The product price rules.
     */
    private final ProductPriceRules rules;

    /**
     * Current nodes to display. May be {@code null}.
     */
    private ArchetypeNodes nodes;

    /**
     * The product listener. May be {@code null}.
     */
    private ProductListener listener;

    /**
     * The practice location.
     */
    private final Party location;

    /**
     * The pricing group.
     */
    private final Lookup pricingGroup;

    /**
     * Used to determine if the current template relates to the product, or needs to be removed.
     */
    private boolean currentTemplate;

    /**
     * Listener for product change events.
     */
    private final ModifiableListener productListener;

    /**
     * Print node name.
     */
    protected static final String PRINT = "print";


    /**
     * Constructs an {@link ActItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public ActItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        rules = ServiceHelper.getBean(ProductPriceRules.class);

        if (act.isNew() && parent != null) {
            // default the act start time to that of the parent
            act.setActivityStartTime(parent.getActivityStartTime());
        }
        location = getLocation(parent, context);
        pricingGroup = getPricingGroup(location);
        productListener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                productModified();
            }
        };
    }

    /**
     * Returns a reference to the customer, obtained from the parent act.
     *
     * @return a reference to the customer or {@code null} if the act
     * has no parent
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
     * has no parent
     */
    public Party getCustomer() {
        return (Party) getObject(getCustomerRef());
    }

    /**
     * Returns a reference to the product.
     *
     * @return a reference to the product, or {@code null} if the act has no product
     */
    public IMObjectReference getProductRef() {
        return getParticipantRef(PRODUCT);
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
        setParticipant(PRODUCT, product);
    }

    /**
     * Sets a product included from a template.
     *
     * @param product  the template product. May be {@code null}
     * @param template the template that included the product. May be {@code null}
     */
    public void setProduct(IMObjectReference product, IMObjectReference template) {
        setTemplateRef(template); // NB: template must be set before product
        setProductRef(product);
    }

    /**
     * Sets a product included from a template.
     *
     * @param product  the product. May be {@code null}
     * @param template the template that included the product. May be {@code null}
     */
    public void setProduct(TemplateProduct product, Product template) {
        setTemplate(template);  // NB: template must be set before product
        if (product != null) {
            // clear the quantity. If the quantity changes after the product is set, don't overwrite with that
            // from the template, as it is the dose quantity for the patient weight
            setQuantity(BigDecimal.ZERO);
            setProduct(product.getProduct());
            if (MathRules.isZero(getQuantity())) {
                setQuantity(product.getHighQuantity());
            }
            if (product.getZeroPrice()) {
                setFixedPrice(BigDecimal.ZERO);
                setUnitPrice(BigDecimal.ZERO);
                setDiscount(BigDecimal.ZERO);
            }
        } else {
            setProduct(null);
        }
    }

    /**
     * Returns the product template.
     *
     * @return the product template, or {@code null} if the act has no template
     */
    public Product getTemplate() {
        return (Product) getObject(getTemplateRef());
    }

    /**
     * Returns a reference to the product template.
     *
     * @return a reference to the product template, or {@code null} if the act has no template
     */
    public IMObjectReference getTemplateRef() {
        return getParticipantRef(TEMPLATE);
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
     * has no patient
     */
    public IMObjectReference getPatientRef() {
        return getParticipantRef(PATIENT);
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
        setParticipant(PATIENT, patient);
    }

    /**
     * Returns a reference to the clinician.
     *
     * @return a reference to the clinician, or {@code null} if the act has
     * no clinician
     */
    public User getClinician() {
        return (User) getObject(getClinicianRef());
    }

    /**
     * Returns a reference to the clinician.
     *
     * @return a reference to the clinician, or {@code null} if the act has
     * no clinician
     */
    public IMObjectReference getClinicianRef() {
        return getParticipantRef(CLINICIAN);
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
        setParticipant(CLINICIAN, clinician);
    }

    /**
     * Sets the product quantity.
     *
     * @param quantity the product quantity. May be {@code null}
     */
    public void setQuantity(BigDecimal quantity) {
        getProperty(QUANTITY).setValue(quantity);
    }

    /**
     * Returns the product quantity.
     *
     * @return the product quantity, or {@code 0} if no quantity is set
     */
    public BigDecimal getQuantity() {
        return getProperty(QUANTITY).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the fixed price.
     *
     * @param fixedPrice the fixed price
     */
    public void setFixedPrice(BigDecimal fixedPrice) {
        getProperty(FIXED_PRICE).setValue(fixedPrice);
    }

    /**
     * Sets the unit price.
     *
     * @param unitPrice the unit price
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        getProperty(UNIT_PRICE).setValue(unitPrice);
    }

    /**
     * Returns the unit price.
     *
     * @return the unit price, or {@code 0} if no unit price is set
     */
    public BigDecimal getUnitPrice() {
        return getProperty(UNIT_PRICE).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the discount.
     *
     * @param discount the discount
     */
    public void setDiscount(BigDecimal discount) {
        getProperty(DISCOUNT).setValue(discount);
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
     * Returns the practice location.
     *
     * @return the practice location. May be {@code null}
     */
    public Party getLocation() {
        return location;
    }

    /**
     * Returns the pricing group.
     *
     * @return the pricing group. May be {@code null}
     */
    public Lookup getPricingGroup() {
        return pricingGroup;
    }

    /**
     * Determines if zero-priced products should be printed.
     *
     * @param print if {@code true}, print zero-priced products, otherwise suppress them
     */
    public void setPrint(boolean print) {
        Property property = getProperty("print");
        if (property != null) {
            property.setValue(print);
        }
    }

    /**
     * Determines if zero-priced products should be printed.
     *
     * @return {@code true}, if zero-priced products should be printed, {@code false} if they should be suppressed
     */
    public boolean getPrint() {
        boolean result = true;
        Property property = getProperty("print");
        if (property != null) {
            result = property.getBoolean(true);
        }
        return result;
    }

    /**
     * Invoked when the product changes.
     * <p/>
     * This delegates to {@link #productModified(Participation)} if there is a current participation.
     */
    protected void productModified() {
        Participation participation = null;
        ProductParticipationEditor editor = getProductEditor();
        if (editor != null) {
            participation = editor.getParticipation();
        }
        if (participation != null) {
            productModified(participation);
        }
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
        // product modification can happen either via user intervention or template expansion. If by template expansion
        // the template is populated before the product, and must be retained. If not, the template must be removed
        if (!currentTemplate) {
            setTemplateRef(null);
        } else {
            currentTemplate = false;
        }
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
        return rules.getProductPrice(product, shortName, getStartTime(), pricingGroup);
    }

    /**
     * Returns the maximum discount for a product price, expressed as a percentage.
     *
     * @param price the price. May be {@code null}
     * @return the maximum discount for the product price
     */
    protected BigDecimal getMaxDiscount(ProductPrice price) {
        return (price != null) ? rules.getMaxDiscount(price) : ProductPriceRules.DEFAULT_MAX_DISCOUNT;
    }

    /**
     * Returns the cost for a product
     *
     * @param price the price. May be {@code null}
     * @return the cost for a product.
     */
    protected BigDecimal getCostPrice(ProductPrice price) {
        return (price != null) ? rules.getCostPrice(price) : BigDecimal.ZERO;
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
        ParticipationEditor<Product> editor = getParticipationEditor(PRODUCT, create);
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
        ParticipationEditor<Party> editor = getParticipationEditor(PATIENT, create);
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
        ParticipationEditor<User> editor = getParticipationEditor(CLINICIAN, create);
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
            product.addModifiableListener(productListener);
        }
        if (patient != null && product != null) {
            product.setPatient(patient.getEntity());
            // NOTE: if layout is called multiple times and the patient editor is not recreated, multiple listeners
            // will be registered
            patient.getEditor().addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    product.setPatient(patient.getEntity());
                }
            });
        }
    }

    /**
     * Determines the practice location.
     * <p/>
     * This uses the location of the parent act, if it defines a {@code location} node, otherwise it uses the
     * location from the context.
     *
     * @param parent  the parent act
     * @param context the layout context
     * @return the practice location. May be {@code null}
     */
    protected Party getLocation(Act parent, LayoutContext context) {
        Party location = null;
        if (parent != null) {
            ActBean bean = new ActBean(parent);
            if (bean.hasNode(LOCATION)) {
                location = (Party) getObject(bean.getNodeParticipantRef(LOCATION));
            }
        }
        if (location == null) {
            location = context.getContext().getLocation();
        }
        return location;
    }

    /**
     * Sets the product template.
     *
     * @param template the product template. May be {@code null}
     */
    protected void setTemplate(Product template) {
        setTemplateRef(template != null ? template.getObjectReference() : null);
    }

    /**
     * Sets the product template.
     *
     * @param template a reference to the product. May be {@code null}
     */
    protected void setTemplateRef(IMObjectReference template) {
        if (getProperty(TEMPLATE) != null) {
            setParticipant(TEMPLATE, template);
            currentTemplate = template != null;
        }
    }

    /**
     * Determines the pricing group from the location.
     *
     * @param location the location. May be {@code null}
     * @return the pricing group. May be {@code null}
     */
    private Lookup getPricingGroup(Party location) {
        Lookup result = null;
        if (location != null) {
            LocationRules locationRules = ServiceHelper.getBean(LocationRules.class);
            result = locationRules.getPricingGroup(location);
        }
        return result;
    }

    /**
     * Act item layout strategy.
     */
    protected class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Constructs an {@link LayoutStrategy}.
         */
        public LayoutStrategy() {
            super(nodes != null ? nodes : new ArchetypeNodes());
        }
    }
}
