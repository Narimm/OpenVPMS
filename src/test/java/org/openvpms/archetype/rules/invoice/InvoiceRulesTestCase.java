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
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;
import java.util.List;


/**
 * Tests the {@link InvoiceRules} class when invoked by the
 * <em>archetypeService.save.act.customerAccountInvoiceItem.after.drl</em> '
 * rule. In order for these tests to be successful, the archetype service
 * must be configured to trigger the above rule.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceRulesTestCase extends ArchetypeServiceTest {

    /**
     * The customer.
     */
    private Party _customer;

    /**
     * The patient.
     */
    private Party _patient;

    /**
     * The clinician.
     */
    private User _clinician;

    /**
     * The reminder.
     */
    private Entity _reminder;

    /**
     * The document template.
     */
    private Entity _template;


    /**
     * Verifies that <em>act.patientReminder</em> and
     * <em>act.patientDocument*</em>s are associated with an invoice item when
     * it is saved.
     */
    public void testSaveInvoiceItem() {
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        item = reload(item); // reload to ensure the item has saved correctly

        // make sure a reminder has been added
        List<Act> reminders = item.getActsForNode("reminders");
        assertEquals(1, reminders.size());
        Act reminder = reminders.get(0);

        // verify the start time is the same as the invoice item start time
        assertEquals(item.getAct().getActivityStartTime(),
                     reminder.getActivityStartTime());

        // verify the end time has been set
        ReminderRules rules = new ReminderRules(getArchetypeService());
        Date endTime = rules.calculateReminderDueDate(
                item.getAct().getActivityStartTime(), _reminder);
        assertEquals(endTime, reminder.getActivityEndTime());

        // make sure a document has been added
        List<Act> documents = item.getActsForNode("documents");
        assertEquals(1, documents.size());
        Act document = documents.get(0);
        assertTrue(TypeHelper.isA(document, "act.patientDocumentAttachment"));

        // verify the start time is the same as the invoice item start time
        assertEquals(item.getAct().getActivityStartTime(),
                     document.getActivityStartTime());

        // check reminder participations
        ActBean reminderBean = new ActBean(reminder);
        assertEquals(_patient,
                     reminderBean.getParticipant("participation.patient"));
        assertEquals(_reminder,
                     reminderBean.getParticipant("participation.reminderType"));

        // check document participations
        ActBean docBean = new ActBean(document);
        assertEquals(_patient, docBean.getParticipant("participation.patient"));
        assertEquals(_template,
                     docBean.getParticipant("participation.documentTemplate"));

        // change the product participation to one without a reminder.
        // The invoice should no longer have any associated reminders or
        // documents
        item.setParticipant("participation.product", createProduct(false));
        item.save();
        item = reload(item);
        reminders = item.getActsForNode("reminders");
        assertTrue(reminders.isEmpty());

        documents = item.getActsForNode("documents");
        assertTrue(documents.isEmpty());
    }

    /**
     * Verifies that reminders that don't have status 'Completed' are removed
     * when an invoice item is deleted.
     */
    public void testRemoveInvoiceItemIncompleteActs() {
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getActsForNode("reminders");
        assertEquals(1, reminders.size());

        List<Act> documents = item.getActsForNode("documents");
        assertEquals(1, documents.size());

        // remove the item and verify it can't be retrieved
        IMObjectReference actRef = item.getAct().getObjectReference();
        remove(item.getAct());
        assertNull(get(actRef));

        // verify the reminders have been removed
        for (Act reminder : reminders) {
            assertNull(get(reminder.getObjectReference()));
        }

        // verify the documents have been removed
        for (Act document : documents) {
            assertNull(get(document.getObjectReference()));
        }
    }

    /**
     * Verifies that reminders and documents that don't have status 'Completed'
     * or 'Posted' aren't removed when an invoice item is deleted.
     */
    public void testRemoveInvoiceItemCompleteActs() {
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getActsForNode("reminders");
        assertEquals(1, reminders.size());

        List<Act> documents = item.getActsForNode("documents");
        assertEquals(1, documents.size());

        // set the reminder status to 'Completed'
        Act reminder = reminders.get(0);
        reminder.setStatus("Completed");
        save(reminder);

        // set the document status to 'Posted'
        Act document = documents.get(0);
        document.setStatus("Posted");
        save(document);

        item = reload(item); // reload to ensure the item has saved correctly

        // remove the item and verify it can't be retrieved
        IMObjectReference actRef = item.getAct().getObjectReference();
        remove(item.getAct());
        assertNull(get(actRef));

        // verify the reminder and document haven't been removed
        assertNotNull(get(reminder.getObjectReference()));
        assertNotNull(get(document.getObjectReference()));
    }

    /**
     * Verifies that reminders and documents that don't have status 'Completed'
     * are removed when an invoice is deleted.
     */
    public void testRemoveInvoiceIncompleteActs() {
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        invoice.addRelationship("actRelationship.customerAccountInvoiceItem",
                                item.getAct());
        invoice.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getActsForNode("reminders");
        assertEquals(1, reminders.size());

        List<Act> documents = item.getActsForNode("documents");
        assertEquals(1, documents.size());

        // remove the invoice and verify it can't be retrieved
        IMObjectReference actRef = invoice.getAct().getObjectReference();
        remove(invoice.getAct());
        assertNull(get(actRef));

        // verify the reminders have been removed
        for (Act reminder : reminders) {
            assertNull(get(reminder.getObjectReference()));
        }

        // verify the documents have been removed
        for (Act document : documents) {
            assertNull(get(document.getObjectReference()));
        }
    }

    /**
     * Verifies that reminders and documents that have status
     * 'Completed' are not removed when an invoice is deleted.
     */
    public void testRemoveInvoiceCompleteActs() {
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        invoice.addRelationship("actRelationship.customerAccountInvoiceItem",
                                item.getAct());
        invoice.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getActsForNode("reminders");
        assertEquals(1, reminders.size());

        List<Act> documents = item.getActsForNode("documents");
        assertEquals(1, documents.size());

        // set the reminder status to 'Completed'
        Act reminder = reminders.get(0);
        reminder.setStatus("Completed");
        save(reminder);

        // set the document status to 'Completed'
        Act document = documents.get(0);
        document.setStatus("Completed");
        save(document);

        // remove the invoice and verify it can't be retrieved
        IMObjectReference actRef = invoice.getAct().getObjectReference();
        remove(invoice.getAct());
        assertNull(get(actRef));

        // verify the reminder and document haven't been removed
        assertNotNull(get(reminder.getObjectReference()));
        assertNotNull(get(document.getObjectReference()));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        _customer = createCustomer();
        _clinician = createClinician();
        _patient = createPatient();
        _reminder = createReminder();
        _template = createDocumentTemplate();
    }

    /**
     * Helper to create an <em>act.customerAccountChargesInvoice</em>.
     *
     * @return a new act
     */
    private ActBean createInvoice() {
        Act act = createAct("act.customerAccountChargesInvoice");
        act.setStatus("In Progress");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", _customer);
        bean.addParticipation("participation.patient", _patient);
        bean.addParticipation("participation.clinician", _clinician);
        return bean;
    }

    /**
     * Helper to create an <em>act.customerAccountInvoiceItem</em>.
     *
     * @return a new act
     */
    private ActBean createInvoiceItem() {
        Act act = createAct("act.customerAccountInvoiceItem");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", _patient);
        bean.addParticipation("participation.clinician", _clinician);
        return bean;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    private Act createAct(String shortName) {
        Act act = (Act) create(shortName);
        assertNotNull(act);
        return act;
    }

       /**
     * Helper to create and save a customer.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        Party customer = (Party) create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("firstName", "J");
        bean.setValue("lastName", "Zoo");
        Contact contact = (Contact) create("contact.phoneNumber");
        assertNotNull(contact);
        customer.addContact(contact);
        save(customer);
        return customer;
    }

    /**
     * Creates and saves a new patient.
     *
     * @return a new patient
     */
    private Party createPatient() {
        Party patient = (Party) create("party.patientpet");
        EntityBean bean = new EntityBean(patient);
        bean.setValue("name", "XPatient");
        bean.setValue("species", "Canine");
        bean.save();
        assertNotNull(patient);
        return patient;
    }

    /**
     * Creates a new user.
     *
     * @return a new user
     */
    private User createClinician() {
        User user = (User) create("security.user");
        assertNotNull(user);
        user.setName("vet");
        return user;
    }

    /**
     * Creates and saves a new reminder.
     *
     * @return a new reminder
     */
    private Entity createReminder() {
        Entity reminder = (Entity) create("entity.reminderType");
        EntityBean bean = new EntityBean(reminder);
        bean.setValue("name", "XReminderType");
        bean.setValue("defaultInterval", 1);
        bean.setValue("defaultUnits", "months");
        bean.save();
        return reminder;
    }

    /**
     * Creates and saves a new document template.
     *
     * @return a new document template
     */
    private Entity createDocumentTemplate() {
        Entity template = (Entity) create("entity.documentTemplate");
        EntityBean bean = new EntityBean(template);
        bean.setValue("name", "XDocumentTemplate");
        bean.setValue("archetype", "act.patientDocumentAttachment");
        bean.save();

        return template;
    }

    /**
     * Creates and saves a new product.
     *
     * @param reminderDoc if <code>true</code> add a reminder and document
     * @return a new product
     */
    private Product createProduct(boolean reminderDoc) {
        Product product = (Product) create("product.medication");
        EntityBean bean = new EntityBean(product);
        bean.setValue("name", "XProduct");
        if (reminderDoc) {
            bean.addRelationship("entityRelationship.productReminder",
                                 _reminder);
            bean.addRelationship("entityRelationship.productDocument",
                                 _template);
        }
        bean.save();
        return product;
    }

    /**
     * Helper to reload an act.
     *
     * @param bean the act bean
     * @return the reloaded act
     */
    private ActBean reload(ActBean bean) {
        Act act = (Act) get(bean.getAct().getObjectReference());
        return new ActBean(act);
    }

}
