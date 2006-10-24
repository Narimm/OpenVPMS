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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * CRUD window for supplier accounts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AccountCRUDWindow extends SupplierActCRUDWindow {

    /**
     * The reverse button.
     */
    private Button _reverse;

    /**
     * The statement button.
     */
    private Button _statement;

    /**
     * The adjust button.
     */
    private Button _adjust;

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
     * Create a new <code>AccountCRUDWindow</code>.
     *
     * @param type         display name for the types of objects that this may
     *                     create
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AccountCRUDWindow(String type, String refModelName,
                             String entityName, String conceptName) {
        super(type, new ShortNameList(refModelName, entityName, conceptName));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        _reverse = ButtonFactory.create(REVERSE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onReverse();
            }
        });
        _statement = ButtonFactory.create(STATEMENT_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onStatement();
            }
        });
        _adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onAdjust();
            }
        });
        buttons.add(_reverse);
        buttons.add(getPrintButton());
        buttons.add(_statement);
        buttons.add(_adjust);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        Row buttons = getButtons();
        buttons.removeAll();
        if (enable) {
            buttons.add(_reverse);
            buttons.add(getPrintButton());
            buttons.add(_statement);
            buttons.add(_adjust);
        }
    }

    /**
     * Invoked when the 'reverse' button is pressed.
     */
    protected void onReverse() {
        final Act act = (Act) getObject();
        String status = act.getStatus();
        if (POSTED.equals(status)) {
            String name = getArchetypeDescriptor().getDisplayName();
            String title = Messages.get("supplier.account.reverse.title", name);
            String message = Messages.get("supplier.account.reverse.message",
                                          name);
            final ConfirmationDialog dialog
                    = new ConfirmationDialog(title, message);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent e) {
                    if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                        reverse(act);
                    }
                }
            });
            dialog.show();
        } else {
            showStatusError(act, "supplier.account.noreverse.title",
                            "supplier.account.noreverse.message");
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
    private void reverse(Act act) {
        try {
            IMObjectCopier copier
                    = new IMObjectCopier(new SupplierActReversalHandler(act));
            Act reversal = (Act) copier.copy(act);
            reversal.setStatus(IN_PROGRESS);
            reversal.setActivityStartTime(new Date());
            setPrintStatus(reversal, false);
            SaveHelper.save(reversal);
        } catch (OpenVPMSException exception) {
            String title = Messages.get("supplier.account.reverse.failed");
            ErrorHelper.show(title, exception);
        }
    }

}
