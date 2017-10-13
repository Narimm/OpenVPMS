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
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.insurance.claim.Attachment;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.ClaimHandler;
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
     * Sets the claim identifier, issued by the insurer.
     * <p>
     * A claim can have a single identifier issued by an insurer. To avoid duplicates, each insurance service must
     * provide a unique archetype.
     *
     * @param archetype the identifier archetype. Must have an <em>actIdentity.insuranceClaim</em> prefix.
     * @param id        the claim identifier
     */
    @Override
    public void setClaimId(String archetype, String id) {
        ActIdentity identity = getIdentity();
        if (identity == null) {
            identity = (ActIdentity) service.create(archetype);
            claim.addValue("insuranceId", identity);
        } else if (!TypeHelper.isA(identity, archetype)) {
            throw new IllegalArgumentException(
                    "Argument 'archetype' must be of the same type as the existing identifier");
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
        return (User) claim.getNodeParticipant("clinician");
    }

    /**
     * Returns the claim handler.
     *
     * @return the claim handler
     */
    @Override
    public ClaimHandler getClaimHandler() {
        if (handler == null) {
            final User user = (User) claim.getNodeParticipant("user");
            final Party location = (Party) claim.getNodeParticipant("location");
            if (user == null) {
                throw new IllegalStateException("Claim has no user");
            }
            handler = new ClaimHandler() {
                @Override
                public String getName() {
                    return user.getName();
                }

                @Override
                public Contact getPhone() {
                    return (location != null) ? customerRules.getTelephoneContact(location) : null;
                }

                @Override
                public Contact getEmail() {
                    return (location != null) ? customerRules.getEmailContact(location) : null;
                }
            };
        }
        return handler;
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
        query.add(join("event", "e").add(
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

    protected List<Attachment> collectAttachments() {
        List<Attachment> result = new ArrayList<>();
        return result;
    }

    /**
     * Returns the claim identity, as specified by the insurance provider.
     *
     * @return the claim identity, or {@code null} if none is registered
     */
    protected ActIdentity getIdentity() {
        return claim.getValue("insuranceId", PredicateUtils.truePredicate(), ActIdentity.class);
    }

}
