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

package org.openvpms.web.component.service;

import org.openvpms.archetype.rules.practice.MailServer;

/**
 * Default implementation of the {@link MailService}.
 *
 * @author Tim Anderson
 */
public class DefaultMailService extends MailService {

    /**
     * The mail server settings.
     */
    private final MailServer settings;

    /**
     * Constructs a {@link DefaultMailService}.
     *
     * @param settings the mail server settings
     */
    public DefaultMailService(MailServer settings) {
        this.settings = settings;
    }

    /**
     * Returns the mail server settings.
     *
     * @return the settings
     */
    @Override
    protected MailServer getMailServer() {
        return settings;
    }
}
