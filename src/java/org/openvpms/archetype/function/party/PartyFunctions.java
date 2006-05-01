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

import java.util.List;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * JXPath extension functions that operate on {@link Party} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PartyFunctions {

    /**
     * The archetype service.
     */
    private static IArchetypeService _service;


    /**
     * Construct a new <code>PartyFunctions</code>.
     *
     * @param service the archetype service
     */
    public PartyFunctions(IArchetypeService service) {
        _service = service;
    }

    /**
     * Returns a stringfield form of a party's contacts.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the party's contacts or
     *         <code>null</code>
     */
    public static String contacts(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }
        
        StringBuffer result = new StringBuffer();

        Party party = (Party) pointer.getValue();
        for (Contact contact : party.getContacts()) {
            String shortName = contact.getArchetypeId().getShortName();
            ArchetypeDescriptor archetype
                    = _service.getArchetypeDescriptor(shortName);
            Boolean preferred = new Boolean(getValue(contact,"preferred",archetype));
            if (preferred) {
                String description = getContactDescription(contact,archetype);
                if (description != null) {
                    if (result.length() != 0) {
                        result.append(", ");
                    }
                    result.append(description);
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns the description of a contact.
     *
     * @param contact the contact
     * @return the description of <code>object</code>. May be <code>null</code>
     */
    private static String getContactDescription(Contact contact, ArchetypeDescriptor archetype) {

        StringBuffer result = new StringBuffer();
        String description = getValue(contact, "description", archetype);
        if (description != null) {
            result.append(description);
        }

        NodeDescriptor purposes
                = archetype.getNodeDescriptor("purposes");

        if (purposes != null) {
            List<IMObject> list = purposes.getChildren(contact);
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
            ArchetypeDescriptor archetype = getArchetype(object);
            if (archetype != null) {
                String value = getValue(object, node, archetype);
                if (value != null) {
                    if (result.length() != 0) {
                        result.append(", ");
                    }
                    result.append(value);
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns the stringified value of a node.
     *
     * @param object    the object
     * @param node      the node name
     * @param archetype the archetype's descriptor
     * @return the stringified value of <code>node</code>. May be
     *         <code>null</code>
     */
    private static String getValue(IMObject object, String node,
                                   ArchetypeDescriptor archetype) {
        String result = null;
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        if (descriptor != null) {
            Object value = descriptor.getValue(object);
            if (value != null) {
                result = value.toString();
            }
        }
        return result;
    }

    /**
     * Helper to return an object's archetype descriptor.
     *
     * @param object the object
     * @return the object's archetype descriptor. May be <code>null</code>
     */
    private static ArchetypeDescriptor getArchetype(IMObject object) {
        String shortName = object.getArchetypeId().getShortName();
        return _service.getArchetypeDescriptor(shortName);
    }
}
