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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.StockOnHand;

import java.math.BigDecimal;

/**
 * Tracks stock quantity.
 *
 * @author Tim Anderson
 */
public class StockQuantity {

    /**
     * The on-hand property.
     */
    private final SimpleProperty property = new SimpleProperty("onHand", null, BigDecimal.class,
                                                               Messages.get("product.stock.onhand"), true);

    /**
     * The component.
     */
    private final ComponentState state;

    /**
     * The act used to calculate the on-hand quantity.
     */
    private final FinancialAct act;

    /**
     * The stock on hand manager.
     */
    private final StockOnHand stockOnHand;

    /**
     * Constructs a {@link StockQuantity}.
     *
     * @param act         the act used to calculate the on-hand quantity
     * @param stockOnHand the stock on hand manager
     * @param context     the layout context
     */
    public StockQuantity(FinancialAct act, StockOnHand stockOnHand, LayoutContext context) {
        this.act = act;
        Component component = context.getComponentFactory().create(property);
        state = new ComponentState(component, property);
        this.stockOnHand = stockOnHand;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public ComponentState getComponent() {
        return state;
    }

    /**
     * Refreshes the display.
     */
    public void refresh() {
        Component component = state.getComponent();
        BigDecimal stock = stockOnHand.getAvailableStock(act);
        property.setValue(stock);
        if (stock != null && stock.compareTo(BigDecimal.ZERO) <= 0) {
            component.setStyleName("OutOfStock");
        } else {
            component.setStyleName(Styles.EDIT); // to highlight read-only status
        }
    }

}
