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
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.invoice;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanException;
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
     * Creates a new <code>InvoiceRules</code>.
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
        if (!TypeHelper.isA(invoice, "act.customerAccountChargesInvoice")) {
            throw new IllegalArgumentException("Invalid argument 'invoice'");
        }
        if (ActStatus.POSTED.equals(invoice.getStatus())) {
            ActBean bean = new ActBean(invoice, service);
            List<Act> acts = bean.getActs("act.customerAccountInvoiceItem");
            for (Act act : acts) {
                DemographicUpdateHelper helper = new DemographicUpdateHelper(
                        act, service);
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
        if (!TypeHelper.isA(invoice, "act.customerAccountChargesInvoice")) {
            throw new IllegalArgumentException("Invalid argument 'invoice'");
        }
        ActBean bean = new ActBean(invoice, service);
        List<Act> acts = bean.getActs("act.customerAccountInvoiceItem");
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
        if (!TypeHelper.isA(act, "act.customerAccountInvoiceItem")) {
            throw new IllegalArgumentException("Invalid argument 'act'");
        }
        List<Act> toRemove = removeInvoiceItemReminders(act);
        toRemove.addAll(removeInvoiceItemDocuments(act));
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
     * Removes relationships to any reminders associated with an
     * <em>act.customerAccountInvoiceItem</em> that don't have status
     * 'Completed'.
     *
     * @param item the invoice item
     * @return the documents to remove
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the reminders node does't exist
     */
    private List<Act> removeInvoiceItemReminders(FinancialAct item) {
        List<Act> toRemove = new ArrayList<Act>();
        ActBean bean = new ActBean(item, service);
        List<Act> acts = bean.getNodeActs("reminders");

        for (Act act : acts) {
            ActRelationship r = bean.getRelationship(act);
            if (!ActStatus.COMPLETED.equals(act.getStatus())) {
                toRemove.add(act);
                act.removeActRelationship(r);
                bean.removeRelationship(r);
            }
        }
        return toRemove;
    }

    /**
     * Removes relationships to any documents associated with an
     * <em>act.customerAccountInvoiceItem</em> that don't have status
     * 'Completed' or 'Posted'.
     *
     * @param item the invoice item
     * @return the documents to remove
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the documents node does't exist
     */
    private List<Act> removeInvoiceItemDocuments(FinancialAct item) {
        List<Act> toRemove = new ArrayList<Act>();
        ActBean bean = new ActBean(item, service);
        List<Act> acts = bean.getNodeActs("documents");

        for (Act act : acts) {
            String status = act.getStatus();
            ActRelationship r = bean.getRelationship(act);
            if (!ActStatus.COMPLETED.equals(status)
                    && !ActStatus.POSTED.equals(status)) {
                toRemove.add(act);
                act.removeActRelationship(r);
                bean.removeRelationship(r);
            }
        }
        return toRemove;
    }

}
