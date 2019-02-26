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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.internal.claim;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.component.system.common.cache.MapIMObjectCache;
import org.openvpms.domain.internal.practice.LocationImpl;
import org.openvpms.domain.party.Email;
import org.openvpms.domain.party.Phone;
import org.openvpms.domain.patient.Patient;
import org.openvpms.domain.practice.Location;
import org.openvpms.insurance.claim.Attachment;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.ClaimHandler;
import org.openvpms.insurance.claim.Condition;
import org.openvpms.insurance.claim.Invoice;
import org.openvpms.insurance.claim.Note;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.internal.i18n.InsuranceMessages;
import org.openvpms.insurance.internal.policy.PolicyImpl;
import org.openvpms.insurance.policy.Policy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link Claim}.
 *
 * @author Tim Anderson
 */
public class ClaimImpl implements Claim {

    /**
     * The claim.
     */
    private final IMObjectBean claim;

    /**
     * The claim act.
     */
    private final Act act;

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * The insurance rules.
     */
    private final InsuranceRules insuranceRules;

    /**
     * The party rules.
     */
    private final PartyRules partyRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

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
     * The claim attachments.
     */
    private List<Attachment> attachments;

    /**
     * The claim handler.
     */
    private ClaimHandler handler;

    /**
     * Constructs a {@link ClaimImpl}.
     *
     * @param claim              the claim
     * @param service            the archetype service
     * @param insuranceRules     the insurance rules
     * @param partyRules         the customer rules
     * @param patientRules       the patient rules
     * @param handlers           the document handlers
     * @param transactionManager the transaction manager
     */
    public ClaimImpl(Act claim, IArchetypeRuleService service, InsuranceRules insuranceRules,
                     PartyRules partyRules, PatientRules patientRules, DocumentHandlers handlers,
                     PlatformTransactionManager transactionManager) {
        this(service.getBean(claim), claim, service, insuranceRules, partyRules, patientRules, handlers,
             transactionManager);
    }

    /**
     * Constructs a {@link ClaimImpl}.
     *
     * @param claim              the claim
     * @param service            the archetype service
     * @param insuranceRules     the insurance rules
     * @param partyRules         the party rules
     * @param patientRules       the patient rules
     * @param handlers           the document handlers
     * @param transactionManager the transaction manager
     */
    public ClaimImpl(IMObjectBean claim, IArchetypeRuleService service, InsuranceRules insuranceRules,
                     PartyRules partyRules, PatientRules patientRules, DocumentHandlers handlers,
                     PlatformTransactionManager transactionManager) {
        this(claim, (Act) claim.getObject(), service, insuranceRules, partyRules, patientRules, handlers,
             transactionManager);
    }

    /**
     * Constructs a {@link ClaimImpl}.
     *
     * @param claim              the claim
     * @param service            the archetype service
     * @param insuranceRules     the insurance rules
     * @param partyRules         the party rules
     * @param patientRules       the patient rules
     * @param handlers           the document handlers
     * @param transactionManager the transaction manager
     */
    private ClaimImpl(IMObjectBean claim, Act act, IArchetypeRuleService service, InsuranceRules insuranceRules,
                      PartyRules partyRules, PatientRules patientRules, DocumentHandlers handlers,
                      PlatformTransactionManager transactionManager) {
        this.claim = claim;
        this.act = act;
        this.service = service;
        this.insuranceRules = insuranceRules;
        this.partyRules = partyRules;
        this.patientRules = patientRules;
        this.handlers = handlers;
        this.transactionManager = transactionManager;
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
    public String getInsurerId() {
        ActIdentity identity = getIdentity();
        return identity != null ? identity.getIdentity() : null;
    }

    /**
     * Sets the claim identifier, issued by the insurer.
     * <p>
     * A claim can have a single identifier issued by an insurer. To avoid duplicates, each insurance service must
     * provide a unique archetype.
     *
     * @param archetype the identifier archetype. Must have an <em>actIdentity.insuranceClaim</em> prefix.
     * @param id        the claim identifier
     * @throws InsuranceException if the identifier cannot be set
     */
    @Override
    public void setInsurerId(String archetype, String id) {
        ActIdentity identity = getIdentity();
        if (identity == null) {
            identity = (ActIdentity) service.create(archetype);
            claim.addValue("insurerId", identity);
        } else if (!identity.isA(archetype)) {
            throw new InsuranceException(InsuranceMessages.differentClaimIdentifierArchetype(
                    identity.getArchetypeId().getShortName(), archetype));
        }
        identity.setIdentity(id);
        claim.save();
    }

    /**
     * Returns the date when the claim was created.
     *
     * @return the date
     */
    @Override
    public OffsetDateTime getCreated() {
        return DateRules.toOffsetDateTime(claim.getDate("startTime"));
    }

    /**
     * Returns the date when the claim was completed.
     * <p>
     * This represents the date when the claim was cancelled, settled, or declined.
     *
     * @return the date, or {@code null} if the claim hasn't been completed
     */
    @Override
    public OffsetDateTime getCompleted() {
        return DateRules.toOffsetDateTime(claim.getDate("endTime"));
    }

    /**
     * Returns the discount amount, including tax.
     *
     * @return the discount amount
     */
    @Override
    public BigDecimal getDiscount() {
        BigDecimal result = BigDecimal.ZERO;
        for (Condition condition : getConditions()) {
            result = result.add(condition.getDiscount());
        }
        return result;
    }

    /**
     * Returns the discount tax amount.
     *
     * @return the discount tax amount
     */
    @Override
    public BigDecimal getDiscountTax() {
        BigDecimal result = BigDecimal.ZERO;
        for (Condition condition : getConditions()) {
            result = result.add(condition.getDiscountTax());
        }
        return result;
    }

    /**
     * Returns the total amount being claimed, including tax.
     *
     * @return the total amount
     */
    @Override
    public BigDecimal getTotal() {
        return claim.getBigDecimal("amount", BigDecimal.ZERO);
    }

    /**
     * Returns the total tax amount.
     *
     * @return the tax amount
     */
    @Override
    public BigDecimal getTotalTax() {
        return claim.getBigDecimal("tax", BigDecimal.ZERO);
    }

    /**
     * Returns the animal that the claim applies to.
     *
     * @return the animal
     */
    @Override
    public Patient getAnimal() {
        return getPolicy().getAnimal();
    }

    /**
     * Returns the policy that a claim is being made on.
     *
     * @return the policy
     */
    @Override
    public Policy getPolicy() {
        if (policy == null) {
            policy = new PolicyImpl(claim.getTarget("policy", Act.class), service, partyRules, patientRules);
        }
        return policy;
    }

    /**
     * Changes the policy for a claim. This can be used if a policy was submitted with an incorrect insurer or policy
     * number.
     *
     * @param insurer      the insurer
     * @param policyNumber the policy number
     * @return the updated policy
     */
    @Override
    public Policy setPolicy(org.openvpms.component.model.party.Party insurer, String policyNumber) {
        if (policyNumber == null) {
            throw new IllegalArgumentException("Argument 'policyNumber' is null");
        }
        getPolicy();
        if (!policy.getInsurer().equals(insurer) || !ObjectUtils.equals(policy.getPolicyNumber(), policyNumber)) {
            Act act = insuranceRules.getPolicyForClaim(getCustomer(), getPatient(), insurer,
                                                       policyNumber, null, (FinancialAct) claim.getObject(),
                                                       policy.getAct());
            Relationship relationship = claim.setTarget("policy", act);
            act.addActRelationship((ActRelationship) relationship);
            claim.save(act);
            this.policy = new PolicyImpl(act, service, partyRules, patientRules);
        }

        return policy;
    }

    /**
     * Returns the customer.
     *
     * @return the customer
     */
    public Party getCustomer() {
        return ((PolicyImpl) getPolicy()).getCustomer();
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     */
    public Party getPatient() {
        return ((PolicyImpl) getPolicy()).getPatient();
    }

    /**
     * Returns the insurer.
     *
     * @return the insurer
     */
    public Party getInsurer() {
        return getPolicy().getInsurer();
    }


    /**
     * Returns the claim status.
     *
     * @return the claim status
     */
    @Override
    public Status getStatus() {
        return Status.valueOf(act.getStatus());
    }

    /**
     * Sets the claim status.
     *
     * @param status the claim status
     */
    @Override
    public void setStatus(Status status) {
        changeStatus(status);
        claim.save();
    }

    /**
     * Sets the claim status, along with any message from the insurer.
     *
     * @param status  the status
     * @param message the message. May be {@code null}
     */
    @Override
    public void setStatus(Status status, String message) {
        changeStatus(status);
        updateMessage(message);
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
     * Returns the attachments.
     *
     * @return the attachments
     */
    @Override
    public List<Attachment> getAttachments() {
        if (attachments == null) {
            attachments = collectAttachments();
        }
        return attachments;
    }

    /**
     * Returns the user handling the claim.
     *
     * @return the clinician
     */
    @Override
    public User getClinician() {
        return claim.getTarget("clinician", User.class);
    }

    /**
     * Returns the claim handler.
     *
     * @return the claim handler
     */
    @Override
    public ClaimHandler getClaimHandler() {
        if (handler == null) {
            final User user = claim.getTarget("user", User.class);
            if (user == null) {
                throw new IllegalStateException("Claim has no user");
            }
            Location location = getLocation();
            handler = new ClaimHandler() {
                @Override
                public String getName() {
                    return user.getName();
                }

                @Override
                public Phone getPhone() {
                    return location != null ? location.getPhone() : null;
                }

                @Override
                public Email getEmail() {
                    return location != null ? location.getEmail() : null;
                }
            };
        }
        return handler;
    }

    /**
     * Returns the location where the claim was created.
     *
     * @return the practice location
     */
    @Override
    public Location getLocation() {
        Party location = claim.getTarget("location", Party.class);
        return (location != null) ? new LocationImpl(location, service, partyRules) : null;
    }

    /**
     * Sets a message on the claim. This may be used by insurance service to convey to users the status of the claim,
     * or why a claim was declined.
     *
     * @param message the message. May be {@code null}
     */
    @Override
    public void setMessage(String message) {
        updateMessage(message);
        claim.save();
    }

    /**
     * Returns the message.
     *
     * @return the message. May be {@code null}
     */
    @Override
    public String getMessage() {
        return claim.getString("message");
    }

    /**
     * Determines if this claim can be cancelled.
     *
     * @return {@code true} if the claim is {@link Status#PENDING}, {@link Status#POSTED}, {@link Status#SUBMITTED}
     * or {@link Status#ACCEPTED}.
     */
    @Override
    public boolean canCancel() {
        Status status = getStatus();
        return (status == Status.PENDING || status == Status.POSTED || status == Status.SUBMITTED
                || Status.ACCEPTED == status);
    }

    /**
     * Finalises the claim prior to submission.
     * <p>
     * The claim can only be finalised if it has {@link Status#PENDING PENDING} status, and all attachments have
     * content, and no attachments have {@link Attachment.Status#ERROR ERROR} status.
     *
     * @throws InsuranceException if the claim cannot be finalised
     */
    @Override
    public void finalise() {
        Status status = getStatus();
        if (status != Status.PENDING) {
            Lookup lookup = claim.getLookup("status");
            String displayName = (lookup != null) ? lookup.getName() : status.name();
            throw new InsuranceException(InsuranceMessages.cannotFinaliseClaimWithStatus(displayName));
        }
        for (Attachment attachment : getAttachments()) {
            if (attachment.getStatus() == Attachment.Status.ERROR) {
                throw new InsuranceException(InsuranceMessages.cannotFinaliseClaimAttachmentError(
                        attachment.getFileName()));
            }
            if (!attachment.hasContent()) {
                throw new InsuranceException(InsuranceMessages.cannotFinaliseClaimNoAttachment(
                        attachment.getFileName()));
            }
        }
        try {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    finaliseClaim();
                }
            });
        } catch (InsuranceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InsuranceException(InsuranceMessages.failedToFinaliseClaim(exception.getMessage()), exception);
        }
    }

    /**
     * Returns the total amount that has been allocated towards this claim.
     * <p>
     * This is the sum of {@link FinancialAct#getAllocatedAmount() allocated amounts} of the invoices being claimed.
     * <p>
     * NOTE: this may be greater than the claim itself, as allocations are handled at the invoice level, not
     * the invoice item level.
     *
     * @return the allocated amount
     */
    public BigDecimal getAllocated() {
        BigDecimal result = BigDecimal.ZERO;
        for (FinancialAct invoice : getInvoices()) {
            result = result.add(invoice.getAllocatedAmount());
        }
        return result;
    }

    /**
     * Reloads the claim.
     *
     * @return the latest instance of the claim
     */
    public ClaimImpl reload() {
        Act object = (Act) service.get(act.getObjectReference());
        if (object == null) {
            throw new IllegalStateException("Cannot reload claim=" + act.getObjectReference()
                                            + ". It has been deleted");
        }
        return newInstance(object, service, insuranceRules, partyRules, patientRules, handlers, transactionManager);
    }

    /**
     * Returns all invoices associated with this claim.
     * <p>
     * NOTE: the results of this call are not cached.
     *
     * @return the invoices
     */
    @SuppressWarnings("unchecked")
    public List<FinancialAct> getInvoices() {
        Map<Reference, IMObject> invoices = new HashMap<>();
        IMObjectCache cache = new MapIMObjectCache(invoices, service);
        for (Condition condition : getConditions()) {
            for (Invoice invoice : condition.getInvoices()) {
                IMObjectReference reference = new IMObjectReference(CustomerAccountArchetypes.INVOICE, invoice.getId());
                if (cache.get(reference) == null) {
                    throw new IllegalStateException("Failed to retrieve invoice=" + reference
                                                    + " associated with claim=" + getId());
                }
            }
        }
        return new ArrayList(invoices.values());
    }

    /**
     * Finalises the claim.
     */
    protected void finaliseClaim() {
        setStatus(Status.POSTED);
        for (Attachment attachment : getAttachments()) {
            if (attachment.getStatus() == Attachment.Status.PENDING) {
                attachment.setStatus(Attachment.Status.POSTED);
            }
        }
    }

    /**
     * Creates a new instance of the claim.
     *
     * @param claim              the claim
     * @param service            the archetype service
     * @param insuranceRules     the insurance rules
     * @param partyRules         the party rules
     * @param patientRules       the patient rules
     * @param handlers           the document handlers
     * @param transactionManager the transaction manager
     * @return a new instance
     */
    protected ClaimImpl newInstance(Act claim, IArchetypeRuleService service, InsuranceRules insuranceRules,
                                    PartyRules partyRules, PatientRules patientRules, DocumentHandlers handlers,
                                    PlatformTransactionManager transactionManager) {
        return new ClaimImpl(claim, service, insuranceRules, partyRules, patientRules, handlers, transactionManager);
    }

    /**
     * Returns the claim.
     *
     * @return the claim
     */
    protected IMObjectBean getClaim() {
        return claim;
    }

    /**
     * Collects the list of claim conditions.
     *
     * @return the claim conditions
     */
    protected List<Condition> collectConditions() {
        List<Condition> result = new ArrayList<>();
        Party patient = getPatient();
        for (Act act : claim.getTargets("items", Act.class)) {
            result.add(new ConditionImpl(act, patient, service));
        }
        return result;
    }

    /**
     * Collects the clinical history up to the time of the claim.
     *
     * @return the clinical history
     */
    protected List<Note> collectHistory() {
        Party patient = getPatient();
        return new NotesQuery(service).query(patient, null, act.getActivityStartTime());
    }

    /**
     * Collects attachments.
     *
     * @return the attachments
     */
    protected List<Attachment> collectAttachments() {
        List<Attachment> result = new ArrayList<>();
        for (DocumentAct act : claim.getTargets("attachments", DocumentAct.class)) {
            result.add(new AttachmentImpl(act, service, handlers));
        }
        return result;
    }

    /**
     * Returns the claim identity, as specified by the insurance provider.
     *
     * @return the claim identity, or {@code null} if none is registered
     */
    protected ActIdentity getIdentity() {
        return claim.getObject("insurerId", ActIdentity.class);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeRuleService getService() {
        return service;
    }

    /**
     * Returns the insurance rules.
     *
     * @return the insurance rules
     */
    protected InsuranceRules getInsuranceRules() {
        return insuranceRules;
    }

    /**
     * Changes the claim status. If the status represents a terminal state (CANCELLED, ACCEPTED, DECLINED), the
     * end date will be updated with the current time, otherwise it will be cleared.
     *
     * @param status the status
     */
    private void changeStatus(Status status) {
        Status current = getStatus();
        if (current == Status.CANCELLED || current == Status.SETTLED || current == Status.DECLINED) {
            throw new IllegalStateException("Cannot update claim status when it is " + current);
        }
        act.setStatus(status.name());
        if (status == Status.CANCELLED || status == Status.SETTLED || status == Status.DECLINED) {
            claim.setValue("endTime", new Date());
        } else {
            claim.setValue("endTime", null);
        }
    }

    /**
     * Updates the claim message, truncating it if it is too long.
     *
     * @param message the message. May be {@code null}
     */
    private void updateMessage(String message) {
        if (!StringUtils.isEmpty(message)) {
            message = StringUtils.abbreviate(message, claim.getMaxLength("message"));
        }
        claim.setValue("message", message);
    }

}
