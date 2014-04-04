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

import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;


/**
 * Product information workspace.
 *
 * @author Tim Anderson
 */
public class InformationWorkspace extends ResultSetCRUDWorkspace<Product> {

    /**
     * Constructs an {@code InformationWorkspace}.
     *
     * @param context the context
     */
    public InformationWorkspace(Context context) {
        super("product", "info", context);
        setArchetypes(Product.class, "product.*");
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Product object) {
        super.setObject(object);
        getContext().setProduct(object);
    }

    /**
     * Returns the latest version of the current context object.
     *
     * @return the latest version of the context object, or {@link #getObject()} if they are the same, or {@code null}
     *         if the context object is not supported by the workspace
     */
    @Override
    protected Product getLatest() {
        return getLatest(getContext().getProduct());
    }

    /**
     * Creates a new CRUD window for viewing and editing Products.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Product> createCRUDWindow() {
        QueryBrowser<Product> browser = getBrowser();
        return new ProductCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(), getContext(),
                                     getHelpContext());
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<Product> createQuery() {
        return new PricingLocationProductQuery(getArchetypes().getShortNames(), getContext());
    }

    /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Product> createBrowser(Query<Product> query) {
        DefaultLayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        return new PricingLocationProductBrowser((PricingLocationProductQuery) query, context);
    }
}
