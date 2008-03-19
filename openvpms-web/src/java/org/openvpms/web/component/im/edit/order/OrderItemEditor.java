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
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;

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
        getProductEditor().setSupplier(supplier);
    }

    /**
     * Sets the stock location.
     *
     * @param location the stock location. May be <tt>null</tt>
     */
    public void setStockLocation(Party location) {
        getProductEditor().setStockLocation(location);
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
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
     * Checks the product supplier relationship for the specified product
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
        int size = getPackageSize();
        String units = getPackageUnits();
        ProductSupplier ps = rules.getProductSupplier(product, supplier,
                                                      size, units);
        boolean save = false;
        String reorderDesc = getReorderDescription();
        String reorderCode = getReorderCode();
        BigDecimal listPrice = getListPrice();
        BigDecimal nettPrice = getNettPrice();
        if (ps == null) {
            ps = rules.createProductSupplier(product, supplier);
            ps.setPackageSize(size);
            ps.setPackageUnits(units);
            ps.setReorderCode(reorderCode);
            ps.setReorderDescription(reorderDesc);
            ps.setListPrice(listPrice);
            ps.setNettPrice(nettPrice);
            ps.setPreferred(true);
            save = true;
        } else {
            if (!equals(listPrice, ps.getListPrice())
                    || !equals(nettPrice, ps.getNettPrice())
                    || !ObjectUtils.equals(ps.getReorderCode(), reorderCode)
                    || !ObjectUtils.equals(ps.getReorderDescription(),
                                           reorderDesc)) {
                ps.setReorderCode(reorderCode);
                ps.setReorderDescription(reorderDesc);
                ps.setListPrice(listPrice);
                ps.setNettPrice(nettPrice);
                save = true;
            }
        }
        if (save) {
            ps.save();
        }
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
