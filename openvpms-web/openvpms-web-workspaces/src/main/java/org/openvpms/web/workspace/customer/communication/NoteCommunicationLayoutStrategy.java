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

package org.openvpms.web.workspace.customer.communication;

import org.openvpms.web.component.property.Property;

/**
 * Layout strategy for <em>act.customerCommunicationNote</em> acts.
 *
 * @author Tim Anderson
 */
public class NoteCommunicationLayoutStrategy extends CommunicationLayoutStrategy {

    /**
     * Constructs an {@link NoteCommunicationLayoutStrategy}.
     */
    public NoteCommunicationLayoutStrategy() {
        this(null, true);
    }

    /**
     * Constructs an {@link CommunicationLayoutStrategy}.
     *
     * @param message     the message property. May be {@code null}
     * @param showPatient determines if the patient node should be displayed when editing
     */
    public NoteCommunicationLayoutStrategy(Property message, boolean showPatient) {
        super(message, null, showPatient);
    }
}
