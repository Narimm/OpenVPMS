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

package org.openvpms.archetype.rules.user;

import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.scheduler.JobScheduler;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.query.TypedQuery;
import org.openvpms.component.query.criteria.CriteriaBuilder;
import org.openvpms.component.query.criteria.CriteriaQuery;
import org.openvpms.component.query.criteria.Root;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * User rules.
 *
 * @author Tim Anderson
 */
public class UserRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a {@link UserRules}.
     *
     * @param service the archetype service
     */
    public UserRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the user with the specified username (login name).
     *
     * @param username the user name
     * @return the corresponding user, or {@code null} if none is found
     */
    public User getUser(String username) {
        ArchetypeQuery query = new ArchetypeQuery(UserArchetypes.USER_ARCHETYPES, true, true);
        query.add(new NodeConstraint("username", username));
        query.setMaxResults(1);
        Iterator<User> iterator = new IMObjectQueryIterator<>(service, query);
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * Returns the user associated with an authentication request.
     *
     * @param authentication the authentication
     * @return the user, or {@code null} if one cannot be determined
     */
    public User getUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        User user = null;
        if (principal instanceof User) {
            user = (User) principal;
        } else if (principal instanceof UserDetails) {
            String name = ((UserDetails) principal).getUsername();
            user = getUser(name);
        }
        return user;
    }

    /**
     * Determines if a user exists with the specified user name.
     *
     * @param username the user name
     * @return {@code true} if a user exists, otherwise {@code false}
     */
    public boolean exists(String username) {
        return checkExists(username, null);
    }

    /**
     * Determines if another user exists with the specified user name.
     *
     * @param username the user name
     * @param user     the user to exclude
     * @return {@code true} if another user exists, otherwise {@code false}
     */
    public boolean exists(String username, User user) {
        return checkExists(username, user);
    }

    /**
     * Determines if a user is a clinician.
     *
     * @param user the user
     * @return {@code true} if the user is a clinician,
     * otherwise {@code false}
     */
    public boolean isClinician(org.openvpms.component.model.user.User user) {
        return isA(user, UserArchetypes.CLINICIAN_USER_TYPE);
    }

    /**
     * Determines if a user has administrator privileges.
     * TODO - needs to be updated for OVPMS-702.
     *
     * @param user the user to check
     * @return {@code true} if the user is an administrator
     */
    public boolean isAdministrator(org.openvpms.component.model.user.User user) {
        boolean result = isA(user, UserArchetypes.ADMINISTRATOR_USER_TYPE);
        if (!result) {
            result = user.isA(UserArchetypes.USER) && "admin".equals(user.getUsername());
        }
        return result;
    }

    /**
     * Determines if a user has a particular user type.
     *
     * @param user     the user
     * @param userType the user type code
     * @return {@code true} if the user has the user type
     */
    public boolean isA(org.openvpms.component.model.user.User user, String userType) {
        if (user.isA(UserArchetypes.USER)) {
            for (Lookup lookup : user.getClassifications()) {
                if (lookup.isA("lookup.userType") && userType.equals(lookup.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the locations associated with a user.
     *
     * @param user the user
     * @return the locations associated with the user
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    public List<Party> getLocations(User user) {
        IMObjectBean bean = service.getBean(user);
        return bean.getTargets("locations", Party.class);
    }

    /**
     * Returns the locations applicable to a user.
     * <p/>
     * These are the locations linked to the user, or if none, those linked to the practice.
     * <p/>
     * Any locations linked to the user but not the practice will be excluded.
     *
     * @param user     the user
     * @param practice the practice
     * @return the locations
     */
    public List<Party> getLocations(User user, Party practice) {
        List<Party> locations = getLocations(user);
        IMObjectBean bean = service.getBean(practice);
        if (locations.isEmpty()) {
            locations = bean.getTargets("locations", Party.class);
        } else {
            // exclude any locations not linked to the practice
            List<Reference> references = bean.getTargetRefs("locations");
            for (Party location : new ArrayList<>(locations)) {
                if (!references.contains(location.getObjectReference())) {
                    locations.remove(location);
                }
            }
        }
        return locations;
    }

    /**
     * Returns the default location associated with a user.
     *
     * @param user the user
     * @return the default location, or the first location if there is no
     * default location or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getDefaultLocation(User user) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(user, "locations", service);
    }

    /**
     * Returns all users in a list of users and groups.
     *
     * @param usersOrGroups the list of users and groups
     * @return the users in the list
     */
    public Set<User> getUsers(List<org.openvpms.component.model.entity.Entity> usersOrGroups) {
        Set<User> result = new HashSet<>();
        Set<org.openvpms.component.model.entity.Entity> groups = new HashSet<>();
        for (org.openvpms.component.model.entity.Entity entity : usersOrGroups) {
            if (entity instanceof User) {
                result.add((User) entity);
            } else if (entity != null) {
                if (!groups.contains(entity)) {
                    groups.add(entity);
                    List<User> users = getUsers(entity);
                    result.addAll(users);
                }
            }
        }
        return result;
    }

    /**
     * Returns all users in a group.
     *
     * @param group the <em>entity.userGroup</em>.
     * @return the users
     */
    public List<User> getUsers(org.openvpms.component.model.entity.Entity group) {
        IMObjectBean bean = service.getBean(group);
        return bean.getTargets("users", User.class);
    }

    /**
     * Returns the follow-up work lists associated with a user.
     *
     * @param user the user
     * @return the follow-up work lists, in the order they are defined
     */
    public List<Entity> getFollowupWorkLists(User user) {
        return new EntityBean(user, service).getNodeTargetEntities("followupWorkLists", SequenceComparator.INSTANCE);
    }

    /**
     * Returns all active users available at a practice location.
     * <p/>
     * These are the users that have a link to the location, or no links to any location.
     *
     * @param location the practice location
     * @return the active users available at the location
     */
    @SuppressWarnings("unchecked")
    public List<User> getClinicians(Party location) {
        IArchetypeQuery query = UserQueryFactory.createClinicianQuery(location, "id");
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List results = service.get(query).getResults();
        return (List<User>) results;
    }

    /**
     * Returns the first active <em>entity.job*</em> where a user is referenced by the <em>runAs</em> or
     * <em>notify</em> node.
     *
     * @param user the user
     * @return the first active job, or {@code null} if none is found
     */
    public org.openvpms.component.model.entity.Entity getJobUsedBy(org.openvpms.component.model.user.User user) {
        CriteriaBuilder builder = service.getCriteriaBuilder();
        CriteriaQuery<Entity> query = builder.createQuery(Entity.class);
        Root<Entity> job = query.from(Entity.class, JobScheduler.JOB_ARCHETYPE);
        query.select(job);
        Root<EntityRelationship> relationship = query.from(EntityRelationship.class, "entityRelationship.jobUser",
                                                           "entityRelationship.jobUserGroup");
        query.where(builder.equal(job.get("id"), relationship.get("source")),
                    builder.equal(relationship.get("target"), user.getId()),
                    builder.equal(job.get("active"), true));
        query.orderBy(builder.asc(job.get("id")));
        TypedQuery<Entity> typedQuery = service.createQuery(query);
        typedQuery.setMaxResults(1);
        List<Entity> list = typedQuery.getResultList();
        return (!list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * Determines if a user exists with the specified user name.
     *
     * @param username the user name
     * @param user     the user to exclude. May be {@code null}
     * @return {@code true} if a user exists, otherwise {@code false}
     */
    private boolean checkExists(String username, User user) {
        ArchetypeQuery query = new ArchetypeQuery(UserArchetypes.USER_ARCHETYPES, true, false);
        query.add(new NodeSelectConstraint("id"));
        query.add(new NodeConstraint("username", username));
        if (user != null && !user.isNew()) {
            query.add(Constraints.ne("id", user.getId()));
        }
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        return iterator.hasNext();
    }

}
