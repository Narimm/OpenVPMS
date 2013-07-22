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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.admin.archetype;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.tools.archetype.loader.Change;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.MessageDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 * Updates objects associated with a changed
 * {@link org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor ArchetypeDescriptor},
 * providing a progress bar and cancel prompt.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ObjectUpdateProgressBarProcessor extends ProgressBarProcessor<IMObject> {

    /**
     * If <tt>true</tt> derive values.
     */
    private boolean derive = false;

    /**
     * The names of the nodes with new assertions.
     */
    private List<String> nodes;

    /**
     * Used to batch-save objects, for performance.
     */
    private List<IMObject> batch = new ArrayList<IMObject>();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Default constructor.
     */
    public ObjectUpdateProgressBarProcessor() {
        super(null);
        service = ServiceHelper.getArchetypeService();
    }

    /**
     * Updates nodes for all objects related to the archetype descriptor change.
     *
     * @param change the change
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    public void update(Change change) {
        derive = change.hasChangedDerivedNodes();
        nodes = change.getNodesWithAddedAssertions(BatchArchetypeUpdater.ASSERTIONS);

        String shortName = change.getNewVersion().getShortName();
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, false);
        query.add(new NodeSortConstraint("id"));

        query.setMaxResults(0);
        query.setCountResults(true);
        IPage<IMObject> page = service.get(query);
        int size = page.getTotalResults();
        query.setMaxResults(100);
        query.setCountResults(false);
        Iterable<IMObject> iter = new IterableIMObjectQuery<IMObject>(service, query);
        setItems(iter, size);
        process();
    }

    /**
     * Processes an object.
     *
     * @param object the object to process
     */
    protected void process(final IMObject object) {
        boolean changed = false;
        try {
            if (!nodes.isEmpty()) {
                changed = updateNodes(object);
            }

            if (changed || derive) {
                service.deriveValues(object);
                changed = true;
            }

            boolean flushed = false;
            if (changed) {
                batch.add(object);
                if (batch.size() > 100) {
                    flushBatch(new ProcessNextContinuation(object));
                    flushed = true;
                }
            }
            if (!flushed) {
                processCompleted(object);
            }
        } catch (Throwable exception) {
            prompt(object, exception, new ProcessNextContinuation(object));
        }
    }

    /**
     * Invoked when batch processing has completed.
     */
    @Override
    protected void processingCompleted() {
        Continuation continuation = new Continuation() {
            public void execute() {
                ObjectUpdateProgressBarProcessor.super.processingCompleted();
            }

            public void cancel() {
                ObjectUpdateProgressBarProcessor.super.processingCompleted();
            }
        };
        flushBatch(continuation);
    }

    /**
     * Updates nodes for an object, if required.
     *
     * @param object the object to update
     * @return <tt>true</tt> if the object was updated
     */
    private boolean updateNodes(IMObject object) {
        boolean changed = false;
        IMObjectBean bean = new IMObjectBean(object);
        for (String name : nodes) {
            String old = bean.getString(name);
            if (old != null) {
                bean.setValue(name, null); // clear out the old value so that propercase conversion runs
                bean.setValue(name, old);
                if (!old.equals(bean.getString(name))) {
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * Flushes the batch.
     *
     * @param continuation the continuation to execute/cancel if the batch needs to be processed asynchronously
     */
    private void flushBatch(Continuation continuation) {
        if (!batch.isEmpty()) {
            try {
                service.save(batch);
                batch.clear();
                continuation.execute();
            } catch (Throwable exception) {
                BatchFlusher flusher = new BatchFlusher(continuation);
                flusher.flush();
            }
        } else {
            continuation.execute();
        }
    }

    /**
     * Prompts to cancel or skip processing when an error occurs.
     *
     * @param object       the object that failed processing
     * @param exception    the error
     * @param continuation the continuation to execute if <em>Skip</em> is selected, or cancel if <em>Cancel</em> is
     *                     selected
     */
    private void prompt(IMObject object, Throwable exception, final Continuation continuation) {
        setSuspend(true);
        String displayName = DescriptorHelper.getDisplayName(object);
        String title = Messages.format("archetype.update.errortitle", displayName);
        String error = ErrorFormatter.format(exception, displayName);
        String message = Messages.format("archetype.update.errormessage", displayName, object.getId(), error);
        ErrorDialog dialog = new ErrorDialog(title, message, MessageDialog.SKIP_CANCEL);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onSkip() {
                continuation.execute();
            }

            @Override
            public void onCancel() {
                continuation.cancel();
            }
        });
        dialog.show();
    }

    /**
     * A <tt>Continuation</tt> is code that is executed after performing some asynchronous behaviour.
     */
    private interface Continuation {

        /**
         * Executes the continuation.
         */
        void execute();

        /**
         * Cancels the continuation.
         */
        void cancel();
    }

    /**
     * Flushes the batch, handling any errors.
     */
    private class BatchFlusher {

        /**
         * The continuation to execute on completion of flushing, or cancel on error.
         */
        private final Continuation continuation;

        /**
         * Iterator over the batch.
         */
        private final ListIterator<IMObject> iter;

        /**
         * Creates a new <tt>BatchFlusher</tt>.
         *
         * @param continuation the continuation to execute on completion of flushing, or cancel on error.
         */
        public BatchFlusher(Continuation continuation) {
            this.continuation = continuation;
            iter = batch.listIterator();
        }

        /**
         * Flushes the batch.
         */
        public void flush() {
            boolean error = false;
            while (iter.hasNext()) {
                IMObject object = iter.next();
                iter.remove();
                try {
                    service.save(object);
                } catch (Throwable exception) {
                    prompt(object, exception, new Continuation() {
                        public void execute() {
                            flush();
                        }

                        public void cancel() {
                            continuation.cancel();
                        }
                    });
                    error = true;
                    break;
                }
            }
            if (!error) {
                continuation.execute();
            }
        }

    }

    /**
     * Continuation that when executed, processes the next object,
     * or completes processing when cancelled.
     */
    private class ProcessNextContinuation implements Continuation {

        /**
         * The object being processed.
         */
        private final IMObject object;

        /**
         * Creates a new <tt>ProcessNextContinuation</tt>.
         *
         * @param object the object being processed.
         */
        public ProcessNextContinuation(IMObject object) {
            this.object = object;
        }

        /**
         * Invokes {@link ObjectUpdateProgressBarProcessor#processCompleted(Object)} and resumes processing.
         */
        public void execute() {
            processCompleted(object);
            if (isSuspended()) {
                process();
            }
        }

        /**
         * Clears the batch and invokes {@link ObjectUpdateProgressBarProcessor#processingCompleted()}.
         */
        public void cancel() {
            batch.clear();
            processingCompleted();
        }
    }

}
