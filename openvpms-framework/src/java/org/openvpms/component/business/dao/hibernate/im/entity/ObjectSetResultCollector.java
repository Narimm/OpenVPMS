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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ObjectSetResultCollector extends HibernateResultCollector<ObjectSet> {

    private String[] names;
    private final List<ObjectSet> result = new ArrayList<ObjectSet>();
    private final Map<String, Set<String>> types;

    public ObjectSetResultCollector() {
        this(null, null);
    }

    public ObjectSetResultCollector(Collection<String> names,
                                    Map<String, Set<String>> types) {
        if (names != null) {
            this.names = names.toArray(new String[0]);
        }
        this.types = types;
    }

    public void collect(Object object) {
        ObjectSet set = createObjectSet();
        ObjectLoader loader = getLoader();
        if (names == null) {
            getNames(object);
        }
        if (object instanceof Object[]) {
            Object[] values = (Object[]) object;
            if (values.length != names.length) {
                throw new IllegalStateException("Mismatch args");
            }
            for (int i = 0; i < names.length; ++i) {
                Object value = values[i];
                if (value != null) {
                    loader.load(value);
                }
                set.add(names[i], value);
            }
        } else if (names.length != 1) {
            throw new IllegalStateException("Mismatch args");
        } else {
            loader.load(object);
            set.add(names[0], object);
        }
        result.add(set);
    }

    private void getNames(Object object) {
        if (object instanceof Object[]) {
            Object[] values = (Object[]) object;
            names = new String[values.length];
            for (int i = 0; i < names.length; ++i) {
                names[i] = "" + i;
            }

        } else {
            names = new String[1];
            names[0] = "0";
        }
    }

    private ObjectSet createObjectSet() {
        ObjectSet result = new ObjectSet();
        if (types != null) {
            for (Map.Entry<String, Set<String>> entry : types.entrySet()) {
                result.addType(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    protected List<ObjectSet> getResults() {
        return result;
    }
}
