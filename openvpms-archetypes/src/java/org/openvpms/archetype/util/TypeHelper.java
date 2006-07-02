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

package org.openvpms.archetype.util;

import java.util.Collection;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
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
     * @param object    the object. May be <code>null</code>
     * @param shortName the archetype short name. May contain wildcards
     * @return <code>true</code> if object is an instance of
     *         <code>shortName</code>
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
     * @param object     the object. May be <code>null</code>
     * @param shortNames the archetype short names. May contain wildcards
     * @return <code>true</code> if object is one of <code>shortNames</code>
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
     * Determines if an object reference refers to an instance of a particular
     * archetype.
     *
     * @param reference the object. May be <code>null</code>
     * @param shortName the archetype short name. May contain wildcards
     * @return <code>true</code> if the reference refers to an instance of
     *         <code>shortName</code>
     */
    public static boolean isA(IMObjectReference reference, String shortName) {
        if (reference != null) {
            return matches(reference.getArchetypeId(), shortName);
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
        String regexp = StringUtilities.toRegEx(wildcard);
        return shortName.matches(regexp);
    }

    /**
     * Returns the first object instance from a collection with matching short
     * name.
     *
     * @param shortName the short name
     * @param objects   the objects to search
     * @return the first object from the collection with matching short name, or
     *         <code>null</code> if none exists.
     */
    public static <T extends IMObject> T
            getObject(String shortName, Collection<T> objects) {
        T result = null;
        for (T object : objects) {
            if (TypeHelper.isA(object, shortName)) {
                result = object;
                break;
            }
        }
        return result;
    }
}
