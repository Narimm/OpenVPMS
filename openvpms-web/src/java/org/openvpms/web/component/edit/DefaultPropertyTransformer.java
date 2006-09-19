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

package org.openvpms.web.component.edit;

import org.openvpms.component.business.service.archetype.ValidationException;


/**
 * Default property handler.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultPropertyTransformer implements PropertyTransformer {

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <code>object</code> if no
     *         transformation is required
     * @throws ValidationException if the object is invalid
     */
    public Object apply(Object object) throws ValidationException {
        return object;
    }

}
