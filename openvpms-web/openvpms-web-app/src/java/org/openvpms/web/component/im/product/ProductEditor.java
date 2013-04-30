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

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.product.ProductPriceUpdater;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.web.app.product.ProductPriceEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collection;
import java.util.List;


/**
 * A {@link Product} editor that recalculates prices when
 * {@link ProductSupplier} relationships change.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductEditor extends AbstractIMObjectEditor {

    /**
     * The product price updater.
     */
    private ProductPriceUpdater updater;

    /**
     * Create a new <tt>ProductEditor</tt>.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context. May be <tt>null</tt>.
     */
    public ProductEditor(Product object, IMObject parent,
                         LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        updater = new ProductPriceUpdater(
            ServiceHelper.getCurrencies(),
            ServiceHelper.getArchetypeService(),
            LookupServiceHelper.getLookupService());
    }

    /**
     * Invoked when layout has completed. This can be used to perform
     * processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        IMObjectCollectionEditor editor = (IMObjectCollectionEditor) getEditor(
            "suppliers");
        if (editor != null) {
            editor.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onSupplierChanged();
                }
            });
        }
    }

    /**
     * Invoked when a product-supplier relationship changes. This recalculates product prices if required.
     */
    private void onSupplierChanged() {
        AbstractIMObjectCollectionEditor suppliers =
            (AbstractIMObjectCollectionEditor) getEditor("suppliers");
        AbstractIMObjectCollectionEditor prices =
            (AbstractIMObjectCollectionEditor) getEditor("prices");
        Collection<IMObjectEditor> currentPrices = prices.getCurrentEditors();
        Collection<IMObjectEditor> editors = suppliers.getCurrentEditors();
        for (IMObjectEditor editor : editors) {
            EntityRelationship rel = (EntityRelationship) editor.getObject();
            ProductSupplier ps = new ProductSupplier(rel);
            List<ProductPrice> updated
                = updater.update((Product) getObject(), ps, false);
            for (ProductPrice price : updated) {
                updatePriceEditor(price, currentPrices);
            }
        }
        prices.refresh();
    }

    /**
     * Refreshes the price editor associated with a product price.
     *
     * @param price   the price
     * @param editors the price editors
     */
    private void updatePriceEditor(ProductPrice price, Collection<IMObjectEditor> editors) {
        for (IMObjectEditor editor : editors) {
            if (editor.getObject().equals(price) && (editor instanceof ProductPriceEditor)) {
                ((ProductPriceEditor) editor).refresh();
                break;
            }
        }
    }

}
