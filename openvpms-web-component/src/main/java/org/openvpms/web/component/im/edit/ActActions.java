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
package org.openvpms.web.component.im.edit;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;

import static org.openvpms.archetype.rules.act.ActStatus.CANCELLED;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;


/**
 * Implementation of {@link IMObjectActions} for acts.
 *
 * @author Tim Anderson
 */
public abstract class ActActions<T extends Act> extends AbstractIMObjectActions<T> {

    /**
     * Determines if an act can be edited.
     *
     * @param act the act to check
     * @return {@code true} if the act status isn't {@code POSTED}
     */
    public boolean canEdit(T act) {
        return super.canEdit(act) && !ActStatus.POSTED.equals(act.getStatus());
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act to check
     * @return {@code true} if the act status isn't {@code POSTED}
     */
    public boolean canDelete(T act) {
        return super.canDelete(act) && !ActStatus.POSTED.equals(act.getStatus());
    }

    /**
     * Determines if an act can be posted (i.e finalised).
     * <p/>
     * This implementation returns {@code true} if the act status isn't {@code POSTED} or {@code CANCELLED}.
     *
     * @param act the act to check
     * @return {@code true} if the act can be posted
     */
    public boolean canPost(T act) {
        String status = act.getStatus();
        return !POSTED.equals(status) && !CANCELLED.equals(status);
    }

    /**
     * Posts the act. This changes the act's status to {@code POSTED}, and saves it.
     *
     * @param act the act to check
     * @return {@code true} if the act was posted
     */
    public boolean post(T act) {
        if (canPost(act)) {
            act.setStatus(POSTED);
            // todo - workaround for OVPMS-734
            if (TypeHelper.isA(act, "act.customerAccount*")) {
                act.setActivityStartTime(new Date());
            }
            return SaveHelper.save(act);
        }
        return false;
    }

    /**
     * Updates an act's printed status.
     * <p/>
     * This suppresses execution of business rules to allow the printed flag to be set on acts that have been POSTED.
     *
     * @param act the act to update
     * @return {@code true} if the act was saved
     */
    public boolean setPrinted(T act) {
        boolean saved = false;
        try {
            if (setPrinted(act, true)) {
                saved = SaveHelper.save(act, ServiceHelper.getArchetypeService(false));
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
        return saved;
    }

    /**
     * Sets an act's print status.
     *
     * @param act     the act to update
     * @param printed the print status
     * @return {@code true} if the print status was changed, or {@code false} if the act doesn't have a 'printed' node
     *         or its value is the same as that supplied
     */
    public boolean setPrinted(T act, boolean printed) {
        ActBean bean = new ActBean(act);
        if (bean.hasNode("printed") && bean.getBoolean("printed") != printed) {
            bean.setValue("printed", printed);
            return true;
        }
        return false;
    }

}
