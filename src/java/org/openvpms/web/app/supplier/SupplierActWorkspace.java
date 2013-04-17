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
 */

package org.openvpms.web.app.supplier;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.BrowserCRUDWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.Archetypes;


/**
 * Supplier act workspace.
 *
 * @author Tim Anderson
 */
public abstract class SupplierActWorkspace<T extends Act>
        extends BrowserCRUDWorkspace<Party, T> {

    /**
     * Constructs a {@code SupplierActWorkspace}.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identifier
     * @param context     the context
     */
    public SupplierActWorkspace(String subsystemId, String workspaceId, Context context) {
        this(subsystemId, workspaceId, null, context);
    }

    /**
     * Constructs a {@code SupplierActWorkspace}.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identifier
     * @param archetypes  the archetype short names that this operates on
     * @param context     the context
     */
    public SupplierActWorkspace(String subsystemId, String workspaceId, Archetypes<T> archetypes, Context context) {
        super(subsystemId, workspaceId, null, archetypes, context);
        setArchetypes(Party.class, "party.supplier*");
        setMailContext(new SupplierMailContext(context, getHelpContext()));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        getContext().setSupplier(object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or {@code null} if there is no summary
     */
    @Override
    public Component getSummary() {
        return SupplierSummary.getSummary(getObject());
    }

    /**
     * Returns the latest version of the current supplier context object.
     *
     * @return the latest version of the context object, or {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return getLatest(getContext().getSupplier());
    }

}
