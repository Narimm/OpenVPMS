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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.claim;

import echopointng.ProgressBar;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.webcontainer.ContainerContext;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.internal.claim.GapClaimImpl;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.MessageDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

/**
 * A dialog that prompts the user to wait for a gap claim benefit.
 *
 * @author Tim Anderson
 */
class BenefitDialog extends MessageDialog {

    /**
     * 'Pay full claim' button identifier.
     */
    public static final String PAY_FULL_CLAIM_ID = "button.payfullclaim";

    /**
     * The progress bar.
     */
    private final ProgressBar bar;

    /**
     * The claim.
     */
    private GapClaimImpl claim;

    /**
     * The time in milliseconds when the claim was last reloaded.
     */
    private long lastReloaded;

    /**
     * The task queue, in order to asynchronously trigger processing.
     */
    private TaskQueueHandle taskQueue;

    /**
     * Constructs a {@link BenefitDialog}.
     *
     * @param claim the claim to poll
     * @param help  the help context
     */
    public BenefitDialog(GapClaimImpl claim, HelpContext help) {
        super(Messages.get("patient.insurance.benefit.title"),
              Messages.format("patient.insurance.benefit.message", claim.getPolicy().getInsurer().getName()),
              new String[]{PAY_FULL_CLAIM_ID, CANCEL_ID}, help);
        this.claim = claim;
        bar = new ProgressBar();
        bar.setCompletedColor(Color.GREEN);
        bar.setNumberOfBlocks(10);
        bar.setMinimum(0);
        bar.setMaximum(10);
        lastReloaded = System.currentTimeMillis();
        enableFullClaim(claim);
    }

    /**
     * Returns the latest instance of the claim.
     *
     * @return the claim
     */
    public GapClaimImpl getClaim() {
        return claim;
    }

    /**
     * Show the window.
     */
    @Override
    public void show() {
        queueRefresh();
        super.show();
    }

    /**
     * Processes a user request to close the window (via the close button).
     * <p>
     * This restores the previous focus
     */
    @Override
    public void userClose() {
        removeTaskQueue();
        super.userClose();
    }

    /**
     * Queues a refresh of the display.
     */
    protected void queueRefresh() {
        ApplicationInstance app = ApplicationInstance.getActive();
        app.enqueueTask(getTaskQueue(), this::refresh);
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label content = LabelFactory.create(true, true);
        content.setText(getMessage());
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, content, bar);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }

    /**
     * Determines if the claim should be reloaded.
     *
     * @param now the current time
     * @return {@code true} if the claim should be reloaded
     */
    protected boolean reload(long now) {
        return now - lastReloaded > DateUtils.MILLIS_PER_SECOND * 4;
    }

    /**
     * Refreshes the display.
     * <p>
     * If the gap status has updated
     */
    protected void refresh() {
        try {
            advance();
            if (!process()) {
                queueRefresh();
                enableFullClaim(claim);
            } else {
                claimUpdated();
            }
        } catch (Throwable throwable) {
            removeTaskQueue();
            ErrorHelper.show(throwable);
        }
    }

    /**
     * If the claim has been accepted, allow the user to pay the full claim without waiting for a benefit amount.
     *
     * @param claim the claim
     */
    private void enableFullClaim(GapClaimImpl claim) {
        boolean accepted = claim.getStatus() == Claim.Status.ACCEPTED;
        getButtons().setEnabled(PAY_FULL_CLAIM_ID, accepted);
    }

    /**
     * Invoked when the benefit is received, or the claim is cancelled or declined.
     * <p>
     * This closes the dialog.
     */
    private void claimUpdated() {
        setAction(OK_ID); // need to set a dummy action
        userClose();
    }

    /**
     * Reloads the claim, and determines if it the gap status has been updated.
     *
     * @return {@code true} if the status has been updated
     */
    private boolean process() {
        boolean result = false;
        long now = System.currentTimeMillis();
        if (reload(now)) {
            lastReloaded = now;
            claim = claim.reload();
            Claim.Status status = claim.getStatus();
            GapClaim.GapStatus gapStatus = claim.getGapStatus();
            if ((gapStatus == GapClaim.GapStatus.RECEIVED && status == Claim.Status.ACCEPTED)
                || (status == Claim.Status.CANCELLING || status == Claim.Status.CANCELLED
                    || status == Claim.Status.DECLINED)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Advances the progress bar.
     */
    private void advance() {
        int value = bar.getValue() + 1;
        if (value > bar.getMaximum()) {
            value = bar.getMinimum();
        }
        bar.setValue(value);
    }

    /**
     * Returns the task queue, creating it if it doesn't exist.
     *
     * @return the task queue
     */
    private TaskQueueHandle getTaskQueue() {
        if (taskQueue == null) {
            ApplicationInstance app = ApplicationInstance.getActive();
            taskQueue = app.createTaskQueue();
            ContainerContext context
                    = (ContainerContext) app.getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
            if (context != null) {
                // set the task queue to call back in 500ms
                context.setTaskQueueCallbackInterval(taskQueue, 500);
            }
        }
        return taskQueue;
    }

    /**
     * Cleans up the task queue.
     */
    private void removeTaskQueue() {
        if (taskQueue != null) {
            final ApplicationInstance app = ApplicationInstance.getActive();
            app.removeTaskQueue(taskQueue);
            taskQueue = null;
        }
    }

}
