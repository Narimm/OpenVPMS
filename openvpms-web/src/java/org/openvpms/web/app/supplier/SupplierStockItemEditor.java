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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;


/**
 * An editor for supplier orders and deliveries, that:
 * <ul>
 * <li>calculates tax.</li>
 * <li>defaults values to the the associated
 * {@link ProductSupplier ProductSupplier} for the selected product and
 * supplier.</li>
 * <li>updates the {@link ProductSupplier ProductSupplier} on save, creating
 * one if none exists.
 * </li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class SupplierStockItemEditor extends SupplierActItemEditor {

    /**
     * Creates a new <tt>SupplierStockItemEditor</tt>.
     *
     * @param act     the act
     * @param parent  the parent act
     * @param context the layout context
     * @throws ArchetypeServiceException for any archetype service error
     */
    public SupplierStockItemEditor(FinancialAct act, Act parent,
                                   LayoutContext context) {
        super(act, parent, context);
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
            Party supplier = (Party) getObject(bean.getNodeParticipantRef("supplier"));
            Party location = (Party) getObject(bean.getNodeParticipantRef("stockLocation"));
            setSupplier(supplier);
            setStockLocation(location);
        }
    }

    /**
     * Invoked when the participation product is changed, to update prices.
     *
     * @param participation the product participation instance
     */
    protected void productModified(Participation participation) {
        IMObjectReference entity = participation.getEntity();
        IMObject object = getObject(entity);
        if (object instanceof Product) {
            ProductParticipationEditor editor = getProductEditor();
            ProductSupplier ps = editor.getProductSupplier();
            if (ps != null) {
                Property reorderCode = getProperty("reorderCode");
                reorderCode.setValue(ps.getReorderCode());
                getProperty("reorderDescription").setValue(
                        ps.getReorderDescription());
                getProperty("packageSize").setValue(ps.getPackageSize());
                getProperty("packageUnits").setValue(ps.getPackageUnits());
                getProperty("listPrice").setValue(ps.getListPrice());
                getProperty("unitPrice").setValue(ps.getNettPrice());
            }
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
            getComponent(); // ensure the component has been laid out
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
        ProductRules rules = new ProductRules();
        ProductSupplier ps = getProductSupplier();
        int size = getPackageSize();
        String units = getPackageUnits();
        if (ps == null) {
            ps = rules.getProductSupplier(product, supplier, size, units);
        }
        boolean save = true;
        String reorderDesc = getReorderDescription();
        String reorderCode = getReorderCode();
        BigDecimal listPrice = getListPrice();
        BigDecimal unitPrice = getUnitPrice();
        if (ps == null) {
            // no product-supplier relationship, so create a new one
            ps = rules.createProductSupplier(product, supplier);
            ps.setPackageSize(size);
            ps.setPackageUnits(units);
            ps.setReorderCode(reorderCode);
            ps.setReorderDescription(reorderDesc);
            ps.setListPrice(listPrice);
            ps.setNettPrice(unitPrice);
            if (rules.getProductSuppliers(product, supplier).isEmpty()) {
                // if there are no relationships for the supplier, mark the
                // new one as the preferred
                ps.setPreferred(true);
            }
        } else if (size != ps.getPackageSize()
                   || !ObjectUtils.equals(units, ps.getPackageUnits())
                   || !equals(listPrice, ps.getListPrice())
                   || !equals(unitPrice, ps.getNettPrice())
                   || !ObjectUtils.equals(ps.getReorderCode(), reorderCode)
                   || !ObjectUtils.equals(ps.getReorderDescription(),
                                          reorderDesc)) {
            // properties are different to an existing relationship
            ps.setPackageSize(size);
            ps.setPackageUnits(units);
            ps.setReorderCode(reorderCode);
            ps.setReorderDescription(reorderDesc);
            ps.setListPrice(listPrice);
            ps.setNettPrice(unitPrice);
        } else {
            save = false;
        }
        if (save) {
            ps.save();
        }
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
     * Returns the unit price (also know as the nett price).
     *
     * @return the unit price
     */
    private BigDecimal getUnitPrice() {
        return (BigDecimal) getProperty("unitPrice").getValue();
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
