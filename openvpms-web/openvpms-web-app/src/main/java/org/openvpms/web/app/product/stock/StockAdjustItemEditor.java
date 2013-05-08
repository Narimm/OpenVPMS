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

package org.openvpms.web.app.product.stock;

import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.i18n.Messages;

import java.math.BigDecimal;
import java.util.List;


/**
 * An editor for <em>act.stockAdjustItem</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockAdjustItemEditor extends ActItemEditor {

    /**
     * Displays the current stock at the stock location.
     */
    private SimpleProperty currentQuantity;

    /**
     * The stock location.
     */
    private Party stockLocation;

    /**
     * The stock rules.
     */
    private final StockRules rules;


    /**
     * Creates a new <tt>StockTransferItemEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act.
     * @param context the layout context. May be <tt>null</tt>
     */
    public StockAdjustItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        rules = new StockRules();
        currentQuantity = new SimpleProperty("fromQuantity", BigDecimal.class);
        currentQuantity.setDisplayName(Messages.get("product.stock.quantity"));
        currentQuantity.setReadOnly(true);
        if (parent != null) {
            ActBean bean = new ActBean(parent);
            setStockLocation((Party) getObject(bean.getNodeParticipantRef("stockLocation")));
        }
        updateCurrentQuantity(getProduct());
    }

    /**
     * Sets the stock location.
     *
     * @param location the stock location
     */
    public void setStockLocation(Party location) {
        stockLocation = location;
    }

    /**
     * Updates the current stock quantity when the product changes.
     *
     * @param product the product. May be <tt>null</tt>
     */
    @Override
    protected void productModified(Product product) {
        updateCurrentQuantity(product);
        notifyProductListener(product);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy() {
            @Override
            protected ComponentSet createComponentSet(
                IMObject object, List<NodeDescriptor> descriptors,
                PropertySet properties,
                LayoutContext context) {
                ComponentSet set = super.createComponentSet(object, descriptors,
                                                            properties,
                                                            context);
                ComponentState component = createComponent(currentQuantity,
                                                           object, context);
                set.add(component);
                return set;
            }
        };
    }

    /**
     * Updates the current quantity display.
     *
     * @param product the product. May be {@code null}
     */
    private void updateCurrentQuantity(Product product) {
        BigDecimal quantity = BigDecimal.ZERO;
        try {
            if (stockLocation != null && product != null) {
                quantity = rules.getStock(product, stockLocation);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        currentQuantity.setValue(quantity);
    }

}
