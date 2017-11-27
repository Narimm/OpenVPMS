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

package org.openvpms.archetype.rules.patient.insurance;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ParticipationConstraint;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.sort;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;

/**
 * Insurance rules.
 *
 * @author Tim Anderson
 */
public class InsuranceRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * Constructs an {@link InsuranceRules}.
     *
     * @param service            the archetype service
     * @param transactionManager the transaction manager
     */
    public InsuranceRules(IArchetypeService service, PlatformTransactionManager transactionManager) {
        this.service = service;
        this.transactionManager = transactionManager;
    }

    /**
     * Returns the current policy for a patient, or the most recent, if there is no current policy.
     *
     * @param patient the patient
     * @return the policy for the patient, or {@code null} if there is none
     */
    public Act getPolicy(final Party patient) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(new TransactionCallback<Act>() {
            @Override
            public Act doInTransaction(TransactionStatus transactionStatus) {
                Act policy = getCurrentPolicy(patient);
                if (policy == null) {
                    policy = getMostRecentPolicy(patient, false);
                }
                return policy;
            }
        });
    }

    /**
     * Returns the current policy for a patient.
     * <p>
     * This is the policy overlapping the current time.
     *
     * @param patient the patient
     * @return the current policy for the patient, or {@code null} if there is none
     */
    public Act getCurrentPolicy(Party patient) {
        Date now = new Date();
        ArchetypeQuery query = createPolicyQuery(patient);
        query.add(Constraints.and(Constraints.lte("startTime", now), Constraints.gt("endTime", now)));

        // if there are multiple policies, pick the most recent
        query.add(Constraints.sort("startTime", false));
        query.add(Constraints.sort("id", false));
        return query(query);
    }

    /**
     * Returns the policy for a patient, with the greatest <em>startTime</em>.
     *
     * @param patient               the patient
     * @param excludeFuturePolicies if {@code true}, exclude future dated policies
     * @return the most recent policy, or {@code null} if there is none
     */
    public Act getMostRecentPolicy(Party patient, boolean excludeFuturePolicies) {
        Date now = new Date();
        ArchetypeQuery query = createPolicyQuery(patient);
        if (excludeFuturePolicies) {
            query.add(Constraints.lte("startTime", now));
        }
        query.add(sort("startTime", false));
        query.add(sort("id", false));
        return query(query);
    }

    /**
     * Returns the insurer associated with a policy or claim.
     *
     * @param act the policy or claim
     * @return the insurer, or {@code null} if the insurer cannot be found
     */
    public Party getInsurer(Act act) {
        Party insurer = null;
        ActBean bean = new ActBean(act, service);
        if (bean.isA(InsuranceArchetypes.POLICY)) {
            insurer = (Party) bean.getNodeParticipant("insurer");
        } else {
            Act policy = (Act) bean.getNodeTargetObject("policy");
            if (policy != null) {
                insurer = getInsurer(policy);
            }
        }
        return insurer;
    }

    /**
     * Creates a new claim, linked to a policy.
     *
     * @param policy the policy
     * @return a new claim
     */
    public FinancialAct createClaim(Act policy) {
        FinancialAct claim = (FinancialAct) service.create(InsuranceArchetypes.CLAIM);
        if (claim == null) {
            throw new IllegalStateException("Failed to create " + InsuranceArchetypes.CLAIM);
        }
        IMObjectBean bean = new IMObjectBean(claim, service);
        bean.addTarget("policy", policy, "claims");
        return claim;
    }

    /**
     * Helper to return the first result of a query.
     *
     * @param query the query
     * @return the result or {@code null} if none is found
     */
    private Act query(ArchetypeQuery query) {
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Creates a policy query for a patient.
     *
     * @param patient the patient
     * @return the query
     */
    private ArchetypeQuery createPolicyQuery(Party patient) {
        ArchetypeQuery query = new ArchetypeQuery(Constraints.shortName("act", InsuranceArchetypes.POLICY));
        query.setMaxResults(1);
        query.add(Constraints.join("patient")
                          .add(eq("entity", patient))
                          .add(new ParticipationConstraint(ActShortName, InsuranceArchetypes.POLICY)));
        return query;
    }

}
