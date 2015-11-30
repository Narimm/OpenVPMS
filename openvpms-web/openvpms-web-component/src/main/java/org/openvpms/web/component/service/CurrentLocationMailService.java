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

package org.openvpms.web.component.service;

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.MailServer;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;

/**
 * A {@link MailService} that gets its mail settings from the current practice location.
 * <p/>
 * If the current practice location has no mail settings, it falls back to those of the practice.
 * <p/>
 * Note that instances of this must be per-session.
 *
 * @author Tim Anderson
 */
public class CurrentLocationMailService extends MailService {

    /**
     * The practice service.
     */
    private final PracticeService service;

    /**
     * The location rules.
     */
    private final LocationRules rules;

    /**
     * Constructs a {@link CurrentLocationMailService}.
     *
     * @param service the practice service
     * @param rules   the location rules
     */
    public CurrentLocationMailService(PracticeService service, LocationRules rules) {
        this.service = service;
        this.rules = rules;
    }

    /**
     * Returns the mail server settings.
     *
     * @return the settings, or {@code null} if none is configured
     */
    @Override
    protected MailServer getMailServer() {
        MailServer result = null;
        ContextApplicationInstance instance = ContextApplicationInstance.getInstance();
        if (instance != null) {
            Context context = instance.getContext();
            Party location = context.getLocation();
            if (location != null) {
                result = rules.getMailServer(location);
            }
        }
        if (result == null) {
            result = service.getMailServer();
        }
        return result;
    }
}
