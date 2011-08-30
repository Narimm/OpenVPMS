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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.supplier.delivery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.test.AbstractAppTest;


/**
 * Tests the {@link DeliveryItemEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DeliveryItemEditorTestCase extends AbstractAppTest {

    /**
     * Tests validation.
     */
    @Test
    public void testValidation() {
        DefaultLayoutContext context = new DefaultLayoutContext();
        Lookup each = TestHelper.getLookup("lookup.uom", "EACH");

        Act delivery = (Act) create(SupplierArchetypes.DELIVERY);
        FinancialAct item = (FinancialAct) create(SupplierArchetypes.DELIVERY_ITEM);
        DeliveryItemEditor editor = new DeliveryItemEditor(item, delivery, context);
        assertFalse(editor.isValid());

        editor.setAuthor(TestHelper.createUser());
        assertTrue(editor.isValid());

        delivery.setStatus(ActStatus.POSTED);
        assertFalse(editor.isValid());

        Product product = TestHelper.createProduct();
        editor.setProduct(product);
        editor.setPackageSize(1);
        editor.setPackageUnits(each.getCode());
        assertTrue(editor.isValid());

        editor.getProperty("packageSize").setValue(null);
        editor.setPackageUnits(null);
        assertFalse(editor.isValid());
    }
}
