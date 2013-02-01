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
 *  Copyright 2007-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.reporting.statement;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.processor.BatchProcessorDialog;
import org.openvpms.web.component.processor.RetryListener;
import org.openvpms.web.component.util.VetoListener;
import org.openvpms.web.component.util.Vetoable;


/**
 * Abstract implementation of the {@link BatchProcessor} for statement
 * generation.
 *
 * @author Tim Anderson
 */
public abstract class AbstractStatementGenerator extends AbstractBatchProcessor {

    /**
     * The generation dialog title.
     */
    private final String title;

    /**
     * The cancel dialog title.
     */
    private final String cancelTitle;

    /**
     * The cancel dialog message.
     */
    private final String cancelMessage;

    /**
     * The listener to veto cancel requests.
     */
    private final VetoListener cancelListener;

    /**
     * The retry dialog retry title.
     */
    private final String retryTitle;

    /**
     * The listener to veto retry requests.
     */
    private final RetryListener<Party> retryListener;

    /**
     * The current generation dialog.
     */
    private BatchProcessorDialog dialog;


    /**
     * Creates a new <tt>AbstractStatementGenerator</tt>.
     *
     * @param title         the generation dialog title
     * @param cancelTitle   the cancel dialog title
     * @param cancelMessage the cancel dialog message
     * @param retryTitle    the retry dialog title
     */
    public AbstractStatementGenerator(String title, String cancelTitle,
                                      String cancelMessage,
                                      String retryTitle) {
        this.title = title;
        this.cancelTitle = cancelTitle;
        this.cancelMessage = cancelMessage;
        cancelListener = new VetoListener() {
            public void onVeto(Vetoable source) {
                onCancel(source);
            }
        };
        this.retryTitle = retryTitle;
        retryListener = new RetryListener<Party>() {
            public void retry(Party customer, Vetoable action,
                              String reason) {
                onRetry(action, reason);
            }
        };
    }

    /**
     * Processes the batch.
     */
    public void process() {
        final StatementProgressBarProcessor processor = getProcessor();
        processor.setListener(new BatchProcessorListener() {
            public void completed() {
                onCompletion();
            }

            public void error(Throwable exception) {
                onError(exception);
            }
        });
        processor.setRetryListener(retryListener);
        if (processor.getCount() > 1) {
            // open a dialog to give the user the opportunity to cancel
            dialog = new BatchProcessorDialog(title, processor);
            dialog.setCancelListener(cancelListener);
            dialog.show();
        } else {
            // process in the background
            processor.process();
        }
    }

    /**
     * Returns the processor.
     *
     * @return the processor
     */
    protected abstract StatementProgressBarProcessor getProcessor();

    /**
     * Returns the listener to veto cancel requests.
     *
     * @return the listener
     */
    protected VetoListener getCancelListener() {
        return cancelListener;
    }

    /**
     * Invoked when generation is complete.
     * Closes the dialog and notifies any listener.
     */
    private void onCompletion() {
        if (dialog != null) {
            dialog.setDefaultCloseAction(null);
            dialog.close();
            dialog = null;
        }
        setProcessed(getProcessor().getProcessed());
        notifyCompleted();
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    private void onError(Throwable exception) {
        setProcessed(getProcessor().getProcessed());
        notifyError(exception);
    }

    /**
     * Invoked when the 'cancel' button is pressed. This prompts for
     * confirmation.
     *
     * @param action the action to veto if cancel is selected
     */
    private void onCancel(final Vetoable action) {
        final StatementProgressBarProcessor processor = getProcessor();
        processor.setCancel(true);
        final ConfirmationDialog dialog
                = new ConfirmationDialog(cancelTitle, cancelMessage);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    action.veto(false);
                    onCompletion();
                } else {
                    action.veto(true);
                    processor.setCancel(false);
                    processor.process();
                }
            }
        });
        dialog.show();
    }

    /**
     * Retries a failed customer.
     *
     * @param action the action to veto or allow
     * @param reason the reason for the failure
     */
    private void onRetry(final Vetoable action, String reason) {
        final ConfirmationDialog dialog
                = new ConfirmationDialog(retryTitle, reason,
                                         ConfirmationDialog.RETRY_CANCEL);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent e) {
                if (ConfirmationDialog.RETRY_ID.equals(dialog.getAction())) {
                    action.veto(false);
                } else {
                    action.veto(true);
                    onCompletion();
                }
            }
        });
        dialog.show();
    }

}
