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

package org.openvpms.web.component.im.edit.order;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.archetype.rules.supplier.ProductSupplier;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.supplierOrderItem</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class OrderItemEditor extends ActItemEditor {

    /**
     * Determines if the act was posted at construction. If so, only a limited
     * set of properties may be edited.
     */
    private final boolean posted;

    /**
     * Node filter, used to disable properties when a product template is
     * selected.
     */
    private static final NodeFilter TEMPLATE_FILTER = new NamedNodeFilter(
            "nettPrice", "total", "reorderCode", "reorderDescription",
            "quantity", "listPrice", "packageSize", "packageUnits");


    /**
     * Construct a new <tt>OrderItemEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public OrderItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.supplierOrderItem")) {
            throw new IllegalArgumentException(
                    "Invalid act type: " + act.getArchetypeId().getShortName());
        }
        posted = ActStatus.POSTED.equals(parent.getStatus());
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
     * Sets the product supplier.
     *
     * @param supplier the product supplier. May be <tt>null</tt>
     */
    public void setSupplier(Party supplier) {
        ProductParticipationEditor editor = getProductEditor();
        if (editor != null) {
            editor.setSupplier(supplier);
        }
    }

    /**
     * Sets the stock location.
     *
     * @param location the stock location. May be <tt>null</tt>
     */
    public void setStockLocation(Party location) {
        ProductParticipationEditor editor = getProductEditor();
        if (editor != null) {
            editor.setStockLocation(location);
        }
    }

    /**
     * Save any edits.
     *
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    protected boolean doSave() {
        if (getObject().isNew()) {
            ProductParticipationEditor editor = getProductEditor();
            Party supplier = editor.getSupplier();
            Product product = editor.getEntity();
            if (supplier != null && product != null) {
                checkProductSupplier(product, supplier);
            }
        }
        return super.doSave();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid
     *         otherwise <tt>false</tt>
     */
    @Override
    public boolean validate(Validator validator) {
        boolean valid = super.validate(validator);
        if (valid) {
            BigDecimal quantity = getQuantity();
            BigDecimal received = getReceivedQuantity();
            BigDecimal cancelled = getCancelledQuantity();
            BigDecimal sum = received.add(cancelled);
            if (sum.compareTo(quantity) > 0) {
                valid = false;
                Property property = getProperty("quantity");
                String message = Messages.get("supplier.order.invalidQuantity",
                                              quantity, sum);
                ValidatorError error = new ValidatorError(property, message);
                validator.add(property, error);
            }
        }
        return valid;
    }

    /**
     * Invoked when the participation product is changed, to update prices.
     *
     * @param participation the product participation instance
     */
    protected void productModified(Participation participation) {
        IMObjectReference entity = participation.getEntity();
        IMObject object = IMObjectHelper.getObject(entity);
        if (object instanceof Product) {
            Product product = (Product) object;
            if (TypeHelper.isA(product, "product.template")) {
                if (getFilter() != TEMPLATE_FILTER) {
                    changeLayout(TEMPLATE_FILTER);
                }
            } else {
                ProductParticipationEditor editor = getProductEditor();
                ProductSupplier ps = editor.getProductSupplier();
                if (getFilter() != null) {
                    // need to change the layout. This recreates the product
                    // editor, so preserve any product-supplier relationship
                    changeLayout(null);
                    editor = getProductEditor();
                    editor.setProductSupplier(ps);
                }
                if (ps != null) {
                    Property reorderCode = getProperty("reorderCode");
                    reorderCode.setValue(ps.getReorderCode());
                    getProperty("reorderDescription").setValue(
                            ps.getReorderDescription());
                    getProperty("packageSize").setValue(ps.getPackageSize());
                    getProperty("packageUnits").setValue(ps.getPackageUnits());
                    getProperty("listPrice").setValue(ps.getListPrice());
                    getProperty("nettPrice").setValue(ps.getNettPrice());
                }
            }
        }
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AbstractLayoutStrategy() {

            /**
             * Creates a component for a property.
             *
             * @param property the property
             * @param parent   the parent object
             * @param context  the layout context
             * @return a component to display <tt>property</tt>
             */
            @Override
            protected ComponentState createComponent(Property property,
                                                     IMObject parent,
                                                     LayoutContext context) {
                if (posted) {
                    String name = property.getName();
                    if (!name.equals("status")
                            && !name.equals("cancelledQuantity")) {
                        property = new DelegatingProperty(property) {
                            @Override
                            public boolean isReadOnly() {
                                return true;
                            }
                        };
                    }
                }
                return super.createComponent(property, parent, context);
            }
        };
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        Act parent = (Act) getParent();
        if (parent != null) {
            // propagate the parent's supplier and stock location. Can only do
            // this once the product editor is created
            ActBean bean = new ActBean(parent);
            Party supplier = (Party) bean.getNodeParticipant("supplier");
            Party location = (Party) bean.getNodeParticipant("stockLocation");
            setSupplier(supplier);
            setStockLocation(location);
        }
    }

    /**
     * Checks the product-supplier relationship for the specified product
     * and supplier.
     * <p/>
     * If no relationship exists with the same package size and units, a new one
     * will be added.
     * <p/>
     * If a relationship exists but the list price, nett price, reorder code
     * or description have changed, it will be updated.
     *
     * @param product  the product
     * @param supplier the supplier
     */
    private void checkProductSupplier(Product product, Party supplier) {
        OrderRules rules = new OrderRules();
        ProductSupplier ps = getProductSupplier();
        int size = getPackageSize();
        String units = getPackageUnits();
        if (ps == null) {
            ps = rules.getProductSupplier(supplier, product, size, units);
        }
        boolean save = true;
        String reorderDesc = getReorderDescription();
        String reorderCode = getReorderCode();
        BigDecimal listPrice = getListPrice();
        BigDecimal nettPrice = getNettPrice();
        if (ps == null) {
            // no product-supplier relationship, so create a new one
            ps = rules.createProductSupplier(product, supplier);
            ps.setPackageSize(size);
            ps.setPackageUnits(units);
            ps.setReorderCode(reorderCode);
            ps.setReorderDescription(reorderDesc);
            ps.setListPrice(listPrice);
            ps.setNettPrice(nettPrice);
            ps.setPreferred(true);
        } else if (size != ps.getPackageSize()
                || !ObjectUtils.equals(units, ps.getPackageUnits())
                || !equals(listPrice, ps.getListPrice())
                || !equals(nettPrice, ps.getNettPrice())
                || !ObjectUtils.equals(ps.getReorderCode(), reorderCode)
                || !ObjectUtils.equals(ps.getReorderDescription(),
                                       reorderDesc)) {
            // properties are different to an existing relationship
            ps.setPackageSize(size);
            ps.setPackageUnits(units);
            ps.setReorderCode(reorderCode);
            ps.setReorderDescription(reorderDesc);
            ps.setListPrice(listPrice);
            ps.setNettPrice(nettPrice);
        } else {
            save = false;
        }
        if (save) {
            ps.save();
        }
    }

    /**
     * Returns the quantity.
     *
     * @return the quantity
     */
    private BigDecimal getQuantity() {
        return (BigDecimal) getProperty("quantity").getValue();
    }

    /**
     * Returns the received quantity.
     *
     * @return the received quantity
     */
    private BigDecimal getReceivedQuantity() {
        return (BigDecimal) getProperty("receivedQuantity").getValue();
    }

    /**
     * Returns the cancelled quantity.
     *
     * @return the cancelled quantity
     */
    private BigDecimal getCancelledQuantity() {
        return (BigDecimal) getProperty("cancelledQuantity").getValue();
    }

    /**
     * Returns the product supplier relationship.
     *
     * @return the relationship. May be <tt>null</tt>
     */
    private ProductSupplier getProductSupplier() {
        ProductParticipationEditor editor = getProductEditor();
        return (editor != null) ? editor.getProductSupplier() : null;
    }

    /**
     * Returns the package size.
     *
     * @return the package size
     */
    private int getPackageSize() {
        Integer value = (Integer) getProperty("packageSize").getValue();
        return (value != null) ? value : 0;
    }

    /**
     * Returns the package units.
     *
     * @return the package units
     */
    private String getPackageUnits() {
        return (String) getProperty("packageUnits").getValue();
    }

    /**
     * Returns the reorder code.
     *
     * @return the reorder code
     */
    private String getReorderCode() {
        return (String) getProperty("reorderCode").getValue();
    }

    /**
     * Returns the reorder description.
     *
     * @return the reorder description
     */
    private String getReorderDescription() {
        return (String) getProperty("reorderDescription").getValue();
    }

    /**
     * Returns the list price.
     *
     * @return the list price
     */
    private BigDecimal getListPrice() {
        return (BigDecimal) getProperty("listPrice").getValue();
    }

    /**
     * Returns the nett price.
     *
     * @return the nett price
     */
    private BigDecimal getNettPrice() {
        return (BigDecimal) getProperty("nettPrice").getValue();
    }

    /**
     * Helper to determine if two decimals are equal.
     *
     * @param lhs the left-hand side. May be <tt>null</tt>
     * @param rhs right left-hand side. May be <tt>null</tt>
     * @return <tt>true</t> if they are equal, otherwise <tt>false</tt>
     */
    private boolean equals(BigDecimal lhs, BigDecimal rhs) {
        if (lhs != null && rhs != null) {
            return lhs.compareTo(rhs) == 0;
        }
        return ObjectUtils.equals(lhs, rhs);
    }
}
