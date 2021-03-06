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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

/**
 * Default implementation of the {@link IMObjectLayoutStrategy} interface.
 *
 * @author Tim Anderson
 */
public class DefaultLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Constructs a {@link DefaultLayoutStrategy}.
     */
    public DefaultLayoutStrategy() {
        this(DEFAULT_NODES);
    }

    /**
     * Constructs a {@link DefaultLayoutStrategy}.
     *
     * @param nodes the nodes to render
     */
    public DefaultLayoutStrategy(ArchetypeNodes nodes) {
        super(nodes);
    }

}
