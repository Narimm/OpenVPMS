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

package org.openvpms.web.workspace.admin.organisation;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;

/**
 * View layout strategy for <em>party.organisationLocation<em>.
 *
 * @author Tim Anderson
 */
public class OrganisationLocationViewLayoutStrategy extends OrganisationLocationLayoutStrategy {

    /**
     * Constructs an {@link OrganisationLocationViewLayoutStrategy}.
     */
    public OrganisationLocationViewLayoutStrategy() {
        super();
    }

    /**
     * Returns a component representing the practice location logo.
     *
     * @param object  the practice location
     * @param context the layout context
     * @return a new component
     */
    @Override
    protected ComponentState getLogo(IMObject object, LayoutContext context) {
        Participation participation = QueryHelper.getParticipation((Entity) object,
                                                                   DocumentArchetypes.LOGO_PARTICIPATION);
        ComponentState logo;
        if (participation != null) {
            logo = context.getComponentFactory().create(participation, object);
        } else {
            logo = new ComponentState(LabelFactory.create("admin.practice.logo.none"));
        }
        logo.setDisplayName(Messages.get("admin.practice.logo"));
        return logo;
    }
}
