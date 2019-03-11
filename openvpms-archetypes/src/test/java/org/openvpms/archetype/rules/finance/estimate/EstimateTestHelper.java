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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.estimate;

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.product.Product;
import org.openvpms.component.model.user.User;

import java.math.BigDecimal;

/**
 * Estimate test helper methods.
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
    public static Act createEstimate(Party customer, User author, org.openvpms.component.model.act.Act... items) {
        Act estimate = (Act) TestHelper.create(EstimateArchetypes.ESTIMATE);
        IMObjectBean bean = new IMObjectBean(estimate);
        bean.setTarget("customer", customer);
        bean.setTarget("author", author);
        BigDecimal lowTotal = BigDecimal.ZERO;
        BigDecimal highTotal = BigDecimal.ZERO;
        for (org.openvpms.component.model.act.Act item : items) {
            IMObjectBean itemBean = new IMObjectBean(item);
            Relationship relationship = bean.addTarget("items", item);
            item.addActRelationship((ActRelationship) relationship);
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
        IMObjectBean bean = createEstimateItem(patient, product, author);
        bean.setValue("fixedPrice", fixedPrice);
        Act item = (Act) bean.getObject();
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
        IMObjectBean bean = createEstimateItem(patient, product, author);
        bean.setValue("highQty", quantity);
        bean.setValue("highUnitPrice", unitPrice);
        Act item = (Act) bean.getObject();
        ArchetypeServiceHelper.getArchetypeService().deriveValues(item);
        return item;
    }

    /**
     * Creates an estimate item.
     *
     * @param patient   the patient
     * @param product   the product
     * @param template  the template
     * @param author    the author
     * @param quantity  the quantity
     * @param unitPrice the unit price
     * @return a new estimation item
     */
    public static Act createEstimateItem(Party patient, Product product, Product template, User author,
                                         BigDecimal quantity, BigDecimal unitPrice) {
        Act act = createEstimateItem(patient, product, author, quantity, unitPrice);
        if (template != null) {
            IMObjectBean bean = new IMObjectBean(act);
            bean.setTarget("template", template);
        }
        return act;
    }

    /**
     * Creates an estimate item.
     *
     * @param patient the patient
     * @param product the product
     * @param author  the author
     * @return a bean wrapping the estimate item
     */
    private static IMObjectBean createEstimateItem(Party patient, Product product, User author) {
        Act item = (Act) TestHelper.create(EstimateArchetypes.ESTIMATE_ITEM);
        IMObjectBean bean = new IMObjectBean(item);
        bean.setTarget("patient", patient);
        bean.setTarget("product", product);
        bean.setTarget("author", author);
        return bean;
    }

}
