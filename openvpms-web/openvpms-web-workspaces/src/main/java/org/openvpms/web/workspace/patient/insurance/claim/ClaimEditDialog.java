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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Edit dialog for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimEditDialog extends EditDialog {

    /**
     * The mail context.
     */
    private MailContext mailContext;

    /**
     * Generate attachments button identifier.
     */
    private static final String GENERATE_ID = "button.generateattachments";

    /**
     * Submit claim button identifier.
     */
    private static final String SUBMIT_ID = "button.submit";

    /**
     * The buttons to display.
     */
    private static final String[] BUTTONS = {APPLY_ID, OK_ID, CANCEL_ID, GENERATE_ID, SUBMIT_ID};

    /**
     * Constructs a {@link ClaimEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public ClaimEditDialog(ClaimEditor editor, Context context) {
        super(editor, BUTTONS, true, context);
    }

    /**
     * Sets the mail context.
     *
     * @param mailContext the mail context. May be {@code null}
     */
    public void setMailContext(MailContext mailContext) {
        this.mailContext = mailContext;
    }

    /**
     * Returns the editor.
     *
     * @return the editor, or {@code null} if none has been set
     */
    @Override
    public ClaimEditor getEditor() {
        return (ClaimEditor) super.getEditor();
    }

    /**
     * Submits the claim, if it is valid.
     */
    public void submit() {
        onSubmit();
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
        if (GENERATE_ID.equals(button)) {
            onGenerate();
        } else if (SUBMIT_ID.equals(button)) {
            onSubmit();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Generates attachments.
     */
    protected void onGenerate() {
        if (save()) {
            ClaimEditor editor = getEditor();
            if (!editor.generateAttachments()) {
                editor.showAttachments();
                editor.checkAttachments();
            }
        }
    }

    /**
     * Submits a claom.
     */
    protected void onSubmit() {
        if (save()) {
            FinancialAct act = (FinancialAct) getEditor().getObject();
            try {
                HelpContext help = getHelpContext();
                ClaimSubmitter submitter = new ClaimSubmitter(getContext(), help);
                final ClaimEditor editor = getEditor();
                FinancialAct object = (FinancialAct) editor.getObject();
                submitter.submit(editor, exception -> {
                    if (exception != null || !Claim.Status.SUBMITTED.isA(editor.getStatus())) {
                        reloadOnSubmitFailure(object, exception);

                    } else {
                        setAction(SUBMIT_ID);
                        close();
                    }
                });
            } catch (Throwable exception) {
                reloadOnSubmitFailure(act, exception);
            }
        }
    }

    /**
     * Invoked when an exception occurs, or the user cancels out of a claim submission.
     * <p>
     * This reloads the claim, and either re-instates the editor if the claim is still editable, or replaces it with
     * a viewer, if it isn't.
     *
     * @param object    the claim
     * @param exception the exception encountered during claim submission. May be {@code null}
     */
    private void reloadOnSubmitFailure(FinancialAct object, Throwable exception) {
        IMObjectEditor editor = getEditor();
        HelpContext help = getHelpContext();
        // the claim wasn't submitted. Reload to make sure the latest instance is being used,
        // and check the attachments
        FinancialAct claim = IMObjectHelper.reload(object);
        if (claim != null) {
            if (Claim.Status.PENDING.isA(claim.getStatus())) {
                ClaimEditor newEditor = new ClaimEditor(claim, null,
                                                        new DefaultLayoutContext(true, getContext(), help));
                setEditor(newEditor);
                if (!newEditor.checkAttachments()) {
                    newEditor.showAttachments();
                }
            } else {
                view(object, help);
            }
        } else {
            // claim has been deleted externally
            setEditor(null);
            saveFailed();
        }
        if (exception != null) {
            ErrorHelper.show(Messages.get("patient.insurance.submit.title"), editor.getDisplayName(), object,
                             exception);
        }
    }

    private void view(FinancialAct claim, HelpContext help) {
        setEditor(null);
        IMObjectViewer viewer = new IMObjectViewer(claim,
                                                   new DefaultLayoutContext(getContext(), help));
        setComponent(viewer.getComponent(), viewer.getFocusGroup(), help);
    }
}
