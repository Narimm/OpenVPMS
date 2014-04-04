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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;

import java.util.List;

/**
 * Filters prices based on the selected pricing location.
 *
 * @author Tim Anderson
 */
public class PricingLocationFilter {

    /**
     * The selected pricing location.
     */
    private Lookup pricingLocation;

    /**
     * Determines if filtering is required.
     */
    private boolean needsFilter;

    /**
     * Determines if prices for all pricing locations should be displayed.
     */
    private boolean showAll;

    /**
     * Listener to notify
     */
    private ActionListener listener;


    /**
     * Constructs a {@link PricingLocationFilter}.
     *
     * @param context the layout context
     */
    public PricingLocationFilter(LayoutContext context) {
        pricingLocation = PricingLocationHelper.getPricingLocation(context.getContext());
        needsFilter = pricingLocation != null || PricingLocationHelper.pricingLocationsConfigured();
        showAll = pricingLocation == null;
    }

    /**
     * Determines if prices need to be filtered.
     *
     * @return {@code true} if prices need to be filtered by pricing location
     */
    public boolean needsFilter() {
        return needsFilter;
    }

    /**
     * Determines if prices for all pricing locations should be displayed.
     *
     * @return {@code true} if prices for all pricing locations should be displayed, {@code false} if the prices for
     *         the selected pricing location should be displayed
     */
    public boolean showAll() {
        return showAll;
    }

    /**
     * Returns the selected pricing location.
     *
     * @return the selected pricing location. May be {@code null}
     */
    public Lookup getPricingLocation() {
        return pricingLocation;
    }

    /**
     * Registers a listener to be notified when the pricing location changes.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    /**
     * Filters the prices according to the selected pricing location.
     *
     * @param prices the prices to filter
     * @return the filtered prices
     */
    public List<IMObject> getPrices(List<IMObject> prices) {
        if (!showAll) {
            return PricingLocationHelper.filterPrices(prices, pricingLocation);
        }
        return prices;
    }

    /**
     * Returns the filter component.
     *
     * @return the filter component
     */
    public Component getComponent() {
        final PricingLocationSelectField filter = new PricingLocationSelectField(pricingLocation, true);
        filter.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                pricingLocation = filter.getSelected();
                showAll = filter.isAllSelected();
                if (listener != null) {
                    listener.onAction(new ActionEvent(this, null));
                }
            }
        });
        return RowFactory.create(Styles.CELL_SPACING, LabelFactory.create("product.pricingLocation"), filter);
    }

}
