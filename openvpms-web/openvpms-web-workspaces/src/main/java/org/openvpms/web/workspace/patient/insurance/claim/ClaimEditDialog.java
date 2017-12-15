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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

import java.util.function.Consumer;

/**
 * Edit dialog for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimEditDialog extends EditDialog {

    /**
     * The claim.
     */
    private FinancialAct claim;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ClaimEditDialog.class);

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
        this.claim = (FinancialAct) editor.getObject();
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
     * Saves the current object, if saving is enabled, and closes the editor.
     */
    @Override
    protected void onOK() {
        if (getEditor() != null) {
            super.onOK();
        } else {
            close(OK_ID);
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
     * Submits a claim.
     */
    protected void onSubmit() {
        Consumer<Throwable> listener = exception -> {
            if (exception != null || !Claim.Status.SUBMITTED.isA(claim.getStatus())) {
                reloadOnSubmitFailure(exception);
            } else {
                setAction(SUBMIT_ID);
                close();
            }
        };
        if (Claim.Status.PENDING.isA(claim.getStatus())) {
            if (save()) {
                try {
                    ClaimSubmitter submitter = new ClaimSubmitter(getContext(), getHelpContext());
                    submitter.submit(getEditor(), listener);
                } catch (Throwable exception) {
                    reloadOnSubmitFailure(exception);
                }
            }
        } else if (Claim.Status.POSTED.isA(claim.getStatus())) {
            // claim was partially submitted before, but failed
            ClaimSubmitter submitter = new ClaimSubmitter(getContext(), getHelpContext());
            submitter.submit(claim, listener);
        } else {
            // claim has already been submitted
            setAction(SUBMIT_ID);
            close();
        }
    }

    /**
     * Invoked when an exception occurs, or the user cancels out of a claim submission.
     * <p>
     * This reloads the claim, and either re-instates the editor if the claim is still editable, or replaces it with
     * a viewer, if it isn't.
     *
     * @param exception the exception encountered during claim submission. May be {@code null}
     */
    private void reloadOnSubmitFailure(Throwable exception) {
        HelpContext help = getHelpContext();
        // the claim wasn't submitted. Reload to make sure the latest instance is being used,
        // and check the attachments
        FinancialAct act = IMObjectHelper.reload(claim);
        if (act != null) {
            claim = act;
            if (Claim.Status.PENDING.isA(act.getStatus())) {
                ClaimEditor newEditor = new ClaimEditor(act, null, new DefaultLayoutContext(true, getContext(), help));
                setEditor(newEditor);
                if (!newEditor.checkAttachments()) {
                    newEditor.showAttachments();
                }
            } else {
                // can no longer edit the claim, so view it
                setEditor(null);
                IMObjectViewer viewer = new IMObjectViewer(claim, new DefaultLayoutContext(getContext(), help));
                setComponent(viewer.getComponent(), viewer.getFocusGroup(), help);
            }
        } else {
            // claim has been deleted externally
            setEditor(null);
            saveFailed();
        }
        if (exception != null) {
            log.error("Failed to submit claim", exception);
            ErrorHelper.show(Messages.get("patient.insurance.submit.title"), DescriptorHelper.getDisplayName(claim),
                             claim, exception);
        }
    }
}
