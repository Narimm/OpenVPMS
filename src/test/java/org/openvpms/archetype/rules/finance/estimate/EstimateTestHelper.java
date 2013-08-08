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

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class EstimateTestHelper {

    /**
     * Creates an estimate.
     *
     * @param customer the customer
     * @param author   the author
     * @param items    the estimate items
     * @return a new estimate
     */
    public static Act createEstimate(Party customer, User author, Act... items) {
        Act estimate = (Act) TestHelper.create(EstimateArchetypes.ESTIMATE);
        ActBean bean = new ActBean(estimate);
        BigDecimal lowTotal = BigDecimal.ZERO;
        BigDecimal highTotal = BigDecimal.ZERO;
        for (Act item : items) {
            ActBean itemBean = new ActBean(item);
            bean.setParticipant(CustomerArchetypes.CUSTOMER_PARTICIPATION, customer);
            bean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION, author);
            bean.addRelationship(EstimateArchetypes.ESTIMATE_ITEM_RELATIONSHIP, item);
            highTotal = highTotal.add(itemBean.getBigDecimal("highTotal"));
            lowTotal = lowTotal.add(itemBean.getBigDecimal("lowTotal"));
        }
        bean.setValue("highTotal", highTotal);
        bean.setValue("lowTotal", lowTotal);
        return estimate;
    }

    /**
     * Creates an estimate item.
     *
     * @param patient    the patient
     * @param product    the product
     * @param author     the author
     * @param fixedPrice the fixed price
     * @return a new estimate item
     */
    public static Act createEstimateItem(Party patient, Product product, User author, BigDecimal fixedPrice) {
        ActBean bean = createEstimateItem(patient, product, author);
        bean.setValue("fixedPrice", fixedPrice);
        Act item = bean.getAct();
        ArchetypeServiceHelper.getArchetypeService().deriveValues(item);
        return item;
    }

    /**
     * Creates an estimate item.
     *
     * @param patient   the patient
     * @param product   the product
     * @param author    the author
     * @param quantity  the quantity
     * @param unitPrice the unit price
     * @return a new estimation item
     */
    public static Act createEstimateItem(Party patient, Product product, User author, BigDecimal quantity,
                                         BigDecimal unitPrice) {
        ActBean bean = createEstimateItem(patient, product, author);
        bean.setValue("highQty", quantity);
        bean.setValue("highUnitPrice", unitPrice);
        Act item = bean.getAct();
        ArchetypeServiceHelper.getArchetypeService().deriveValues(item);
        return item;
    }

    /**
     * Creates an estimate item.
     *
     * @param patient the patient
     * @param product the product
     * @param author  the author
     * @return a bean wrapping the estimate item
     */
    private static ActBean createEstimateItem(Party patient, Product product, User author) {
        Act item = (Act) TestHelper.create(EstimateArchetypes.ESTIMATE_ITEM);
        ActBean bean = new ActBean(item);
        bean.setParticipant(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.setParticipant(ProductArchetypes.PRODUCT_PARTICIPATION, product);
        bean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION, author);
        return bean;
    }

}
