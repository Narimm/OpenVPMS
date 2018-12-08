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

import nextapp.echo2.app.Extent;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.QueryState;
import org.openvpms.web.component.im.query.ResultSet;


/**
 * Query implementation that queries customers. The search can be further
 * constrained to match on:
 * <ul>
 * <li>partial patient name;
 * <li>patient id; and/or
 * <li>partial contact description
 * </ul>
 * <p/>
 * The returned {@link ObjectSet}s contain:
 * <ul>
 * <li>the customer:
 * <pre>Party customer = (Party) set.get("customer");</pre>
 * <li>the patient, if searching on patients:
 * <pre>Party patient = (Party) set.get("patient");</pre>
 * <li>the contact, if searching on contacts:
 * <pre>Contact contact = (Contact) set.get("contact");</pre>
 * <li>the identity, if searching on identities:
 * <pre>EntityIdentity identity = (EntityIdentity) set.get("identity");</pre>
 * </ul>
 *
 * @author Tim Anderson
 * @see CustomerResultSet
 */
public class CustomerObjectSetQuery extends CustomerPatientObjectSetQuery {

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT = {new NodeSortConstraint("customer", "name")};

    /**
     * Constructs a {@link CustomerObjectSetQuery} that queries customers instances with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public CustomerObjectSetQuery(String[] shortNames) {
        super(shortNames, true, null);
        setDefaultSortConstraint(DEFAULT_SORT);
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return {@code true} if the object reference is selected by the query
     */
    @Override
    public boolean selects(IMObjectReference reference) {
        CustomerResultSet set = (CustomerResultSet) createResultSet(null);
        set.setReferenceConstraint(reference);
        return set.hasNext();
    }

    /**
     * Returns the preferred height of the query when rendered.
     *
     * @return the preferred height, or {@code null} if it has no preferred height
     */
    @Override
    public Extent getHeight() {
        return getHeight(2);
    }

    /**
     * Returns the query state.
     *
     * @return the query state
     */
    public QueryState getQueryState() {
        return new CustomerMemento(this);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        String patientWildcard = getWildcardedText(getPatient());
        String contactWildcard = getWildcardedText(getContact(), true);

        return new CustomerResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), patientWildcard,
                                     contactWildcard, getConstraints(), sort, getMaxResults(), isDistinct());
    }

    private static class CustomerMemento extends Memento {

        public CustomerMemento(CustomerPatientObjectSetQuery query) {
            super(query);
        }
    }

}
