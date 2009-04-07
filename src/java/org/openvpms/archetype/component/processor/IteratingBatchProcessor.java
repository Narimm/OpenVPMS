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
 * Abstract implementation of the {@link BatchProcessor} interface that
 * iterates over the batch, processing each item.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class IteratingBatchProcessor<Type>
        extends AbstractBatchProcessor {

    /**
     * The iterator.
     */
    private Iterator<Type> iterator;


    /**
     * Creates a new <tt>AbstractBatchProcessor</tt>.
     *
     * @param iterator the iterator
     */
    public IteratingBatchProcessor(Iterator<Type> iterator) {
        setIterator(iterator);
    }

    /**
     * Creates a new <tt>AbstractBatchProcessor</tt>.
     * The iterator must be set using {@link #setIterator}.
     */
    public IteratingBatchProcessor() {
    }

    /**
     * Processes the batch.
     */
    public void process() {
        try {
            while (iterator.hasNext()) {
                Type object = iterator.next();
                process(object);
                incProcessed(object);
            }
            // processing completed.
            processingCompleted();
        } catch (OpenVPMSException exception) {
            processingError(exception);
        }
    }

    /**
     * Processes an object.
     *
     * @param object the object to process
     * @throws OpenVPMSException if the object cannot be processed
     */
    protected abstract void process(Type object);

    /**
     * Invoked when batch processing has completed.
     * This implementation delegates to {@link #notifyCompleted()}.
     */
    protected void processingCompleted() {
        notifyCompleted();
    }

    /**
     * Invoked when batch processing has terminated due to error.
     * This implementation delegates to {@link #notifyError}.
     *
     * @param exception the error
     */
    protected void processingError(Throwable exception) {
        notifyError(exception);
    }

    /**
     * Sets the iterator.
     * This resets the processed count.
     *
     * @param iterator the iterator over the batch to process
     */
    protected void setIterator(Iterator<Type> iterator) {
        setProcessed(0);
        this.iterator = iterator;
    }

    /**
     * Returns the iterator.
     *
     * @return the iterator
     */
    protected Iterator<Type> getIterator() {
        return iterator;
    }

    /**
     * Increments the count of processed objects.
     * <p/>
     * This implementation increments it by <tt>1</tt>.
     *
     * @param object the processed object
     */
    @SuppressWarnings("UnusedDeclaration")
    protected void incProcessed(Type object) {
        incProcessed(1);
    }

}
