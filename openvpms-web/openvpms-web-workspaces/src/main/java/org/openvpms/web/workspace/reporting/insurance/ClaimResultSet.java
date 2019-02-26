package org.openvpms.web.workspace.reporting.insurance;

import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.leftJoin;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.web.component.im.query.QueryHelper.addParticipantConstraint;

/**
 * A {@link ResultSet} for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimResultSet extends ActResultSet<Act> {

    /**
     * The claim identifier.
     */
    private final String id;

    /**
     * The insurer.
     */
    private final Party insurer;

    /**
     * The gap status.
     */
    private final String gapStatus;

    /**
     * Constructs a {@link ActResultSet}.
     *
     * @param archetypes the act archetype constraint
     * @param id         an identifier to search on. May be the claim id, policy number, or insurerId of the claim
     * @param location   the practice location. May be {@code null}
     * @param insurer    the insurer. May be {@code null}
     * @param clinician  the clinician. May be {@code null}
     * @param from       the act start-from date. May be {@code null}
     * @param to         the act start-to date. May be {@code null}
     * @param statuses   the act statuses. If empty, indicates all acts
     * @param gapStatus  the gap status. May be {@code null}
     * @param pageSize   the maximum no. of results per page
     * @param sort       the sort criteria. May be {@code null}
     */
    public ClaimResultSet(ShortNameConstraint archetypes, String id,
                          Party location, Party insurer, User clinician, Date from, Date to,
                          String[] statuses, String gapStatus, int pageSize, SortConstraint[] sort) {
        super(archetypes, getParticipants(location, clinician), from, to, statuses, false, null, pageSize, sort);
        this.id = id;
        this.insurer = insurer;
        this.gapStatus = gapStatus;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();
        if (gapStatus != null) {
            query.add(eq("status2", gapStatus));
        }
        if (id != null) {
            query.add(leftJoin("insurerId", "claimId"));
            query.add(leftJoin("policy", "p").add(leftJoin("target", "t").add(leftJoin("insurerId", "policyNumber"))));
            OrConstraint or = or(eq("claimId.identity", id), eq("policyNumber.identity", id));
            Long actId = getId(id);
            if (actId != null) {
                or.add(eq("id", actId));
            }
            query.add(or);
        }
        if (insurer != null) {
            query.add(join("policy").add(join("target").add(join("insurer").add(eq("entity", insurer)))));
        }
        return query;
    }

    private static ParticipantConstraint[] getParticipants(Party location, User clinician) {
        List<ParticipantConstraint> list = new ArrayList<>();
        if (location != null) {
            addParticipantConstraint(list, "location", "participation.location", location);
        }
        if (clinician != null) {
            addParticipantConstraint(list, "clinician", UserArchetypes.CLINICIAN_PARTICIPATION, clinician);
        }
        return list.toArray(new ParticipantConstraint[list.size()]);
    }
}
