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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.insurance;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.act.ActIdentity;
import org.openvpms.component.model.act.FinancialAct;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Identity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ParticipationConstraint;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static org.openvpms.component.system.common.query.Constraints.and;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.in;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.not;
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
    public InsuranceRules(IArchetypeRuleService service, PlatformTransactionManager transactionManager) {
        this.service = service;
        this.transactionManager = transactionManager;
    }

    /**
     * Creates a new insurance policy.
     * <p>
     * The caller is responsible for saving it.
     *
     * @param customer     the customer
     * @param patient      the patient
     * @param insurer      the insurer
     * @param policyNumber the policy number. May be {@code null}
     * @param author       the author. May be {@code null}
     * @return a new policy
     */
    public Act createPolicy(Party customer, Party patient, Party insurer, String policyNumber, User author) {
        Act policy = (Act) service.create(InsuranceArchetypes.POLICY);
        IMObjectBean bean = service.getBean(policy);
        bean.setTarget("customer", customer);
        bean.setTarget("patient", patient);
        bean.setTarget("insurer", insurer);
        bean.setTarget("author", author);
        if (policyNumber != null) {
            ActIdentity identity = (ActIdentity) service.create(InsuranceArchetypes.POLICY_IDENTITY);
            identity.setIdentity(policyNumber);
            policy.addIdentity(identity);
        }
        return policy;
    }

    /**
     * Returns a policy to be associated with a claim.
     * <p>
     * This is designed to be used when associating policies with claims to limit the number of policies a patient has.
     * <ul>
     * <li>if there is already a policy with the same customer, patient, insurer and policy number, this is
     * returned; else</li>
     * <li>if an existing policy is supplied for the customer and patient, and has no claims associated with it,
     * this will be returned with the insurer and policy number set to that supplied; else</li>
     * <li>if there is a policy for the customer and patient, without any claims associated with it, this will
     * be returned with the insurer and policy number set to that supplied; else</li>
     * <li>a new policy will be created</li>
     * </ul>
     * Any policy returned will have its expiry date set to {@code null}.<br/>
     * Any new policy must be saved by the caller.
     *
     * @param customer       the customer
     * @param patient        the patient
     * @param insurer        the insurer
     * @param policyNumber   the policy number
     * @param author         the author. May be {@code null}
     * @param existingClaim  if specified, ignore this claim when determining if a policy can be re-used
     * @param existingPolicy an existing policy. May be {@code null}
     * @return the policy
     */
    public Act getPolicyForClaim(Party customer, Party patient, Party insurer, String policyNumber, User author,
                                 FinancialAct existingClaim, Act existingPolicy) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(transactionStatus -> {
            Act result = getPolicy(customer, patient, insurer, policyNumber);
            if (result == null) {
                // no match on customer, patient, insurer and policy number
                if (existingPolicy != null) {
                    // see if the existing policy can be re-used. Can only re-use if it is for the same customer
                    // and patient, and has no other claims
                    IMObjectBean bean = service.getBean(existingPolicy);
                    if (bean.getTargetRef("patient").equals(patient.getObjectReference())
                        && bean.getTargetRef("customer").equals(customer.getObjectReference())
                        && !hasClaims(bean, existingClaim)) {
                        setPolicyNumber(existingPolicy, policyNumber, bean);
                        bean.setTarget("insurer", insurer);
                        result = existingPolicy;
                    }
                }
                if (result == null) {
                    // see if there is an existing policy for the customer and patient
                    result = getPolicy(customer, patient);
                    if (result != null) {
                        // can only re-use if it has no other claims
                        IMObjectBean bean = service.getBean(result);
                        if (!hasClaims(bean, existingClaim)) {
                            setPolicyNumber(result, policyNumber, bean);
                            bean.setTarget("insurer", insurer);
                        } else {
                            result = null;
                        }
                    }
                    if (result == null) {
                        // need to create a new policy
                        result = createPolicy(customer, patient, insurer, policyNumber, author);
                    }
                }
            }
            result.setActivityEndTime(null);
            return result;
        });
    }

    /**
     * Returns the current policy for a patient, or the most recent, if there is no current policy.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return the policy for the patient, or {@code null} if there is none
     */
    public Act getPolicy(Party customer, Party patient) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(transactionStatus -> {
            Act policy = getCurrentPolicy(customer, patient);
            if (policy == null) {
                policy = getMostRecentPolicy(customer, patient, false);
            }
            return policy;
        });
    }

    /**
     * Returns the most recent policy for a patient.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param insurer  the insurer
     * @return the policy. May be {@code null}
     */
    public Act getPolicy(Party customer, Party patient, Party insurer) {
        ArchetypeQuery query = createPolicyQuery(customer, patient, insurer);
        return query(query);
    }

    /**
     * Returns a policy.
     *
     * @param customer     the customer
     * @param patient      the patient
     * @param insurer      the insurer
     * @param policyNumber the policy number. May be {@code null}
     * @return the corresponding policy, or {@code null} if none is found
     */
    public Act getPolicy(Party customer, Party patient, Party insurer, String policyNumber) {
        ArchetypeQuery query = createPolicyQuery(customer, patient, insurer);
        query.add(join("insurerId").add(eq("identity", policyNumber)));
        return query(query);
    }

    /**
     * Returns the current policy for a patient.
     * <p>
     * This is the policy overlapping the current time.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return the current policy for the patient, or {@code null} if there is none
     */
    public Act getCurrentPolicy(Party customer, Party patient) {
        Date now = new Date();
        ArchetypeQuery query = createPolicyQuery(customer, patient);
        query.add(and(Constraints.lte("startTime", now),
                      Constraints.or(Constraints.isNull("endTime"), Constraints.gt("endTime", now))));
        return query(query);
    }

    /**
     * Returns the policy for a patient, with the greatest <em>startTime</em>.
     *
     * @param customer              the customer
     * @param patient               the patient
     * @param excludeFuturePolicies if {@code true}, exclude future dated policies
     * @return the most recent policy, or {@code null} if there is none
     */
    public Act getMostRecentPolicy(Party customer, Party patient, boolean excludeFuturePolicies) {
        Date now = new Date();
        ArchetypeQuery query = createPolicyQuery(customer, patient);
        if (excludeFuturePolicies) {
            query.add(Constraints.lte("startTime", now));
        }
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
        IMObjectBean bean = service.getBean(act);
        if (bean.isA(InsuranceArchetypes.POLICY)) {
            insurer = bean.getTarget("insurer", Party.class);
        } else {
            Act policy = bean.getTarget("policy", Act.class);
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
        IMObjectBean bean = service.getBean(claim);
        bean.addTarget("policy", policy, "claims");
        return claim;
    }

    /**
     * Determines if a policy's policy number can be changed.
     * <p>
     * A policy number can be changed if it has no claims with statuses other than {@code PENDING} or {@code POSTED}.
     *
     * @param policy the policy
     * @return {@code true} if the policy number can be changed, otherwise {@code false}
     */
    public boolean canChangePolicyNumber(Act policy) {
        ArchetypeQuery query = new ArchetypeQuery(policy.getObjectReference());
        query.add(join("claims").add(join("source").add(not(in("status", ClaimStatus.PENDING, ClaimStatus.POSTED)))));
        query.add(new NodeSelectConstraint("id"));
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        return !iterator.hasNext();
    }

    /**
     * Returns the policy number of a policy.
     *
     * @param policy the policy
     * @return the policy number. May be {@code null}
     */
    public String getPolicyNumber(Act policy) {
        IMObjectBean bean = service.getBean(policy);
        Identity insurerId = bean.getObject("insurerId", Identity.class);
        return (insurerId != null) ? insurerId.getIdentity() : null;
    }

    /**
     * Determines if an invoice is fully or partially claimed.
     * <p/>
     * This excludes claims with CANCELLED status.
     *
     * @param invoice the invoice
     * @return {@code true} if the invoice is claimed
     */
    public boolean isClaimed(FinancialAct invoice) {
        ArchetypeQuery query = new ArchetypeQuery(InsuranceArchetypes.CLAIM);
        query.setMaxResults(1);
        query.setDistinct(true);
        query.add(Constraints.ne("status", ClaimStatus.CANCELLED));
        query.add(join("items", "conditions").add(
                join("target", "condition").add(
                        join("items", "charges").add(
                                join("target", "item").add(
                                        join("invoice").add(eq("source", invoice)))))));
        Iterator<FinancialAct> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext();
    }

    /**
     * Returns the current claims for an invoice.
     * <p>
     * These are those that aren't CANCELLED, SETTLED or DECLINED.
     *
     * @param invoice the invoice
     * @return the claims
     */
    @SuppressWarnings("unchecked")
    public List<FinancialAct> getCurrentClaims(FinancialAct invoice) {
        List<FinancialAct> result;
        ArchetypeQuery query = new ArchetypeQuery(InsuranceArchetypes.CLAIM);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        query.setDistinct(true);
        query.add(Constraints.and(Constraints.ne("status", ClaimStatus.CANCELLED),
                                  Constraints.ne("status", ClaimStatus.SETTLED),
                                  Constraints.ne("status", ClaimStatus.DECLINED)));
        query.add(join("items", "conditions").add(
                join("target", "condition").add(
                        join("items", "charges").add(
                                join("target", "item").add(
                                        join("invoice").add(eq("source", invoice)))))));
        result = (List) service.get(query).getResults();
        return result;
    }

    /**
     * Determines if an invoice has a gap claim that is yet to be paid, cancelled or declined.
     *
     * @param invoice the invoice
     * @return any current gap claims that the invoice is part of
     */
    public List<FinancialAct> getCurrentGapClaims(FinancialAct invoice) {
        List<FinancialAct> result = new ArrayList<>();
        for (FinancialAct claim : getCurrentClaims(invoice)) {
            IMObjectBean bean = service.getBean(claim);
            if (bean.getBoolean("gapClaim") && !isPaid(claim)) {
                result.add(claim);
            }
        }
        return result;
    }

    /**
     * Determines if a gap claim has been paid.
     *
     * @param claim the gap claim
     * @return {@code true} if the gap claim has been paid, otherwise {@code false}
     */
    protected boolean isPaid(FinancialAct claim) {
        String status = claim.getStatus2();
        return ClaimStatus.GAP_CLAIM_PAID.equals(status) || ClaimStatus.GAP_CLAIM_NOTIFIED.equals(status);
    }

    /**
     * Creates a policy query for a customer, patient, and insurer.
     * <p>
     * This returns the most recent policy first.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param insurer  the insurer
     * @return the query
     */
    protected ArchetypeQuery createPolicyQuery(Party customer, Party patient, Party insurer) {
        ArchetypeQuery query = createPolicyQuery(customer, patient);
        query.add(join("insurer").add(eq("entity", insurer)).add(
                new ParticipationConstraint(ActShortName, InsuranceArchetypes.POLICY)));
        return query;
    }

    /**
     * Determines if a policy has any claims.
     *
     * @param bean  the policy
     * @param claim if non-null, ignore this claim when performing the check
     * @return {@code true} if the policy has claims, otherwise {@code false}
     */
    private boolean hasClaims(IMObjectBean bean, Act claim) {
        boolean result;
        if (claim == null) {
            result = !bean.getValues("claims").isEmpty();
        } else {
            Reference source = claim.getObjectReference();
            Predicate<Relationship> predicate = relationship -> !ObjectUtils.equals(source, relationship.getSource());
            result = bean.getValue("claims", Relationship.class, predicate) != null;
        }
        return result;
    }

    /**
     * Updates a policy's policy number.
     *
     * @param policy       the policy
     * @param policyNumber the policy number. May be {@code null}
     * @param bean         the bean wrapping the policy
     */
    private void setPolicyNumber(Act policy, String policyNumber, IMObjectBean bean) {
        ActIdentity identity = bean.getObject("insurerId", ActIdentity.class);
        if (policyNumber != null) {
            if (identity == null) {
                identity = (ActIdentity) service.create(InsuranceArchetypes.POLICY_IDENTITY);
                policy.addIdentity(identity);
            }
            identity.setIdentity(policyNumber);
        } else if (identity != null) {
            policy.removeIdentity(identity);
        }
    }

    /**
     * Helper to return the first result of a query.
     *
     * @param query the query
     * @return the result or {@code null} if none is found
     */
    private Act query(ArchetypeQuery query) {
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Creates a policy query for a customer and patient.
     * <p>
     * This returns the most recent policy first.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return the query
     */
    private ArchetypeQuery createPolicyQuery(Party customer, Party patient) {
        ArchetypeQuery query = new ArchetypeQuery(Constraints.shortName("act", InsuranceArchetypes.POLICY));
        query.setMaxResults(1);
        query.add(join("customer").add(eq("entity", customer)).add(
                new ParticipationConstraint(ActShortName, InsuranceArchetypes.POLICY)));
        query.add(join("patient").add(eq("entity", patient)).add(
                new ParticipationConstraint(ActShortName, InsuranceArchetypes.POLICY)));

        // if there are multiple policies, pick the most recent
        query.add(Constraints.sort("startTime", false));
        query.add(Constraints.sort("id", false));
        return query;
    }

}
