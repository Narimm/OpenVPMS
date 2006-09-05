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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

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
     * Verifies that <em>act.patientReminder</em>s are associated with an
     * invoice item when it is saved.
     */
    public void testAddReminders() {
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getActs("act.patientReminder");
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

        // check participations
        ActBean bean = new ActBean(reminder);
        assertEquals(_patient, bean.getParticipant("participation.patient"));
        assertEquals(_reminder,
                     bean.getParticipant("participation.reminderType"));

        // change the product participation to one without a reminder.
        // The invoice should no longer have any associated reminders
        item.setParticipant("participation.product", createProduct(false));
        item.save();
        item = reload(item);
        reminders = item.getActs("act.patientReminder");
        assertTrue(reminders.isEmpty());
    }

    /**
     * Verifies that reminders that don't have status 'Completed' are removed
     * when an invoice item is deleted.
     */
    public void testRemoveIncompleteReminders() {
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getActs("act.patientReminder");
        assertEquals(1, reminders.size());

        // remove the item and verify it can't be retrieved
        IMObjectReference actRef = item.getAct().getObjectReference();
        remove(item.getAct());
        assertNull(get(actRef));

        // verify the reminders have been removed
        for (Act reminder : reminders) {
            assertNull(get(reminder.getObjectReference()));
        }
    }

    /**
     * Verifies that reminders that don't have status 'Completed' aren't removed
     * when an invoice item is deleted.
     */
    public void testNoRemoveCompleteReminders() {
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getActs("act.patientReminder");
        assertEquals(1, reminders.size());

        // set the reminder status to 'Completed'
        Act reminder = reminders.get(0);
        reminder.setStatus("Completed");
        save(reminder);

        item = reload(item); // reload to ensure the item has saved correctly

        // remove the item and verify it can't be retrieved
        IMObjectReference actRef = item.getAct().getObjectReference();
        remove(item.getAct());
        assertNull(get(actRef));

        // verify the reminder hasn't been removed
        assertNotNull(get(reminder.getObjectReference()));
    }

    /**
     * Verifies that reminders that don't have status 'Completed' are removed
     * when an invoice is deleted.
     */
    public void testRemoveIncompleteRemindersForInvoice() {
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        invoice.addRelationship("actRelationship.customerAccountInvoiceItem",
                                item.getAct());
        invoice.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getActs("act.patientReminder");
        assertEquals(1, reminders.size());

        // remove the invoice and verify it can't be retrieved
        IMObjectReference actRef = invoice.getAct().getObjectReference();
        remove(invoice.getAct());
        assertNull(get(actRef));

        // verify the reminders have been removed
        for (Act reminder : reminders) {
            assertNull(get(reminder.getObjectReference()));
        }
    }

    /**
     * Verifies that reminders that have status 'Completed' are not removed
     * when an invoice is deleted.
     */
    public void testNoRemoveCompleteRemindersForInvoice() {
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addParticipation("participation.product", createProduct(true));
        item.save();
        invoice.addRelationship("actRelationship.customerAccountInvoiceItem",
                                item.getAct());
        invoice.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getActs("act.patientReminder");
        assertEquals(1, reminders.size());

        // set the reminder status to 'Completed'
        Act reminder = reminders.get(0);
        reminder.setStatus("Completed");
        save(reminder);

        // remove the invoice and verify it can't be retrieved
        IMObjectReference actRef = invoice.getAct().getObjectReference();
        remove(invoice.getAct());
        assertNull(get(actRef));

        // verify the reminder hasn't been removed
        assertNotNull(get(reminder.getObjectReference()));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        _clinician = createClinician();
        _patient = createPatient();
        _reminder = createReminder();
    }

    /**
     * Helper to create an <em>act.customerAccountChargesInvoice</em>.
     *
     * @return a new act
     */
    private ActBean createInvoice() {
        Act act = createAct("act.customerAccountChargesInvoice");
        ActBean bean = new ActBean(act);
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
     * Creates and saves a new product.
     *
     * @param reminder if <code>true</code> add a reminder
     * @return a new product
     */
    private Product createProduct(boolean reminder) {
        Product product = (Product) create("product.medication");
        EntityBean bean = new EntityBean(product);
        bean.setValue("name", "XProduct");
        if (reminder) {
            bean.addRelationship("entityRelationship.productReminder",
                                 _reminder);
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
