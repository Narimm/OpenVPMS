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

package org.openvpms.web.workspace.workflow.roster;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.EntityQuery;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Query for <em>entity.rosterArea</em> objects that restricts them to the current practice location.
 *
 * @author Tim Anderson
 */
public class AreaQuery extends EntityQuery<Entity> {

    /**
     * Constructs an {@link AreaQuery}.
     *
     * @param context the context
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public AreaQuery(Context context) {
        super(new String[]{"entity.rosterArea"}, context);
        setLocation(context.getLocation());
    }

    /**
     * Sets the practice location to constrain areas to.
     *
     * @param location the practice location. May be {@code null}
     */
    public void setLocation(Party location) {
        if (location != null) {
            setConstraints(join("location").add(eq("target", location)));
        } else {
            setConstraints(null);
        }
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return {@code true} if the query should be run automatically; otherwise {@code false}
     */
    @Override
    public boolean isAuto() {
        return true;
    }

}
