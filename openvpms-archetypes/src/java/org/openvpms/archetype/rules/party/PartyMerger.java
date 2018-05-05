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

package org.openvpms.archetype.rules.party;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DefaultIMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.PeriodRelationship;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Merges two parties.
 *
 * @author Tim Anderson
 */
public abstract class PartyMerger {

    /**
     * The short name of the type of parties that may be merged.
     */
    private final String type;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The {@link EntityRelationship} copier.
     */
    private final IMObjectCopier relationshipCopier;

    /**
     * The {@link Contact} copier.
     */
    private final IMObjectCopier contactCopier;


    /**
     * Constructs a {@link PartyMerger}.
     *
     * @param type    the short name of the type of parties that may be merged
     * @param service the archetype service
     */
    public PartyMerger(String type, IArchetypeService service) {
        this.type = type;
        this.service = service;
        relationshipCopier = new IMObjectCopier(new IMObjectRelationshipCopyHandler(), service);
        contactCopier = new IMObjectCopier(new ContactCopyHandler(), service);
    }

    /**
     * Merges one {@link Party} with another.
     * <p>
     * On completion, the 'to' party will contain all of the 'from' party's contacts, identities, classifications and
     * relationships, any act participations will be changed to reference the 'to'
     * party. The 'from' party will be deleted.
     * <p>
     * This operation should be invoked within a transaction.
     *
     * @param from the party to merge from
     * @param to   the party to merge to
     */
    public void merge(Party from, Party to) {
        if (from.getObjectReference().equals(to.getObjectReference())) {
            throw new MergeException(MergeException.ErrorCode.CannotMergeToSameObject, getDisplayName());
        }
        checkParty(from);
        checkParty(to);

        Set<IMObject> merged = new LinkedHashSet<>();
        merge(from, to, merged);
        service.save(merged);
        service.remove(from);
    }

    /**
     * Merges one {@link Party} with another.
     * <p>
     * On completion, the 'to' party will contain all of the 'from' party's contacts, identities, classifications and
     * relationships, any act participations will be changed to reference the 'to'
     * party.
     *
     * @param from   the party to merge from
     * @param to     the party to merge to
     * @param merged the set of changed objects
     */
    protected void merge(Party from, Party to, Set<IMObject> merged) {
        copyContacts(from, to);
        copyClassifications(from, to);
        copyEntityRelationships(from, to);
        copyEntityLinks(from, to);
        copyIdentities(from, to);

        List<IMObject> participations = moveParticipations(from, to);

        merged.add(to);
        merged.addAll(participations);

        for (Relationship relationship : from.getEntityRelationships()) {
            Entity other = getRelated(from, to, (EntityRelationship) relationship);
            if (other != null) {
                other.removeEntityRelationship((EntityRelationship) relationship);
                merged.add(other);
            }
        }
    }

    /**
     * Copies contacts from one party to another.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     */
    protected void copyContacts(Party from, Party to) {
        Contact[] contacts = from.getContacts().toArray(new Contact[from.getContacts().size()]);
        for (Contact contact : contacts) {
            List<IMObject> objects = contactCopier.apply(contact);
            Contact copy = (Contact) objects.get(0);
            to.addContact(copy);
        }
    }

    /**
     * Copies classifications from one party to another.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     */
    protected void copyClassifications(Party from, Party to) {
        for (Lookup lookup : from.getClassifications()) {
            to.addClassification(lookup);
        }
    }

    /**
     * Copies entity relationships from one party to another,
     * excluding any relationships which would duplicate an existing
     * relationship in the 'to' party.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void copyEntityRelationships(Party from, Party to) {
        for (Relationship relationship : from.getEntityRelationships()) {
            EntityRelationship copy = copyEntityRelationship((EntityRelationship) relationship, from, to);
            if (!exists(copy, to.getEntityRelationships())) {
                to.addEntityRelationship(copy);
            }
        }
    }

    /**
     * Copies entity links from one party to another, excluding any link which would duplicate an existing
     * relationship in the 'to' party.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void copyEntityLinks(Party from, Party to) {
        for (Relationship relationship : from.getEntityLinks()) {
            EntityLink copy = copyEntityLink((EntityLink) relationship, from, to);
            if (!exists(copy, to.getEntityLinks())) {
                to.addEntityLink(copy);
            }
        }
    }

    /**
     * Copies entity relationships from one party to another.
     * If the source of the relationship refers to the 'from' party, it
     * will be replaced with the 'to' party; otherwise the target of the
     * relationship will be replaced with the 'to' party.
     *
     * @param relationship the relationship to copy
     * @param from         the party to copy from
     * @param to           the party to copy to
     * @return the copied relationship
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected EntityRelationship copyEntityRelationship(EntityRelationship relationship, Party from, Party to) {
        IMObjectReference fromRef = from.getObjectReference();
        IMObjectReference toRef = to.getObjectReference();

        List<IMObject> objects = relationshipCopier.apply(relationship);
        EntityRelationship copy = (EntityRelationship) objects.get(0);
        if (ObjectUtils.equals(copy.getSource(), fromRef)) {
            copy.setSource(toRef);
        } else {
            copy.setTarget(toRef);
        }
        return copy;
    }

    /**
     * Copies entity links from one party to another.
     * If the source of the link refers to the 'from' party, it will be replaced with the 'to' party; otherwise the
     * target of the relationship will be replaced with the 'to' party.
     *
     * @param relationship the relationship to copy
     * @param from         the party to copy from
     * @param to           the party to copy to
     * @return the copied relationship
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected EntityLink copyEntityLink(EntityLink relationship, Party from, Party to) {
        IMObjectReference fromRef = from.getObjectReference();
        IMObjectReference toRef = to.getObjectReference();

        List<IMObject> objects = relationshipCopier.apply(relationship);
        EntityLink copy = (EntityLink) objects.get(0);
        if (ObjectUtils.equals(copy.getSource(), fromRef)) {
            copy.setSource(toRef);
        } else {
            copy.setTarget(toRef);
        }
        return copy;
    }

    /**
     * Copies identities from one party to another.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     */
    protected void copyIdentities(Party from, Party to) {
        IMObjectCopier copier = new IMObjectCopier(
                new DefaultIMObjectCopyHandler(), service);
        for (org.openvpms.component.model.entity.EntityIdentity identity : from.getIdentities()) {
            List<IMObject> objects = copier.apply((EntityIdentity) identity);
            EntityIdentity copy = (EntityIdentity) objects.get(0);
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
    protected List<IMObject> moveParticipations(Party from, Party to) {
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

    /**
     * Verifies a party is of the correct type for merging.
     *
     * @param party the party
     * @throws MergeException if the party is invalid
     */
    protected void checkParty(Party party) {
        if (!TypeHelper.isA(party, type)) {
            throw new MergeException(MergeException.ErrorCode.InvalidType,
                                     DescriptorHelper.getDisplayName(party,
                                                                     service));
        }
    }

    /**
     * Returns a display name for the parties which may be merged,
     * for error reporting purposes.
     *
     * @return the display name
     */
    protected String getDisplayName() {
        return DescriptorHelper.getDisplayName(type, service);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Determines if a party already has an entity relationship.
     *
     * @param relationship  the relationship
     * @param relationships the relationships to check against
     * @return {@code true} if a relationship exists that has the same archetype id, source, and target, and is
     * still active; otherwise {@code false}
     */
    protected boolean exists(PeriodRelationship relationship, Set<? extends PeriodRelationship> relationships) {
        boolean result = false;
        String archetype = relationship.getArchetype();
        for (PeriodRelationship r : relationships) {
            if (ObjectUtils.equals(r.getSource(), relationship.getSource())
                && ObjectUtils.equals(r.getTarget(), relationship.getTarget())
                && r.getArchetype().equals(archetype)
                && r.getActiveEndTime() == null
                && relationship.getActiveEndTime() == null) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the related entity in a relationship.
     * This is the source or target of the relationship that isn't the same as {@code from}.
     *
     * @param from         the merge from party
     * @param to           the merge to party
     * @param relationship the relationship
     * @return the related entity
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getRelated(Party from, Party to, EntityRelationship relationship) {
        IMObjectReference source = relationship.getSource();
        if (from.getObjectReference().equals(source)) {
            IMObjectReference target = relationship.getTarget();
            if (to.getObjectReference().equals(target)) {
                return to;
            }
            return getEntity(target);
        } else {
            if (to.getObjectReference().equals(source)) {
                return to;
            }
            return getEntity(source);
        }
    }

    /**
     * Returns the entity with the specified reference.
     *
     * @param reference the reference. May be {@code null}
     * @return the corresponding entity, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getEntity(IMObjectReference reference) {
        return (reference != null) ? (Entity) service.get(reference) : null;
    }
}
