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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.discount.DiscountTestHelper;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MEDICATION;

/**
 * Tests the {@link EstimateEditor}.
 *
 * @author Tim Anderson
 */
public class EstimateEditorTestCase extends AbstractEstimateEditorTestCase {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The context.
     */
    private Context context;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();

        context = new LocalContext();
        context.setPractice(getPractice());
        context.setCustomer(customer);
        context.setClinician(TestHelper.createClinician());
        context.setUser(TestHelper.createUser());
    }

    /**
     * Tests template expansion.
     */
    @Test
    public void testTemplateExpansion() {
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient(customer);
        PatientTestHelper.createWeight(patient, new Date(), new BigDecimal("4.2"), WeightUnits.KILOGRAMS);
        Entity discount = DiscountTestHelper.createDiscount(TEN, true, DiscountRules.PERCENTAGE);

        BigDecimal fixedPrice = ONE;
        BigDecimal unitPrice = ONE;
        Product template = ProductTestHelper.createTemplate("templateA");

        // product1 has a dose, which should be selected over the template include quantity
        Product product1 = createProduct(MEDICATION, fixedPrice, unitPrice);
        IMObjectBean productBean = new IMObjectBean(product1);
        productBean.setValue("concentration", ONE);
        ProductTestHelper.addDose(product1, ProductTestHelper.createDose(null, ZERO, TEN, ONE));

        Product product2 = createProduct(MEDICATION, fixedPrice, unitPrice);
        Product product3 = createProduct(MEDICATION, fixedPrice, unitPrice);
        addDiscount(product3, discount);
        addDiscount(customer, discount);                           // give customer a discount for product3
        ProductTestHelper.addInclude(template, product1, 0, 2, false);
        ProductTestHelper.addInclude(template, product2, 2, 4, false);
        ProductTestHelper.addInclude(template, product3, 3, 6, true); // zero price

        Act estimate = (Act) TestHelper.create(EstimateArchetypes.ESTIMATE);
        EstimateEditor editor = new EstimateEditor(estimate, null, layout);
        editor.getComponent();
        assertFalse(editor.isValid());
        IMObjectEditor itemEditor = editor.getItems().add();
        assertTrue(itemEditor instanceof EstimateItemEditor);
        EstimateItemEditor estimateItemEditor = (EstimateItemEditor) itemEditor;
        estimateItemEditor.setPatient(patient);
        estimateItemEditor.setProduct(template);

        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));

        estimate = get(estimate);
        ActBean bean = new ActBean(estimate);
        List<Act> items = bean.getNodeActs("items");
        assertEquals(3, items.size());

        User author = context.getUser();
        BigDecimal three = BigDecimal.valueOf(3);
        BigDecimal five = BigDecimal.valueOf(5);

        checkEstimate(estimate, customer, author, new BigDecimal("4.00"), new BigDecimal("10.20"));
        checkItem(items, patient, product1, author, ZERO, new BigDecimal("4.2"), unitPrice, unitPrice,
                  fixedPrice, ZERO, ZERO, ONE, new BigDecimal("5.20"));
        checkItem(items, patient, product2, author, 2, 4, unitPrice, unitPrice, fixedPrice, ZERO, ZERO, three, five);
        checkItem(items, patient, product3, author, 3, 6, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }

    /**
     * Verifies an estimate matches that expected.
     *
     * @param estimate  the estimate to check
     * @param customer  the expected customer
     * @param author    the expected author
     * @param lowTotal  the expected low total
     * @param highTotal the expected high total
     */
    private void checkEstimate(Act estimate, Party customer, User author, BigDecimal lowTotal, BigDecimal highTotal) {
        ActBean bean = new ActBean(estimate);
        assertEquals(customer.getObjectReference(), bean.getNodeParticipantRef("customer"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        checkEquals(lowTotal, bean.getBigDecimal("lowTotal"));
        checkEquals(highTotal, bean.getBigDecimal("highTotal"));
    }
}
