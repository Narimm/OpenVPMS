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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.esci.adapter.dispatcher;

import java.util.List;


/**
 * Dispatches documents to registered {@link DocumentProcessor}s.
 *
 * @author Tim Anderson
 */
public interface ESCIDispatcher {

    /**
     * Registers the document processors.
     *
     * @param processors the processors
     */
    void setDocumentProcessors(List<DocumentProcessor> processors);

    /**
     * Dispatch documents.
     * <p/>
     * This will dispatch documents until there is either:
     * <ul>
     * <li>no more documents available</li>
     * <li>the {@link #stop} method is invoked, from another thread</li>
     * </ul>
     * If {@link #stop} is called, only the executing dispatch terminates.
     */
    void dispatch();

    /**
     * Dispatch documents.
     * <p/>
     * This will dispatch documents until there is either:
     * <ul>
     * <li>no more documents available</li>
     * <li>the {@link #stop} method is invoked, from another thread</li>
     * <li>an error occurs, and the supplied handler's {@link ErrorHandler#terminateOnError} method returns
     * {@code true}</li>
     * </ul>
     * If {@link #stop} is called, only the executing dispatch terminates.
     */
    void dispatch(ErrorHandler handler);

    /**
     * Flags the current dispatch to stop.
     * <p/>
     * This does not block waiting for the dispatch to complete.
     */
    void stop();

}