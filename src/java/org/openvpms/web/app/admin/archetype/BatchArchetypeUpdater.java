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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Updates objects associated with a batch of {@link ArchetypeDescriptor}s,
 * providing a progress bar and cancel prompt.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BatchArchetypeUpdater
        extends AbstractAsynchronousBatchProcessor<ArchetypeDescriptor> {

    /**
     * The update progress dialog.
     */
    private UpdateDialog dialog;

    /**
     * The progress bar.
     */
    private DerivedProgressBarProcessor processor;


    /**
     * Constructs a new <tt>BatchArchetypeUpdater</tt>.
     *
     * @param descriptors the archetype descriptors of the objects to update
     */
    public BatchArchetypeUpdater(List<ArchetypeDescriptor> descriptors) {
        super(descriptors.iterator());
    }

    /**
     * Processes an object.
     *
     * @param descriptor the object to process
     */
    protected void process(final ArchetypeDescriptor descriptor) {
        if (processor == null) {
            processor = new DerivedProgressBarProcessor();
            processor.setListener(new BatchProcessorListener() {
                public void completed() {
                    process(); // process the next archetype descriptor, if any
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
        dialog.setArchetype(descriptor.getDisplayName());
        processor.derive(descriptor.getShortName());
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
            dialog.close();
            dialog = null;
        }
    }

    private class DerivedProgressBarProcessor
            extends ProgressBarProcessor<IMObject> {

        /**
         * Used to batch-save objects, for performance.
         */
        private List<IMObject> batch = new ArrayList<IMObject>();

        /**
         * The archetype service.
         */
        private IArchetypeService service;


        /**
         * Default constructor.
         */
        public DerivedProgressBarProcessor() {
            super(null);
            // don't want to trigger rules when deriving values
            service = ServiceHelper.getArchetypeService(false);
        }

        /**
         * Updates derived nodes for all objects matching the specified short
         * name.
         *
         * @param shortName the archetype short name
         * @throws ArchetypeServiceException for any archetype service error
         */
        public void derive(String shortName) {
            ArchetypeQuery query = new ArchetypeQuery(shortName, false, false);

            query.setMaxResults(0);
            query.setCountResults(true);
            IPage<IMObject> page = service.get(query);
            int size = page.getTotalResults();
            query.setMaxResults(100);
            query.setCountResults(false);
            Iterable<IMObject> iter
                    = new IterableIMObjectQuery<IMObject>(service, query);
            setItems(iter, size);
            process();
        }

        /**
         * Processes an object.
         *
         * @param object the object to process
         */
        protected void process(IMObject object) {
            service.deriveValues(object);
            batch.add(object);
            if (batch.size() > 100) {
                flushBatch();
            }
            processCompleted(object);
        }

        /**
         * Invoked when batch processing has completed.
         */
        @Override
        protected void processingCompleted() {
            flushBatch();
            super.processingCompleted();
        }

        private void flushBatch() {
            service.save(batch);
            batch.clear();
        }
    }

    private class UpdateDialog extends PopupDialog {

        /**
         * The archetype name label.
         */
        private final Label label;

        /**
         * Creates a new <tt>UpdateDialog</tt>.
         */
        public UpdateDialog(DerivedProgressBarProcessor processor) {
            super(Messages.get("archetype.derived.updating.title"), CANCEL);
            setModal(true);
            label = LabelFactory.create();
            Row row = RowFactory.create("CellSpacing", label,
                                        processor.getComponent());
            Column column = ColumnFactory.create("Inset", row);
            getLayout().add(column);
        }

        /**
         * Sets the archetype being processed.
         *
         * @param displayName the archetype display name
         */
        public void setArchetype(String displayName) {
            label.setText(Messages.get("archetype.derived.updating.message",
                                       displayName));
        }

    }
}
