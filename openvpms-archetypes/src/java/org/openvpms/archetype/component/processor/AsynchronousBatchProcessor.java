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


/**
 * A {@link BatchProcessor} that may be suspended and resumed.
 * This is useful for interactive batch processing.
 * Processing is suspended by invoking <tt>setSuspend(true)</tt>, and is
 * resumed by invoking {@link #process()}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface AsynchronousBatchProcessor extends BatchProcessor {

    /**
     * Processes the batch.
     * This sets the suspend state to <tt>false</tt> and processes the
     * next available item. This repeats until there are no items left to
     * process, or processing is suspended. If suspended, the method returns;
     * processing may be resumed by invoking {@link #process()} again.
     * <p/>
     * On completion of the last item in the batch, notifies the listener
     * (if any).
     */
    void process();

    /**
     * Sets the suspend state
     *
     * @param suspend if <tt>true</tt> suspend processing
     */
    void setSuspend(boolean suspend);

    /**
     * Determines if processing has been suspended.
     *
     * @return <tt>true</tt> if processing has been suspended
     */
    boolean isSuspended();
}
