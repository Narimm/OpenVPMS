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

package org.openvpms.web.app.supplier;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.DefaultActActions;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.i18n.Messages;

import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;


/**
 * CRUD window for supplier accounts.
 *
 * @author Tim Anderson
 */
public class AccountCRUDWindow extends SupplierActCRUDWindow<FinancialAct> {

    /**
     * Reverse button identifier.
     */
    private static final String REVERSE_ID = "reverse";

    /**
     * Statement button identifier.
     */
    private static final String STATEMENT_ID = "statement";

    /**
     * Adjust button identifier.
     */
    private static final String ADJUST_ID = "adjust";


    /**
     * Constructs an {@code AccountCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public AccountCRUDWindow(Archetypes<FinancialAct> archetypes, Context context, HelpContext help) {
        super(archetypes, DefaultActActions.<FinancialAct>getInstance(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        Button reverse = ButtonFactory.create(REVERSE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onReverse();
            }
        });
        Button statement = ButtonFactory.create(STATEMENT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onStatement();
            }
        });
        Button adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onAdjust();
            }
        });
        buttons.add(reverse);
        buttons.add(createPrintButton());
        buttons.add(statement);
        buttons.add(adjust);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.setEnabled(REVERSE_ID, enable);
        buttons.setEnabled(PRINT_ID, enable);
        buttons.setEnabled(STATEMENT_ID, enable);
        buttons.setEnabled(ADJUST_ID, enable);
    }

    /**
     * Invoked when the 'reverse' button is pressed.
     */
    protected void onReverse() {
        final FinancialAct act = getObject();
        String status = act.getStatus();
        if (POSTED.equals(status)) {
            String name = getArchetypeDescriptor().getDisplayName();
            String title = Messages.get("supplier.account.reverse.title", name);
            String message = Messages.get("supplier.account.reverse.message", name);
            HelpContext help = getHelpContext().subtopic("reverse");
            final ConfirmationDialog dialog = new ConfirmationDialog(title, message, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    reverse(act);
                }
            });
            dialog.show();
        } else {
            showStatusError(act, "supplier.account.noreverse.title", "supplier.account.noreverse.message");
        }
    }

    /**
     * Invoked when the 'statement' button is pressed.
     */
    protected void onStatement() {
    }

    /**
     * Invoked when the 'adjust' button is pressed.
     */
    protected void onAdjust() {
    }

    /**
     * Reverse an invoice or credit act.
     *
     * @param act the act to reverse
     */
    private void reverse(FinancialAct act) {
        try {
            IMObjectCopier copier = new IMObjectCopier(new SupplierActReversalHandler(act));
            List<IMObject> objects = copier.apply(act);
            FinancialAct reversal = (FinancialAct) objects.get(0);
            reversal.setStatus(IN_PROGRESS);
            reversal.setActivityStartTime(new Date());
            getActions().setPrinted(reversal, false);
            SaveHelper.save(objects);
        } catch (OpenVPMSException exception) {
            String title = Messages.get("supplier.account.reverse.failed");
            ErrorHelper.show(title, exception);
        }
    }

}
