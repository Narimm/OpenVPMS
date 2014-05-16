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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.help.HelpListener;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.patient.mr.PatientClinicalEventEditDialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * {@link EditDialogFactory} test case.
 *
 * @author Tim Anderson
 */
public class EditDialogFactoryTestCase extends AbstractAppTest {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * Verifies that a {@link EditDialog} is returned when no other class is configured.
     */
    @Test
    public void testCreateDefaultDialog() {
        checkCreate(ContactArchetypes.PHONE, EditDialog.class);
    }

    /**
     * Verifies that a {@link CustomerChargeActEditDialog} is returned for an <em>act
     * .customerAccountChargesInvoice</em>.
     */
    @Test
    public void testCustomerChargeActEditDialog() {
        checkCreate(CustomerAccountArchetypes.INVOICE, CustomerChargeActEditDialog.class);
    }

    /**
     * Verifies that a {@link ActEditDialog} is returned for customer account acts.
     */
    @Test
    public void testCreateActEditDialog() {
        checkCreate(CustomerAccountArchetypes.COUNTER, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.CREDIT, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.DEBIT_ADJUST, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.PAYMENT, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.REFUND, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.INITIAL_BALANCE, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.BAD_DEBT, ActEditDialog.class);
    }

    /**
     * Verifies that a {@link PatientClinicalEventEditDialog} is returned for an <em>act.patientClinicalEvent</em>.
     */
    public void testCreatePatientClinicalEventEditDialog() {
        checkCreate(PatientArchetypes.CLINICAL_EVENT, PatientClinicalEventEditDialog.class);
    }

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        service = ServiceHelper.getArchetypeService();
    }

    /**
     * Verifies that the dialog returned by {@link EditDialogFactory#create} matches that expected.
     *
     * @param shortName name the archetype short name
     * @param type      the expected editor class
     */
    private void checkCreate(String shortName, Class type) {
        HelpContext help = new HelpContext("dummy", new HelpListener() {
            public void show(HelpContext context) {
            }
        });
        LocalContext local = new LocalContext();
        local.setPractice(TestHelper.getPractice());
        LayoutContext context = new DefaultLayoutContext(local, help);
        IMObject object = service.create(shortName);
        assertNotNull("Failed to create object with shortname=" + shortName, object);
        IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(object, context);
        assertNotNull("Failed to create editor for shortname=" + shortName, editor);
        EditDialog dialog = EditDialogFactory.create(editor, context.getContext());
        assertEquals("Incorrect dialog type for shortname=" + shortName, type, dialog.getClass());
    }

}