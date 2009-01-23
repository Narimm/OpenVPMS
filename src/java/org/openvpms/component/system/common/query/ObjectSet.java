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

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.util.AbstractPropertySet;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.component.system.common.util.PropertySetException;
import static org.openvpms.component.system.common.util.PropertySetException.ErrorCode.PropertyNotFound;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Set of objects returned by an {@link ArchetypeQuery}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ObjectSet extends AbstractPropertySet implements Serializable {

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The objects.
     */
    private Map<String, Object> objects;

    /**
     * The type short names, keyed on type alias.
     */
    private Map<String, Set<String>> types;


    /**
     * Creates an empty <tt>ObjectSet</tt>.
     */
    public ObjectSet() {
        objects = new LinkedHashMap<String, Object>();
        types = new HashMap<String, Set<String>>();
    }

    /**
     * Creates a new <tt>ObjectSet</tt> from an existing set, by performing
     * a shallow copy.
     *
     * @param set the set to copy
     */
    public ObjectSet(PropertySet set) {
        this();
        for (String name : set.getNames()) {
            set(name, set.get(name));
        }
    }

    /**
     * Creates a new <tt>ObjectSet</tt> from an existing set, by performing
     * a shallow copy.
     *
     * @param set the set to copy
     */
    public ObjectSet(ObjectSet set) {
        objects = new LinkedHashMap<String, Object>(set.objects);
        types = new HashMap<String, Set<String>>(set.types);
    }

    /**
     * Returns the object names, in the order they were queried.
     *
     * @return the object names
     */
    public Set<String> getNames() {
        return objects.keySet();
    }

    /**
     * Adds a type.
     *
     * @param alias      the type alias
     * @param shortNames the type short names
     */
    public void addType(String alias, Set<String> shortNames) {
        types.put(alias, shortNames);
    }

    /**
     * Sets the value of a property.
     *
     * @param name  the propery name
     * @param value the property value
     */
    public void set(String name, Object value) {
        objects.put(name, value);
    }

    /**
     * Adds an object.
     *
     * @param name  the object name
     * @param value the object value
     * @deprecated use {@link #set}
     */
    @Deprecated
    public void add(String name, Object value) {
        objects.put(name, value);
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws PropertySetException if the property doesn't exist
     */
    public Object get(String name) {
        if (objects.containsKey(name)) {
            return objects.get(name);
        }
        throw new PropertySetException(PropertyNotFound, name);
    }

    /**
     * Returns the type alias for a name.
     *
     * @return the type alias for a name
     */
    public String getAlias(String name) {
        int index = name.indexOf(".");
        return (index == -1) ? name : name.substring(0, index);
    }

    /**
     * Returns the matching short names for a name.
     *
     * @return the matching short names, or <cpde>null</tt> if there
     *         is no type information available.
     */
    public Collection<String> getShortNames(String name) {
        return types.get(getAlias(name));
    }

    /**
     * Returns the node descriptor for a name.
     *
     * @return the node descriptor for the name, or <tt>null</tt> if none
     *         is found
     */
    public NodeDescriptor getDescriptor(String name) {
        NodeDescriptor result = null;
        int index = name.indexOf(".");
        if (index != -1) {
            String alias = name.substring(0, index);
            String nodeName = name.substring(index + 1);
            String shortName;
            Set<String> shortNames = types.get(alias);
            if (shortNames != null && !shortNames.isEmpty()) {
                shortName = shortNames.toArray(new String[0])[0];
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                ArchetypeDescriptor archetype = service.getArchetypeDescriptor(
                        shortName);
                if (archetype != null) {
                    result = archetype.getNodeDescriptor(nodeName);
                }
            }
        }
        return result;
    }


}
