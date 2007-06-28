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


/**
 * Processor that may be used to asynchronously process an object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface AsynchronousProcessor<Action, Type, Event>
        extends Processor<Action, Type, Event> {

    /**
     * Start processing an object.
     *
     * @param object the object to process
     * @throws OpenVPMSException for any error
     */
    public void start(Type object);

    /**
     * Complete processing an object.
     *
     * @param object the object to process
     * @throws OpenVPMSException for any error
     */
    public void end(Type object);
}
