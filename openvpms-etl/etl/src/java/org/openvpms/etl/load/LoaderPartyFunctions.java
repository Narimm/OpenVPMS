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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.apache.commons.jxpath.ExpressionContext;
import org.openvpms.archetype.function.party.PartyFunctions;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.HashSet;
import java.util.Set;


/**
 * Extension to {@link PartyFunctions} that excludes default contacts
 * whilst loading customers.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LoaderPartyFunctions extends PartyFunctions {

    /**
     * Returns a set of default Contacts for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return an empty set
     */
    public Set<Contact> getDefaultContacts(ExpressionContext context) {
        return new HashSet<Contact>();
    }

    /**
     * Returns a list of default contacts.
     *
     * @param party the party
     * @return <tt>null</tt>
     */
    public Set<Contact> getDefaultContacts(Party party) {
        return new HashSet<Contact>();
    }

}
