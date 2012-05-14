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
