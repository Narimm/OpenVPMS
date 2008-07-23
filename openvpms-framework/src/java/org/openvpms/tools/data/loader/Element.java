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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.tools.data.loader;

import org.apache.commons.lang.StringUtils;
import static org.openvpms.tools.data.loader.ArchetypeDataLoaderException.ErrorCode.InvalidArchetype;
import static org.openvpms.tools.data.loader.ArchetypeDataLoaderException.ErrorCode.UnexpectedElement;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class Element {

    private final String shortName;
    private Map<String, String> attributes = new LinkedHashMap<String, String>();

    private final String collection;
    private final String id;

    private final Location location;

    public Element(XMLStreamReader reader) {
        shortName = reader.getAttributeValue(null, "archetype");
        location = reader.getLocation();
        if (StringUtils.isEmpty(shortName)) {
            throw new ArchetypeDataLoaderException(InvalidArchetype,
                                                   location.getLineNumber(),
                                                   location.getColumnNumber());
        }

        if (!"data".equals(reader.getLocalName())) {
            throw new ArchetypeDataLoaderException(UnexpectedElement,
                                                   reader.getLocalName(),
                                                   location.getLineNumber(),
                                                   location.getColumnNumber());
        }
        id = reader.getAttributeValue(null, "id");
        collection = reader.getAttributeValue(null, "collection");

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            if (!"archetype".equals(name) && !"id".equals(name)
                    && !"collection".equals(name)
                    && !StringUtils.isEmpty(value)) {
                attributes.put(name, value);
            }
        }
    }

    /**
     * Returns the identifier.
     *
     * @return the identifier. May be <tt>null</tt>
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the child identifier.
     *
     * @return the child identifier. May be <tt>null</tt>
     */
    public String getChildId() {
        return attributes.get("childId");
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name.
     */
    public String getShortName() {
        return shortName;
    }

    public String getCollection() {
        return collection;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Location getLocation() {
        return location;
    }

    public String toString() {
        StringBuffer result = new StringBuffer("<data ");
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            result.append(attribute.getKey());
            result.append("=\"");
            result.append(attribute.getValue());
            result.append("\" ");
        }
        result.append("/>");
        return result.toString();
    }

    public boolean isComplete(IdRefCache cache) {
        for (String value : attributes.values()) {
            if (value.startsWith(IMObjectState.ID_PREFIX)) {
                if (cache.getReference(value) == null) {
                    return false;
                }
            }
        }
        return true;
    }
}
