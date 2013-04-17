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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.app;

import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.system.ServiceHelper;


/**
 * Context helper.
 *
 * @author Tim Anderson
 */
public class ContextHelper {

    /**
     * Owner entity relationship.
     */
    private static final String OWNER = "entityRelationship.patientOwner";


    /**
     * Sets the context customer. If the context patient doesn't have a relationship to it, sets it to {@code null}.
     *
     * @param context  the context
     * @param customer the customer
     */
    public static void setCustomer(Context context, Party customer) {
        context.setCustomer(customer);
        if (customer != null) {
            Party patient = context.getPatient();
            if (patient != null) {
                IMObjectReference ref = patient.getObjectReference();
                boolean found = false;
                for (EntityRelationship relationship : customer.getEntityRelationships()) {
                    IMObjectReference target = relationship.getTarget();
                    if (target != null && target.equals(ref)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    context.setPatient(null);
                }
            }
        }
    }

    /**
     * Sets the context patient. If the patient doesn't have a  relationship to the current customer, sets the customer
     * to the patient's owner.
     *
     * @param context the context
     * @param patient the patient. May be {@code null}
     */
    public static void setPatient(Context context, Party patient) {
        context.setPatient(patient);
        if (patient != null) {
            Party customer = context.getCustomer();
            boolean found = false;
            if (customer != null) {
                IMObjectReference ref = customer.getObjectReference();
                for (EntityRelationship relationship :
                        patient.getEntityRelationships()) {
                    IMObjectReference source = relationship.getSource();
                    if (source != null && source.equals(ref)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                // look for an active patient-owner relationship
                for (EntityRelationship relationship :
                        patient.getEntityRelationships()) {
                    IMObjectReference source = relationship.getSource();
                    if (TypeHelper.isA(relationship, OWNER)
                            && relationship.isActive()) {
                        Party owner = (Party) IMObjectHelper.getObject(source, context);
                        if (owner != null) {
                            context.setCustomer(owner);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the currency associated with the context's practice.
     *
     * @return the currency, or {@code null} if none is found.
     */
    public static Currency getPracticeCurrency(Context context) {
        Currency result = null;
        Party practice = context.getPractice();
        if (practice != null) {
            IMObjectBean bean = new IMObjectBean(practice);
            String code = bean.getString("currency");
            if (code != null) {
                Currencies currencies = ServiceHelper.getCurrencies();
                result = currencies.getCurrency(code);
            }
        }
        return result;
    }

}
