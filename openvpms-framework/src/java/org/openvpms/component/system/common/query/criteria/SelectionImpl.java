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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.query.criteria;

import javax.persistence.criteria.Selection;
import java.util.List;

/**
 * Default implementation of {@link Selection}.
 *
 * @author Tim Anderson
 */
public class SelectionImpl<T> implements javax.persistence.criteria.Selection<T> {

    /**
     * The type of the selection.
     */
    private final Type<T> type;

    /**
     * The selection alias.
     */
    private String alias;

    /**
     * Construct a {@link SelectionImpl}.
     *
     * @param type the type of the selection
     */
    public SelectionImpl(Type<T> type) {
        this.type = type;
    }

    /**
     * Return the Java type of the tuple element.
     *
     * @return the Java type of the tuple element
     */
    @Override
    public Class<? extends T> getJavaType() {
        return type.getType();
    }

    /**
     * Sets the selection alias.
     *
     * @param alias the alias
     * @return the selection
     */
    @Override
    public Selection<T> alias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * Returns the selection alias.
     *
     * @return the selection alias. May be {@code null}
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Returns the type of the selection.
     *
     * @return the type of the selection
     */
    public Type<T> getType() {
        return type;
    }

    /**
     * Whether the selection item is a compound selection.
     *
     * @return boolean indicating whether the selection is a compound
     * selection
     */
    @Override
    public boolean isCompoundSelection() {
        return false;
    }

    /**
     * Return the selection items composing a compound selection.
     * Modifications to the list do not affect the query.
     *
     * @return list of selection items
     * @throws IllegalStateException if selection is not a compound selection
     */
    @Override
    public List<javax.persistence.criteria.Selection<?>> getCompoundSelectionItems() {
        throw new IllegalStateException();
    }
}
