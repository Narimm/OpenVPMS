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

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link Processor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractProcessor<Type, Event>
        implements NotifyingProcessor<Type, Event> {

    /**
     * The listeners. Listen to all events.
     */
    private final List<ProcessorListener<Event>> listeners
            = new ArrayList<ProcessorListener<Event>>();

    /**
     * Registers a listener for all events.
     *
     * @param listener the listener to add
     */
    public void addListener(ProcessorListener<Event> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(ProcessorListener<Event> listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies listeners of an event.
     *
     * @param event the event
     */
    protected void notifyListeners(Event event) {
        notifyListeners(listeners, event);
    }

    /**
     * Notifies listeners of an event.
     *
     * @param list  the listeners to notify
     * @param event the event
     */
    protected void notifyListeners(List<ProcessorListener<Event>> list,
                                   Event event) {
        for (ProcessorListener<Event> listener : list) {
            listener.process(event);
        }
    }
}
