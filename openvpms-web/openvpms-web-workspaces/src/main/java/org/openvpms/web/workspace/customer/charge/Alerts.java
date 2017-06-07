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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks alerts associated with an invoice, to avoid duplicate alerts being generated.
 *
 * @author Tim Anderson
 */
public class Alerts {

    /**
     * The alerts, keyed on reference.
     */
    private final Map<IMObjectReference, Act> alerts = new HashMap<>();


    /**
     * Constructs an {@link Alerts}.
     */
    public Alerts() {
        super();
    }

    /**
     * Adds an alert.
     *
     * @param alert the alert to add
     */
    public void add(Act alert) {
        alerts.put(alert.getObjectReference(), alert);
    }

    /**
     * Removes an alert.
     *
     * @param alert the alert to remove
     */
    public void remove(Act alert) {
        alerts.remove(alert.getObjectReference());

    }

    /**
     * Returns new alerts from each of the charge items.
     *
     * @return a list of new alerts
     */
    public List<Act> getNewAlerts() {
        List<Act> result = new ArrayList<>();
        for (Act alert : alerts.values()) {
            if (alert.isNew()) {
                result.add(alert);
            }
        }
        return result;
    }

    /**
     * Returns the alert types associated with a product.
     *
     * @param product the product
     * @return the alert types
     */
    public List<Entity> getAlertTypes(Product product) {
        List<Entity> result;
        EntityBean bean = new EntityBean(product);
        if (bean.hasNode("alerts")) {
            result = bean.getNodeTargetEntities("alerts");
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Determines if a patient has an IN_PROGRESS alert of the specified alert type, active at the current time.
     *
     * @param patient   the patient
     * @param alertType the alert
     * @return {@code true} if the patient has the alert
     */
    public boolean hasAlert(Party patient, Entity alertType) {
        IMObjectReference patientRef = patient.getObjectReference();
        IMObjectReference alertRef = alertType.getObjectReference();
        Date now = new Date();
        for (Act alert : alerts.values()) {
            ActBean bean = new ActBean(alert);
            if (ActStatus.IN_PROGRESS.equals(alert.getStatus())
                && (alert.getActivityEndTime() == null || DateRules.compareTo(alert.getActivityEndTime(), now) > 0)
                && ObjectUtils.equals(patientRef, bean.getNodeParticipantRef("patient"))
                && ObjectUtils.equals(alertRef, bean.getNodeParticipantRef("alertType"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes an invoice item.
     * <p/>
     * If the item is linked to an alert, the alert is removed
     *
     * @param item the invoice item
     */
    public void removeItem(FinancialAct item) {
        ActBean bean = new ActBean(item);
        if (bean.hasNode("alerts")) {
            for (IMObjectReference alert : bean.getNodeTargetObjectRefs("alerts")) {
                alerts.remove(alert);
            }
        }
    }

}
