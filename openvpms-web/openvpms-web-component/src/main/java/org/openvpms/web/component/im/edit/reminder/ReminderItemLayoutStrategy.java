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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.reminder;

import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;

/**
 * A layout strategy for <em>act.patientReminderItem*</em> that suppresses the error node if empty.
 *
 * @author Tim Anderson
 */
public class ReminderItemLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The nodes to display.
     */
    private static final ArchetypeNodes nodes = new ArchetypeNodes().excludeIfEmpty("error");

    /**
     * Default constructor.
     */
    public ReminderItemLayoutStrategy() {
        super(nodes);
    }
}
