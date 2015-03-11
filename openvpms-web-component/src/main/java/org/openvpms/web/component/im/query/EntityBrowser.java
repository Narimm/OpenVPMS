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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Entity browser that displays the archetype, name and description of
 * the entities being queried.
 * <p/>
 * The archetype column is only displayed if more than one archetype is being queried.
 * <p/>
 * The active column is displayed if both active and inactive instances are being queried.
 *
 * @author Tim Anderson
 */
public class EntityBrowser extends AbstractEntityBrowser<Entity> {


    /**
     * Constructs an {@link EntityBrowser}.
     *
     * @param query   the query
     * @param context the layout context
     */
    public EntityBrowser(EntityQuery<Entity> query, LayoutContext context) {
        super(query, context);
    }

}
