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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.subsystem;

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.EventListener;


/**
 * Event listener for {@link AbstractViewCRUDWindow} events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface CRUDWindowListener<T extends IMObject> extends EventListener {

    /**
     * Invoked when an object is saved.
     *
     * @param object the saved object
     * @param isNew  determines if the object is a new instance
     */
    void saved(T object, boolean isNew);

    /**
     * Invoked when an object is deleted.
     *
     * @param object the deleted object
     */
    void deleted(T object);

    /**
     * Invoked when the parent needs to refresh an object.
     *
     * @param object the object to refresh
     */
    void refresh(T object);
}
