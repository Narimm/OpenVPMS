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
        extends AbstractAsynchronousBatchProcessor<Type> {

    /**
     * The processor.
     */
    private Processor<Action, Type, Event> processor;


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
     * Processes an object.
     *
     * @param object the object to process
     */
    protected void process(Type object) {
        processor.process(object);
        incProcessed();
    }

    /**
     * Sets the processor.
     *
     * @param processor the processor
     */
    protected void setProcessor(Processor<Action, Type, Event> processor) {
        this.processor = processor;
    }

}
