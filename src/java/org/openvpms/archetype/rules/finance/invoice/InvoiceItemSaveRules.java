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
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
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
        if (!TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            throw new IllegalArgumentException("Invalid argument 'act'");
        }
        this.item = act;
        itemBean = new ActBean(act, service);
        product = (Product) itemBean.getParticipant(ProductArchetypes.PRODUCT_PARTICIPATION);
        if (product != null) {
            productBean = new EntityBean(product, service);
        }
        reminderRules = new ReminderRules(service);
    }

    /**
     * Invoked after the invoice item is saved. This:
     * <ul>
     * <li>adds reminders and document acts</li>
     * <li>adds dispensing, investigation and document acts to the patient's associated
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
        linkActsToProductEntities("reminders", "reminders", ReminderArchetypes.REMINDER_TYPE_PARTICIPATION,
                                  new ActLinker() {
                                      public void link(Entity entity) {
                                          addReminder(entity);
                                      }
                                  });
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
        IMObjectReference patient = itemBean.getParticipantRef(PatientArchetypes.PATIENT_PARTICIPATION);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.addParticipation(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION, reminderType);
        if (product != null) {
            bean.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, product);
        }
        itemBean.addRelationship("actRelationship.invoiceItemReminder", reminder);
        toSave.add(reminder);
    }

    /**
     * Add documents to the invoice item.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void addDocuments() {
        linkActsToProductEntities("documents", "documents", "participation.documentTemplate",
                                  new ActLinker() {
                                      public void link(Entity entity) {
                                          addDocument(entity);
                                      }
                                  });
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
            IMObjectReference patient = itemBean.getParticipantRef(PatientArchetypes.PATIENT_PARTICIPATION);
            documentAct.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
            documentAct.addParticipation("participation.documentTemplate", document);
            IMObjectReference clinician = itemBean.getParticipantRef(UserArchetypes.CLINICIAN_PARTICIPATION);
            if (clinician != null) {
                documentAct.addParticipation(UserArchetypes.CLINICIAN_PARTICIPATION, clinician);
            }

            if (TypeHelper.isA(act, "act.patientDocumentForm")) {
                IMObjectReference product = itemBean.getParticipantRef(ProductArchetypes.PRODUCT_PARTICIPATION);
                documentAct.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, product);
            }
            toSave.add(act);
            newDocs.add(act);
            itemBean.addRelationship("actRelationship.invoiceItemDocument", documentAct.getAct());
        }
    }

    /**
     * Adds relationships between dispensing, investigation and document acts to the
     * associated patient's <em>act.patientClinicalEvent</em>.
     */
    private void addEventRelationships() {
        List<Act> acts = new ArrayList<Act>();
        acts.addAll(itemBean.getNodeActs("dispensing"));
        acts.addAll(itemBean.getNodeActs("investigations"));
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
        return (ref != null) ? service.get(ref) : null;
    }

    /**
     * Creates or deletes acts related to the invoice item based on the entities associated with the current product.
     * This:
     * <ol>
     * <li>gets all entities associated with the product's <tt>entityNode</tt></li>
     * <li>iterates through the acts associated with the invoice item's <tt>actNode</tt> and:</li>
     * <ol>
     * <li>queues removal of acts that don't have participation to any of the <em>entityNode</em> entities</li>
     * <li>retains acts which have participations to any of the <em>entityNode</em> entities</li>
     * </ol>
     * <li>uses the <tt>linker</tt> to create and link acts for each entity that doesn't yet have an act</li>
     * </ol>
     *
     * @param actNode    the invoice item node to use to get the related acts
     * @param entityNode the product node to use to get the related entities
     * @param archetype  the participation archetype that links the act with the entity
     * @param linker     used to link the invoice item with a new act created by the linker for a given entity
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void linkActsToProductEntities(String actNode, String entityNode, String archetype, ActLinker linker) {
        List<Act> acts = itemBean.getNodeActs(actNode);
        Set<IMObjectReference> productTypes = new HashSet<IMObjectReference>();
        Set<IMObjectReference> actTypes = new HashSet<IMObjectReference>();
        if (product != null && productBean.hasNode(entityNode)) {
            for (EntityRelationship r : productBean.getValues(entityNode, EntityRelationship.class)) {
                IMObjectReference target = r.getTarget();
                if (target != null) {
                    productTypes.add(target);
                }
            }
        }
        for (Act act : acts) {
            ActBean bean = new ActBean(act, service);
            IMObjectReference type = bean.getParticipantRef(archetype);
            if (type != null && !productTypes.contains(type)) {
                toRemove.add(act);
                ActRelationship r = itemBean.getRelationship(act);
                itemBean.removeRelationship(r);
                act.removeActRelationship(r);
            } else {
                actTypes.add(type);
            }
        }

        // add any entities associated with the current product
        for (IMObjectReference typeRef : productTypes) {
            if (!actTypes.contains(typeRef)) {
                Entity entity = (Entity) getObject(typeRef);
                if (entity != null) {
                    linker.link(entity);
                }
            }
        }
    }

    /**
     * Used to create a new act to link to the invoice item.
     */
    interface ActLinker {

        /**
         * Creates and links a new act to the invoice item.
         *
         * @param entity the entity to create the act for
         * @throws ArchetypeServiceException for any archetype service error
         */
        void link(Entity entity);
    }
}
