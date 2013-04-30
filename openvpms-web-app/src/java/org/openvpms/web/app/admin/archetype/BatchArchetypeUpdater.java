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

package org.openvpms.web.app.admin.archetype;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.component.processor.AbstractAsynchronousBatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.tools.archetype.loader.Change;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Updates objects associated with a batch of changed {@link ArchetypeDescriptor}s,
 * providing a progress bar and cancel prompt.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BatchArchetypeUpdater
    extends AbstractAsynchronousBatchProcessor<Change> {

    /**
     * Assertion names to check for during updates.
     */
    public static String[] ASSERTIONS = {"propercase", "lowercase", "uppercase"};

    /**
     * The update progress dialog.
     */
    private UpdateDialog dialog;

    /**
     * The progress bar.
     */
    private ObjectUpdateProgressBarProcessor processor;


    /**
     * Constructs a new <tt>BatchArchetypeUpdater</tt>.
     *
     * @param changes the changed archetype descriptors of the objects to update
     */
    public BatchArchetypeUpdater(List<Change> changes) {
        super(changes.iterator());
    }

    /**
     * Processes an object.
     *
     * @param change the object to process
     */
    protected void process(final Change change) {
        if (processor == null) {
            processor = new ObjectUpdateProgressBarProcessor();
            processor.setListener(new BatchProcessorListener() {
                public void completed() {
                    process(); // process the next change, if any
                }

                public void error(Throwable exception) {
                    notifyError(exception);
                }
            });
        }
        if (dialog == null) {
            dialog = new UpdateDialog(processor);
            dialog.show();
        }
        setSuspend(true);
        dialog.setArchetype(change.getNewVersion().getDisplayName());
        processor.update(change);
    }

    /**
     * Invoked when batch processing has completed.
     * This implementation delegates to {@link #notifyCompleted()}.
     */
    @Override
    protected void processingCompleted() {
        closeDialog();
        setProcessed(processor.getProcessed());
        super.processingCompleted();
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    @Override
    protected void notifyError(Throwable exception) {
        closeDialog();
        setSuspend(true);
        super.notifyError(exception);
    }

    /**
     * Closes the dialog if it is open.
     */
    private void closeDialog() {
        if (dialog != null) {
            dialog.setDefaultCloseAction(PopupDialog.CLOSE_ID);
            dialog.close();
            dialog = null;
        }
    }

    private class UpdateDialog extends PopupDialog {

        /**
         * The archetype name label.
         */
        private final Label label;

        /**
         * Creates a new <tt>UpdateDialog</tt>.
         *
         * @param processor the processor
         */
        public UpdateDialog(ObjectUpdateProgressBarProcessor processor) {
            super(Messages.get("archetype.updating.title"), CANCEL);
            setModal(true);
            label = LabelFactory.create();
            Row row = RowFactory.create("CellSpacing", label, processor.getComponent());
            Column column = ColumnFactory.create("Inset", row);
            getLayout().add(column);
        }

        /**
         * Sets the archetype being processed.
         *
         * @param displayName the archetype display name
         */
        public void setArchetype(String displayName) {
            label.setText(Messages.get("archetype.updating.message", displayName));
        }

        /**
         * Invoked when the 'cancel' button is pressed.
         */
        @Override
        protected void onCancel() {
            processor.processingCompleted();
            super.onCancel();
        }
    }

}
