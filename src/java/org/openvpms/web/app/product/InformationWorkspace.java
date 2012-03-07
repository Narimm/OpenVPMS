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

package org.openvpms.web.app.product;

import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ResultSetCRUDWorkspace;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.QueryBrowser;


/**
 * Product information workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class InformationWorkspace extends ResultSetCRUDWorkspace<Product> {

    /**
     * Constructs an <tt>InformationWorkspace</tt>.
     */
    public InformationWorkspace() {
        super("product", "info");
        setArchetypes(Product.class, "product.*");
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Product object) {
        super.setObject(object);
        GlobalContext.getInstance().setProduct(object);
    }

    /**
     * Returns the latest version of the current context object.
     *
     * @return the latest version of the context object, or {@link #getObject()}
     *         if they are the same, or <tt>null</tt> if the context object is
     *         not supported by the workspace
     */
    @Override
    protected Product getLatest() {
        return getLatest(GlobalContext.getInstance().getProduct());
    }

    /**
     * Creates a new CRUD window for viewing and editing Products.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Product> createCRUDWindow() {
        QueryBrowser<Product> browser = getBrowser();
        return new ProductCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet());
    }

}
