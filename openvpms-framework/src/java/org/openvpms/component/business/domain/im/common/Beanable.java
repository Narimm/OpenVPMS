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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.domain.im.common;

import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Resolves an object suitable for use by {@link IMObjectBean}.
 *
 * @author Tim Anderson
 */
public interface Beanable {

    /**
     * Returns an object suitable for use by {@link IMObjectBean}.
     *
     * @return the object
     * @throws IllegalStateException if an object cannot be resolved
     */
    IMObject getObject();
}
