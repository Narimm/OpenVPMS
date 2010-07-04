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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.ObjectSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * An {@link ResultSet} implementation that queries customers. The search can be
 * further constrained to match on:
 * <ul>
 * <li>customer identity; and/or
 * <li>partial patient name; and/or
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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerResultSet extends AbstractEntityResultSet<ObjectSet> {

    /**
     * The patient name to query on. May be <tt>null</tt>
     */
    private final String patientName;

    /**
     * The contact description to query on. May be <tt>null</tt>
     */
    private final String contact;


    /**
     * Creates a new <tt>CustomerResultSet</tt>.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be <tt>null</tt>
     * @param searchIdentities if <tt>true</tt> search on identity name
     * @param patientName      if non-null, query on patient name
     * @param contact          if non-null,  query on contact description
     * @param sort             the sort criteria. May be <tt>null</tt>
     * @param rows             the maximum no. of rows per page
     * @param distinct         if <tt>true</tt> filter duplicate rows
     */
    public CustomerResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                             String patientName, String contact, SortConstraint[] sort, int rows, boolean distinct) {
        super(archetypes, value, searchIdentities, null, sort, rows, distinct, new ObjectSetQueryExecutor());
        archetypes.setAlias("customer");
        this.patientName = patientName;
        this.contact = contact;
    }

    /**
     * Determines if the result set contains patient details.
     *
     * @return <tt>true</tt> if the result set contains patient details
     */
    public boolean isSearchingOnPatient() {
        return !StringUtils.isEmpty(patientName);
    }

    /**
     * Determines if the result set contains contact details.
     *
     * @return <tt>true</tt> if the result set contains contact details
     */
    public boolean isSearchingOnContact() {
        return !StringUtils.isEmpty(contact);
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();
        query.add(new ObjectSelectConstraint("customer"));
        if (isSearchingOnPatient()) {
            query.add(Constraints.join("patients"));
            query.add(Constraints.shortName("patient", "party.patientpet"));
            query.add(new IdConstraint("source", "customer"));
            query.add(new IdConstraint("target", "patient"));
            Long id = getId(patientName);
            if (id != null) {
                query.add(Constraints.eq("patient.id", id));
            } else {
                query.add(Constraints.eq("patient.name", patientName));
            }
            query.add(new ObjectSelectConstraint("patient"));
        }
        if (isSearchingOnContact()) {
            query.add(Constraints.join("contacts", "contact").add(Constraints.eq("description", contact)));
            query.add(new ObjectSelectConstraint("contact"));
        }
        if (isSearchingIdentities()) {
            query.add(new ObjectSelectConstraint("identity"));
        }
        return query;
    }

}
