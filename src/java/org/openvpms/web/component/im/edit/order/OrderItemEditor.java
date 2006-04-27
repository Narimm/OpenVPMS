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

import java.math.BigDecimal;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;


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
            "qty", "unitCost", "total");


    /**
     * Construct a new <code>EstimationItemEdtor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    protected OrderItemEditor(Act act, Act parent,
                              LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Sets the product quantity.
     *
     * @param quantity the product quantity
     */
    public void setQuantity(BigDecimal quantity) {
        getProperty("qty").setValue(quantity);
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object  the object to edit
     * @param parent  the parent act
     * @param context the layout context
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        LayoutContext context) {
        IMObjectEditor result = null;
        if (IMObjectHelper.isA(object, "act.supplierOrderItem")
            && parent instanceof Act) {
            result = new OrderItemEditor((Act) object, (Act) parent,
                                         context);
        }
        return result;
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
            if (IMObjectHelper.isA(product, "product.template")) {
                if (getFilter() != TEMPLATE_FILTER) {
                    changeLayout(TEMPLATE_FILTER);
                }
                // zero out the unit cost
                Property unitCost = getProperty("unitCost");
                unitCost.setValue(BigDecimal.ZERO);
            } else {
                if (getFilter() != null) {
                    changeLayout(null);
                }
                Property unitCost = getProperty("unitCost");
                ProductPrice unit = getPrice("productPrice.unitPrice", product);
                if (unit != null) {
                    unitCost.setValue(unit.getPrice());
                }
            }
        }
    }

}
