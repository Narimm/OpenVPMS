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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.invoice;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * Rules for saving <em>act.customerAccountInvoiceItem</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class InvoiceItemSaveRules {

    /**
     * Helper for the item.
     */
    private final ActBean itemBean;

    /**
     * Helper for the product.
     */
    private EntityBean productBean;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an <tt>InvoiceItemSaveRules</tt>.
     *
     * @param act     the invoice item act
     * @param service the archetype service
     */
    public InvoiceItemSaveRules(Act act, IArchetypeService service) {
        this.service = service;
        if (!TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            throw new IllegalArgumentException("Invalid argument 'act'");
        }
        itemBean = new ActBean(act, service);
        Product product = (Product) itemBean.getParticipant(ProductArchetypes.PRODUCT_PARTICIPATION);
        if (product != null) {
            productBean = new EntityBean(product, service);
        }
    }

    /**
     * Invoked after the invoice item is saved. This:
     * <ul>
     * <li>processes any demographic updates associated with the product</li>
     * </ul>
     * Note that the dispensing acts must be saved <em>prior</em> to the invoice
     * item.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void save() {
        if (productBean != null) {
            DemographicUpdateHelper helper = new DemographicUpdateHelper(itemBean, productBean, service);
            helper.processDemographicUpdates();
        }
    }

}
