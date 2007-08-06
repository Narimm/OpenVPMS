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
import org.openvpms.web.component.util.ErrorHelper;


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
    }

    /**
     * Processes the batch.
     */
    public void process() {
        GenerationDialog dialog = new GenerationDialog(getProcessor());
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                notifyCompleted();
            }
        });
        dialog.show();
    }

    /**
     * Returns the processor.
     *
     * @return the processor
     */
    protected abstract StatementProgressBarProcessor getProcessor();


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
            processor.setListener(new BatchProcessorListener() {
                public void completed() {
                    close();
                    notifyCompleted();
                }

                public void error(Throwable exception) {
                    ErrorHelper.show(title, exception);
                }
            });
            this.processor = processor;
        }

        /**
         * Shows the dialog, and starts the statement generation.
         */
        public void show() {
            super.show();
            processor.process();
        }

        /**
         * Invoked when the 'cancel' button is pressed. This prompts for
         * confirmation.
         */
        @Override
        protected void onCancel() {
            final ConfirmationDialog dialog
                    = new ConfirmationDialog(cancelTitle, cancelMessage);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent e) {
                    if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                        GenerationDialog.this.close(CANCEL_ID);
                    } else {
                        processor.process();
                    }
                }
            });
            processor.setSuspend(true);
            dialog.show();
        }
    }
}
