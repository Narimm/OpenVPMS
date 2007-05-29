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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.IMObjectTableBrowserFactory;


/**
 * Customer act workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class CustomerActWorkspace<T extends Act>
        extends ActWorkspace<Party, T> {

    /**
     * Constructs a new <tt>CustomerActWorkspace</tt>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public CustomerActWorkspace(String subsystemId, String workspaceId) {
        this(subsystemId, workspaceId, new ShortNameList("party.customer*"));
    }

    /**
     * Constructs a new <tt>CustomerActWorkspace</tt>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     * @param shortNames  the archetype short names that this operates on
     */
    public CustomerActWorkspace(String subsystemId, String workspaceId,
                                ShortNames shortNames) {
        super(subsystemId, workspaceId, shortNames);
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
        layoutWorkspace(object);
        initQuery(object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <tt>null</tt>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof Party) {
            setObject((Party) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + Party.class.getName());
        }
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <tt>null</tt> if there is no summary
     */
    @Override
    public Component getSummary() {
        return CustomerSummary.getSummary(getObject());
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

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Party latest = getLatest();
        if (latest != getObject()) {
            setObject(latest);
        }
    }

    /**
     * Creates a new browser to query and display acts.
     * Default sort order is by descending starttime.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<T> createBrowser(ActQuery<T> query) {
        SortConstraint[] sort = {new NodeSortConstraint("startTime", false)};
        return IMObjectTableBrowserFactory.create(query, sort,
                                                  createTableModel());
    }


}
