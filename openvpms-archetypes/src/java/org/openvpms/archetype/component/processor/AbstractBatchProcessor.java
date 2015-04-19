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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Basic abstract implementation of the {@link BatchProcessor} interface,
 * that provides event notification support.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractBatchProcessor implements BatchProcessor {

    /**
     * The listener for events.
     */
    private BatchProcessorListener listener;

    /**
     * The no. of objects processed.
     */
    private int processed;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(AbstractBatchProcessor.class);


    /**
     * Sets a listener to notify of batch processor events.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setListener(BatchProcessorListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the no. of objects processed.
     *
     * @return the no. of objects processed
     */
    public int getProcessed() {
        return processed;
    }

    /**
     * Sets the processed counter.
     *
     * @param processed the processed counter
     */
    protected void setProcessed(int processed) {
        this.processed = processed;
    }

    /**
     * Increments the processed counter by the specified amount.
     *
     * @param count the amount to increment by
     */
    protected void incProcessed(int count) {
        processed += count;
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
    protected void notifyError(Throwable exception) {
        if (listener != null) {
            listener.error(exception);
        } else {
            log.error(exception);
        }
    }
}
