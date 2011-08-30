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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.app;

import nextapp.echo2.app.ApplicationInstance;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import static org.openvpms.web.component.im.relationship.RelationshipHelper.getDefaultTarget;
import org.openvpms.web.system.SpringApplicationInstance;

import java.util.List;


/**
 * An <code>ApplicationInstance</code> associated with a {@link GlobalContext}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ContextApplicationInstance
        extends SpringApplicationInstance {

    /**
     * Application context.
     */
    private GlobalContext context = new GlobalContext();


    /**
     * Constructs a new <code>ContextApplicationInstance</code>.
     */
    public ContextApplicationInstance() {
        initUser();
        initPractice();
        initLocation();
    }

    /**
     * Returns the instance associated with the current thread.
     *
     * @return the current instance, or <code>null</code>
     */
    public static ContextApplicationInstance getInstance() {
        return (ContextApplicationInstance) ApplicationInstance.getActive();
    }

    /**
     * Returns the current context.
     *
     * @return the current context
     */
    public GlobalContext getContext() {
        return context;
    }

    /**
     * Switches the current workspace to display an object.
     *
     * @param object the object to view
     */
    public abstract void switchTo(IMObject object);

    /**
     * Clears the current context.
     */
    protected void clearContext() {
        context = new GlobalContext();
    }

    /**
     * Initialises the user.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void initUser() {
        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            UserRules rules = new UserRules();
            User user = rules.getUser(auth.getName());
            if (user != null) {
                context.setUser(user);
                if (rules.isClinician(user)) {
                    context.setClinician(user);
                }
            }
        }
    }

    /**
     * Initialises the practice.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void initPractice() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        // First get the Practice.  Should only be one but get first if more.
        List<IMObject> rows = ArchetypeQueryHelper.get(
                service, "party", "organisationPractice", null, true,
                0, 1).getResults();
        if (!rows.isEmpty()) {
            Party practice = (Party) rows.get(0);
            context.setPractice(practice);
        }
    }

    /**
     * Initialises the location specific information.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void initLocation() {
        // Get the current Practice
        Party practice = context.getPractice();

        // Get the current user.
        User user = context.getUser();

        // If pactice and/or user not set then exit.
        if (practice == null || user == null) {
            return;
        }

        // Now get the default location for the user or the first location if
        // no default.
        Party location = (Party) getDefaultTarget(user, "locations");

        // If no locations defined for user find default location for Practice
        // or the first location if no default.
        if (location == null) {
            location = (Party) getDefaultTarget(practice, "locations");
        }

        // If no location then return
        if (location == null) {
            return;
        }
        context.setLocation(location);

        // Now get the default Deposit object.
        context.setDeposit((Party) getDefaultTarget(location,
                                                    "depositAccounts"));

        // Now get the default Till object.
        context.setTill((Party) getDefaultTarget(location, "tills"));

        // Now get the default Schedule object.
        context.setSchedule((Party) getDefaultTarget(location, "schedules"));

        // Now get the default WorkList object.
        context.setWorkList((Party) getDefaultTarget(location, "workLists"));
    }

}
