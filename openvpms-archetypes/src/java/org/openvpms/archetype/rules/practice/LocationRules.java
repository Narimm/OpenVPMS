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

package org.openvpms.archetype.rules.practice;

import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Rules for <em>party.organisationLocation</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LocationRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>LocationRules</tt>.
     */
    public LocationRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>LocationRules</tt>.
     *
     * @param service the archetype service
     */
    public LocationRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the default deposit account associated with a location.
     *
     * @param location the location
     * @return the default deposit account or <tt>null</tt> if none is found
     */
    public Party getDefaultDepositAccount(Party location) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(
                location, "depositAccounts", service);
    }

    /**
     * Returns the default till associated with a location.
     *
     * @param location the location
     * @return the default till or <tt>null</tt> if none is found
     */
    public Party getDefaultTill(Party location) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(
                location, "tills", service);
    }

    /**
     * Returns the default schedule associated with a location.
     *
     * @param location the location
     * @return the default schedule or <tt>null</tt> if none is found
     */
    public Party getDefaultSchedule(Party location) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(
                location, "schedules", service);
    }

    /**
     * Returns the default schedule associated with a location.
     *
     * @param location the location
     * @return the default schedule or <tt>null</tt> if none is found
     */
    public Party getDefaultWorkList(Party location) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(
                location, "workLists", service);
    }

}
