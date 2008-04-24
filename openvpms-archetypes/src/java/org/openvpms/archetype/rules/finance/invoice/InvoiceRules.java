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
     * or documents that don't have status 'Completed'.
     * <p/>
     * TODO - all modifications should be done within a transaction
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
     * Invoked when an invoice item has been removed. Removes any reminders
     * or documents that don't have status 'Completed.
     * <p/>
     * NOTE: when invoked on deletion of an invoice, this must occur prior
     * to the invoice being deleted, in order for the invoice items to be
     * resolved. When invoked on deletion of an invoice item, it can occur
     * after the invoice item has been removed.
     *
     * @param act the act
     */
    public void removeInvoiceItem(FinancialAct act) {
        if (!TypeHelper.isA(act, "act.customerAccountInvoiceItem")) {
            throw new IllegalArgumentException("Invalid argument 'act'");
        }
        removeInvoiceItemReminders(act);
        removeInvoiceItemDocuments(act);
    }

    /**
     * Deletes any reminders associated with an
     * <em>act.customerAccountInvoiceItem</em> that don't have status
     * 'Completed'.
     *
     * @param item the invoice item
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the reminders node does't exist
     */
    private void removeInvoiceItemReminders(FinancialAct item) {
        ActBean bean = new ActBean(item, service);
        List<Act> acts = bean.getNodeActs("reminders");

        for (Act act : acts) {
            ActRelationship r = bean.getRelationship(act);
            if (!ActStatus.COMPLETED.equals(act.getStatus())) {
                service.remove(act);
                bean.removeRelationship(r);
            }
        }
    }

    /**
     * Deletes any documents associated with an
     * <em>act.customerAccountInvoiceItem</em> that don't have status
     * 'Completed' or 'Posted'.
     *
     * @param item the invoice item
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the documents node does't exist
     */
    private void removeInvoiceItemDocuments(FinancialAct item) {
        ActBean bean = new ActBean(item, service);
        List<Act> acts = bean.getNodeActs("documents");

        for (Act act : acts) {
            String status = act.getStatus();
            ActRelationship r = bean.getRelationship(act);
            if (!ActStatus.COMPLETED.equals(status)
                    && !ActStatus.POSTED.equals(status)) {
                service.remove(act);
                bean.removeRelationship(r);
            }
        }
    }

}
