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

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;


/**
 * Query implementation that queries patients. The search can be further
 * constrained to only include those patients associated with the current
 * customer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientQuery extends QueryAdapter<ObjectSet, Party> {

    /**
     * Constructs a <tt>PatientQuery</tt> that queries patients instances with the specified short names.
     *
     * @param shortNames the short names
     * @param context    the context
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public PatientQuery(String[] shortNames, Context context) {
        this(shortNames, context.getCustomer());
    }

    /**
     * Constructs a <tt>PatientQuery</tt> that queries patients instances with the specified short names.
     *
     * @param shortNames the short names
     * @param customer   the customer. May be <tt>null</tt>
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public PatientQuery(String[] shortNames, Party customer) {
        super(new PatientObjectSetQuery(shortNames, customer), Party.class);
    }

    /**
     * Determines if the 'all patients' checkbox should be displayed.
     *
     * @param show if <tt>true</tt>, display the 'all patients' checkbox
     */
    public void setShowAllPatients(boolean show) {
        ((PatientObjectSetQuery) getQuery()).setShowAllPatients(show);
    }

    /**
     * Determines if all patients should be returned by the query.
     * <p/>
     * Only applies if {@link #setShowAllPatients} has been invoked i.e. <tt>setShowAllPatients(true)</tt>
     *
     * @param all if <tt>true</tt> query all patients, otherwise query patients associated with the customer
     */
    public void setQueryAllPatients(boolean all) {
        ((PatientObjectSetQuery) getQuery()).setQueryAllPatients(all);
    }

    /**
     * Determines if all patients are being queried.
     *
     * @return <tt>true</tt> if all patients are being queried, <tt>false</tt> if only those patient associated with
     *         the customer are being queried
     */
    public boolean isQueryAllPatients() {
        return ((PatientObjectSetQuery) getQuery()).isQueryAllPatients();
    }

    /**
     * Converts a result set.
     *
     * @param set the set to convert
     * @return the converted set
     */
    protected ResultSet<Party> convert(ResultSet<ObjectSet> set) {
        return new ObjectSetResultSetAdapter<Party>(set, "patient", Party.class);
    }
}
