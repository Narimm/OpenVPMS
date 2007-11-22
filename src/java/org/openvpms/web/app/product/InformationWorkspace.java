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

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.app.customer.account.AccountCRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWorkspace;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.resource.util.Messages;


/**
 * Product information workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class InformationWorkspace extends CRUDWorkspace<Product> {

    /**
     * Construct a new <tt>InformationWorkspace</tt>.
     */
    public InformationWorkspace() {
        super("product", "info", new ShortNameList("product.*"));
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
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <tt>null</tt>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof Product) {
            setObject((Product) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + Product.class.getName());
        }
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        Product product = GlobalContext.getInstance().getProduct();
        if (product != getObject()) {
            setObject(product);
        }
    }
    /**
     * Creates a new CRUD window for viewing and editing Products.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Product> createCRUDWindow() {
        return new ProductCRUDWindow(getType(), getShortNames());
    }

}
