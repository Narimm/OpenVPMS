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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.test.EchoTestHelper;
import org.openvpms.web.workspace.patient.PatientEditor;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CustomerEditor}.
 *
 * @author Tim Anderson
 */
public class CustomerEditorTestCase extends AbstractAppTest {

    /**
     * Tests the {@link CustomerEditor#newInstance()} method.
     */
    @Test
    public void testNewInstance() {
        Party customer = TestHelper.createCustomer();
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        CustomerEditor editor = new CustomerEditor(customer, null, context);

        IMObjectEditor newInstance = editor.newInstance();
        assertTrue(newInstance instanceof CustomerEditor);
    }

    /**
     * Verifies a new patient can added to a customer.
     */
    @Test
    public void testAddNewPatient() {
        String species = TestHelper.getLookup(PatientArchetypes.SPECIES, "CANINE").getCode();

        // create a customer, and edit it
        Party customer = TestHelper.createCustomer();
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        CustomerEditor editor = new CustomerEditor(customer, null, context);
        editor.getComponent();

        // add a new patient
        PatientEntityRelationshipCollectionEditor patientCollectionEditor = editor.getPatientCollectionEditor();
        assertNotNull(patientCollectionEditor);
        EchoTestHelper.fireButton(patientCollectionEditor.getComponent(), "add");
        PatientOwnerRelationshipEditor relationshipEditor
                = (PatientOwnerRelationshipEditor) patientCollectionEditor.getCurrentEditor();
        assertNotNull(relationshipEditor);
        EchoTestHelper.fireButton(relationshipEditor.getComponent(), "button.select");
        BrowserDialog patientBrowser = EchoTestHelper.findBrowserDialog();
        assertNotNull(patientBrowser);
        EchoTestHelper.fireButton(patientBrowser, "new");

        // edit the patient
        EditDialog patientEditDialog = EchoTestHelper.findEditDialog();
        assertNotNull(patientEditDialog);
        PatientEditor patientEditor = (PatientEditor) patientEditDialog.getEditor();
        patientEditor.getProperty("name").setValue("Fido");
        patientEditor.getProperty("species").setValue(species);
        EchoTestHelper.fireButton(patientBrowser, "new");
        EchoTestHelper.fireButton(patientEditDialog, "ok");
        assertTrue(SaveHelper.save(editor));

        // verify the patient has been added to the customer
        customer = get(customer);
        IMObjectBean bean = getBean(customer);
        List<Party> patients = bean.getTargets("patients", Party.class);
        assertEquals(1, patients.size());
        assertEquals(patients.get(0), patientEditor.getObject());
    }
}
