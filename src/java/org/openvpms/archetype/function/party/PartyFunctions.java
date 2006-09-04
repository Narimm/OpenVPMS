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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.function.party;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.List;


/**
 * JXPath extension functions that operate on {@link Party} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PartyFunctions {

    /**
     * Returns a formatted list of contacts for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return a formatted list of contacts. May be <code>null</code>
     */
    public static String contacts(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }

        return contacts((Party) pointer.getValue());
    }

    /**
     * Returns a formatted list of contacts for a party.
     *
     * @param party the party
     * @return a formatted list of contacts
     */
    public static String contacts(Party party) {
        StringBuffer result = new StringBuffer();
        if (party != null) {
            for (Contact contact : party.getContacts()) {
                IMObjectBean bean = new IMObjectBean(contact);
                if (bean.hasNode("preferred")) {
                    boolean preferred = bean.getBoolean("preferred");
                    if (preferred) {
                        String description = getContactDescription(bean);
                        if (description != null) {
                            if (result.length() != 0) {
                                result.append(", ");
                            }
                            result.append(description);
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return a formatted billing address, or <code>null</code>
     */

    public static String billingAddress(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }
        return billingAddress((Party) pointer.getValue());
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param party the party
     * @return a formatted billing address
     */
    public static String billingAddress(Party party) {
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

    /**
     * Returns the description of a contact.
     *
     * @param contact the contact
     * @return the description of <code>object</code>. May be <code>null</code>
     */
    private static String getContactDescription(IMObjectBean contact) {
        StringBuffer result = new StringBuffer();
        if (contact.hasNode("description")) {
            result.append(contact.getString("description"));
        }

        if (contact.hasNode("purposes")) {
            List<IMObject> list = contact.getValues("purposes");
            if (!list.isEmpty()) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append("(");
                result.append(getValues(list, "name"));
                result.append(")");
            }
        }
        return result.toString();
    }

    /**
     * Returns a concatenated list of values for a set of objects.
     *
     * @param objects the objects
     * @param node    the node name
     * @return the stringified value of <code>node</code> for each object,
     *         separated by ", "
     */
    private static String getValues(List<IMObject> objects, String node) {
        StringBuffer result = new StringBuffer();

        for (IMObject object : objects) {
            IMObjectBean bean = new IMObjectBean(object);
            if (bean.hasNode(node)) {
                if (result.length() != 0) {
                    result.append(", ");
                }
                result.append(bean.getString(node));
            }
        }
        return result.toString();
    }

    /**
     * Returns a stringfield form of a party's identities.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the party's identities or
     *         <code>null</code>
     */
    public static String identities(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }

        StringBuffer result = new StringBuffer();

        Party party = (Party) pointer.getValue();
        for (EntityIdentity identity : party.getIdentities()) {
            IMObjectBean bean = new IMObjectBean(identity);
            if (bean.hasNode("name")) {
                String name = bean.getString("name");
                String displayName = bean.getDisplayName();
                if (name != null) {
                    if (result.length() != 0) {
                        result.append(", ");
                    }
                    result.append(displayName);
                    result.append(": ");
                    result.append(name);
                }
            }
        }
        return result.toString();
    }

}
