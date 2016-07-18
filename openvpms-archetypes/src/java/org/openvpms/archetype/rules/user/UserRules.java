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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.user;

import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.Iterator;
import java.util.List;


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
    public boolean isClinician(User user) {
        if (TypeHelper.isA(user, UserArchetypes.USER)) {
            for (Lookup lookup : user.getClassifications()) {
                if (TypeHelper.isA(lookup, "lookup.userType")
                    && "CLINICIAN".equals(lookup.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if a user has administrator privileges.
     * TODO - needs to be updated for OVPMS-702.
     *
     * @param user the user to check
     * @return {@code true} if the user is an administrator
     */
    public boolean isAdministrator(User user) {
        if (TypeHelper.isA(user, UserArchetypes.USER)) {
            if ("admin".equals(user.getUsername())) {
                return true;
            } else {
                for (Lookup lookup : user.getClassifications()) {
                    if (TypeHelper.isA(lookup, "lookup.userType")
                        && "ADMINISTRATOR".equals(lookup.getCode())) {
                        return true;
                    }
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
        IMObjectBean bean = new IMObjectBean(user, service);
        return bean.getNodeTargetObjects("locations", Party.class);
    }

    /**
     * Returns the locations applicable to a user.
     * <p/>
     * These are the locations linked to the user, or if none, those linked to the practice.
     *
     * @param user     the user
     * @param practice the practice
     * @return the locations
     */
    public List<Party> getLocations(User user, Party practice) {
        List<Party> locations = getLocations(user);
        if (locations.isEmpty()) {
            IMObjectBean bean = new IMObjectBean(practice, service);
            locations = bean.getNodeTargetObjects("locations", Party.class);
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
     * Returns the follow-up work lists associated with a user.
     *
     * @param user the user
     * @return the follow-up work lists, in the order they are defined
     */
    public List<Entity> getFollowupWorkLists(User user) {
        return new EntityBean(user, service).getNodeTargetEntities("followupWorkLists", SequenceComparator.INSTANCE);
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
