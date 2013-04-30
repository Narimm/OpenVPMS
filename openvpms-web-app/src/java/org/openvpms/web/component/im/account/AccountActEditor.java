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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.account;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.util.Date;


/**
 * Editor for account acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AccountActEditor extends ActEditor {

    /**
     * Creates a new <tt>AccountActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
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
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    public boolean save() {
        Property status = getProperty("status");
        if (status != null && ActStatus.POSTED.equals(status.getValue())) {
            setStartTime(new Date(), true);
        }
        return super.save();
    }

}
