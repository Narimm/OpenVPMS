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

package org.openvpms.etl;


/**
 * A symbolic reference to an object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Reference {

    /**
     * The object id. May be <tt>null</tt>
     */
    private final String objectId;

    /**
     * The object archetype.
     */
    private final String archetype;

    private final String legacyId;

    private final String name;

    private final String value;

    public Reference(String objectId) {
        this.objectId = objectId;
        archetype = null;
        legacyId = null;
        name = null;
        value = null;
    }
    public Reference(String archetype, String legacyId) {
        this.objectId = null;
        this.archetype = archetype;
        this.legacyId = legacyId;
        this.name = null;
        this.value = null;
    }

    public Reference(String archetype, String name, String value) {
        this.objectId = null;
        this.archetype = archetype;
        this.legacyId = null;
        this.name = name;
        this.value = value;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getArchetype() {
        return archetype;
    }

    public String getLegacyId() {
        return legacyId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        StringBuffer result= new StringBuffer();
        result.append("<").append(archetype).append(">");
        if (legacyId != null) {
            result.append(legacyId);
        } else {
            result.append(name).append("=").append(value);
        }
        return result.toString();
    }
}
