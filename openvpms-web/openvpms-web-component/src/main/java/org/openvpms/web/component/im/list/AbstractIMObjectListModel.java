/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.list;

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.ArrayList;
import java.util.List;


/**
 * * List model for {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectListModel<T extends IMObject> extends AllNoneListModel {

    /**
     * The objects.
     */
    private List<T> objects;


    /**
     * Constructs an empty {@code AbstractIMObjectListModel}.
     */
    public AbstractIMObjectListModel() {
        objects = new ArrayList<T>();
    }

    /**
     * Constructs an {@code AbstractIMObjectListModel}.
     *
     * @param objects the objects to populate the list with.
     * @param all     if {@code true}, add a localised "All"
     * @param none    if {@code true}, add a localised "None"
     */
    public AbstractIMObjectListModel(List<? extends T> objects, boolean all, boolean none) {
        setObjects(objects, all, none);
    }

    /**
     * Returns the object at the specified index.
     *
     * @param index the index
     * @return the object, or {@code null} if the index represents 'All' or
     *         'None'
     */
    public T getObject(int index) {
        return objects.get(index);
    }

    /**
     * Returns the index of the specified object.
     *
     * @param object the object
     * @return the index of {@code object}, or {@code -1} if it doesn't exist
     */
    public int indexOf(T object) {
        return objects.indexOf(object);
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    public int size() {
        return objects.size();
    }

    /**
     * Returns the objects in the list.
     * <p/>
     * Any index representing 'All' or 'None' will be {@code null}.
     *
     * @return the objects
     */
    public List<T> getObjects() {
        return objects;
    }

    /**
     * Sets the objects.
     * <p/>
     * This invokes {@link #initObjects(List, boolean, boolean)} before {@link #fireContentsChanged(int, int)}.
     *
     * @param objects the objects to populate the list with.
     * @param all     if {@code true}, add a localised "All"
     * @param none    if {@code true}, add a localised "None"
     */
    protected void setObjects(List<? extends T> objects, boolean all, boolean none) {
        initObjects(objects, all, none);
        fireContentsChanged(0, objects.size());
    }

    /**
     * Initialises the objects.
     *
     * @param objects the objects to populate the list with.
     * @param all     if {@code true}, add a localised "All"
     * @param none    if {@code true}, add a localised "None"
     */
    protected void initObjects(List<? extends T> objects, boolean all, boolean none) {
        int index = 0;
        this.objects = new ArrayList<T>();
        if (all) {
            this.objects.add(null);
            setAll(index++);
        }
        if (none) {
            this.objects.add(null);
            setNone(index++);
        }

        for (int i = 0; i < objects.size(); ++i, ++index) {
            this.objects.add(objects.get(i));
        }
    }

}
