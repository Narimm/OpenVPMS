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
import org.openvpms.etl.ETLNode;
import org.openvpms.etl.ETLObject;
import org.openvpms.etl.ETLReference;
import org.openvpms.etl.ETLText;
import org.openvpms.etl.Reference;
import org.openvpms.etl.ReferenceParser;

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
    private final Map<String, Node> nodes = new HashMap<String, Node>();
    private final Map<String, ETLNode> collections = new HashMap<String, ETLNode>();
    private Map<String, ETLObject> objects = new LinkedHashMap<String, ETLObject>();
    private final RowMapperListener listener;

    public RowMapper(Mappings mappings, RowMapperListener listener)
            throws KettleException {
        this.mappings = mappings;
        this.listener = listener;
        for (Mapping mapping : mappings.getMapping()) {
            String target = mapping.getTarget();
            Node node = NodeParser.parse(target);
            if (node == null) {
                throw new KettleException("Failed to parse " + target);
            }
            nodes.put(target, node);
        }
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
                    source = new Value(source.getName(), mapping.getValue());
                }
                mapValue(id, source, mapping);
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

    private void mapValue(String legacyId, Value source, Mapping mapping)
            throws KettleException {
        Node node = nodes.get(mapping.getTarget());
        ETLNode collection = null;
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
                collection.addValue(new ETLReference(object));
                collection = null;
            }
            if (node.getIndex() != -1) {
                collection = collections.get(node.getPath());
                if (collection == null) {
                    collection = new ETLNode(name);
                    collections.put(node.getPath(), collection);
                    object.addNode(collection);
                }
            }
            node = node.getChild();
        }
        if (collection != null) {
            if (!mapping.getIsReference()) {
                throw new KettleException(
                        "Last node is a collection with no child and mapping doesn't specify a reference");
            }
            if (source.isNull()) {
                throw new KettleException("Reference is null");
            }
            Reference ref = ReferenceParser.parse(source.getString());
            if (ref == null) {
                throw new KettleException(
                        "Failed to parse reference: " + source.getString());
            }
            collection.addValue(new ETLReference(ref.toString()));
        } else {
            ETLNode etl = new ETLNode(name);
            etl.addValue(new ETLText(source.getString()));
            object.addNode(etl);
        }
    }

}
