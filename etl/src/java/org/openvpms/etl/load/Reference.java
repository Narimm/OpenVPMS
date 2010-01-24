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


/**
 * A symbolic reference to an object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Reference {

    /**
     * The object archetype. May be <tt>null</tt>
     */
    private final String archetype;

    /**
     * The legacy row identifier. May be <tt>null</tt>.
     */
    private final String rowId;

    /**
     * The node name. May be <tt>null</tt>.
     */
    private final String name;

    /**
     * The value. May be <tt>null</tt>.
     */
    private final String value;


    /**
     * Constructs a new <tt>Reference</tt> containing an archetype and
     * legacy identifier.
     *
     * @param archetype the archetype short name
     * @param legacyId  the legacy identifier
     */
    public Reference(String archetype, String legacyId) {
        this.archetype = archetype;
        this.rowId = legacyId;
        this.name = null;
        this.value = null;
    }

    /**
     * Constructs a new <tt>Reference</tt> containing an archetype, node name
     * and value.
     *
     * @param archetype the archetype short name
     * @param name      the node name
     * @param value     the node value
     */
    public Reference(String archetype, String name, String value) {
        this.archetype = archetype;
        this.rowId = null;
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the archetype short name.
     *
     * @return the short name. May be <tt>null</tt>
     */
    public String getArchetype() {
        return archetype;
    }

    /**
     * Returns the legacy row identifier.
     *
     * @return the legacy row identifier. May be <tt>null</tt>
     */
    public String getRowId() {
        return rowId;
    }

    /**
     * Returns the node name.
     *
     * @return the node name. May be <tt>null</tt>
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the node value.
     *
     * @return the node value. May be <tt>null</tt>
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns a string representation of the reference.
     *
     * @return the string representation of this
     */
    public String toString() {
        String result;
        if (rowId != null) {
            result = create(archetype, rowId);
        } else {
            result = create(archetype, name, value);
        }
        return result;
    }

    /**
     * Creates a reference string from an archetype and row identifier.
     *
     * @param archetype the archetype
     * @param rowId  the legacy identifier
     * @return a new reference string
     */
    public static String create(String archetype, String rowId) {
        return "<" + archetype + ">" + rowId;
    }

    /**
     * Creates a reference string from an archetype, node name and value.
     *
     * @param archetype the archetype
     * @param name      the node name
     * @param value     the node value
     * @return a new reference string
     */
    public static String create(String archetype, String name, String value) {
        return "<" + archetype + ">" + name + "=" + value;
    }
}
