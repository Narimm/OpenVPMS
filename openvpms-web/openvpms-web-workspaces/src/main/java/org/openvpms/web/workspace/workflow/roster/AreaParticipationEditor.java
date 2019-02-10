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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Roster area participation area.
 *
 * @author Tim Anderson
 */
public class AreaParticipationEditor extends ParticipationEditor<Entity> {

    /**
     * Constructs an {@link AreaParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context
     */
    public AreaParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
    }

    /**
     * Sets the location to constrain areas to.
     *
     * @param location the practice location. May be {@code null}
     */
    public void setLocation(Party location) {
        IMObjectReferenceEditor<Entity> editor = getEntityEditor();
        if (editor instanceof AreaReferenceEditor) {
            ((AreaReferenceEditor) editor).setLocation(location);
        }
    }
}
