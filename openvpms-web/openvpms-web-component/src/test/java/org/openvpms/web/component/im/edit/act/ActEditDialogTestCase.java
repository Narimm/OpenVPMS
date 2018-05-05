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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import nextapp.echo2.app.Button;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.DefaultIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link ActEditDialog}.
 *
 * @author Tim Anderson
 */
public class ActEditDialogTestCase extends AbstractAppTest {

    /**
     * The layout context.
     */
    private LayoutContext context;

    /**
     * The charge.
     */
    private FinancialAct charge;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Party customer = TestHelper.createCustomer();
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(BigDecimal.TEN, customer, product,
                                                                           ActStatus.IN_PROGRESS);
        save(acts);
        charge = acts.get(0);
        context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
    }

    /**
     * Verifies that when the status is changed to {@link ActStatus#POSTED}, the Apply button is disabled.
     */
    @Test
    public void testChangeStatus() {
        DefaultIMObjectEditor editor = new DefaultIMObjectEditor(charge, context);

        ActEditDialog dialog = new ActEditDialog(editor, context.getContext());
        dialog.show();
        checkEnabled(dialog, PopupDialog.APPLY_ID, true);
        checkEnabled(dialog, PopupDialog.OK_ID, true);
        checkEnabled(dialog, PopupDialog.CANCEL_ID, true);

        setStatus(dialog, ActStatus.POSTED);
        checkEnabled(dialog, PopupDialog.APPLY_ID, false);
        checkEnabled(dialog, PopupDialog.OK_ID, true);
        checkEnabled(dialog, PopupDialog.CANCEL_ID, true);

        setStatus(dialog, ActStatus.IN_PROGRESS);
        checkEnabled(dialog, PopupDialog.APPLY_ID, true);
        checkEnabled(dialog, PopupDialog.OK_ID, true);
        checkEnabled(dialog, PopupDialog.CANCEL_ID, true);
    }

    /**
     * Verifies that the dialog supports reloading, and that the button enable states reflect the status of the act.
     */
    @Test
    public void testReload() {
        IMObjectEditor editor = new DefaultIMObjectEditor(charge, context);
        ActEditDialog dialog = new ActEditDialog(editor, context.getContext());
        dialog.show();

        setStatus(dialog, ActStatus.POSTED);
        checkEnabled(dialog, PopupDialog.APPLY_ID, false);
        checkEnabled(dialog, PopupDialog.OK_ID, true);
        checkEnabled(dialog, PopupDialog.CANCEL_ID, true);

        // now change the act outside the editor and save it. The dialog should revert to the saved instance
        Act copy = get(charge);
        copy.setStatus(ActStatus.COMPLETED);
        save(copy);

        dialog.save(true);
        assertNotNull(dialog.getEditor());
        assertNotEquals(editor, dialog.getEditor());
        checkStatus(dialog, ActStatus.COMPLETED);

        checkEnabled(dialog, PopupDialog.APPLY_ID, true);
        checkEnabled(dialog, PopupDialog.OK_ID, true);
        checkEnabled(dialog, PopupDialog.CANCEL_ID, true);

        setStatus(dialog, ActStatus.POSTED);
        checkEnabled(dialog, PopupDialog.APPLY_ID, false);
        checkEnabled(dialog, PopupDialog.OK_ID, true);
        checkEnabled(dialog, PopupDialog.CANCEL_ID, true);

        // change the status to posted outside the editor
        copy.setStatus(ActStatus.POSTED);
        save(copy);

        // now try and save the editor. It should be reloaded, and Apply and OK disabled as the act is POSTED.
        editor = dialog.getEditor();
        dialog.save(true);
        assertNotNull(dialog.getEditor());
        assertNotEquals(editor, dialog.getEditor());
        checkStatus(dialog, ActStatus.POSTED);

        checkEnabled(dialog, PopupDialog.APPLY_ID, false);
        checkEnabled(dialog, PopupDialog.OK_ID, false);
        checkEnabled(dialog, PopupDialog.CANCEL_ID, true);
    }

    /**
     * Verifies that the Apply and OK buttons are disabled if the object is deleted outside the editor.
     */
    @Test
    public void testDelete() {
        IMObjectEditor editor = new DefaultIMObjectEditor(charge, context);
        ActEditDialog dialog = new ActEditDialog(editor, context.getContext());
        dialog.show();

        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                ServiceHelper.getArchetypeService().remove(charge);
            }
        });

        setStatus(dialog, ActStatus.COMPLETED);
        dialog.save(true);
        assertNotNull(dialog.getEditor());
        assertEquals(editor, dialog.getEditor());

        checkEnabled(dialog, PopupDialog.APPLY_ID, false);
        checkEnabled(dialog, PopupDialog.OK_ID, false);
        checkEnabled(dialog, PopupDialog.CANCEL_ID, true);
    }

    /**
     * Sets the act status.
     *
     * @param dialog the edit dialog
     * @param status the new act status
     */
    private void setStatus(ActEditDialog dialog, String status) {
        IMObjectEditor editor = dialog.getEditor();
        assertNotNull(editor);
        editor.getProperty("status").setValue(status);
    }

    /**
     * Verifies the act status matches that expected.
     *
     * @param dialog the edit dialog
     * @param status the expected status
     */
    private void checkStatus(ActEditDialog dialog, String status) {
        IMObjectEditor editor = dialog.getEditor();
        assertNotNull(editor);
        assertEquals(status, editor.getProperty("status").getString());
    }

    /**
     * Verifies that a button enabled status matches that expected.
     *
     * @param dialog   the dialog
     * @param id       the button identifier
     * @param expected the expected enabled state
     */
    private void checkEnabled(ActEditDialog dialog, String id, boolean expected) {
        Button button = dialog.getButtons().getButton(id);
        assertNotNull(button);
        assertEquals(expected, button.isEnabled());
    }
}
