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

package org.openvpms.web.workspace.product;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.product.ProductQuery;
import org.openvpms.web.component.im.util.UserHelper;


/**
 * An {@link ProductQuery} that allows the pricing location to be set if the current user is an administrator.
 *
 * @author Tim Anderson
 */
public class PricingLocationProductQuery extends ProductQuery {

    /**
     * Determines if the current user is an administrator.
     */
    private final boolean admin;

    /**
     * The pricing location listener. May be {@code null}
     */
    private PricingLocationListener pricingLocationListener;

    public interface PricingLocationListener {

        void onLocationChanged();
    }

    /**
     * Constructs a {@link PricingLocationProductQuery} that queries products with the specified short names.
     *
     * @param shortNames the short names
     * @param context    the context
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public PricingLocationProductQuery(String[] shortNames, Context context) {
        super(shortNames, context);
        admin = UserHelper.isAdmin(context.getUser());
    }

    /**
     * Registers a listener for pricing location changes.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setPricingLocationListener(PricingLocationListener listener) {
        pricingLocationListener = listener;
    }

    /**
     * Lays out the component in a container.
     * <p/>
     * If the current user is an administrator, and pricing locations are configured, this also adds a filter to
     * select the pricing location.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        if (admin && LookupServiceHelper.getLookupService().getLookups("lookup.pricingLocation").size() != 0) {
            addPricingLocationSelector(container);
        }
    }

    /**
     * Invoked when the pricing location changes.
     *
     * @param location the selected pricing location
     */
    @Override
    protected void onPricingLocationChanged(Lookup location) {
        super.onPricingLocationChanged(location);
        if (pricingLocationListener != null) {
            pricingLocationListener.onLocationChanged();
        }
    }
}
