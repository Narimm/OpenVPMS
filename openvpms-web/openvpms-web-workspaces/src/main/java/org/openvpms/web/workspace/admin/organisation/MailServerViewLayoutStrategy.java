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

package org.openvpms.web.workspace.admin.organisation;

import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;

/**
 * Layout strategy for <em>entity.mailServer<em> that masks the "password" node.
 *
 * @author Tim Anderson
 */
public class MailServerViewLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Exclude the password node from display.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude("password");

    /**
     * Constructs an {@link MailServerViewLayoutStrategy}.
     */
    public MailServerViewLayoutStrategy() {
        super(NODES);
    }
}
