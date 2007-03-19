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
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import org.apache.commons.lang.StringUtils;
import org.openvpms.etl.ETLCollectionNode;
import org.openvpms.etl.ETLObject;
import org.openvpms.etl.ETLValueNode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class RowMapper {

    private final Mappings mappings;
    private final Map<String, ETLCollectionNode> collections = new HashMap<String, ETLCollectionNode>();
    private Map<String, ETLObject> objects = new LinkedHashMap<String, ETLObject>();
    private final RowMapperListener listener;

    public RowMapper(Mappings mappings, RowMapperListener listener) {
        this.mappings = mappings;
        this.listener = listener;
    }

    public void map(Row row) throws KettleException {
        Value idValue = row.searchValue(mappings.getIdColumn());
        if (idValue == null) {
            throw new KettleException(
                    "Failed to find id column: " + mappings.getIdColumn());
        }
        String id = getId(idValue);
        if (StringUtils.isEmpty(id)) {
            throw new KettleException(
                    "id column null: " + mappings.getIdColumn());
        }

        objects.clear();
        collections.clear();
        for (Mapping mapping : mappings.getMapping()) {
            Value source = row.searchValue(mapping.getSource());
            if (source == null) {
                throw new KettleException(
                        "No column named " + mapping.getSource());
            }
            if (!source.isNull()
                    || (source.isNull() && !mapping.getExcludeNull())) {
                if (!StringUtils.isEmpty(mapping.getValue())) {
                    source = new Value(mapping.getValue(),
                                       Value.VALUE_TYPE_STRING);
                }
                mapValue(id, source, mapping.getTarget());

            }
        }
        listener.output(objects.values());
    }

    private String getId(Value id) {
        String value = id.getString();
        if (value != null && id.getType() == Value.VALUE_TYPE_NUMBER
                && id.getPrecision() == 0 && value.endsWith(".0")) {
            value = value.substring(0, value.length() - 2);
        }
        return value;
    }

    private void mapValue(String legacyId, Value source, String target)
            throws KettleException {
        Node node = NodeParser.parse(target);
        if (node == null) {
            throw new KettleException("Failed to parse " + target);
        }
        ETLCollectionNode collection = null;
        String name = null;
        ETLObject object = null;
        while (node != null) {
            name = node.getName();
            object = objects.get(node.getObjectPath());
            if (object == null) {
                object = new ETLObject(node.getArchetype());
                object.setLegacyId(legacyId);
                objects.put(node.getObjectPath(), object);
            }

            if (collection != null) {
                collection.addValue(object);
                collection = null;
            }
            if (node.getIndex() != -1) {
                collection = collections.get(node.getPath());
                if (collection == null) {
                    collection = new ETLCollectionNode(node.getName());
                    collections.put(node.getPath(), collection);
                    object.addNode(collection);
                }
            }
            node = node.getChild();
        }
        object.addNode(new ETLValueNode(name, source.getString()));
    }

}
