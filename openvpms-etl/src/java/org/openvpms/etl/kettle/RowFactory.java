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

package org.openvpms.etl.kettle;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;


/**
 * Creates new rows.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class RowFactory {
    public static final String TYPE = "type";
    public static final String OBJECT_ID = "object_id";
    public static final String PARENT_ID = "parent_id";
    public static final String LEGACY_ID = "legacy_id";
    public static final String ARCHETYPE = "archetype";
    public static final String INDEX = "index";
    public static final String NAME = "name";
    public static final String VALUE = "value";

    public static Row create(String stepName) {
        Value type = new Value(TYPE, Value.VALUE_TYPE_STRING);
        Value objectId = createStringValue(OBJECT_ID);
        Value parentId = createStringValue(PARENT_ID);
        Value legacyId = createStringValue(LEGACY_ID);
        Value archetype = createStringValue(ARCHETYPE);
        Value nodeName = createStringValue(NAME);
        Value index = new Value(INDEX, Value.VALUE_TYPE_INTEGER);
        index.setLength(3);
        Value value = createStringValue(VALUE, 1024);

        Row row = new Row();
        row.addValue(type);
        row.addValue(objectId);
        row.addValue(parentId);
        row.addValue(legacyId);
        row.addValue(archetype);
        row.addValue(nodeName);
        index.setValue(-1);
        row.addValue(index);
        row.addValue(value);
        for (int i = 0; i < row.size(); ++i) {
            Value v = row.getValue(i);
            v.setOrigin(stepName);
        }
        return row;
    }

    private static Value createStringValue(String name) {
        return createStringValue(name, 256);
    }

    private static Value createStringValue(String name, int length) {
        Value value = new Value(name, Value.VALUE_TYPE_STRING);
        value.setLength(length);
        return value;
    }

    public static Row createObjectRow(String stepName, String objectId,
                                      String legacyId, String archetype) {
        Row row = create(stepName);
        row.searchValue(TYPE).setValue("object");
        row.searchValue(OBJECT_ID).setValue(objectId);
        row.searchValue(LEGACY_ID).setValue(legacyId);
        row.searchValue(ARCHETYPE).setValue(archetype);
        return row;
    }

    public static Row createNodeRow(String stepName, String objectId,
                                    String nodeName, String value) {
        Row row = create(stepName);
        row.searchValue(TYPE).setValue("node");
        row.searchValue(OBJECT_ID).setValue(objectId);
        row.searchValue(NAME).setValue(nodeName);
        row.searchValue(VALUE).setValue(value);
        return row;
    }

    public static Row createCollectionRow(String stepName, String objectId,
                                          String parentId, String name,
                                          int index) {
        Row row = create(stepName);
        row.searchValue(TYPE).setValue("collection");
        row.searchValue(OBJECT_ID).setValue(objectId);
        row.searchValue(PARENT_ID).setValue(parentId);
        row.searchValue(NAME).setValue(name);
        row.searchValue(INDEX).setValue(index);
        return row;
    }
}
