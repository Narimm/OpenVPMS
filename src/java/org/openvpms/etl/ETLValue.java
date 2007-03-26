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
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLValue {
    private long valueId;
    private long version;
    private String archetype;
    private String objectId;
    private String legacyId;
    private boolean isReference;
    private String name;
    private int index;
    private String value;


    public ETLValue() {
        this(null, null, null);
    }

    public ETLValue(String objectId, String archetype, String legacyId) {
        this(objectId, archetype, legacyId, null, -1);
    }

    public ETLValue(String objectId, String archetype, String legacyId,
                    String name, int index) {
        this(objectId, archetype, legacyId, name, index, null);
    }

    public ETLValue(String objectId, String archetype, String legacyId,
                    String name, String value) {
        this(objectId, archetype, legacyId, name, -1, value);
    }

    public ETLValue(String objectId, String archetype, String legacyId,
                    String name, int index, String value) {
        this.objectId = objectId;
        this.archetype = archetype;
        this.legacyId = legacyId;
        this.name = name;
        this.index = index;
        this.value = value;
    }

    public long getValueId() {
        return valueId;
    }

    public void setValueId(long valueId) {
        this.valueId = valueId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getArchetype() {
        return archetype;
    }

    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(String legacyId) {
        this.legacyId = legacyId;
    }

    public boolean isReference() {
        return isReference;
    }

    public void setReference(boolean reference) {
        isReference = reference;
    }
}
