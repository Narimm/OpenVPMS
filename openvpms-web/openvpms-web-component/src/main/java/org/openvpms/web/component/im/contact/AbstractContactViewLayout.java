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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.contact;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;

/**
 * A view layout strategy for {@link Contact} archetypes that excludes the "purposes" node if empty.
 *
 * @author Tim Anderson
 */
public abstract class AbstractContactViewLayout extends AbstractLayoutStrategy{

    /**
     * The nodes to display.
     */
    private final static ArchetypeNodes nodes = new ArchetypeNodes().excludeIfEmpty("purposes");

    /**
     * Constructs an {@link AbstractContactViewLayout}.
     */
    public AbstractContactViewLayout() {
        super(nodes);
    }
}
