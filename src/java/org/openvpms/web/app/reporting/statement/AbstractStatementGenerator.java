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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.statement;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.VetoListener;
import org.openvpms.web.component.util.Vetoable;


/**
 * Abstract implementation of the {@link BatchProcessor} for statement
 * generation.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractStatementGenerator
        extends AbstractBatchProcessor {

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
     * The current generation dialog.
     */
    private GenerationDialog dialog;


    /**
     * Creates a new <tt>AbstractStatementGenerator</tt>.
     *
     * @param title       the generator title
     * @param cancelTitle the generator message
     */
    public AbstractStatementGenerator(String title, String cancelTitle,
                                      String cancelMessage) {
        this.title = title;
        this.cancelTitle = cancelTitle;
        this.cancelMessage = cancelMessage;
        cancelListener = new VetoListener() {
            public void onVeto(Vetoable source) {
                onCancel(source);
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
        if (processor.getCount() > 1) {
            // open a dialog to give the user the opportunity to cancel
            dialog = new GenerationDialog(processor);
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
     */
    private void onCancel(final Vetoable source) {
        final StatementProgressBarProcessor processor = getProcessor();
        processor.setSuspend(true);
        final ConfirmationDialog dialog
                = new ConfirmationDialog(cancelTitle, cancelMessage);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    source.veto(false);
                    onCompletion();
                } else {
                    source.veto(true);
                    processor.process();
                }
            }
        });
        dialog.show();
    }

    private class GenerationDialog extends PopupDialog {

        /**
         * The processor.
         */
        private final StatementProgressBarProcessor processor;


        /**
         * Creates a new <tt>GenerationDialog</tt>.
         */
        public GenerationDialog(StatementProgressBarProcessor processor) {
            super(title, CANCEL);
            setModal(true);
            Column column = ColumnFactory.create(
                    "Inset", processor.getComponent());
            getLayout().add(column);
            this.processor = processor;
        }

        /**
         * Shows the dialog, and starts the statement generation.
         */
        public void show() {
            super.show();
            processor.process();
        }

    }
}
