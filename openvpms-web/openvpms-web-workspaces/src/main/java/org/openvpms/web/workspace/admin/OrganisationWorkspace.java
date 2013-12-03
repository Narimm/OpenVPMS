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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;
import org.openvpms.web.workspace.admin.organisation.OrganisationCRUDWindow;


/**
 * Organisation workspace.
 *
 * @author Tim Anderson
 */
public class OrganisationWorkspace extends ResultSetCRUDWorkspace<Entity> {

    /**
     * Constructs an {@code OrganisationWorkspace}.
     *
     * @param context the context
     */
    public OrganisationWorkspace(Context context) {
        super("admin", "organisation", context);
        setArchetypes(Entity.class, "party.organisation*", "entity.organisation*", "entity.SMSConfig*",
                      "entity.job*");
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Entity object) {
        super.setObject(object);
        // need to update the context in case organisations have changed.
        // May need to refine this so that the context is only updated if the
        // organisation is a newer version of that currently in the context
        // (i,e don't change for different organisations).
        Context context = getContext();
        if (TypeHelper.isA(object, "party.organisationSchedule")) {
            context.setSchedule((Party) object);
        } else if (TypeHelper.isA(object, "party.organisationWorkList")) {
            context.setWorkList((Party) object);
        } else if (TypeHelper.isA(object, "party.organisationTill")) {
            context.setTill((Party) object);
        }
    }

    /**
     * Determines if the workspace can be updated with instances of the specified archetype.
     * <p/>
     * This implementation always returns {@code false}, largely to avoid receiving updates when changing practice
     * locations.
     *
     * @param shortName the archetype's short name
     * @return {@code false}
     */
    @Override
    public boolean canUpdate(String shortName) {
        return false;
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Entity> createCRUDWindow() {
        QueryBrowser<Entity> browser = getBrowser();
        return new OrganisationCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(),
                                          getContext(), getHelpContext());
    }
}
