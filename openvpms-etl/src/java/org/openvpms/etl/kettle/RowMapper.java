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
import org.openvpms.etl.ETLValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Maps input rows according to a {@link Mappings}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class RowMapper {

    /**
     * The mappings.
     */
    private final Mappings mappings;

    /**
     * The nodes to map, keyed on input field name.
     */
    private final Map<String, Node> nodes = new HashMap<String, Node>();

    /**
     * Object identifiers ({@link ETLValue#getObjectId()}, keyed on object
     * path {@link Node#getObjectPath()}.
     */
    private Map<String, String> objectIds = new HashMap<String, String>();

    /**
     * Mapped values, keyed on object id.
     */
    private Map<String, ETLValue> values = new HashMap<String, ETLValue>();

    /**
     * The mapped values.
     */
    private List<ETLValue> mapped = new ArrayList<ETLValue>();

    /**
     * Seed for generating object identifiers.
     */
    private int seed;


    /**
     * Constructs a new <tt>RowMapper</tt>.
     *
     * @param mappings the mappings
     * @throws KettleException
     */
    public RowMapper(Mappings mappings) throws KettleException {
        this.mappings = mappings;
        for (Mapping mapping : mappings.getMapping()) {
            String target = mapping.getTarget();
            Node node = NodeParser.parse(target);
            if (node == null) {
                throw new KettleException("Failed to parse " + target);
            }
            nodes.put(target, node);
        }
    }

    /**
     * Maps a row,
     *
     * @param row the row to map
     * @return the mapped values
     * @throws KettleException
     */
    public List<ETLValue> map(Row row) throws KettleException {
        Value idValue = row.searchValue(mappings.getIdColumn());
        if (idValue == null) {
            throw new KettleException(
                    "Failed to find id column: " + mappings.getIdColumn());
        }
        String id = getLegacyId(idValue);
        if (StringUtils.isEmpty(id)) {
            throw new KettleException(
                    "id column null: " + mappings.getIdColumn());
        }

        seed = 0;
        objectIds.clear();
        values.clear();
        mapped.clear();
        for (Mapping mapping : mappings.getMapping()) {
            Value source = row.searchValue(mapping.getSource());
            if (source == null) {
                throw new KettleException(
                        "No column named " + mapping.getSource());
            }
            if (!source.isNull()
                    || (source.isNull() && !mapping.getExcludeNull())) {
                if (!StringUtils.isEmpty(mapping.getValue())) {
                    source = new Value(source.getName(), mapping.getValue());
                }
                mapValue(id, source, mapping);
            }
        }
        return mapped;
    }

    /**
     * Maps a value.
     *
     * @param legacyId the legacy identifier
     * @param source   the value to map
     * @param mapping  the mapping
     */
    private void mapValue(String legacyId, Value source, Mapping mapping) {
        Node node = nodes.get(mapping.getTarget());
        String objectPath = node.getObjectPath();
        String objectId = objectIds.get(objectPath);
        ETLValue value;
        ETLValue parent = null;
        while (node.getChild() != null) {
            if (objectId == null) {
                objectId = newObjectId(legacyId, objectPath);
            }
            value = values.get(node.getNodePath());
            if (value == null) {
                value = new ETLValue(objectId, node.getArchetype(), legacyId,
                                     node.getName(), node.getIndex());
                mapped.add(value);
                values.put(node.getNodePath(), value);
            }
            if (parent != null && parent.getValue() == null) {
                parent.setReference(true);
                parent.setValue(objectId);
            }
            parent = value;
            node = node.getChild();
            objectPath = node.getObjectPath();
            objectId = objectIds.get(objectPath);
        }
        if (objectId == null) {
            objectId = newObjectId(legacyId, objectPath);
        }
        value = new ETLValue(objectId, node.getArchetype(), legacyId,
                             node.getName(), node.getIndex(), convert(source));
        if (mapping.getIsReference()) {
            value.setReference(true);
        }
        mapped.add(value);
        values.put(node.getNodePath(), value);
        if (parent != null && parent.getValue() == null) {
            parent.setReference(true);
            parent.setValue(objectId);
        }
    }

    /**
     * Allocates a new object id, based on a legacy identifier and seed.
     *
     * @param legacyId   the legacy identifier
     * @param objectPath the object path
     * @return a new object id
     */
    private String newObjectId(String legacyId, String objectPath) {
        ++seed;
        String objectId = legacyId + "." + seed;
        objectIds.put(objectPath, objectId);
        return objectId;
    }

    /**
     * Helper to get a stringified form of the legacy identifier.
     * In particular, this removes any trailing .0 decimal place for
     * numeric identifiers.
     *
     * @param id the legacy id
     * @return the stringified form of the legacy identifier
     */
    private String getLegacyId(Value id) {
        String value = convert(id);
        if (value != null && id.getType() == Value.VALUE_TYPE_NUMBER
                && id.getPrecision() == 0 && value.endsWith(".0")) {
            value = value.substring(0, value.length() - 2);
        }

        return value;
    }

    /**
     * Converts a {@link Value} to a string.
     * Dates are formatted using JDBC timestamp escape format.
     *
     * @param value the value to convert
     * @return the converted value
     */
    private String convert(Value value) {
        String result;
        if (value.getType() == Value.VALUE_TYPE_DATE && !value.isNull()) {
            Timestamp datetime = new Timestamp(value.getDate().getTime());
            result = datetime.toString();
        } else {
            result = value.toString();
        }
        return result;
    }

}
