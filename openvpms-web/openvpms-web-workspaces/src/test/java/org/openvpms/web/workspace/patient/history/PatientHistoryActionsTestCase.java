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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PatientHistoryActions} class.
 *
 * @author Tim Anderson
 */
public class PatientHistoryActionsTestCase extends AbstractAppTest {

    /**
     * Tests the {@link PatientHistoryActions#canDelete(Act)} method.
     */
    @Test
    public void testCanDelete() {
        PatientHistoryActions actions = new PatientHistoryActions();
        Party patient = TestHelper.createPatient();
        Act event1 = PatientTestHelper.createEvent(patient);
        Act problem = PatientTestHelper.createProblem(new Date(), patient);
        Act medication = PatientTestHelper.createMedication(patient);
        Act investigation = PatientTestHelper.createInvestigation(patient, ProductTestHelper.createInvestigationType());
        Act note = PatientTestHelper.createNote(new Date(), patient);
        Act weight = PatientTestHelper.createWeight(new Date(), patient);
        Act attachment = PatientTestHelper.createDocumentAttachment(new Date(), patient);
        Act form = PatientTestHelper.createDocumentForm(patient);
        Act letter = PatientTestHelper.createDocumentLetter(new Date(), patient);
        Act image = PatientTestHelper.createDocumentImage(new Date(), patient);
        assertTrue(actions.canDelete(event1));
        assertTrue(actions.canDelete(problem));
        assertTrue(actions.canDelete(medication));
        assertTrue(actions.canDelete(investigation));
        assertTrue(actions.canDelete(note));
        assertTrue(actions.canDelete(weight));
        assertTrue(actions.canDelete(attachment));
        assertTrue(actions.canDelete(form));
        assertTrue(actions.canDelete(letter));
        assertTrue(actions.canDelete(image));

        Act event2 = PatientTestHelper.createEvent(new Date(), patient, problem, medication, investigation, note,
                                                   weight, attachment, form, letter, image);
        assertFalse(actions.canDelete(event2));
        assertTrue(actions.canDelete(problem));
        assertTrue(actions.canDelete(medication));
        assertTrue(actions.canDelete(investigation));
        assertTrue(actions.canDelete(note));
        assertTrue(actions.canDelete(weight));
        assertTrue(actions.canDelete(attachment));
        assertTrue(actions.canDelete(form));
        assertTrue(actions.canDelete(letter));
        assertTrue(actions.canDelete(image));

        // verifies that items linked to an invoice item cannot be deleted, unless they are patient documents
        Act invoiceItem = (Act) create(CustomerAccountArchetypes.INVOICE_ITEM);
        ActBean bean = new ActBean(invoiceItem);
        bean.addNodeRelationship("dispensing", medication);
        bean.addNodeRelationship("investigations", investigation);
        bean.addNodeRelationship("documents", attachment);
        bean.addNodeRelationship("documents", form);
        bean.addNodeRelationship("documents", letter);
        bean.addNodeRelationship("documents", image);

        assertFalse(actions.canDelete(medication));
        assertFalse(actions.canDelete(investigation));
        assertTrue(actions.canDelete(attachment));
        assertTrue(actions.canDelete(form));
        assertTrue(actions.canDelete(letter));
        assertTrue(actions.canDelete(image));

        // verifies that a problem cannot be deleted if it has items
        Act problem2 = PatientTestHelper.createProblem(new Date(), patient, note);
        assertFalse(actions.canDelete(problem2));
    }
}
