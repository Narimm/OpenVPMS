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

package org.openvpms.web.app.customer;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.BrowserCRUDWorkspace;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.util.Archetypes;


/**
 * Customer act workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class CustomerActWorkspace<T extends Act>
        extends BrowserCRUDWorkspace<Party, T> {

    /**
     * Constructs a new <tt>CustomerActWorkspace</tt>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public CustomerActWorkspace(String subsystemId, String workspaceId) {
        this(subsystemId, workspaceId, null);
    }


    /**
     * Constructs a new <tt>CustomerActWorkspace</tt>.
     *
     * @param subsystemId   the subsystem localisation identifier
     * @param workspaceId   the workspace localisation identfifier
     * @param actArchetypes the act archetypes that this operates on
     */
    public CustomerActWorkspace(String subsystemId, String workspaceId,
                                Archetypes<T> actArchetypes) {
        super(subsystemId, workspaceId, null, actArchetypes);
        setArchetypes(Party.class, "party.customer*");
    }

    /**
     * Constructs a new <tt>CustomerActWorkspace</tt>.
     *
     * @param subsystemId     the subsystem localisation identifier
     * @param workspaceId     the workspace localisation identfifier
     * @param partyArchetypes the party archetypes that this operates on
     * @param actArchetypes   the act archetypes that this operates on
     */
    public CustomerActWorkspace(String subsystemId, String workspaceId,
                                Archetypes<Party> partyArchetypes,
                                Archetypes<T> actArchetypes) {
        super(subsystemId, workspaceId, partyArchetypes, actArchetypes);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        ContextHelper.setCustomer(object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <tt>null</tt> if there is no summary
     */
    @Override
    public Component getSummary() {
        CustomerSummary summarizer = new CustomerSummary();
        return summarizer.getSummary(getObject());
    }

    /**
     * Returns the latest version of the current customer context object.
     *
     * @return the latest version of the customer context object, or
     *         {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return getLatest(GlobalContext.getInstance().getCustomer());
    }


}
