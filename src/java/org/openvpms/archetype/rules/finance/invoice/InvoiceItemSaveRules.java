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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.invoice;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Rules for saving <em>act.customerAccountInvoiceItem</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class InvoiceItemSaveRules {

    /**
     * The <em>act.customerAccountInvoiceItem</em>.
     */
    private final Act item;

    /**
     * Helper for the item.
     */
    private final ActBean itemBean;

    /**
     * The product. May be <tt>null</tt>.
     */
    private final Product product;

    /**
     * Helper for the product.
     */
    private EntityBean productBean;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The list of reminder and/or document acts to remove.
     */
    private List<IMObject> toRemove = new ArrayList<IMObject>();

    /**
     * The list of acts to save.
     */
    private List<IMObject> toSave = new ArrayList<IMObject>();

    /**
     * The set of new document acts to link to the associated patient's
     * <em>act.patientClinicalEvent</em>.
     */
    private List<Act> newDocs = new ArrayList<Act>();

    /**
     * The reminder rules object
     */
    private final ReminderRules reminderRules;


    /**
     * Creates a new <tt>InvoiceItemSaveRules</tt>.
     *
     * @param act     the invoice item act
     * @param service the archetype service
     */
    public InvoiceItemSaveRules(Act act, IArchetypeService service) {
        this.service = service;
        if (!TypeHelper.isA(act, "act.customerAccountInvoiceItem")) {
            throw new IllegalArgumentException("Invalid argument 'act'");
        }
        this.item = act;
        itemBean = new ActBean(act, service);
        product = (Product) itemBean.getParticipant("participation.product");
        if (product != null) {
            productBean = new EntityBean(product, service);
        }
        reminderRules = new ReminderRules(service);
    }

    /**
     * Invoked after the invoice item is saved. This:
     * <ul>
     * <li>adds reminders and document acts</li>
     * <li>adds dispensing and document acts to the patient's associated
     * <em>act.patientClinicalEvent</em>; and</li>
     * <li>processes any demographic updates associated with the product</li>
     * </ul>
     * Note that the dispensing acts must be saved <em>prior</em> to the invoice
     * item.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void save() {
        addReminders();
        addDocuments();

        if (!toRemove.isEmpty()) {
            service.save(item);     // TODO - shouldn't have to save
            service.save(toRemove); // these prior to removing toRemove

            for (IMObject object : toRemove) {
                service.remove(object);
            }
        }
        // Now save all the acts.
        // TODO:  Modified due to batch save not working with rules in 1.1 version and rules not
        // being triggered in rules causing reminders completions not to work.
        // Need to modify back when this fixed in 1.2.
        for (IMObject object : toSave) {
            if (TypeHelper.isA(object, ReminderArchetypes.REMINDER)) {
                reminderRules.markMatchingRemindersCompleted((Act) object);
            }
            service.save(object);
        }

        addEventRelationships();

        if (productBean != null) {
            DemographicUpdateHelper helper = new DemographicUpdateHelper(
                    itemBean, productBean, service);
            helper.processDemographicUpdates();
        }
    }

    /**
     * Add reminders to the invoice item.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void addReminders() {
        List<Act> reminders = itemBean.getNodeActs("reminders");

        Set<IMObjectReference> actReminders         // reminderTypes referenced
                = new HashSet<IMObjectReference>(); // by the acts

        Set<IMObjectReference> productReminders     // reminderTypes referenced
                = new HashSet<IMObjectReference>(); // by the product

        // get the set of references to entity.reminderTypes for the product
        if (product != null && productBean.hasNode("reminders")) {
            for (IMObject object : productBean.getValues("reminders")) {
                EntityRelationship r = (EntityRelationship) object;
                if (r.getTarget() != null) {
                    productReminders.add(r.getTarget());
                }
            }
        }

        // remove any existing reminders not referenced by the current product
        for (Act reminder : reminders) {
            ActBean bean = new ActBean(reminder, service);
            IMObjectReference type
                    = bean.getParticipantRef(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION);
            if (type == null || !productReminders.contains(type)) {
                ActRelationship r = itemBean.getRelationship(reminder);
                toRemove.add(reminder);
                itemBean.removeRelationship(r);
                reminder.removeActRelationship(r);
            } else {
                actReminders.add(type);
            }
        }

        // add any reminders associated with the current product
        for (IMObjectReference reminderRef : productReminders) {
            if (!actReminders.contains(reminderRef)) {
                Entity reminderType = (Entity) getObject(reminderRef);
                if (reminderType != null) {
                    addReminder(reminderType);
                }
            }
        }
    }

    /**
     * Adds an <em>act.patientReminder</em> to the invoice item.
     *
     * @param reminderType the reminder type
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void addReminder(Entity reminderType) {
        Act reminder = (Act) service.create(ReminderArchetypes.REMINDER);
        Date startTime = item.getActivityStartTime();
        Date endTime = null;
        if (startTime != null) {
            ReminderRules rules = new ReminderRules(service);
            endTime = rules.calculateReminderDueDate(startTime, reminderType);
        }
        reminder.setActivityStartTime(startTime);
        reminder.setActivityEndTime(endTime);

        ActBean bean = new ActBean(reminder, service);
        IMObjectReference patient = itemBean.getParticipantRef(
                "participation.patient");
        bean.addParticipation("participation.patient", patient);
        bean.addParticipation(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION, reminderType);
        if (product != null) {
            bean.addParticipation("participation.product", product);
        }
        itemBean.addRelationship("actRelationship.invoiceItemReminder",
                                 reminder);
        toSave.add(reminder);
    }

    /**
     * Add documents to the invoice item.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void addDocuments() {
        List<Act> documents = itemBean.getNodeActs("documents");

        Set<IMObjectReference> actDocs              // entity.documentTemplates
                = new HashSet<IMObjectReference>(); // referenced by the acts

        Set<IMObjectReference> productDocs          // entity.documentTemplates
                = new HashSet<IMObjectReference>(); // referenced by the product

        // get the set of references to entity.documentTemplates for the product
        if (product != null && productBean.hasNode("documents")) {
            for (IMObject object : productBean.getValues("documents")) {
                EntityRelationship r = (EntityRelationship) object;
                if (r.getTarget() != null) {
                    productDocs.add(r.getTarget());
                }
            }
        }

        // remove any existing documents not referenced by the current product
        for (Act document : documents) {
            ActBean bean = new ActBean(document, service);
            IMObjectReference template
                    = bean.getParticipantRef("participation.documentTemplate");
            if (template == null || !productDocs.contains(template)) {
                ActRelationship r = itemBean.getRelationship(document);
                toRemove.add(document);
                itemBean.removeRelationship(r);
                document.removeActRelationship(r);
            } else {
                actDocs.add(template);
            }
        }

        // add any documents associated with the current product
        for (IMObjectReference templateRef : productDocs) {
            if (!actDocs.contains(templateRef)) {
                Entity template = (Entity) getObject(templateRef);
                if (template != null) {
                    addDocument(template);
                }
            }
        }
    }

    /**
     * Adds an <em>act.patientDocument*</em> to the invoice item.
     *
     * @param document the document template
     * @throws ArchetypeServiceException for any error
     */
    private void addDocument(Entity document) {
        EntityBean bean = new EntityBean(document, service);
        String shortName = bean.getString("archetype");
        if (StringUtils.isEmpty(shortName)) {
            shortName = "act.patientDocumentForm";
        }
        if (TypeHelper.matches(shortName, "act.patientDocument*")) {
            Act act = (Act) service.create(shortName);
            act.setActivityStartTime(item.getActivityStartTime());
            ActBean documentAct = new ActBean(act, service);
            IMObjectReference patient = itemBean.getParticipantRef(
                    "participation.patient");
            documentAct.addParticipation("participation.patient", patient);
            documentAct.addParticipation("participation.documentTemplate",
                                         document);
            IMObjectReference clinician = itemBean.getParticipantRef(
                    "participation.clinician");
            if (clinician != null) {
                documentAct.addParticipation("participation.clinician",
                                             clinician);
            }

            if (TypeHelper.isA(act, "act.patientDocumentForm")) {

                IMObjectReference product = itemBean.getParticipantRef(
                        "participation.product");
                documentAct.addParticipation("participation.product", product);
            }
            toSave.add(act);
            newDocs.add(act);
            itemBean.addRelationship("actRelationship.invoiceItemDocument",
                                     documentAct.getAct());
        }
    }

    /**
     * Adds relationships between dispensing and document acts to the
     * associated patient's <em>act.patientClinicalEvent</em>.
     */
    private void addEventRelationships() {
        List<Act> acts = new ArrayList<Act>();
        for (Act medication : itemBean.getNodeActs("dispensing")) {
            acts.add(medication);
        }
        acts.addAll(newDocs);
        MedicalRecordRules rules = new MedicalRecordRules(service);
        Date startTime = item.getActivityStartTime();
        if (startTime == null) {
            startTime = new Date();
        }
        rules.addToEvents(acts, startTime);
    }

    /**
     * Helper to retrieve an object given its reference.
     *
     * @param ref the reference
     * @return the object corresponding to the reference, or <tt>null</tt>
     *         if it can't be retrieved
     * @throws ArchetypeServiceException for any error
     */
    private IMObject getObject(IMObjectReference ref) {
        if (ref != null) {
            return service.get(ref);
        }
        return null;
    }
}
