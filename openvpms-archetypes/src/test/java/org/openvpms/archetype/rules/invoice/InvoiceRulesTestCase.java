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

import static org.openvpms.archetype.rules.act.ActStatus.*;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
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
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The reminder.
     */
    private Entity reminder;

    /**
     * The document template.
     */
    private Entity template;


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
                item.getAct().getActivityStartTime(), this.reminder);
        assertEquals(endTime, reminder.getActivityEndTime());

        // make sure a document has been added
        List<Act> documents = item.getActsForNode("documents");
        assertEquals(1, documents.size());
        Act document = documents.get(0);
        assertTrue(TypeHelper.isA(document, "act.patientDocumentForm"));

        // verify the start time is the same as the invoice item start time
        assertEquals(item.getAct().getActivityStartTime(),
                     document.getActivityStartTime());

        // check reminder participations
        ActBean reminderBean = new ActBean(reminder);
        assertEquals(patient,
                     reminderBean.getParticipant("participation.patient"));
        assertEquals(this.reminder,
                     reminderBean.getParticipant("participation.reminderType"));

        // check document participations
        ActBean docBean = new ActBean(document);
        assertEquals(patient, docBean.getParticipant("participation.patient"));
        assertEquals(template,
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
        reminder.setStatus(COMPLETED);
        save(reminder);

        // set the document status to 'Posted'
        Act document = documents.get(0);
        document.setStatus(POSTED);
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
        reminder.setStatus(COMPLETED);
        save(reminder);

        // set the document status to 'Completed'
        Act document = documents.get(0);
        document.setStatus(COMPLETED);
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
        customer = TestHelper.createCustomer();
        clinician = TestHelper.createClinician(false);
        patient = TestHelper.createPatient();
        reminder = ReminderTestHelper.createReminderType();
        template = createDocumentTemplate();
    }

    /**
     * Helper to create an <em>act.customerAccountChargesInvoice</em>.
     *
     * @return a new act
     */
    private ActBean createInvoice() {
        Act act = createAct("act.customerAccountChargesInvoice");
        act.setStatus(IN_PROGRESS);
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", customer);
        bean.addParticipation("participation.patient", patient);
        bean.addParticipation("participation.clinician", clinician);
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
        bean.addParticipation("participation.patient", patient);
        bean.addParticipation("participation.clinician", clinician);
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
     * Creates and saves a new document template.
     *
     * @return a new document template
     */
    private Entity createDocumentTemplate() {
        Entity template = (Entity) create("entity.documentTemplate");
        EntityBean bean = new EntityBean(template);
        bean.setValue("name", "XDocumentTemplate");
        bean.setValue("archetype", "act.patientDocumentForm");
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
                                 reminder);
            bean.addRelationship("entityRelationship.productDocument",
                                 template);
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
