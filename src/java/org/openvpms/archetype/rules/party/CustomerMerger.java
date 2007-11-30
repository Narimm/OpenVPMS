/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.party;

import static org.openvpms.archetype.rules.party.MergeException.ErrorCode.CannotMergeToSameParty;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DefaultIMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;

import java.util.ArrayList;
import java.util.List;


/**
 * Merges two <em>party.customerperson</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class CustomerMerger {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Customer rules.
     */
    private final CustomerRules rules;


    /**
     * Creates a new <tt>CustomerMerger</tt>.
     */
    public CustomerMerger() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>CustomerMerger</tt>.
     *
     * @param service the archetype service
     */
    public CustomerMerger(IArchetypeService service) {
        this.service = service;
        rules = new CustomerRules(service);
    }

    /**
     * Merges one <em>party.customerperson</em> with another.
     * One completion, the 'to' customer will contain all of the 'from'
     * customer's contacts, identities, classifications and relationships,
     * and any act participations will be changed to reference the 'to'
     * customer. The 'from' customer will be deleted.
     * If both the 'from' and 'to' customer's have
     * <em>lookup.customerAccountType</em> classifications, the 'to' customer's
     * classification will take precedence.
     *
     * @param from the customer to merge from
     * @param to   the customer to merge to
     */
    public void merge(Party from, Party to) {
        if (from.getObjectReference().equals(to.getObjectReference())) {
            throw new MergeException(CannotMergeToSameParty);
        }
        if (!TypeHelper.isA(from, "party.customerperson")) {
            throw new MergeException(MergeException.ErrorCode.InvalidPartyType,
                                     DescriptorHelper.getDisplayName(from,
                                                                     service));
        }
        if (!TypeHelper.isA(to, "party.customerperson")) {
            throw new MergeException(MergeException.ErrorCode.InvalidPartyType,
                                     DescriptorHelper.getDisplayName(to,
                                                                     service));
        }

        copyContacts(from, to);
        copyClassifications(from, to);
        copyEntityRelationships(from, to);
        copyIdentities(from, to);

        List<IMObject> participations = moveParticipations(from, to);

        List<IMObject> merged = new ArrayList<IMObject>();
        merged.add(to);
        merged.addAll(participations);
        service.save(merged);
        service.remove(from);
    }

    /**
     * Copies contacts from one party to another.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     */
    private void copyContacts(Party from, Party to) {
        Contact[] contacts = from.getContacts().toArray(new Contact[0]);
        IMObjectCopier contactCopier = new IMObjectCopier(
                new ContactCopyHandler(), service);
        for (Contact contact : contacts) {
            Contact copy = (Contact) contactCopier.copy(contact);
            to.addContact(copy);
        }
    }

    /**
     * Copies classifications from one party to another.
     * This ensures that only one <em>lookup.customerAccountType</em> appears in
     * the 'to' party, to avoid cardinality violations. If both parties have a
     * <em>lookup.customerAccountType</em>, the 'to' party's type takes
     * precedence.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     */
    private void copyClassifications(Party from, Party to) {
        for (Lookup lookup : from.getClassifications()) {
            if (!TypeHelper.isA(lookup, "lookup.customerAccountType")) {
                to.addClassification(lookup);
            }
        }
        Lookup accountType = rules.getAccountType(to);
        if (accountType == null) {
            accountType = rules.getAccountType(from);
            if (accountType != null) {
                to.addClassification(accountType);
            }
        }
    }

    /**
     * Copies entity relationships from one party to another.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     */
    private void copyEntityRelationships(Party from, Party to) {
        IMObjectReference fromRef = from.getObjectReference();
        IMObjectReference toRef = to.getObjectReference();
        IMObjectCopier copier = new IMObjectCopier(
                new EntityRelationshipCopyHandler(), service);

        for (EntityRelationship relationship : from.getEntityRelationships()) {
            EntityRelationship copy
                    = (EntityRelationship) copier.copy(relationship);
            if (copy.getSource().equals(fromRef)) {
                copy.setSource(toRef);
            } else {
                copy.setTarget(toRef);
            }
            to.addEntityRelationship(copy);
        }
    }

    /**
     * Copies identities from one party to another.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     */
    private void copyIdentities(Party from, Party to) {
        IMObjectCopier copier = new IMObjectCopier(
                new DefaultIMObjectCopyHandler(), service);
        for (EntityIdentity identity : from.getIdentities()) {
            EntityIdentity copy = (EntityIdentity) copier.copy(identity);
            to.addIdentity(copy);
        }
    }

    /**
     * Moves act participations from one party to another.
     * This loads all participations referencing the 'from' party,
     * and assigns them to the 'to' party.
     *
     * @param from the party to move from
     * @param to   the party to move to
     * @return the moved participations
     */
    private List<IMObject> moveParticipations(Party from, Party to) {
        IMObjectReference fromRef = from.getObjectReference();
        IMObjectReference toRef = to.getObjectReference();

        ArchetypeQuery query
                = new ArchetypeQuery("participation.*", true, false);
        query.add(new ObjectRefNodeConstraint("entity", fromRef));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> participations = service.get(query).getResults();
        for (IMObject object : participations) {
            Participation participation = (Participation) object;
            participation.setEntity(toRef);
        }
        return participations;
    }

}
