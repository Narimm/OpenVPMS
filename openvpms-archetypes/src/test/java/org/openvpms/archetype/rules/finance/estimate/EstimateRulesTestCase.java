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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.estimate;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link EstimateRules} class.
 *
 * @author Tim Anderson
 */
public class EstimateRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private EstimateRules rules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new EstimateRules(getArchetypeService());
    }

    /**
     * Tests the {@link EstimateRules#copy(Act)} method.
     */
    @Test
    public void testCopy() {
        Party customer = TestHelper.createCustomer();
        Product product = TestHelper.createProduct();
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createClinician();
        BigDecimal fixedPrice = new BigDecimal("10.00");

        Act item = EstimateTestHelper.createEstimateItem(patient, product, author, fixedPrice);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);
        estimate.setStatus(EstimateActStatus.IN_PROGRESS);
        save(estimate, item);

        ActBean bean = new ActBean(estimate);
        ActBean itemBean = new ActBean(item);

        String title = "Copy";
        Act copy = rules.copy(estimate, title);
        assertTrue(copy.getId() != estimate.getId());
        ActBean copyBean = new ActBean(copy);

        assertEquals(title, copy.getTitle());
        assertEquals(EstimateActStatus.COMPLETED, copy.getStatus());
        checkParticipantRef(customer, copyBean, "customer");
        checkParticipantRef(author, copyBean, "author");

        checkEquals(bean.getBigDecimal("lowTotal"), copyBean.getBigDecimal("lowTotal"));
        checkEquals(bean.getBigDecimal("highTotal"), copyBean.getBigDecimal("highTotal"));

        List<Act> acts = copyBean.getActs();
        assertEquals(1, acts.size());
        Act itemCopy = acts.get(0);
        assertTrue(itemCopy.getId() != item.getId());
        ActBean itemCopyBean = new ActBean(itemCopy);

        checkEquals(itemBean.getBigDecimal("fixedPrice"), itemCopyBean.getBigDecimal("fixedPrice"));
        checkEquals(itemBean.getBigDecimal("lowTotal"), itemCopyBean.getBigDecimal("lowTotal"));
        checkEquals(itemBean.getBigDecimal("highTotal"), itemCopyBean.getBigDecimal("highTotal"));

        checkParticipantRef(patient, itemCopyBean, "patient");
        checkParticipantRef(product, itemCopyBean, "product");
    }

    /**
     * Tests the {@link EstimateRules#invoice(Act, User)} method.
     */
    @Test
    public void testInvoice() {
        Party customer = TestHelper.createCustomer();
        Product product = TestHelper.createProduct();
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createClinician();
        User clinician = TestHelper.createClinician();
        BigDecimal fixedPrice = new BigDecimal("10.00");
        Act item = EstimateTestHelper.createEstimateItem(patient, product, author, fixedPrice);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        save(estimate, item);

        FinancialAct invoice = rules.invoice(estimate, clinician);
        assertEquals(ActStatus.IN_PROGRESS, invoice.getStatus());
        assertEquals(EstimateActStatus.INVOICED, estimate.getStatus());

        ActBean bean = new ActBean(invoice);
        ActBean estimateBean = new ActBean(estimate);
        ActBean estimateItemBean = new ActBean(item);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(1, items.size());
        ActBean itemBean = new ActBean(items.get(0));
        checkEquals(estimateBean.getBigDecimal("highTotal"), bean.getBigDecimal("amount"));

        checkParticipantRef(customer, bean, "customer");
        checkParticipantRef(author, bean, "author");
        checkParticipantRef(clinician, bean, "clinician");

        checkParticipantRef(patient, itemBean, "patient");
        checkParticipantRef(product, itemBean, "product");
        checkParticipantRef(author, itemBean, "author");
        checkParticipantRef(clinician, itemBean, "clinician");

        checkEquals(itemBean.getBigDecimal("total"), estimateItemBean.getBigDecimal("highTotal"));

        // check the dispensing act
        List<Act> dispensing = itemBean.getNodeActs("dispensing");
        assertEquals(1, dispensing.size());
        ActBean medicationBean = new ActBean(dispensing.get(0));
        checkParticipantRef(patient, medicationBean, "patient");
        checkParticipantRef(product, medicationBean, "product");
        checkParticipantRef(author, medicationBean, "author");
    }

    /**
     * Verifies a participant node references the expected entity.
     *
     * @param expected the expected object
     * @param bean     wraps the act
     * @param node     the participant node
     */
    private void checkParticipantRef(Entity expected, ActBean bean, String node) {
        Entity entity = bean.getNodeParticipant(node);
        assertNotNull(entity);
        assertEquals(expected.getId(), entity.getId());
    }

}
