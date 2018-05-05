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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.account;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.util.Date;


/**
 * Editor for account acts.
 *
 * @author Tim Anderson
 */
public abstract class AccountActEditor extends ActEditor {

    /**
     * Constructs a {@link AccountActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public AccountActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Save any edits.
     * <p/>
     * If the act status is {@link ActStatus#POSTED POSTED}, the
     * start time will be set to the current time, as a workaround for
     * OVPMS-734. todo - revert after 1.1
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    public void save() {
        Property status = getProperty("status");
        if (status != null && ActStatus.POSTED.equals(status.getValue())) {
            setStartTime(new Date(), true);
        }
        super.save();
    }

}
