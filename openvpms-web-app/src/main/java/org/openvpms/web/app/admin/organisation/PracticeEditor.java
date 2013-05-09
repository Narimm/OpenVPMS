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
 *  $Id: AdminSubsystem.java 3769 2010-06-28 13:32:18Z tanderson $
 */

package org.openvpms.web.app.admin.organisation;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.resource.subscription.SubscriptionHelper;
import org.openvpms.web.system.ServiceHelper;

/**
 * Editor for <em>party.organisationPractice</em>.
 * <p/>
 * This adds a tab to manage subscription.
 *
 * @author Tim Anderson
 */
public class PracticeEditor extends AbstractIMObjectEditor {

    /**
     * The participation editor.
     */
    private IMObjectEditor participationEditor;

    /**
     * Constructs an <tt>OrganisationPracticeEditor</tt>.
     *
     * @param practice the practice to edit
     * @param parent   the parent object. May be <tt>null</tt>
     * @param context  the layout context. May be <tt>null</tt>.
     */
    public PracticeEditor(Party practice, IMObject parent, LayoutContext context) {
        super(practice, parent, context);

        IArchetypeService service = ServiceHelper.getArchetypeService();
        Participation participation = SubscriptionHelper.getSubscriptionParticipation(practice, service);
        if (participation == null) {
            participation = (Participation) IMObjectCreator.create("participation.subscription");
        }
        participationEditor = new SubscriptionParticipationEditor(participation, practice, context);
        ((SubscriptionParticipationEditor) participationEditor).setDeleteAct(true);

        getEditors().add(participationEditor);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PracticeLayoutStrategy(participationEditor.getComponent(), participationEditor.getFocusGroup());
    }
}
