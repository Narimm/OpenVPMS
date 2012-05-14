package org.openvpms.web.app.reporting.till;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.ResultSet;


/**
 * Query for <em>party.organisationTill</em>.
 *
 * @author Tim Anderson
 */
public class TillQuery extends AbstractEntityQuery<Party> {

    /**
     * The short names to query;.
     */
    private static final String SHORT_NAMES[] = {"party.organisationTill"};

    /**
     * The location to constrain tills to. May be {@code null}.
     */
    private final Party location;

    /**
     * Constructs a <tt>TillQuery</tt>.
     *
     * @param location the location to constrain tills to. May be {@code null}
     */
    public TillQuery(Party location) {
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
        return new TillResultSet(location, getArchetypeConstraint(), getValue(), isIdentitySearch(), getConstraints(),
                                 sort, getMaxResults(), isDistinct());
    }
}
