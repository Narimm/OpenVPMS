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

package org.openvpms.archetype.rules.invoice;

import org.openvpms.archetype.rules.patient.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;
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
    private final IArchetypeService _service;


    /**
     * Creates a new <code>InvoiceRules</code>.
     *
     * @param service the archetype service
     */
    public InvoiceRules(IArchetypeService service) {
        _service = service;
    }

    /**
     * Add reminders to an <em>act.customerAccountInvoiceItem</em>.
     * <p/>
     * TODO - all modifications should be done within a transaction
     *
     * @param item the item
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addReminders(FinancialAct item) {
        boolean save = false;
        ActBean bean = new ActBean(item, _service);
        List<Act> acts = bean.getActs("act.patientReminder");
        IMObject product = bean.getParticipant("participation.product");

        // remove any existing reminders not referenced by the current product
        for (Act act : acts) {
            if (product == null || !hasProduct(act, product)) {
                ActRelationship r = bean.getRelationship(act);
                bean.removeRelationship(r);
                save = true;
            }
        }

        // add any reminders associated with the current product
        if (product != null) {
            IMObjectBean productBean = new IMObjectBean(product);
            if (productBean.hasNode("reminders")) {
                List<IMObject> reminders = productBean.getValues("reminders");
                if (!reminders.isEmpty()) {
                    for (IMObject object : reminders) {
                        EntityRelationship relationship
                                = (EntityRelationship) object;
                        Entity reminderType = (Entity) getObject(
                                relationship.getTarget());
                        if (reminderType != null &&
                                !hasReminder(acts, reminderType)) {
                            addReminder(bean, reminderType);
                            save = true;
                        }
                    }
                }
            }
        }
        if (save) {
            bean.save();
        }
    }

    /**
     * Removes any reminders associated with an
     * <em>act.customerAccountChargesInvoice</em>
     * <em>act.customerAccountInvoiceItem</em>.
     * For <em>act.customerAccountChargesInvoice</em>, the reminders are
     * associated via the child <em>act.customerAccountInvoiceItem</em>s.
     * <p/>
     * TODO - all modifications should be done within a transaction
     *
     * @param act the act
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void removeReminders(FinancialAct act) {
        if (TypeHelper.isA(act, "act.customerAccountChargesInvoice")) {
            removeInvoiceReminders(act);
        } else if (TypeHelper.isA(act, "act.customerAccountInvoiceItem")) {
            removeInvoiceItemReminders(act);
        }
    }

    /**
     * Removes reminders associated with an invoice through its child
     * <em>act.customerAccountInvoiceItem</em>.
     *
     * @param invoice the invoice
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void removeInvoiceReminders(FinancialAct invoice) {
        ActBean bean = new ActBean(invoice);
        List<Act> acts = bean.getActs();
        for (Act act : acts) {
            if (TypeHelper.isA(act, "act.customerAccountInvoiceItem")) {
                removeInvoiceItemReminders((FinancialAct) act);
            }
        }
    }

    /**
     * Deletes any reminders associated with an
     * <em>act.customerAccountInvoiceItem</em> that don't have status
     * 'Completed'.
     *
     * @param item the invoice item
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void removeInvoiceItemReminders(FinancialAct item) {
        ActBean bean = new ActBean(item, _service);
        List<Act> acts = bean.getActs("act.patientReminder");

        for (Act act : acts) {
            ActRelationship r = bean.getRelationship(act);
            if (!"Completed".equals(act.getStatus())) {
                _service.remove(act);
                bean.removeRelationship(r);
            }
        }
    }

    /**
     * Adds an <em>act.patientReminder</em> to an
     * <em>act.customerAccountInvoiceItem</em>.
     *
     * @param item         the invoice item
     * @param reminderType the reminder type
     */
    private void addReminder(ActBean item, Entity reminderType) {
        Act act = (Act) _service.create("act.patientReminder");
        Date startTime = item.getAct().getActivityStartTime();
        Date endTime = null;
        if (startTime != null) {
            ReminderRules rules = new ReminderRules(_service);
            endTime = rules.calculateReminderDueDate(startTime, reminderType);
        }
        act.setActivityStartTime(startTime);
        act.setActivityEndTime(endTime);

        ActBean bean = new ActBean(act);
        IMObjectReference patient = item.getParticipantRef(
                "participation.patient");
        bean.addParticipation("participation.patient", patient);
        bean.addParticipation("participation.reminderType", reminderType);
        bean.save();

        item.addRelationship("actRelationship.invoiceItemReminder", act);
    }

    /**
     * Determines if an act references a product.
     *
     * @param act     the act
     * @param product the product
     * @return <code>true</code> if the act references <code>product</code>;
     *         otherwise <code>false</code>
     */
    private boolean hasProduct(Act act, IMObject product) {
        ActBean bean = new ActBean(act, _service);
        IMObjectReference ref = bean.getParticipantRef("participation.product");
        return ref != null && product.getObjectReference().equals(ref);
    }

    /**
     * Determines if a reminder type is referenced by a set of
     * <em>act.patientReminder</em>s.
     *
     * @param reminders    the <em>act.patientReminder</em>s
     * @param reminderType the reminder type
     * @return <code>true</code> if at least one act references the reminder
     *         type; otherwise <code>false</code>
     */
    private boolean hasReminder(List<Act> reminders, Entity reminderType) {
        IMObjectReference reminderRef = reminderType.getObjectReference();
        for (Act act : reminders) {
            ActBean bean = new ActBean(act, _service);
            IMObjectReference ref
                    = bean.getParticipantRef("participation.reminderType");
            if (ref != null && reminderRef.equals(ref)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper to retrieve an object given its reference.
     *
     * @param ref the reference
     * @return the object corresponding to the reference, or <code>null</code>
     *         if it can't be retrieved
     */
    private IMObject getObject(IMObjectReference ref) {
        if (ref != null) {
            return ArchetypeQueryHelper.getByObjectReference(_service, ref);
        }
        return null;
    }

}
