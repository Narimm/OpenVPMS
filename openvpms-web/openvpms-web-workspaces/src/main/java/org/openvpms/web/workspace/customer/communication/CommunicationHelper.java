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

import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.List;

/**
 * Communication helper.
 *
 * @author Tim Anderson
 */
public class CommunicationHelper {

    /**
     * Determines if communication logging is enabled.
     *
     * @param practiceService the practice service
     * @param service         the archetype service
     * @return {@code true} if communication logging is enabled
     */
    public static boolean isLoggingEnabled(PracticeService practiceService, IArchetypeService service) {
        return isLoggingEnabled(practiceService.getPractice(), service);
    }

    /**
     * Determines if communication logging is enabled.
     *
     * @param practice the practice. May be {@code null}
     * @param service  the archetype service
     * @return {@code true} if communication logging is enabled
     */
    public static boolean isLoggingEnabled(Party practice, IArchetypeService service) {
        boolean result = false;
        if (practice != null) {
            IMObjectBean bean = new IMObjectBean(practice, service);
            result = bean.getBoolean("logCommunication");
        }
        return result;
    }

    /**
     * Helper to return the attachment names as a new line separated list.
     *
     * @param documents the attachments
     * @return a formatted string
     */
    public static String getAttachments(List<Document> documents) {
        StringBuilder result = new StringBuilder();
        for (Document document : documents) {
            if (result.length() > 0) {
                result.append("\n");
            }
            result.append(document.getName());
        }
        return result.toString();
    }
}
