/*
 * Version: 1.0
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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.reporting.deposit;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.ResultSet;


/**
 * Query for <em>party.organisationDeposit</em>.
 *
 * @author Tim Anderson
 */
public class DepositQuery extends AbstractEntityQuery<Party> {

    /**
     * The short names to query;.
     */
    private static final String SHORT_NAMES[] = {"party.organisationDeposit"};

    /**
     * The location to constrain deposit accounts to. May be {@code null}.
     */
    private final Party location;

    /**
     * Constructs a <tt>DepositQuery</tt>.
     *
     * @param location the location to constrain deposit accounts to. May be {@code null}
     */
    public DepositQuery(Party location) {
        super(SHORT_NAMES, Party.class);
        this.location = location;
        setAuto(true);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     * @return a new result set
     */
    @Override
    protected ResultSet<Party> createResultSet(SortConstraint[] sort) {
        return new DepositResultSet(location, getArchetypeConstraint(), getValue(), isIdentitySearch(),
                                    getConstraints(), sort, getMaxResults(), isDistinct());
    }
}
