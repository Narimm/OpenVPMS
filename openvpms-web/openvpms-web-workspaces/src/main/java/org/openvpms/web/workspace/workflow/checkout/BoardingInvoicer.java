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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.workflow.CageType;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.workspace.customer.charge.AbstractInvoicer;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Charges boarding for one or more visits.
 *
 * @author Tim Anderson
 */
class BoardingInvoicer extends AbstractInvoicer {

    /**
     * Invoices boarding charges for each patient visit.
     * <p/>
     * If visits have not ended, they are charged up to the current time.
     *
     * @param visits the visits
     * @param editor the invoice editor
     */
    public void invoice(Visits visits, CustomerChargeActEditor editor) {
        invoice(visits, editor, new Date());
    }

    /**
     * Invoices boarding charges for each patient visit.
     *
     * @param visits  the visits
     * @param endTime the boarding end time, if an event hasn't already ended
     * @param editor  the invoice editor
     */
    public void invoice(Visits visits, CustomerChargeActEditor editor, Date endTime) {
        for (Visit visit : visits) {
            if (!visit.isCharged()) {
                CageType cageType = visit.getCageType();
                Date visitEndTime = visit.getEndTime(endTime);
                if (cageType != null) {
                    chargeBoarding(visit, visitEndTime, editor);
                    if (cageType.isLateCheckout(visitEndTime)) {
                        chargeLateCheckout(visit, editor);
                    }
                    visit.setCharged(true);
                }
            }
        }
    }

    /**
     * Charges boarding.
     *
     * @param visit   the visit
     * @param endTime the boarding end time, if the event hasn't already ended
     * @param editor  the invoice editor
     */
    private void chargeBoarding(Visit visit, Date endTime, CustomerChargeActEditor editor) {
        CageType cageType = visit.getCageType();
        int days = visit.getDays(endTime);
        boolean overnight = days > 1 || visit.isOvernight(endTime);
        Product product = cageType.getProduct(days, overnight, visit.isFirstPet());
        if (product != null) {
            BigDecimal quantity = BigDecimal.valueOf(days);
            addItem(visit.getPatient(), product, quantity, editor);
        }
    }

    /**
     * Charges a late checkout.
     *
     * @param visit  the visit
     * @param editor the charge editor
     */
    private void chargeLateCheckout(Visit visit, CustomerChargeActEditor editor) {
        CageType cageType = visit.getCageType();
        Product product = cageType.getLateCheckoutProduct();
        if (product != null) {
            addItem(visit.getPatient(), product, BigDecimal.ONE, editor);
        }
    }

    /**
     * Adds an invoice item.
     *
     * @param patient  the patient
     * @param product  the product
     * @param quantity the quantity
     * @param editor   the invoice editor
     */
    private void addItem(Party patient, Product product, BigDecimal quantity, CustomerChargeActEditor editor) {
        CustomerChargeActItemEditor itemEditor = getItemEditor(editor);
        itemEditor.setPatient(patient);
        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            editor.getItems().expandTemplate(itemEditor, product, quantity);
        } else {
            itemEditor.setProduct(product);
            itemEditor.setQuantity(quantity);
        }
    }
}
