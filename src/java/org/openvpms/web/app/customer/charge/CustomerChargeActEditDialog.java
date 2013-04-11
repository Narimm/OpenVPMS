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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.customer.charge;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.act.ActEditDialog;

import java.util.List;


/**
 * An edit dialog for {@link CustomerChargeActEditor} editors.
 * <p/>
 * This performs printing of unprinted documents that have their <em>interactive</em> flag set to {@code true}
 * when <em>Apply</em> or <em>OK</em> is pressed.
 *
 * @author Tim Anderson
 */
public class CustomerChargeActEditDialog extends ActEditDialog {

    /**
     * Completed button identifier.
     */
    private static final String COMPLETED_ID = "completed";

    /**
     * In Progress button identifier.
     */
    private static final String IN_PROGRESS_ID = "inprogress";


    /**
     * Constructs a {@code CustomerChargeActEditDialog}.
     *
     * @param editor the editor
     * @param help   the help context
     */
    public CustomerChargeActEditDialog(CustomerChargeActEditor editor, HelpContext help) {
        super(editor, help);
        addButton(COMPLETED_ID, false);
        addButton(IN_PROGRESS_ID, false);
        setDefaultCloseAction(CANCEL_ID);
    }

    /**
     * Saves the current object.
     * <p/>
     * Any documents added as part of the save that have a template with an IMMEDIATE print mode will be printed.
     */
    @Override
    protected void onOK() {
        CustomerChargeDocuments docs = new CustomerChargeDocuments((CustomerChargeActEditor) getEditor(),
                                                                   getHelpContext());
        List<Act> existing = docs.getUnprinted();
        if (save()) {
            ActionListener printListener = new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    close(OK_ID);
                }
            };
            if (!docs.printNew(existing, printListener)) {
                // nothing to print, so close now
                close(OK_ID);
            }
        }
    }

    /**
     * Saves the current object.
     * <p/>
     * Any documents added as part of the save that have a template with an IMMEDIATE print mode will be printed.
     */
    @Override
    protected void onApply() {
        CustomerChargeDocuments docs = new CustomerChargeDocuments((CustomerChargeActEditor) getEditor(),
                                                                   getHelpContext());
        List<Act> existing = docs.getUnprinted();
        if (save()) {
            docs.printNew(existing, null);
        }
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (IN_PROGRESS_ID.equals(button)) {
            onInProgress();
        } else if (COMPLETED_ID.equals(button)) {
            onCompleted();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Invoked when the 'In Progress' button is pressed.
     * <p/>
     * If the act hasn't been POSTED, then this sets the status to IN_PROGRESS, and attempts to save and close the
     * dialog.
     */
    private void onInProgress() {
        if (!isPosted()) {
            CustomerChargeActEditor editor = (CustomerChargeActEditor) getEditor();
            editor.setStatus(ActStatus.IN_PROGRESS);
            onOK();
        }
    }

    /**
     * Invoked when the 'Completed' button is pressed.
     * <p/>
     * If the act hasn't been POSTED, then this sets the status to COMPLETED, and attempts to save and close the
     * dialog.
     */
    private void onCompleted() {
        if (!isPosted()) {
            CustomerChargeActEditor editor = (CustomerChargeActEditor) getEditor();
            editor.setStatus(ActStatus.COMPLETED);
            onOK();
        }
    }

}