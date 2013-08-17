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

package org.openvpms.web.component.im.view;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Represents a selected object or node.
 *
 * @author Tim Anderson
 * @see IMObjectComponent
 */
public class Selection {

    /**
     * The selected node. May be {@code null}
     */
    private final String node;

    /**
     * The selected object. May be {@code null}
     */
    private final IMObject object;

    /**
     * Constructs a {@link Selection}.
     *
     * @param node   the selected node. May be {@code null}
     * @param object the selected object. May be {@code null}
     */
    public Selection(String node, IMObject object) {
        this.node = node;
        this.object = object;
    }

    /**
     * Returns the selected node.
     *
     * @return the selected node. May be {@code null}
     */
    public String getNode() {
        return node;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be {@code null}
     */
    public IMObject getObject() {
        return object;
    }

}
