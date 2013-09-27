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

package org.openvpms.web.workspace.admin.organisation;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectDeleter;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.help.HelpContext;

/**
 * CRUD window for the Organisation workspace..
 *
 * @author Tim Anderson
 */
public class OrganisationCRUDWindow extends ResultSetCRUDWindow<Entity> {

    /**
     * Constructs a {@link OrganisationCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public OrganisationCRUDWindow(Archetypes<Entity> archetypes, Query<Entity> query, ResultSet<Entity> set,
                                  Context context, HelpContext help) {
        super(archetypes, query, set, context, help);
    }

    /**
     * Creates a deleter to delete an object.
     * <p/>
     * This ensures that entity.job* can be deleted rather than deactivated.
     *
     * @param object the object to delete
     * @return a new deleter
     */
    @Override
    protected IMObjectDeleter createDeleter(Entity object) {
        IMObjectDeleter deleter = super.createDeleter(object);
        if (TypeHelper.isA(object, "entity.job*")) {
            deleter.setExcludeRelationships("entityRelationship.jobUser");
        }
        return deleter;
    }
}
