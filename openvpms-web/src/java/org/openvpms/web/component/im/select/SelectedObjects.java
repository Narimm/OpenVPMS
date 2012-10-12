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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.select;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Manages the selection of multiple objects, by name.
 * <p/>
 * Names are entered as a single string, delimited by ';'. These names must then be matched to queried objects.
 * The name string can be re-entered, with new names added, or names deleted; this class applies the additions and
 * deletions to the queried objects.
 *
 * @author Tim Anderson
 */
public class SelectedObjects<T extends IMObject> {

    /**
     * The object names.
     */
    private List<String> names = new ArrayList<String>();

    /**
     * The objects. This list will contain nulls where there is no object for a particular name in {@code names}.
     */
    private List<T> objects = new ArrayList<T>();

    /**
     * Default constructor.
     */
    public SelectedObjects() {

    }

    /**
     * Sets the objects.
     * <p/>
     * The names will be taken from the objects.
     *
     * @param objects the objects
     */
    public void setObjects(List<T> objects) {
        this.names.clear();
        this.objects.clear();
        for (T object : objects) {
            addObject(object);
        }
    }

    /**
     * Returns the object names.
     *
     * @return the object names
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * Returns the objects.
     * <p/>
     * This list will contain nulls where there is no object for a particular name.
     * Where an object is present, its name will correspond to that in {@link #getNames() names}, at the same index.
     *
     * @return the objects
     */
    public List<T> getObjects() {
        return objects;
    }

    /**
     * Adds an object.
     *
     * @param object the object to add
     */
    public void addObject(T object) {
        setObject(objects.size(), object);
    }

    /**
     * Adds objects.
     *
     * @param objects the objects to add
     */
    public void addObjects(T... objects) {
        for (T object : objects) {
            addObject(object);
        }
    }

    /**
     * Sets the object at the specified index.
     *
     * @param index  the index
     * @param object the object
     */
    public void setObject(int index, T object) {
        if (index < objects.size()) {
            objects.set(index, object);
            names.set(index, object.getName());
        } else {
            objects.add(index, object);
            names.add(index, object.getName());
        }
    }

    /**
     * Returns the number of elements.
     *
     * @return the number of elements
     */
    public int size() {
        return objects.size();
    }

    /**
     * Returns the names as text, each separated by '; '.
     *
     * @return the names as text
     */
    public String getText() {
        StringBuilder buffer = new StringBuilder();
        for (String name : names) {
            buffer.append(name);
            buffer.append("; ");
        }
        return buffer.toString();
    }

    /**
     * Determines if this is valid.
     *
     * @return {@code true} if there is an object for each name
     */
    public boolean isValid() {
        boolean valid = false;
        if (names.size() == objects.size()) {
            boolean found = true;
            for (int i = 0; i < names.size(); ++i) {
                String name = names.get(i);
                T object = objects.get(i);
                if (object == null || !StringUtils.equals(name, object.getName())) {
                    found = false;
                    break;
                }
            }
            valid = found;
        }
        return valid;
    }

    /**
     * Parses names from the supplied text, passing them to {@link #setNames(String...)}.
     * <p/>
     * The names must be in the same format returned by {@link #getText()}.
     *
     * @param text the text to parse
     */
    public void parseNames(String text) {
        setNames(parse(text));
    }

    /**
     * Sets the object names.
     * <p/>
     * This matches each name to an existing object, if possible. If an object doesn't have a match, it is removed.
     *
     * @param names the object names
     */
    public void setNames(String... names) {
        this.names.clear();
        this.names.addAll(Arrays.asList(names));

        List<T> newObjects = new ArrayList<T>();
        int last = 0;
        for (int i = 0; i < names.length; ++i) {
            String name = names[i];
            int index = indexOf(name, last);
            if (index != -1) {
                for (int j = newObjects.size(); j < i; ++j) {
                    newObjects.add(null);
                }
                newObjects.add(objects.get(index));
                last = index;
            } else {
                newObjects.add(null);
            }
        }
        objects = newObjects;
    }

    /**
     * Clears the state.
     */
    public void clear() {
        names.clear();
        objects.clear();
    }

    /**
     * Determines if the name and object at the specified index match.
     *
     * @param index the index
     * @return {@code true} if the name and object match, {@code false} if there is no object, or it has a different
     *         name
     */
    public boolean haveMatch(int index) {
        T object = objects.get(index);
        return object != null && StringUtils.equals(names.get(index), object.getName());
    }

    /**
     * Tries to locate the first object whose name matches that supplied, ignoring case.
     *
     * @param name  the name to search for
     * @param start the index to start from
     * @return the index of the matching object or {@code -1} if none is found
     */
    private int indexOf(String name, int start) {
        for (int i = start; i < objects.size(); ++i) {
            T object = objects.get(i);
            if (object != null && StringUtils.equalsIgnoreCase(name, object.getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Parses names from a string.
     *
     * @param names the name string
     * @return the names
     */
    private String[] parse(String names) {
        String[] result;
        if (names != null) {
            List<String> parsed = new ArrayList<String>();
            String[] split = names.split(";");
            for (String aSplit : split) {
                String name = aSplit.trim();
                if (!name.isEmpty()) {
                    parsed.add(name);
                }
            }
            result = parsed.toArray(new String[parsed.size()]);
        } else {
            result = new String[0];
        }
        return result;
    }

}
