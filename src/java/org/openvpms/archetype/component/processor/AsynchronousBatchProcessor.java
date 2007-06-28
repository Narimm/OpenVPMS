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

package org.openvpms.archetype.component.processor;

import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.util.Iterator;


/**
 * A {@link BatchProcessor} that may be suspended and resumed.
 * This is useful for interactive batch processing.
 * Processing is suspended by invoking <tt>setSuspend(true)</tt>, and is
 * resumed by invoking {@link #process()}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AsynchronousBatchProcessor<Action, Type, Event>
        implements BatchProcessor {

    /**
     * The iterator.
     */
    private Iterator<Type> iterator;

    /**
     * The processor.
     */
    private Processor<Action, Type, Event> processor;

    /**
     * The listener for events.
     */
    private BatchProcessorListener listener;


    /**
     * Indicates if processing should suspend, so the client can be updated.
     */
    private boolean suspend;


    /**
     * Creates a new <tt>AsynchronousBatchProcessor</tt>.
     *
     * @param processor the processor
     * @param iterator  an iterator over the batch to process
     */
    public AsynchronousBatchProcessor(Processor<Action, Type, Event> processor,
                                      Iterator<Type> iterator) {
        setProcessor(processor);
        setIterator(iterator);
    }

    /**
     * Creates a new <tt>AsynchronousBatchProcessor</tt>.
     * The processor and iterator must be set using {@link #setProcessor} and
     * {@link #setIterator} respectively.
     */
    protected AsynchronousBatchProcessor() {
    }

    /**
     * Sets a listener to notify of batch processor events.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setListener(BatchProcessorListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the processor.
     *
     * @param processor the processor
     */
    protected void setProcessor(Processor<Action, Type, Event> processor) {
        this.processor = processor;
    }

    /**
     * Sets the iterator.
     *
     * @param iterator the iterator over the batch to process
     */
    protected void setIterator(Iterator<Type> iterator) {
        this.iterator = iterator;
    }

    /**
     * Process the batch.
     */
    public void process() {
        try {
            setSuspend(false);
            while (!isSuspended() && iterator.hasNext()) {
                processor.process(iterator.next());
            }
            if (!isSuspended()) {
                // processing completed.
                processingCompleted();
            }
        } catch (OpenVPMSException exception) {
            processingError(exception);
        }
    }

    /**
     * Sets the suspend state
     *
     * @param suspend if <tt>true</tt> suspend processing
     */
    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    /**
     * Determines if processing has been suspended.
     *
     * @return <tt>true</tt> if processing has been suspended
     */
    public boolean isSuspended() {
        return suspend;
    }

    /**
     * Invoked when batch processing has completed.
     * This implementation delegates to {@link #notifyCompleted()}.
     */
    protected void processingCompleted() {
        notifyCompleted();
    }

    /**
     * Notifies the listener (if any) of processing completion.
     */
    protected void notifyCompleted() {
        if (listener != null) {
            listener.completed();
        }
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    protected void processingError(OpenVPMSException exception) {
        if (listener != null) {
            listener.error(exception);
        }
    }

}
