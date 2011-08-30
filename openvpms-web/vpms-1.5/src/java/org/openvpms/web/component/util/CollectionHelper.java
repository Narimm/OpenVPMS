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

package org.openvpms.web.component.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.lang.reflect.Array;
import java.util.Collection;


/**
 * Collection operations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CollectionHelper {

    /**
     * Concatenates a set of string arrays.
     *
     * @param arrays the arrays to concatenate
     * @return the concatentation of the arguments
     */
    public static String[] concat(String[] ... arrays) {
        return concat(String.class, arrays);
    }

    /**
     * Concatenates a string array with strings.
     *
     * @param array the string array
     * @param elts  the elements to add
     * @return the concatenation of the arguments
     */
    public static String[] concat(String[] array, String ... elts) {
        return concat(String.class, array, elts);
    }

    /**
     * Finds the first element in the given collection which matches the given
     * predicate.
     * <p/>
     * If the input collection or predicate is null, or no element of the
     * collection matches the predicate, null is returned.
     *
     * @param collection the collection to search. May be <tt>null</tt>
     * @param predicate  the predicate to use. May be <tt>null</tt>
     * @return the first element of the collection which matches the predicate
     *         or <Tt>null</tt> if none could be found
     */
    @SuppressWarnings("unchecked")
    public static <T> T find(Collection<T> collection, Predicate predicate) {
        return (T) CollectionUtils.find(collection, predicate);
    }

    /**
     * Concatenates a set of string arrays.
     *
     * @param arrays the arrays to concatenate
     * @return the concatentation of the arguments
     */
    private static <T> T[] concat(Class type, T[] ... arrays) {
        int size = 0;
        for (T[] array : arrays) {
            size += array.length;
        }
        T[] result = (T[]) Array.newInstance(type, size);
        int offset = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
