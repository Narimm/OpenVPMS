package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.insurance.claim.Claim;

/**
 * Claim helper methods.
 *
 * @author Tim Anderson
 */
class ClaimHelper {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a new {@link ClaimHelper}.
     *
     * @param service the archetype service
     */
    public ClaimHelper(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Determines if an invoice item has already been claimed by another insurance claim.
     *
     * @param item    the charge item
     * @param exclude the claim to exclude. May be {@code null}
     * @return the claim that the charge item has a relationship to, if it isn't CANCELLED or DECLINED
     */
    public Act getClaim(Act item, Act exclude) {
        Act result = null;
        IMObjectBean chargeBean = service.getBean(item);
        for (Act claimItem : chargeBean.getSources("claims", Act.class)) {
            IMObjectBean bean = service.getBean(claimItem);
            Act claim = bean.getSource("claim", Act.class);
            if (claim != null && (exclude == null || !claim.equals(exclude))) {
                String status = claim.getStatus();
                if (!Claim.Status.CANCELLED.isA(status) && !Claim.Status.DECLINED.isA(status)) {
                    result = claim;
                    break;
                }
            }
        }
        return result;
    }

}
