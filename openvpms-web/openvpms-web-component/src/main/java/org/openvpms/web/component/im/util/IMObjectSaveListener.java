/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.Collection;

/**
 * Listener for save events.
 *
 * @author Tim Anderson
 */
public interface IMObjectSaveListener {

    /**
     * Invoked when a collection of objects are saved.
     *
     * @param objects the saved objects
     */
    void saved(Collection<? extends IMObject> objects);

    /**
     * Invoked when a collection of objects fail to save.
     *
     * @param objects   the objects
     * @param exception the error
     */
    void error(Collection<? extends IMObject> objects, Throwable exception);

}
