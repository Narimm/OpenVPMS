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
 * A processor that takes an object, performs operations on it, and
 * notifies any interested listeners.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see BatchProcessor
 */
public interface ActionProcessor<Action, Type, Event> extends Processor<Type> {

    /**
     * Registers a listener for all events.
     *
     * @param listener the listener to add
     */
    void addListener(ProcessorListener<Event> listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    void removeListener(ProcessorListener<Event> listener);


    /**
     * Registers a listener for a specific action.
     *
     * @param action   the action to register the listener for
     * @param listener the listener to add
     */
    void addListener(Action action, ProcessorListener<Event> listener);

    /**
     * Removes a listener for a specific action.
     *
     * @param action   the action to remove the listener for
     * @param listener the listener to remove
     */
    void removeListener(Action action, ProcessorListener<Event> listener);

}
