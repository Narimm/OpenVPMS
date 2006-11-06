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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
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
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanException;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
     * Invoked after an invoice item has been saved. Updates any reminders
     * and documents associated with the product.
     * <p/>
     * TODO - all modifications should be done within a transaction
     *
     * @param act the act
     */
    public void saveInvoiceItem(FinancialAct act) {
        if (!TypeHelper.isA(act, "act.customerAccountInvoiceItem")) {
            throw new IllegalArgumentException("Invalid argument 'act'");
        }
        ActBean actBean = new ActBean(act, _service);
        Entity product = actBean.getParticipant("participation.product");
        EntityBean productBean = null;
        if (product != null) {
            productBean = new EntityBean(product, _service);
        }
        boolean save = addReminders(actBean, productBean);
        save |= addDocuments(actBean, productBean);
        if (save) {
            actBean.save();
        }
    }

    /**
     * Invoked <em>prior</em> to an invoice being removed. Removes any reminders
     * or documents that don't have status 'Completed.
     * <p/>
     * TODO - all modifications should be done within a transaction
     *
     * @param invoice the invoice
     */
    public void removeInvoice(FinancialAct invoice) {
        if (!TypeHelper.isA(invoice, "act.customerAccountChargesInvoice")) {
            throw new IllegalArgumentException("Invalid argument 'invoice'");
        }
        ActBean bean = new ActBean(invoice);
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
     * Add reminders to an <em>act.customerAccountInvoiceItem</em>.
     *
     * @param item    the invoice item
     * @param product the product. May be <code>null</code>
     * @return <code>true</code> if the item needs to be saved
     * @throws ArchetypeServiceException for any archetype service error
     */
    private boolean addReminders(ActBean item, EntityBean product) {
        boolean save = false;
        List<Act> reminders = item.getActsForNode("reminders");

        Set<IMObjectReference> actReminders         // reminderTypes referenced
                = new HashSet<IMObjectReference>(); // by the acts

        Set<IMObjectReference> productReminders     // reminderTypes referenced
                = new HashSet<IMObjectReference>(); // by the product

        // get the set of references to entity.reminderTypes for the product
        if (product != null && product.hasNode("reminders")) {
            for (IMObject object : product.getValues("reminders")) {
                EntityRelationship r = (EntityRelationship) object;
                if (r.getTarget() != null) {
                    productReminders.add(r.getTarget());
                }
            }
        }

        // remove any existing reminders not referenced by the current product
        for (Act reminder : reminders) {
            ActBean bean = new ActBean(reminder, _service);
            IMObjectReference type
                    = bean.getParticipantRef("participation.reminderType");
            if (type == null || !productReminders.contains(type)) {
                ActRelationship r = item.getRelationship(reminder);
                item.removeRelationship(r);
                save = true;
            } else {
                actReminders.add(type);
            }
        }

        // add any reminders associated with the current product
        for (IMObjectReference reminderRef : productReminders) {
            if (!actReminders.contains(reminderRef)) {
                Entity reminderType = (Entity) getObject(reminderRef);
                if (reminderType != null) {
                    addReminder(item, reminderType);
                    save = true;
                }
            }
        }
        return save;
    }

    /**
     * Add documents to an <em>act.customerAccountInvoiceItem</em>.
     *
     * @param item    the invoice item
     * @param product the product. May be <code>null</code>
     * @return <code>true</code> if the item needs to be saved
     * @throws ArchetypeServiceException for any archetype service error
     */
    private boolean addDocuments(ActBean item, EntityBean product) {
        boolean save = false;
        List<Act> documents = item.getActsForNode("documents");

        Set<IMObjectReference> actDocs              // entity.documentTemplates
                = new HashSet<IMObjectReference>(); // referenced by the acts

        Set<IMObjectReference> productDocs          // entity.documentTemplates
                = new HashSet<IMObjectReference>(); // referenced by the product

        // get the set of references to entity.documentTemplates for the product
        if (product != null && product.hasNode("documents")) {
            for (IMObject object : product.getValues("documents")) {
                EntityRelationship r = (EntityRelationship) object;
                if (r.getTarget() != null) {
                    productDocs.add(r.getTarget());
                }
            }
        }

        // remove any existing documents not referenced by the current product
        for (Act document : documents) {
            ActBean bean = new ActBean(document, _service);
            IMObjectReference template
                    = bean.getParticipantRef("participation.documentTemplate");
            if (template == null || !productDocs.contains(template)) {
                ActRelationship r = item.getRelationship(document);
                item.removeRelationship(r);
                save = true;
            } else {
                actDocs.add(template);
            }
        }

        // add any documents associated with the current product
        for (IMObjectReference templateRef : productDocs) {
            if (!actDocs.contains(templateRef)) {
                Entity template = (Entity) getObject(templateRef);
                if (template != null) {
                    addDocument(item, template);
                    save = true;
                }
            }
        }
        return save;
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
        ActBean bean = new ActBean(item, _service);
        List<Act> acts = bean.getActsForNode("reminders");

        for (Act act : acts) {
            ActRelationship r = bean.getRelationship(act);
            if (!ActStatus.COMPLETED.equals(act.getStatus())) {
                _service.remove(act);
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
        ActBean bean = new ActBean(item, _service);
        List<Act> acts = bean.getActsForNode("documents");

        for (Act act : acts) {
            String status = act.getStatus();
            ActRelationship r = bean.getRelationship(act);
            if (!ActStatus.COMPLETED.equals(status)
                    && !ActStatus.POSTED.equals(status)) {
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
     * Adds an <em>act.patientDocument*</em> to an
     * <em>act.customerAccountInvoiceItem</em>.
     *
     * @param item     the invoice item
     * @param document the document template
     */
    private void addDocument(ActBean item, Entity document) {
        EntityBean bean = new EntityBean(document, _service);
        String shortName = bean.getString("archetype");
        if (StringUtils.isEmpty(shortName)) {
            shortName = "act.patientDocumentForm";
        }
        IMObject object = _service.create(shortName);
        if (TypeHelper.isA(object, "act.patientDocument*")) {
            Act act = (Act) object;
            act.setActivityStartTime(item.getAct().getActivityStartTime());
            ActBean documentAct = new ActBean(act);
            IMObjectReference patient = item.getParticipantRef(
                    "participation.patient");
            documentAct.addParticipation("participation.patient", patient);
            documentAct.addParticipation("participation.documentTemplate",
                                         document);
            documentAct.save();
            item.addRelationship("actRelationship.invoiceItemDocument",
                                 documentAct.getAct());
        }
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
