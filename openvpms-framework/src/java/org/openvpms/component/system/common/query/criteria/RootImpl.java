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

package org.openvpms.component.system.common.query.criteria;

import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.query.criteria.Root;

/**
 * Default implementation of {@link Root}.
 *
 * @author Tim Anderson
 */
public class RootImpl<T extends IMObject> extends FromImpl<T, T> implements Root<T> {

    /**
     * Constructs a {@link RootImpl}.
     *
     * @param type    the type
     * @param context the context
     */
    public RootImpl(Type<T> type, Context context) {
        super(type, null, context);
    }

    /**
     * Sets the root alias.
     *
     * @param alias the alias
     * @return the root
     */
    @Override
    public Root<T> alias(String alias) {
        return (Root<T>) super.alias(alias);
    }

}
