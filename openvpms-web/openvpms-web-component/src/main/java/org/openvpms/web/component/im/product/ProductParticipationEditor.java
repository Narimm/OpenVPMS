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

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for products.
 *
 * @author Tim Anderson
 */
public class ProductParticipationEditor extends ParticipationEditor<Product> {

    /**
     * The patient, used to constrain searches to a particular species. May be {@code null}.
     */
    private Party patient;

    /**
     * The current supplier.
     */
    private Party supplier;

    /**
     * The product supplier relationship.
     */
    private ProductSupplier productSupplier;

    /**
     * The practice location, used to determine price service ratios. May be {@code null}
     */
    private Party location;

    /**
     * The stock location, used to constrain searches to a particular location. May be {@code null}.
     */
    private Party stockLocation;


    /**
     * Constructs a new {@link ProductParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be {@code null}
     */
    public ProductParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, ProductArchetypes.PRODUCT_PARTICIPATION,
                            StockArchetypes.STOCK_PARTICIPATION)) {
            throw new IllegalArgumentException("Invalid participation type:"
                                               + participation.getArchetypeId().getShortName());
        }
    }

    /**
     * Sets the patient, used to constrain product searches to a set of species.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        this.patient = patient;
    }

    /**
     * Returns the patient .
     *
     * @return the patient. May be {@code null}
     */
    public Party getPatient() {
        return patient;
    }

    /**
     * Sets the product supplier.
     *
     * @param supplier the supplier. May be {@code null}
     */
    public void setSupplier(Party supplier) {
        this.supplier = supplier;
        productSupplier = null;
    }

    /**
     * Returns the product supplier.
     *
     * @return the product supplier. May be {@code null}
     */
    public Party getSupplier() {
        return supplier;
    }

    /**
     * Sets the practice location.
     * <p/>
     * This is used to determine price service ratios.
     *
     * @param location the practice location. May be {@code null}
     */
    public void setLocation(Party location) {
        this.location = location;
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
     * Sets the stock location. If set, only those products that have
     * an relationship with the location, or no stock relationships at all
     * will be returned.
     *
     * @param location the stock location. May be {@code null}
     */
    public void setStockLocation(Party location) {
        this.stockLocation = location;
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location. May be {@code null}
     */
    public Party getStockLocation() {
        return stockLocation;
    }

    /**
     * The <em>entityRelationship.productSupplier</em> relationship associated with the product. Only populated when
     * the user selects the product.
     *
     * @return the product supplier relationship. May be {@code null}
     */
    public ProductSupplier getProductSupplier() {
        return productSupplier;
    }

    /**
     * Sets the product supplier relationship.
     *
     * @param relationship the product supplier relationship. May be {@code null}
     */
    public void setProductSupplier(ProductSupplier relationship) {
        productSupplier = relationship;
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Product> createEntityEditor(Property property) {
        return new ProductReferenceEditor(this, property, getLayoutContext());
    }

}
