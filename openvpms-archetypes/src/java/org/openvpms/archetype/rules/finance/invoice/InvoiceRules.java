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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.invoice;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Invoice rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Statuses of <em>act.patientInvestigation</em> acts that should be retained even if their associated
     * invoice item is deleted.
     */
    private static final String[] INVESTIGATION_STATUSES = {InvestigationActStatus.COMPLETED,
                                                            InvestigationActStatus.RECEIVED,
                                                            InvestigationActStatus.PRELIMINARY,
                                                            InvestigationActStatus.FINAL};

    /**
     * Statuses of <em>act.patientReminder</em> acts that should be retained.
     */
    private static final String[] REMINDER_STATUSES = {ActStatus.COMPLETED};

    /**
     * Statuses of <em>act.patientDocument*</em> acts that should be retained.
     */
    private static final String[] DOCUMENT_STATUSES = {ActStatus.COMPLETED, ActStatus.POSTED};


    /**
     * Creates a new <tt>InvoiceRules</tt>.
     *
     * @param service the archetype service
     */
    public InvoiceRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Invoked after an invoice item has been saved. Updates any reminders
     * and documents associated with the product.
     *
     * @param act the act
     */
    public void saveInvoiceItem(FinancialAct act) {
        InvoiceItemSaveRules rules = new InvoiceItemSaveRules(act, service);
        rules.save();
    }

    /**
     * Invoked <em>after</em> an invoice is saved. Processes any demographic
     * updates if the invoice is 'Posted'.
     *
     * @param invoice the invoice
     */
    public void saveInvoice(FinancialAct invoice) {
        if (!TypeHelper.isA(invoice, CustomerAccountArchetypes.INVOICE)) {
            throw new IllegalArgumentException("Invalid argument 'invoice'");
        }
        if (ActStatus.POSTED.equals(invoice.getStatus())) {
            ActBean bean = new ActBean(invoice, service);
            List<Act> acts = bean.getActs(CustomerAccountArchetypes.INVOICE_ITEM);
            for (Act act : acts) {
                DemographicUpdateHelper helper = new DemographicUpdateHelper(act, service);
                helper.processDemographicUpdates(invoice);
            }
        }
    }

    /**
     * Invoked <em>prior</em> to an invoice being removed. Removes any reminders
     * or documents that don't have status <em>COMPLETED</em>.
     *
     * @param invoice the invoice
     */
    public void removeInvoice(FinancialAct invoice) {
        if (!TypeHelper.isA(invoice, CustomerAccountArchetypes.INVOICE)) {
            throw new IllegalArgumentException("Invalid argument 'invoice'");
        }
        ActBean bean = new ActBean(invoice, service);
        List<Act> acts = bean.getActs(CustomerAccountArchetypes.INVOICE_ITEM);
        for (Act act : acts) {
            removeInvoiceItem((FinancialAct) act);
        }
    }

    /**
     * Invoked prior to an invoice item has been removed. Removes any reminders
     * or documents that don't have status <em>COMPLETED</em>.
     *
     * @param act the act
     */
    public void removeInvoiceItem(FinancialAct act) {
        if (!TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            throw new IllegalArgumentException("Invalid argument 'act'");
        }
        List<Act> toRemove = new ArrayList<Act>();
        removeRelatedActs(act, "investigations", INVESTIGATION_STATUSES, toRemove);
        removeRelatedActs(act, "reminders", REMINDER_STATUSES, toRemove);
        removeRelatedActs(act, "documents", DOCUMENT_STATUSES, toRemove);

        if (!toRemove.isEmpty()) {
            // TODO - need to save to update session prior
            // to removing child acts. Shouldn't need this
            service.save(act);
            service.save(toRemove);
            for (Act remove : toRemove) {
                service.remove(remove);
            }
        }
    }

    /**
     * Removes relationships between an invoice item and related acts that meet the specified criteria.
     * The acts that match the criteria are added to <tt>toRemove</tt>.
     *
     * @param item     the invoice item
     * @param node     the node of the related acts
     * @param statuses act statuses. If a related act has one of these, it will be retained
     * @param toRemove the acts to remove
     */
    private void removeRelatedActs(FinancialAct item, String node, String[] statuses, List<Act> toRemove) {
        ActBean bean = new ActBean(item, service);
        List<Act> acts = bean.getNodeActs(node);

        for (Act act : acts) {
            String status = act.getStatus();
            ActRelationship r = bean.getRelationship(act);

            boolean retain = false;
            for (String s : statuses) {
                if (s.equals(status)) {
                    retain = true;
                    break;
                }
            }
            if (!retain) {
                toRemove.add(act);
                act.removeActRelationship(r);
                bean.removeRelationship(r);
            }
        }
    }

}
