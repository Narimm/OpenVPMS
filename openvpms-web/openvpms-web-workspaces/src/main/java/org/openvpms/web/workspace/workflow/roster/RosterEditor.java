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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityLinkCollectionEditor;

/**
 * Editor for <em>entity.rosterArea</em>.
 *
 * @author Tim Anderson
 */
public class RosterEditor extends AbstractIMObjectEditor {

    /**
     * Constructs an {@link AbstractIMObjectEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public RosterEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Invoked when layout has completed.
     * <p>
     * This can be used to perform processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        getEditor("location").addModifiableListener(modifiable -> locationChanged());
    }

    /**
     * Updates the schedules with the selected location to ensure that only schedules associated with the
     * location may be selected.
     */
    private void locationChanged() {
        Party location = (Party) getObject(getBean(getObject()).getTargetRef("location"));
        EntityLinkCollectionEditor schedules = (EntityLinkCollectionEditor) getEditor("schedules");
        for (IMObject object : schedules.getCurrentObjects()) {
            IMObjectEditor editor = schedules.getEditor(object);
            if (editor instanceof RosterAreaScheduleEditor) {
                ((RosterAreaScheduleEditor) editor).setLocation(location);
            }
        }
        schedules.resetValid();
    }
}
