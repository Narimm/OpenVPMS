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

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.StringUtilities;


/**
 * Archetype helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TypeHelper {

    /**
     * Determines if an object is an instance of a particular archetype.
     *
     * @param object    the object. May be <tt>null</tt>
     * @param shortName the archetype short name. May contain wildcards
     * @return <tt>true</tt> if object is an instance of <tt>shortName</tt>
     */
    public static boolean isA(IMObject object, String shortName) {
        if (object != null) {
            return matches(object.getArchetypeId(), shortName);
        }
        return false;
    }

    /**
     * Determines if an object is one of a set of archetypes.
     *
     * @param object     the object. May be <tt>null</tt>
     * @param shortNames the archetype short names. May contain wildcards
     * @return <tt>true</tt> if object is one of <tt>shortNames</tt>
     */
    public static boolean isA(IMObject object, String ... shortNames) {
        if (object != null) {
            for (String shortName : shortNames) {
                if (isA(object, shortName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if a reference is one of a set of archetypes.
     *
     * @param reference  the reference. May be <tt>null</tt>
     * @param shortNames the archetype short names. May contain wildcards
     * @return <tt>true</tt> if object is one of <tt>shortNames</tt>
     */
    public static boolean isA(IMObjectReference reference,
                              String ... shortNames) {
        if (reference != null) {
            for (String shortName : shortNames) {
                if (isA(reference, shortName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if an object reference refers to an instance of a particular
     * archetype.
     *
     * @param reference the object. May be <tt>null</tt>
     * @param shortName the archetype short name. May contain wildcards
     * @return <tt>true</tt> if the reference refers to an instance of
     *         <tt>shortName</tt>
     */
    public static boolean isA(IMObjectReference reference, String shortName) {
        if (reference != null) {
            return matches(reference.getArchetypeId(), shortName);
        }
        return false;
    }

    /**
     * Determines if a reference is one of a set of archetypes.
     *
     * @param id         the archetype identifier
     * @param shortNames the archetype short names. May contain wildcards
     * @return <tt>true</tt> if object is one of <tt>shortNames</tt>
     */
    public static boolean isA(ArchetypeId id, String ... shortNames) {
        if (id != null) {
            for (String shortName : shortNames) {
                if (matches(id, shortName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if an archetype descriptor matches one of a set of
     * archetype short names.
     *
     * @param descriptor the descriptor. May be <tt>null</tt>
     * @param shortNames the archetype short names. May contain wildcards
     * @return <tt>true</tt> if the descriptor short name matches one of
     *         <tt>shortNames</tt>
     */
    public static boolean isA(ArchetypeDescriptor descriptor,
                              String ... shortNames) {
        if (descriptor != null) {
            for (String shortName : shortNames) {
                if (matches(descriptor.getShortName(), shortName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if an archetype descriptor matches a short name.
     *
     * @param descriptor the descriptor. May be <tt>null</tt>
     * @param shortName  the archetype short name. May contain wildcards
     * @return <tt>true</tt> if the descriptor short name matches
     *         <tt>shortName</tt>
     */
    public static boolean isA(ArchetypeDescriptor descriptor,
                              String shortName) {
        if (descriptor != null) {
            if (matches(descriptor.getShortName(), shortName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if an archetype id matches a short name. The short name
     * may contain wildcards.
     *
     * @param id        the archetype identifier
     * @param shortName the short name
     */
    public static boolean matches(ArchetypeId id, String shortName) {
        return matches(id.getShortName(), shortName);
    }

    /**
     * Determines if a short name matches a wildcard.
     *
     * @param shortName the short name
     * @param wildcard  the wildcard
     */
    public static boolean matches(String shortName, String wildcard) {
        return StringUtilities.matches(shortName, wildcard);
    }

    /**
     * Determines if a set of short names match a wildcard.
     *
     * @param shortNames the short names
     * @param wildcard   the archetype wildcard
     */
    public static boolean matches(String[] shortNames, String wildcard) {
        for (String shortName : shortNames) {
            if (!matches(shortName, wildcard)) {
                return false;
            }
        }
        return true;
    }

}