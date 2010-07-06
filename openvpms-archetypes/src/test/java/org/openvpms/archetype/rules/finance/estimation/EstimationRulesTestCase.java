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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.estimation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.EstimationActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
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


/**
 * Tests the {@link EstimationRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EstimationRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private EstimationRules rules;


    /**
     * Tests the {@link EstimationRules#copy(Act)} method.
     */
    @Test
    public void testCopy() {
        Party customer = TestHelper.createCustomer();
        Product product = TestHelper.createProduct();
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createClinician();
        BigDecimal fixedPrice = new BigDecimal("10.00");

        Act item = createEstimationItem(patient, product, author, fixedPrice);
        Act estimation = createEstimation(customer, author, item);
        estimation.setStatus(EstimationActStatus.IN_PROGRESS);
        save(estimation, item);

        ActBean bean = new ActBean(estimation);
        ActBean itemBean = new ActBean(item);

        String title = "Copy";
        Act copy = rules.copy(estimation, title);
        assertTrue(copy.getId() != estimation.getId());
        ActBean copyBean = new ActBean(copy);

        assertEquals(title, copy.getTitle());
        assertEquals(EstimationActStatus.COMPLETED, copy.getStatus());
        checkParticipantRef(customer, copyBean, "customer");
        checkParticipantRef(author, copyBean, "author");

        checkEquals(bean.getBigDecimal("lowTotal"),
                     copyBean.getBigDecimal("lowTotal"));
        checkEquals(bean.getBigDecimal("highTotal"),
                     copyBean.getBigDecimal("highTotal"));

        List<Act> acts = copyBean.getActs();
        assertEquals(1, acts.size());
        Act itemCopy = acts.get(0);
        assertTrue(itemCopy.getId() != item.getId());
        ActBean itemCopyBean = new ActBean(itemCopy);

        checkEquals(itemBean.getBigDecimal("fixedPrice"),
                     itemCopyBean.getBigDecimal("fixedPrice"));
        checkEquals(itemBean.getBigDecimal("lowTotal"),
                     itemCopyBean.getBigDecimal("lowTotal"));
        checkEquals(itemBean.getBigDecimal("highTotal"),
                     itemCopyBean.getBigDecimal("highTotal"));

        checkParticipantRef(patient, itemCopyBean, "patient");
        checkParticipantRef(product, itemCopyBean, "product");
    }

    /**
     * Tests the {@link EstimationRules#invoice(Act, User)} method.
     */
    @Test
    public void testInvoice() {
        Party customer = TestHelper.createCustomer();
        Product product = TestHelper.createProduct();
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createClinician();
        User clinician = TestHelper.createClinician();
        BigDecimal fixedPrice = new BigDecimal("10.00");
        Act item = createEstimationItem(patient, product, author, fixedPrice);
        Act estimation = createEstimation(customer, author, item);

        save(estimation, item);

        FinancialAct invoice = rules.invoice(estimation, clinician);
        assertEquals(ActStatus.IN_PROGRESS, invoice.getStatus());
        assertEquals(EstimationActStatus.INVOICED, estimation.getStatus());

        ActBean bean = new ActBean(invoice);
        ActBean estimationBean = new ActBean(estimation);
        ActBean estimationItemBean = new ActBean(item);
        List<FinancialAct> items
                = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(1, items.size());
        ActBean itemBean = new ActBean(items.get(0));
        checkEquals(estimationBean.getBigDecimal("highTotal"),
                     bean.getBigDecimal("amount"));

        checkParticipantRef(customer, bean, "customer");
        checkParticipantRef(author, bean, "author");
        checkParticipantRef(clinician, bean, "clinician");

        checkParticipantRef(patient, itemBean, "patient");
        checkParticipantRef(product, itemBean, "product");
        checkParticipantRef(author, itemBean, "author");
        checkParticipantRef(clinician, itemBean, "clinician");

        checkEquals(itemBean.getBigDecimal("total"),
                     estimationItemBean.getBigDecimal("highTotal"));

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
    private void checkParticipantRef(Entity expected, ActBean bean,
                                     String node) {
        Entity entity = bean.getNodeParticipant(node);
        assertNotNull(entity);
        assertEquals(expected.getId(), entity.getId());
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new EstimationRules();
    }

    /**
     * Creates an estimation.
     *
     * @param customer the customer
     * @param author   the author
     * @param item     the estimation item
     * @return a new estimation
     */
    private Act createEstimation(Party customer, User author, Act item) {
        Act estimation = (Act) create(EstimationArchetypes.ESTIMATION);
        ActBean bean = new ActBean(estimation);
        ActBean itemBean = new ActBean(item);
        bean.setParticipant(CustomerArchetypes.CUSTOMER_PARTICIPATION,
                            customer);
        bean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION, author);
        bean.addRelationship(EstimationArchetypes.ESTIMATION_ITEM_RELATIONSHIP,
                             item);
        bean.setValue("highTotal", itemBean.getBigDecimal("highTotal"));
        bean.setValue("lowTotal", itemBean.getBigDecimal("lowTotal"));
        return estimation;
    }

    /**
     * Creates an estimation item.
     *
     * @param patient    the patient
     * @param product    the product
     * @param author     the author
     * @param fixedPrice the fixed price
     * @return a new estimation item
     */
    private Act createEstimationItem(Party patient, Product product,
                                     User author, BigDecimal fixedPrice) {
        Act item = (Act) create(EstimationArchetypes.ESTIMATION_ITEM);
        ActBean itemBean = new ActBean(item);
        itemBean.setParticipant(PatientArchetypes.PATIENT_PARTICIPATION,
                                patient);
        itemBean.setParticipant(ProductArchetypes.PRODUCT_PARTICIPATION,
                                product);
        itemBean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION,
                                author);
        itemBean.setValue("fixedPrice", fixedPrice);
        getArchetypeService().deriveValues(item);
        return item;
    }
}
