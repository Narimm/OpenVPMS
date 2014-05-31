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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.party;

import org.openvpms.component.business.domain.im.party.Contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Operations for collections of {@link Contact} instances.
 *
 * @author Tim Anderson
 */
public class Contacts {

    /**
     * Sorts contacts on increasing identifier.
     * <p/>
     * Note that this operation modifies the supplied list.
     *
     * @param contacts the contacts to sort
     * @return the sorted contact
     */
    public static List<Contact> sort(List<Contact> contacts) {
        if (contacts.size() > 1) {
            Collections.sort(contacts, new Comparator<Contact>() {
                @Override
                public int compare(Contact o1, Contact o2) {
                    return o1.getId() < o2.getId() ? -1 : o1.getId() == o2.getId() ? 0 : 1;
                }
            });
        }
        return contacts;
    }

    /**
     * Sorts contacts on increasing identifier.
     *
     * @param contacts the contacts to sort
     * @return the sorted contacts
     */
    public static List<Contact> sort(Collection<Contact> contacts) {
        return sort(new ArrayList<Contact>(contacts));
    }

    /**
     * Looks for a contact that matches the criteria.
     * <p/>
     * For consistent results over multiple calls, sort the contacts first.
     *
     * @param contacts the contacts
     * @param matcher  the contact matcher
     * @return the matching contact or {@code null} if none is found
     */
    public static Contact find(Collection<Contact> contacts, ContactMatcher matcher) {
        for (Contact contact : contacts) {
            if (matcher.matches(contact)) {
                break;
            }
        }
        return matcher.getMatch();
    }

}
