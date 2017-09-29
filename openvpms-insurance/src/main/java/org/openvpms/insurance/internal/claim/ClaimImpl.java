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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.internal.claim;

import org.apache.commons.collections.PredicateUtils;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.Condition;
import org.openvpms.insurance.claim.Note;
import org.openvpms.insurance.internal.policy.PolicyImpl;
import org.openvpms.insurance.policy.Policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lte;
import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * Default implementation of {@link Claim}.
 *
 * @author Tim Anderson
 */
public class ClaimImpl implements Claim {

    /**
     * The claim.
     */
    private final ActBean claim;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The policy.
     */
    private PolicyImpl policy;

    /**
     * The claim conditions.
     */
    private List<Condition> conditions;

    /**
     * The clinical history, up to the time of the claim.
     */
    private List<Note> history;

    /**
     * Constructs a {@link ClaimImpl}.
     *
     * @param claim         the claim
     * @param service       the archetype service
     * @param customerRules the customer rules
     * @param patientRules  the patient rules
     */
    public ClaimImpl(Act claim, IArchetypeService service, CustomerRules customerRules, PatientRules patientRules) {
        this.claim = new ActBean(claim, service);
        this.customerRules = customerRules;
        this.patientRules = patientRules;
        this.service = service;
    }

    /**
     * Returns the OpenVPMS identifier for this claim.
     *
     * @return the claim identifier
     */
    @Override
    public long getId() {
        return claim.getObject().getId();
    }

    /**
     * Returns the claim identifier, issued by the insurer.
     *
     * @return the claim identifier, or {@code null} if none has been issued
     */
    @Override
    public String getClaimId() {
        ActIdentity identity = getIdentity();
        return identity != null ? identity.getIdentity() : null;
    }

    /**
     * Sets the cleaim identifier, issued by the insurer.
     *
     * @param id the claim identifier
     */
    @Override
    public void setClaimId(String id) {
        ActIdentity identity = getIdentity();
        if (identity == null) {
            identity = (ActIdentity) service.create("actIdentity.insuranceClaim");
            claim.addValue("claimId", identity);
        }
        identity.setIdentity(id);
        claim.save();
    }

    /**
     * Returns the policy that a claim is being made on.
     *
     * @return the policy
     */
    @Override
    public Policy getPolicy() {
        if (policy == null) {
            policy = new PolicyImpl((Act) claim.getNodeTargetObject("policy"), service, customerRules, patientRules);
        }
        return policy;
    }

    /**
     * Returns the claim status
     *
     * @return the claim status
     */
    @Override
    public Status getStatus() {
        return Status.valueOf(claim.getStatus());
    }

    /**
     * Sets the claim status.
     *
     * @param status the claim status
     */
    @Override
    public void setStatus(Status status) {
        claim.setStatus(status.name());
        claim.save();
    }

    /**
     * Returns the conditions being claimed.
     *
     * @return the conditions being claimed
     */
    @Override
    public List<Condition> getConditions() {
        if (conditions == null) {
            conditions = collectConditions();
        }
        return conditions;
    }

    /**
     * Returns the clinical history for the patient.
     *
     * @return the clinical history
     */
    @Override
    public List<Note> getClinicalHistory() {
        if (history == null) {
            history = collectHistory();
        }
        return history;
    }

    /**
     * Collects the list of claim conditions.
     *
     * @return the claim conditions
     */
    protected List<Condition> collectConditions() {
        List<Condition> result = new ArrayList<>();
        for (Act act : claim.getNodeActs("items")) {
            result.add(new ConditionImpl(act, service));
        }
        return result;
    }

    /**
     * Collects the clinical history up to the time of the claim.
     *
     * @return the clinical history
     */
    protected List<Note> collectHistory() {
        List<Note> result = new ArrayList<>();
        Party patient = ((PolicyImpl) getPolicy()).getPatient();
        ArchetypeQuery query = new ArchetypeQuery(Constraints.shortName("note", PatientArchetypes.CLINICAL_NOTE));
        Date startTime = claim.getAct().getActivityStartTime();
        query.add(join("event").add(
                join("source", "event")
                        .add(join("patient").add(eq("entity", patient)))
                        .add(lte("event.startTime", startTime))))
                .add(lte("note.startTime", startTime));
        query.add(sort("event.startTime"));
        query.add(sort("event.id"));
        query.add(sort("note.startTime"));
        query.add(sort("note.id"));
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            Act note = iterator.next();
            result.add(new NoteImpl(note, service));
        }
        return result;
    }

    /**
     * Returns the claim identity, as specified by the insurance provider.
     *
     * @return the claim identity, or {@code null} if none is registered
     */
    protected ActIdentity getIdentity() {
        return claim.getValue("claimId", PredicateUtils.truePredicate(), ActIdentity.class);
    }

}
