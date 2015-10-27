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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.list;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.AbstractListModel;
import nextapp.echo2.app.list.ListCellRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * List model that manages key-value pairs.
 *
 * @author Tim Anderson
 */
public class PairListModel extends AbstractListModel {

    public static class Pair {
        final Object key;
        final Object value;

        public Pair(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    /**
     * The default renderer.
     */
    public static ListCellRenderer RENDERER = new ListCellRenderer() {
        @Override
        public Object getListCellRendererComponent(Component list, Object value, int index) {
            AbstractListComponent l = (AbstractListComponent) list;
            PairListModel model = (PairListModel) l.getModel();
            return model.getValue(index);
        }
    };

    /**
     * The pairs.
     */
    private List<Pair> pairs = new ArrayList<>();

    /**
     * Default constructor.
     */
    public PairListModel() {
    }

    /**
     * Constructs a {@link PairListModel}.
     *
     * @param pairs the pairs
     */
    public PairListModel(List<Pair> pairs) {
        this.pairs.addAll(pairs);
    }

    /**
     * Returns the key at the specified index in the list.
     *
     * @param index the index
     * @return the key. May be {@code null}
     */
    @Override
    public Object get(int index) {
        return pairs.get(index).key;
    }

    /**
     * Returns the value of the specified index.
     * <p/>
     * This is rendered in the list.
     *
     * @param index the index
     * @return the corresponding value. May be {@code null}
     */
    public Object getValue(int index) {
        return pairs.get(index).value;
    }

    /**
     * Adds a key-value pair.
     *
     * @param key   the key. May be {@code null}
     * @param value the value to render. May be {@code null}
     */
    public void add(Object key, Object value) {
        int index = pairs.size();
        pairs.add(new Pair(key, value));
        fireIntervalAdded(index, index);
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    @Override
    public int size() {
        return pairs.size();
    }
}
