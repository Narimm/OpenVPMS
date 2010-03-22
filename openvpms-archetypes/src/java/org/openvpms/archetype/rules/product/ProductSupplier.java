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

package org.openvpms.archetype.rules.product;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;


/**
 * Wrapper for <em>entityRelationship.productSupplier</em> relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductSupplier {

    /**
     * Bean wrapper for the relationship.
     */
    private final IMObjectBean bean;


    /**
     * Creates a new <tt>ProductSupplier</em>.
     *
     * @param relationship the relationship
     */
    public ProductSupplier(EntityRelationship relationship) {
        this(relationship, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>ProductSupplier</em>.
     *
     * @param relationship the relationship
     * @param service      the archetype service
     */
    public ProductSupplier(EntityRelationship relationship,
                           IArchetypeService service) {
        bean = new IMObjectBean(relationship, service);
    }

    /**
     * Returns the product.
     *
     * @return the product, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Product getProduct() {
        return (Product) bean.getObject("source");
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getSupplier() {
        return (Party) bean.getObject("target");
    }

    /**
     * Returns a reference to the suplier.
     *
     * @return the supplier reference, or <tt>null</tt> if none is found
     */
    public IMObjectReference getSupplierRef() {
        return getRelationship().getTarget();
    }

    /**
     * Sets the reorder code.
     *
     * @param code the reorder code. May be <tt>null</tt>
     */
    public void setReorderCode(String code) {
        bean.setValue("reorderCode", code);
    }

    /**
     * Returns the reorder code.
     *
     * @return the reorder code. May be <tt>null</tt>
     */
    public String getReorderCode() {
        return bean.getString("reorderCode");
    }

    /**
     * Sets the reorder description.
     *
     * @param description the reorder description. May be <tt>null</tt>
     */
    public void setReorderDescription(String description) {
        bean.setValue("reorderDescription", description);
    }

    /**
     * Returns the reorder description.
     *
     * @return the reorder description. May be <tt>null</tt>
     */
    public String getReorderDescription() {
        return bean.getString("reorderDescription");
    }

    /**
     * Sets the bar code.
     *
     * @param barCode the bar code. May be <tt>null</tt>
     */
    public void setBarCode(String barCode) {
        bean.setValue("barCode", barCode);
    }

    /**
     * Returns the bar code.
     *
     * @return the bar code. May be <tt>null</tt>
     */
    public String getBarCode() {
        return bean.getString("barCode");
    }

    /**
     * Sets the package size.
     *
     * @param size the package size
     */
    public void setPackageSize(int size) {
        bean.setValue("packageSize", size);
    }

    /**
     * Returns the package size.
     *
     * @return the package size
     */
    public int getPackageSize() {
        return bean.getInt("packageSize");
    }

    /**
     * Sets the package units.
     *
     * @param units the package units. May be <tt>null</tt>
     */
    public void setPackageUnits(String units) {
        bean.setValue("packageUnits", units);
    }

    /**
     * Returns the package units.
     *
     * @return the package units. May be <tt>null</tt>
     */
    public String getPackageUnits() {
        return bean.getString("packageUnits");
    }

    /**
     * Sets the list price.
     *
     * @param price the list price. May be <tt>null</tt>
     */
    public void setListPrice(BigDecimal price) {
        bean.setValue("listPrice", price);
    }

    /**
     * Returns the list price.
     *
     * @return the list price. May be <tt>null</tt>
     */
    public BigDecimal getListPrice() {
        return bean.getBigDecimal("listPrice");
    }

    /**
     * Sets the nett price.
     *
     * @param price the nett price. MAy be <tt>null</tt>
     */
    public void setNettPrice(BigDecimal price) {
        bean.setValue("nettPrice", price);
    }

    /**
     * Returns the nett price.
     *
     * @return the nett price
     */
    public BigDecimal getNettPrice() {
        return bean.getBigDecimal("nettPrice");
    }

    /**
     * Indicates if this is the preferred relatiobship for the supplier
     * and product.
     *
     * @param preferred if <tt>true</tt>, this is the preferred relationship
     */
    public void setPreferred(boolean preferred) {
        bean.setValue("preferred", preferred);
    }

    /**
     * Determines if this is the preferred relationship for the supplier
     * and product.
     *
     * @return <tt>true</tt> if this is the preferred relationship
     */
    public boolean isPreferred() {
        return bean.getBoolean("preferred");
    }

    /**
     * Determines if changes to the list price should trigger recalculation
     * of the <em>cost</em> and <em>price</em> nodes of any
     * <em>productPrice.unitPrice</em> associated with the product.
     *
     * @return <tt>true</tt> if unit prices should be updated
     */
    public boolean isAutoPriceUpdate() {
        return bean.getBoolean("autoPriceUpdate");
    }

    /**
     * Determines if changes to the list price should trigger recalculation
     * of the <em>cost</em> and <em>price</em> nodes of any
     * <em>productPrice.unitPrice</em> associated with the product.
     *
     * @param autoUpdate if <tt>true</tt>, unit prices should be updated
     */
    public void setAutoPriceUpdate(boolean autoUpdate) {
        bean.setValue("autoPriceUpdate", autoUpdate);
    }

    /**
     * Returns the underlying relationship.
     *
     * @return the underlying relationship
     */
    public EntityRelationship getRelationship() {
        return (EntityRelationship) bean.getObject();
    }

    /**
     * Saves the underlying relationship.
     *
     * @throws ArchetypeServiceException if the object can't be saved
     */
    public void save() {
        bean.save();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param other the reference object with which to compare
     * @return <tt>true</tt> if other is a <tt>ProductSupplier</tt> whose
     *         underlying {@link EntityRelationship} equals this one.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ProductSupplier) {
            ProductSupplier p = ((ProductSupplier) other);
            return p.bean.getObject().equals(bean.getObject());
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return bean.getObject().hashCode();
    }

}
