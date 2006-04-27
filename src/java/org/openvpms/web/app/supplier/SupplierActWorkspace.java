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
 *  $Id: SupplierActWorkspace.java 748 2006-04-11 04:09:07Z tanderson $
 */

package org.openvpms.web.app.supplier;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.component.app.Context;


/**
 * Act workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-11 14:09:07 +1000 (Tue, 11 Apr 2006) $
 */
public abstract class SupplierActWorkspace extends ActWorkspace {

    /**
     * Construct a new <code>SupplierActWorkspace</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public SupplierActWorkspace(String subsystemId, String workspaceId,
                                String refModelName, String entityName,
                                String conceptName) {
        super(subsystemId, workspaceId, refModelName, entityName, conceptName);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    @Override
    public Component getSummary() {
        return SupplierSummary.getSummary((Party) getObject());
    }

    /**
     * Determines if the workspace should be refreshed. This implementation
     * returns true if the current supplier has changed.
     *
     * @return <code>true</code> if the workspace should be refreshed, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        Party supplier = Context.getInstance().getSupplier();
        return (supplier != getObject());
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Party supplier = Context.getInstance().getSupplier();
        setObject(supplier);
        if (supplier != null) {
            layoutWorkspace(supplier, container);
            initQuery(supplier);
        }
    }

    /**
     * Invoked when a supplier is selected.
     *
     * @param supplier the selected supplier
     */
    @Override
    protected void onSelected(IMObject supplier) {
        super.onSelected(supplier);
        Party party = (Party) supplier;
        Context.getInstance().setSupplier(party);
        if (getWorkspace() == null) {
            layoutWorkspace(party, getComponent());
        }
        initQuery(party);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

}
