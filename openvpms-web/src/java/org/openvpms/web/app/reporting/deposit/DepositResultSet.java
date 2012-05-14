package org.openvpms.web.app.reporting.deposit;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityResultSet;
import org.openvpms.web.component.im.query.DefaultQueryExecutor;

import java.util.Date;


/**
 * A query for <em>party.organisationDeposit</em> objects that optionally constrains them to a specified
 * <em>party.organisationLocation</em>.
 *
 * @author Tim Anderson
 */
public class DepositResultSet extends AbstractEntityResultSet<Party> {

    /**
     * The practice location. May be {@code null}.
     */
    private final Party location;

    /**
     * Constructs a <tt>DepositResultSet</tt>.
     *
     * @param location         the practice location. May be {@code null}
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be <tt>null</tt>
     * @param searchIdentities if <tt>true</tt> search on identity name
     * @param constraints      additional query constraints. May be <tt>null</tt>
     * @param sort             the sort criteria. May be <tt>null</tt>
     * @param rows             the maximum no. of rows per page
     * @param distinct         if <tt>true</tt> filter duplicate rows
     */
    public DepositResultSet(Party location, ShortNameConstraint archetypes, String value, boolean searchIdentities,
                            IConstraint constraints, SortConstraint[] sort, int rows, boolean distinct) {
        super(archetypes, value, searchIdentities, constraints, sort, rows, distinct,
              new DefaultQueryExecutor<Party>());
        this.location = location;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();

        if (location != null) {
            Date now = new Date();
            query.add(Constraints.join("locations",
                                       Constraints.shortName("rel", "entityRelationship.locationDeposit")));
            query.add(Constraints.eq("rel.source", location.getObjectReference()));
            query.add(Constraints.lte("rel.activeStartTime", now));
        }
        return query;
    }

}
