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
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;
import java.util.List;


/**
 * An editor for <em>act.stockTransferItem</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockTransferItemEditor extends ActItemEditor {

    /**
     * Displays the current stock at the from location.
     */
    private SimpleProperty fromQuantity;

    /**
     * Displays the current stock at the to location.
     */
    private SimpleProperty toQuantity;

    /**
     * The stock rules.
     */
    private final StockRules rules;

    /**
     * The transfer-from location.
     */
    private Party transferFrom;

    /**
     * The transfer-to location
     */
    private Party transferTo;


    /**
     * Creates a new <tt>StockTransferItemEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context. May be <tt>null</tt>
     */
    public StockTransferItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        rules = new StockRules();
        fromQuantity = new SimpleProperty("fromQuantity", BigDecimal.class);
        fromQuantity.setDisplayName(Messages.get("product.stock.fromQuantity"));
        fromQuantity.setValue(BigDecimal.ZERO);
        fromQuantity.setReadOnly(true);

        toQuantity = new SimpleProperty("toQuantity", BigDecimal.class);
        toQuantity.setDisplayName(Messages.get("product.stock.toQuantity"));
        toQuantity.setValue(BigDecimal.ZERO);
        toQuantity.setReadOnly(true);

        if (parent != null) {
            ActBean bean = new ActBean(parent);
            setTransferFrom((Party) getObject(bean.getNodeParticipantRef("stockLocation")));
            setTransferTo((Party) getObject(bean.getNodeParticipantRef("to")));
        }
    }

    /**
     * Sets the 'transfer from' stock location.
     *
     * @param location the stock location to transfer from
     */
    public void setTransferFrom(Party location) {
        transferFrom = location;
        updateFromQuantity(getProduct());
    }

    /**
     * Sets the 'transfer to' stock location.
     *
     * @param location the stock location to transfer to
     */
    public void setTransferTo(Party location) {
        transferTo = location;
        updateToQuantity(getProduct());
    }

    /**
     * Invoked when the product is changed.
     *
     * @param product the product. May be <tt>null</tt>
     */
    @Override
    protected void productModified(Product product) {
        updateFromQuantity(product);
        updateToQuantity(product);
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
                ComponentState from = createComponent(fromQuantity, object,
                                                      context);
                ComponentState to = createComponent(toQuantity, object,
                                                    context);
                set.add(from);
                set.add(to);
                return set;
            }
        };
    }

    /**
     * Updates the transfer-from stock quantity.
     *
     * @param product the product. May be <tt>null</tt>
     */
    private void updateFromQuantity(Product product) {
        fromQuantity.setValue(getStock(product, transferFrom));
    }

    /**
     * Updates the transfer-to stock quantity.
     *
     * @param product the product. May be <tt>null</tt>
     */
    private void updateToQuantity(Product product) {
        toQuantity.setValue(getStock(product, transferTo));
    }

    /**
     * Returns the stock at the specified stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @return the stock quantity
     */
    private BigDecimal getStock(Product product, Party stockLocation) {
        BigDecimal result = BigDecimal.ZERO;
        try {
            if (product != null && stockLocation != null) {
                result = rules.getStock(product, stockLocation);
            }
        } catch (OpenVPMSException error) {
            ErrorHelper.show(error);
        }
        return result;
    }
}
