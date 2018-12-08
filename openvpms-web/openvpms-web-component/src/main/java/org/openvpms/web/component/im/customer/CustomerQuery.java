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

package org.openvpms.web.component.im.customer;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.AbstractQueryState;
import org.openvpms.web.component.im.query.ObjectSetResultSetAdapter;
import org.openvpms.web.component.im.query.QueryAdapter;
import org.openvpms.web.component.im.query.QueryState;
import org.openvpms.web.component.im.query.ResultSet;


/**
 * Query implementation that queries customers. The search can be further
 * constrained to match on:
 * <ul>
 * <li>partial patient name
 * <li>partial contact description
 * </ul>
 *
 * @author Tim Anderson
 */
public class CustomerQuery extends QueryAdapter<ObjectSet, Party> {

    /**
     * Constructs a {@link CustomerQuery}.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public CustomerQuery(String[] shortNames) {
        super(new CustomerObjectSetQuery(shortNames), Party.class);
    }

    /**
     * Sets the name or id of the patient to search on.
     *
     * @param value the patient name or id. May be {@code null}
     */
    public void setPatient(String value) {
        ((CustomerObjectSetQuery) getQuery()).setPatient(value);
    }

    /**
     * Sets the contact description to search on.
     *
     * @param value the contact description to search on. May be {@code null}
     */
    public void setContact(String value) {
        ((CustomerObjectSetQuery) getQuery()).setContact(value);
    }

    /**
     * Determines if the query selects a particular object.
     *
     * @param object the object to check
     * @return {@code true} if the object is selected by the query
     */
    public boolean selects(Party object) {
        return ((CustomerObjectSetQuery) getQuery()).selects(object);
    }

    /**
     * Returns the query state.
     *
     * @return the query state
     */
    public QueryState getQueryState() {
        return new Memento(this);
    }

    /**
     * Sets the query state.
     *
     * @param state the query state
     */
    @Override
    public void setQueryState(QueryState state) {
        Memento memento = (Memento) state;
        getQuery().setQueryState(memento.state);
    }

    /**
     * Converts a result set.
     *
     * @param set the set to convert
     * @return the converted set
     */
    protected ResultSet<Party> convert(ResultSet<ObjectSet> set) {
        return new ObjectSetResultSetAdapter<>(set, "customer", Party.class);
    }

    private static class Memento extends AbstractQueryState {

        private final QueryState state;

        public Memento(CustomerQuery query) {
            super(query);
            state = query.getQuery().getQueryState();
        }
    }
}
