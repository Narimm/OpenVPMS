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

package org.openvpms.component.query.criteria;

import org.openvpms.component.model.object.IMObject;

/**
 * A root type in a from clause. These correspond to an archetype.
 *
 * @author Tim Anderson
 */
public interface Root<T extends IMObject> extends From<T, T> {

    /**
     * Sets the root alias.
     *
     * @param alias the alias
     * @return the root
     */
    @Override
    Root<T> alias(String alias);
}
