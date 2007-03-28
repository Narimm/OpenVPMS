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
 * Represents a single object node for use when loading data into OpenVPMS from
 * 3rd party applications.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLValue {

    /**
     * The value identifier.
     */
    private long valueId;

    /**
     * The version.
     */
    private long version;

    /**
     * The archetype short name.
     */
    private String archetype;

    /**
     * The object identifier.
     */
    private String objectId;

    /**
     * The legacy identifier.
     */
    private String legacyId;

    /**
     * Determines if the value refers to another object.
     */
    private boolean isReference;

    /**
     * The node name.
     */
    private String name;

    /**
     * The collection index, or <tt>-1</tt> if this value is not part of
     * a collection.
     */
    private int index;

    /**
     * The value.
     */
    private String value;

    /**
     * Determines if all default collection objects in the target object
     * should be removed.
     */
    private boolean removeDefaultObjects;


    /**
     * Constructs a new <tt>ETLValue</tt>.
     */
    public ETLValue() {
        this(null, null, null);
    }

    /**
     * Constructs a new <tt>ETLValue</tt>.
     *
     * @param objectId  the object id
     * @param archetype the archetype short name
     * @param legacyId  the legacy id
     */
    public ETLValue(String objectId, String archetype, String legacyId) {
        this(objectId, archetype, legacyId, null, -1);
    }

    /**
     * Constructs a new <tt>ETLValue</tt>.
     *
     * @param objectId  the object id
     * @param archetype the archetype short name
     * @param legacyId  the legacy id
     * @param name      the node name
     * @param index     the collection index. May be <tt>-1</tt> to indicate
     *                  that the value is not part of a collection
     */
    public ETLValue(String objectId, String archetype, String legacyId,
                    String name, int index) {
        this(objectId, archetype, legacyId, name, index, null);
    }

    /**
     * Constructs a new <tt>ETLValue</tt>.
     *
     * @param objectId  the object id
     * @param archetype the archetype short name
     * @param legacyId  the legacy id
     * @param name      the node name
     * @param value     the node value
     */
    public ETLValue(String objectId, String archetype, String legacyId,
                    String name, String value) {
        this(objectId, archetype, legacyId, name, -1, value);
    }

    /**
     * Constructs a new <tt>ETLValue</tt>.
     *
     * @param objectId  the object id
     * @param archetype the archetype short name
     * @param legacyId  the legacy id
     * @param name      the node name
     * @param index     the collection index. May be <tt>-1</tt> to indicate
     *                  that the value is not part of a collection
     * @param value     the node value
     */
    public ETLValue(String objectId, String archetype, String legacyId,
                    String name, int index, String value) {
        this(objectId, archetype, legacyId, name, index, value, false);
    }

    /**
     * Constructs a new <tt>ETLValue</tt>.
     *
     * @param objectId  the object id
     * @param archetype the archetype short name
     * @param legacyId  the legacy id
     * @param name      the node name
     * @param index     the collection index. May be <tt>-1</tt> to indicate
     *                  that the value is not part of a collection
     * @param value     the node value
     * @param reference denotes if the value species a reference
     */
    public ETLValue(String objectId, String archetype, String legacyId,
                    String name, int index, String value, boolean reference) {
        this.objectId = objectId;
        this.archetype = archetype;
        this.legacyId = legacyId;
        this.name = name;
        this.index = index;
        this.value = value;
        this.isReference = reference;
    }

    /**
     * Returns the value identifier.
     * This is unique across all {@link ETLValue} instances.
     *
     * @return the value identifier
     */
    public long getValueId() {
        return valueId;
    }

    /**
     * Sets the value identifier.
     *
     * @param valueId
     */
    public void setValueId(long valueId) {
        this.valueId = valueId;
    }

    /**
     * Returns the version.
     *
     * @return the version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version the version
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Returns the object identifier.
     *
     * @return the object identifier
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Sets the object identifier.
     *
     * @param objectId the object identifier
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name
     */
    public String getArchetype() {
        return archetype;
    }

    /**
     * Sets the archetype short name.
     *
     * @param archetype the archetype short name
     */
    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    /**
     * Returns the node name.
     *
     * @return the node name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the node name.
     *
     * @param name the node name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value.
     *
     * @param value the value. May be <tt>null</tt>
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the value.
     *
     * @return the value. May be <tt>null</tt>
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the collection index.
     *
     * @return the collection index, or <tt>-1</tt> if this value is not part
     *         of a collection
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the collection index.
     *
     * @param index the collection index. May be <tt>-1</tt> to indicate that
     *              the value is not part of a collection
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the legacy identifier.
     *
     * @return the legacy identifier
     */
    public String getLegacyId() {
        return legacyId;
    }

    /**
     * Sets the legacy identifier.
     *
     * @param legacyId the legacy identifier
     */
    public void setLegacyId(String legacyId) {
        this.legacyId = legacyId;
    }

    /**
     * Determines if the value refers to another object.
     *
     * @return <tt>true</tt> if the value refers to another object
     */
    public boolean isReference() {
        return isReference;
    }

    /**
     * Determines if the value refers to another object.
     *
     * @param reference <tt>true</tt> if the value refers to another object
     */
    public void setReference(boolean reference) {
        isReference = reference;
    }

    /**
     * Determines if default objects added during the creation of the target
     * object should be removed.
     *
     * @return <tt>true</tt> if default objects should be removed
     */
    public boolean getRemoveDefaultObjects() {
        return removeDefaultObjects;
    }

    /**
     * Determines if default objects added during the creation of the target
     * object should be removed.
     *
     * @param removeDefaultObjects <tt>true</tt> if default objects should be
     *                             removed
     */
    public void setRemoveDefaultObjects(boolean removeDefaultObjects) {
        this.removeDefaultObjects = removeDefaultObjects;
    }
}
