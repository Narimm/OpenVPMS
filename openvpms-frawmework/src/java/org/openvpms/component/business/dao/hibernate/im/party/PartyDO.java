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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.party;

import org.openvpms.component.business.dao.hibernate.im.entity.EntityDO;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.Set;


/**
 * Data object interface corresponding to the {@link Party} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface PartyDO extends EntityDO {

    /**
     * Returns the contacts.
     *
     * @return the contacts
     */
    Set<ContactDO> getContacts();

    /**
     * Adds a contact.
     *
     * @param contact contact to add
     */
    void addContact(ContactDO contact);

    /**
     * Removes a contact.
     *
     * @param contact the contact to remove
     */
    void removeContact(ContactDO contact);
}
