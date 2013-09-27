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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.util;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Tests the {@link IMObjectDeleter} class.
 *
 * @author Tim Anderson
 */
public class IMObjectDeleterTestCase extends AbstractAppTest {

    /**
     * The help context.
     */
    private final HelpContext help = new HelpContext("foo", null);


    /**
     * Verifies that attempting to delete an entity with participations deactivates it.
     */
    @Test
    public void testDeleteEntityWithParticipations() {
        // create a customer and associated invoice
        Party customer = TestHelper.createCustomer();
        Party pet = TestHelper.createPatient(customer);
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(new Money("100"),
                                                                              customer, pet,
                                                                              TestHelper.createProduct(),
                                                                              ActStatus.POSTED);
        save(invoice);     // customer has participation relationships to the invoice

        TestDeleter deleter = new TestDeleter();
        TestListener listener = new TestListener();
        deleter.delete(customer, help, listener);

        // verify the customer has been deactivated rather than deleted
        checkDeleter(deleter, false, true, false);
        checkListener(listener, false);

        customer = get(customer);
        assertNotNull(customer);
        assertFalse(customer.isActive());

        // now attempt deletion again. The deactivated() method should be invoked
        deleter = new TestDeleter();
        listener = new TestListener();
        deleter.delete(customer, help, listener);
        checkDeleter(deleter, false, false, true);
        checkListener(listener, false);
    }

    /**
     * Verifies that attempting to delete an entity which is the source of a relationship deactivates it instead.
     */
    @Test
    public void testDeleteSourceWithEntityRelationships() {
        Party customer = TestHelper.createCustomer();
        Party pet = TestHelper.createPatient(customer);

        TestDeleter deleter = new TestDeleter();
        TestListener listener = new TestListener();
        deleter.delete(customer, help, listener);

        // verify the customer has been deactivated
        checkDeleter(deleter, false, true, false);
        checkListener(listener, false);

        customer = get(customer);
        assertNotNull(customer);
        assertFalse(customer.isActive());
        assertNotNull(get(pet));
    }

    /**
     * Verifies that attempting to delete an entity which is the target of a relationship
     * invokes {@link IMObjectDeleter#remove}, and performs the removal.
     */
    @Test
    public void testDeleteTargetWithEntityRelationships() {
        Party customer = TestHelper.createCustomer();
        Party pet = TestHelper.createPatient(customer);

        TestDeleter deleter = new TestDeleter();
        TestListener listener = new TestListener();
        deleter.delete(pet, help, listener);

        // verify the customer has been deleted
        checkDeleter(deleter, true, false, false);
        checkListener(listener, true);

        assertNull(get(pet));
        assertNotNull(get(customer));
    }

    /**
     * Verifies that <em>entity.documentTemplate</em> can be deleted despite having an
     * <em>participation.document</em> participation.
     */
    @Test
    public void testDeleteTemplate() {
        TestDeleter deleter = new TestDeleter();

        // create a template with associated act.documentTemplate
        Entity template = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_FORM);
        TestListener listener = new TestListener();
        deleter.delete(template, help, listener);

        // verify the template has been deleted
        checkDeleter(deleter, true, false, false);
        checkListener(listener, true);

        assertNull(get(template));
    }

    /**
     * Verifies that <em>entity.documentTemplate</em> can't be deleted if it has participations
     * other than <em>participation.document</em>. These will be deactivated.
     */
    @Test
    public void testDeleteTemplateWithParticipations() {
        TestDeleter deleter = new TestDeleter();

        Entity template = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_FORM);
        Act act = (Act) create(PatientArchetypes.DOCUMENT_FORM);
        Party patient = TestHelper.createPatient();
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", patient);
        bean.addNodeParticipation("documentTemplate", template);
        save(act);

        TestListener listener = new TestListener();
        deleter.delete(template, help, listener);

        // verify the template has been deactivated
        checkDeleter(deleter, false, true, false);
        checkListener(listener, false);

        template = get(template);
        assertNotNull(template);
        assertFalse(template.isActive());

        // now attempt deletion again. The deactivated() method should be invoked
        deleter = new TestDeleter();
        listener = new TestListener();
        deleter.delete(template, help, listener);
        checkDeleter(deleter, false, false, true);
        checkListener(listener, false);
    }

    @Test
    public void testDeleteWithRelationshipsExcluded() {
        TestDeleter deleter = new TestDeleter();
        TestListener listener = new TestListener();

        Entity job = (Entity) create("entity.jobESCIInboxReader");
        Entity runAs = TestHelper.createUser();
        EntityBean bean = new EntityBean(job);
        bean.addNodeRelationship("runAs", runAs);
        save(job, runAs);

        deleter.delete(job, help, listener);

        // verify the job has been deactivated rather than deleted
        checkDeleter(deleter, false, true, false);
        checkListener(listener, false);

        // now attempt deletion again, but this time exclude the runAs relationship
        deleter = new TestDeleter();
        listener = new TestListener();
        deleter.setExcludeRelationships("entityRelationship.jobUser");
        deleter.delete(job, help, listener);

        checkDeleter(deleter, true, false, false);
        checkListener(listener, true);
    }

    /**
     * Verifies that the appropriate deleter methods have been invoked.
     *
     * @param deleter     the deleter to check
     * @param remove      if {@code true} expect remove() to have been invoked
     * @param deactivate  if {@code true} expect deactivate() to have been invoked
     * @param deactivated if {@code true} expect deactivated() to have been invoked
     */
    private void checkDeleter(TestDeleter deleter, boolean remove, boolean deactivate, boolean deactivated) {
        assertEquals(remove, deleter.removeInvoked());
        assertEquals(deactivate, deleter.deactivateInvoked());
        assertEquals(deactivated, deleter.deactivatedInvoked());
    }

    /**
     * Verifies that the appropriate listener methods have been invoked.
     *
     * @param listener the listener to check
     * @param deleted  if {@code true} expect the deleted() method to have been invoked
     */
    private void checkListener(TestListener listener, boolean deleted) {
        assertEquals(deleted, listener.deletedInvoked());
        assertFalse(listener.failedInvoked());
    }

    private static class TestDeleter extends IMObjectDeleter {

        /**
         * Determines if remove() was invoked.
         */
        private boolean remove;

        /**
         * Determines if deactivate() has been invoked.
         */
        private boolean deactivate;

        /**
         * Determines if deactivated() has been invoked.
         */
        private boolean deactivated;

        /**
         * Constructs a {@code TestDeleter}.
         */
        public TestDeleter() {
            super(new LocalContext());
        }

        /**
         * Determines if remove() has been invoked.
         *
         * @return {@code true} if remove() was invoked
         */
        public boolean removeInvoked() {
            return remove;
        }

        /**
         * Determines if deactivate() has been invoked.
         *
         * @return {@code true} if deactivate() was invoked
         */
        public boolean deactivateInvoked() {
            return deactivate;
        }

        /**
         * Determines if deactivated() has been invoked.
         *
         * @return {@code true} if deactivated() was invoked
         */
        public boolean deactivatedInvoked() {
            return deactivated;
        }

        /**
         * Invoked to remove an object.
         *
         * @param object   the object to remove
         * @param listener the listener to notify
         * @param help     the help context
         */
        protected <T extends IMObject> void remove(T object, IMObjectDeletionListener<T> listener, HelpContext help) {
            remove = true;
            doRemove(object, listener, help);
        }

        /**
         * Invoked to deactivate an object.
         *
         * @param object   the object to deactivate
         * @param listener the listener
         * @param help     the help context
         */
        protected <T extends IMObject> void deactivate(T object, IMObjectDeletionListener<T> listener,
                                                       HelpContext help) {
            deactivate = true;
            doDeactivate(object, listener);
        }

        /**
         * Invoked when an object cannot be de deleted, and has already been deactivated.
         *
         * @param object the object
         * @param help   the help context
         */
        protected <T extends IMObject> void deactivated(T object, HelpContext help) {
            deactivated = true;
        }
    }

    private class TestListener extends AbstractIMObjectDeletionListener<Entity> {

        /**
         * Determines if deleted() was invoked.
         */
        private boolean deleted;

        /**
         * Determines if one of the failed*() methods was invoked.
         */
        private boolean failed;

        /**
         * Determines if deleted() was invoked.
         *
         * @return {@code true} if deleted() was invoked
         */
        public boolean deletedInvoked() {
            return deleted;
        }

        /**
         * Determines if one of the failed*() methods was invoked.
         *
         * @return {@code true} if a failed*() method was invoked
         */
        public boolean failedInvoked() {
            return failed;
        }

        @Override
        public void deleted(Entity object) {
            deleted = true;
        }


        @Override
        public void failed(Entity object, Throwable cause) {
            failed = true;
        }

        @Override
        public void failed(Entity object, Throwable cause, IMObjectEditor editor) {
            failed = true;
        }
    }
}
