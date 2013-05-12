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
 */

package org.openvpms.web.app.supplier.order;

import org.junit.Test;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link OrderItemEditor} class.
 *
 * @author Tim Anderson
 */
public class OrderItemEditorTestCase extends AbstractAppTest {

    /**
     * Tests validation.
     */
    @Test
    public void testValidation() {
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));

        Act order = (Act) create(SupplierArchetypes.ORDER);
        FinancialAct item = (FinancialAct) create(SupplierArchetypes.ORDER_ITEM);
        OrderItemEditor editor = new OrderItemEditor(item, order, context);
        assertFalse(editor.isValid());

        editor.setAuthor(TestHelper.createUser());
        assertFalse(editor.isValid());

        Product product = TestHelper.createProduct();
        editor.setProduct(product);
        assertTrue(editor.isValid());

        editor.setProduct(null);
        assertFalse(editor.isValid());

        editor.setProduct(product);
        assertTrue(editor.isValid());

        // verify that cancelled must be <= quantity
        editor.setQuantity(BigDecimal.ONE);
        editor.setCancelledQuantity(new BigDecimal(2));
        assertFalse(editor.isValid());

        editor.setCancelledQuantity(BigDecimal.ONE);
        assertTrue(editor.isValid());
    }
}