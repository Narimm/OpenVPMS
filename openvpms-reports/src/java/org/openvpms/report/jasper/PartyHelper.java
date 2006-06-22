/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.jasper;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;


/**
 * <code>Party</code> helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PartyHelper {

    /**
     * Returns a formatted billing address for a party.
     *
     * @param party the party
     * @return a formatted billing address
     */
    public static String getFormattedBillingAddress(Party party) {
        String result = "";
        if (party != null) {
            Contact mail = null;
            for (Contact contact : party.getContacts()) {
                if (mail == null) {
                    mail = contact;
                } else if (contact.getArchetypeId().getShortName().equals(
                        "contact.location")) {
                    mail = contact;
                }
            }
            if (mail != null) {
                result = mail.getDescription();
            }
        }
        return result;
    }
}
