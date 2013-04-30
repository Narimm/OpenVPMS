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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for tills. This updates the context when a till is selected.
 *
 * @author Tim Anderson
 */
public class TillParticipationEditor extends ParticipationEditor<Party> {

    /**
     * Constructs a {@code TillParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <tt>null</tt>
     */
    public TillParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.till")) {
            throw new IllegalArgumentException("Invalid participation type: "
                                               + participation.getArchetypeId().getShortName());
        }
        if (participation.isNew() && participation.getEntity() == null) {
            Party till = getLayoutContext().getContext().getTill();
            setEntity(till);
        }
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Party> createEntityEditor(Property property) {
        return new TillReferenceEditor(property, getParent(), getLayoutContext()) {

            @Override
            public boolean setObject(Party object) {
                getLayoutContext().getContext().setTill(object);
                return super.setObject(object);
            }
        };
    }
}
