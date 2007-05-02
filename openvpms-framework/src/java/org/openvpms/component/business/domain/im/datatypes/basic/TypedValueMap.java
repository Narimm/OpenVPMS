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

package org.openvpms.component.business.domain.im.datatypes.basic;

import org.apache.commons.collections.keyvalue.DefaultMapEntry;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * A map that adapts an underlying map of name->TypedValue pairs to
 * a map of name->Object pairs.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TypedValueMap implements Map<String, Object> {

    /**
     * The underlying map.
     */
    private final Map<String, TypedValue> map;


    /**
     * Constructs a new <tt>TypedValueMap</tt>, backed by a <tt>HashMap</tt>.
     */
    public TypedValueMap() {
        this(new HashMap<String, TypedValue>());
    }

    /**
     * Constructs a new <tt>TypedValueMap</tt>.
     *
     * @param map the underlying map
     */
    public TypedValueMap(Map<String, TypedValue> map) {
        this.map = map;
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *         key.
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map (optional).
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *                              does not permit <tt>null</tt> keys
     *                              (optional).
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     * @throws ClassCastException   if the value is of an inappropriate type for
     *                              this map (optional).
     * @throws NullPointerException if the value is <tt>null</tt> and this map
     *                              does not permit <tt>null</tt> values
     *                              (optional).
     */
    public boolean containsValue(Object value) {
        return map.containsValue(new TypedValue(value));
    }

    /**
     * Returns the value to which this map maps the specified key.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <tt>null</tt> if the map contains no mapping for this key.
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map (optional).
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *                              does not permit <tt>null</tt> keys (optional).
     * @see #containsKey(Object)
     */
    public Object get(Object key) {
        TypedValue value = map.get(key);
        return (value != null) ? value.getObject() : null;
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).
     *
     * @param key   key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key, if the implementation supports
     *         <tt>null</tt> values.
     * @throws UnsupportedOperationException if the <tt>put</tt> operation is
     *                                       not supported by this map.
     * @throws ClassCastException            if the class of the specified key or value
     *                                       prevents it from being stored in this map.
     * @throws IllegalArgumentException      if some aspect of this key or value
     *                                       prevents it from being stored in this map.
     * @throws NullPointerException          if this map does not permit <tt>null</tt>
     *                                       keys or values, and the specified key or value is
     *                                       <tt>null</tt>.
     */
    public Object put(String key, Object value) {
        Object old = get(key);
        map.put(key, new TypedValue(value));
        return old;
    }

    /**
     * Removes the mapping for this key from this map if it is present
     * (optional operation).
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.
     * @throws ClassCastException            if the key is of an inappropriate type for
     *                                       this map (optional).
     * @throws NullPointerException          if the key is <tt>null</tt> and this map
     *                                       does not permit <tt>null</tt> keys (optional).
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *                                       not supported by this map.
     */
    public Object remove(Object key) {
        Object old = get(key);
        map.remove(key);
        return old;
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation).
     *
     * @param t Mappings to be stored in this map.
     * @throws UnsupportedOperationException if the <tt>putAll</tt> method is
     *                                       not supported by this map.
     * @throws ClassCastException            if the class of a key or value in the
     *                                       specified map prevents it from being stored in this map.
     * @throws IllegalArgumentException      some aspect of a key or value in the
     *                                       specified map prevents it from being stored in this map.
     * @throws NullPointerException          if the specified map is <tt>null</tt>, or if
     *                                       this map does not permit <tt>null</tt> keys or values, and the
     *                                       specified map contains <tt>null</tt> keys or values.
     */
    public void putAll(Map<? extends String, ? extends Object> t) {
        for (Map.Entry<? extends String, ? extends Object> entry :
                t.entrySet()) {
            map.put(entry.getKey(), new TypedValue(entry.getValue()));
        }
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Returns a set view of the keys contained in this map.  The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection<Object> values() {
        return new ValueCollection();
    }

    /**
     * Returns a set view of the mappings contained in this map.  Each element
     * in the returned set is a {@link Entry}.  The set is backed by the
     * map, so changes to the map are reflected in the set, and vice-versa.
     *
     * @return a set view of the mappings contained in this map.
     */
    public Set<Entry<String, Object>> entrySet() {
        return new EntrySet();
    }

    /**
     * Helper to create a new map of names to {@link TypedValue} instances
     * from a name->object map.
     *
     * @param map the map to convert. May be <tt>null</tt>
     * @return a new map, or <tt>null</tt>
     */
    public static Map<String, TypedValue> create(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Map<String, TypedValue> result
                = new HashMap<String, TypedValue>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), new TypedValue(entry.getValue()));
        }
        return result;
    }

    /**
     * Adapts the underlying map's values collection.
     */
    private class ValueCollection extends AbstractCollection<Object> {

        /**
         * Returns an iterator over the elements contained in this collection.
         *
         * @return an iterator over the elements contained in this collection.
         */
        public Iterator<Object> iterator() {
            return new ValueIterator(map.values().iterator());
        }

        /**
         * Returns the number of elements in this collection.  If the collection
         * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
         * <tt>Integer.MAX_VALUE</tt>.
         *
         * @return the number of elements in this collection.
         */
        public int size() {
            return map.size();
        }

    }

    /**
     * Adapts the underlying map's values iterator.
     */
    private class ValueIterator implements Iterator<Object> {

        private final Iterator<TypedValue> iterator;

        public ValueIterator(Iterator<TypedValue> iterator) {
            this.iterator = iterator;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next element in the iteration.  Calling this method
         * repeatedly until the {@link #hasNext()} method returns false will
         * return each element in the underlying collection exactly once.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public Object next() {
            return iterator.next().getObject();
        }

        /**
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation).  This method can be called only once per
         * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
         * the underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         *
         * @throws UnsupportedOperationException if the <tt>remove</tt>
         *                                       operation is not supported by this Iterator.
         * @throws IllegalStateException         if the <tt>next</tt> method has not
         *                                       yet been called, or the <tt>remove</tt> method has already
         *                                       been called after the last call to the <tt>next</tt>
         *                                       method.
         */
        public void remove() {
            iterator.remove();
        }
    }

    /**
     * Adapts the underlying map's entry set.
     */
    private class EntrySet extends AbstractSet<Map.Entry<String, Object>> {

        /**
         * Returns an iterator over the elements contained in this collection.
         *
         * @return an iterator over the elements contained in this collection.
         */
        public Iterator<Entry<String, Object>> iterator() {
            return new EntrySetIterator(map.entrySet().iterator());
        }

        /**
         * Returns the number of elements in this collection.
         *
         * @return the number of elements in this collection.
         */
        public int size() {
            return map.entrySet().size();
        }
    }

    /**
     * Adapts the underlying map's entry set iterator.
     */
    private class EntrySetIterator
            implements Iterator<Map.Entry<String, Object>> {

        private final Iterator<Map.Entry<String, TypedValue>> iterator;

        public EntrySetIterator(
                Iterator<Map.Entry<String, TypedValue>> iterator) {
            this.iterator = iterator;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements.
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public Map.Entry<String, Object> next() {
            Map.Entry<String, TypedValue> entry = iterator.next();
            return new DefaultMapEntry(entry.getKey(),
                                       entry.getValue().getObject());
        }

        /**
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation).
         *
         * @throws UnsupportedOperationException if the <tt>remove</tt>
         *                                       operation is not supported by
         *                                       this Iterator.
         * @throws IllegalStateException         if the <tt>next</tt> method has
         *                                       not yet been called, or the
         *                                       <tt>remove</tt> method has
         *                                       already been called after the
         *                                       last call to the <tt>next</tt>
         *                                       method.
         */
        public void remove() {
            iterator.remove();
        }
    }


}
