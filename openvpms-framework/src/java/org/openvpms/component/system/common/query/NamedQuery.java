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

package org.openvpms.component.system.common.query;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Named query.
 *
 * @author Tim Anderson
 */
public class NamedQuery extends AbstractArchetypeQuery {

    /**
     * The query name.
     */
    private String query;

    /**
     * Object names. May be {@code null}
     */
    private Collection<String> names;

    /**
     * The query parameters. May be {@code null}
     */
    private Map<String, Object> parameters;


    /**
     * Constructs a {@link NamedQuery}.
     *
     * @param query the query name
     */
    public NamedQuery(String query) {
        this(query, (Collection<String>) null);
    }

    /**
     * Constructs a {@link NamedQuery}.
     *
     * @param query the query name
     * @param names names to assign the objects in the result set. If {@code null}, or empty, names will generated
     */
    public NamedQuery(String query, Collection<String> names) {
        this(query, names, null);
    }

    /**
     * Constructs a {@link NamedQuery}.
     *
     * @param query the query name
     * @param names names to assign the objects in the result set. If or empty, names will generated
     */
    public NamedQuery(String query, String... names) {
        this(query, names.length != 0 ? Arrays.asList(names) : null, null);
    }

    /**
     * Constructs a {@link NamedQuery}.
     *
     * @param query      the query name
     * @param names      names to assign the objects in the result set. If {@code null}, or empty, names will generated
     * @param parameters the query parameters. May be {@code null}
     */
    public NamedQuery(String query, Collection<String> names, Map<String, Object> parameters) {
        this.query = query;
        if (names != null && !names.isEmpty()) {
            this.names = names;
        } else {
            this.names = null;
        }
        if (parameters != null) {
            this.parameters = parameters;
        } else {
            this.parameters = new HashMap<String, Object>();
        }
    }

    /**
     * Returns the query name.
     *
     * @return the query name
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the object names.
     *
     * @return the object names. May be {@code null}
     */
    public Collection<String> getNames() {
        return names;
    }

    /**
     * Returns the query parameters.
     *
     * @return the query parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Sets a parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     */
    public void setParameter(String name, Object value) {
        parameters.put(name, value);
    }

    /**
     * Returns a parameter value.
     *
     * @param name the parameter name
     * @return the parameter value. May be {@code null}
     */
    public Object getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof NamedQuery)) {
            return false;
        }

        NamedQuery rhs = (NamedQuery) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(rhs))
                .append(query, rhs.query)
                .append(names, rhs.names)
                .append(parameters, rhs.parameters)
                .isEquals();
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("query", query)
                .append("names", names)
                .append("parameters", parameters)
                .toString();
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        NamedQuery copy = (NamedQuery) super.clone();
        copy.names = new ArrayList<String>(names);
        if (parameters != null) {
            copy.parameters = new HashMap<String, Object>(parameters);
        }
        return copy;
    }
}
