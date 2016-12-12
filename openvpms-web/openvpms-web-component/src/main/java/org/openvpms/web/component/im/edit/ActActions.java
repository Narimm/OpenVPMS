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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
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
     * Determines if a confirmation should be displayed prior to printing unfinalised acts.
     */
    private final boolean warnOnPrintUnfinalised;

    /**
     * View actions.
     */
    private static ActActions VIEW = new ActActions() {
        @Override
        public boolean canCreate() {
            return false;
        }

        @Override
        public boolean canDelete(Act act) {
            return false;
        }

        @Override
        public boolean canEdit(Act act) {
            return false;
        }

        @Override
        public boolean canPost(Act act) {
            return false;
        }

        @Override
        public boolean post(Act act) {
            return false;
        }

        @Override
        public boolean setPrinted(Act act, boolean printed) {
            return false;
        }
    };

    /**
     * Default edit actions.
     */
    private static final ActActions EDIT = new ActActions() {
    };

    /**
     * Edit actions for acts that a warning should be displayed for if they are unfinalised when printing.
     */
    private static final ActActions EDIT_WARN_ON_PRINT_UNFINALISED = new ActActions(true) {
    };

    /**
     * Returns actions that only support viewing acts.
     *
     * @return the actions
     */
    @SuppressWarnings("unchecked")
    public static <T extends Act> ActActions<T> view() {
        return VIEW;
    }

    /**
     * Returns actions that support editing acts.
     *
     * @return the actions
     */
    @SuppressWarnings("unchecked")
    public static <T extends Act> ActActions<T> edit() {
        return EDIT;
    }

    /**
     * Returns actions that support editing acts.
     *
     * @param warnOnPrintUnfinalised if {@code true}, printing an unfinalised act should display a confirmation
     * @return the actions
     */
    @SuppressWarnings("unchecked")
    public static <T extends Act> ActActions<T> edit(boolean warnOnPrintUnfinalised) {
        return warnOnPrintUnfinalised ? EDIT_WARN_ON_PRINT_UNFINALISED : EDIT;
    }

    /**
     * Default constructor.
     */
    public ActActions() {
        this(false);
    }

    /**
     * Constructs an {@link ActActions}.
     *
     * @param warnOnPrintUnfinalised if {@code true}, printing an unfinalised act should display a confirmation
     */
    public ActActions(boolean warnOnPrintUnfinalised) {
        this.warnOnPrintUnfinalised = warnOnPrintUnfinalised;
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act to check
     * @return {@code true} if the act isn't locked
     */
    public boolean canEdit(T act) {
        return super.canEdit(act) && !isLocked(act);
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act to check
     * @return {@code true} if the act isn't locked
     */
    public boolean canDelete(T act) {
        return super.canDelete(act) && !isLocked(act);
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
     * or its value is the same as that supplied
     */
    public boolean setPrinted(T act, boolean printed) {
        ActBean bean = new ActBean(act);
        if (bean.hasNode("printed") && bean.getBoolean("printed") != printed) {
            bean.setValue("printed", printed);
            return true;
        }
        return false;
    }

    /**
     * Determines if an act is unfinalised, for the purposes of printing.
     *
     * @param act the act
     * @return {@code true} if the act is unfinalised, otherwise {@code false}
     */
    public boolean isUnfinalised(Act act) {
        return !ActStatus.POSTED.equals(act.getStatus());
    }

    /**
     * Determines if a confirmation should be displayed before printing an unfinalised act.
     *
     * @return {@code false}
     */
    public boolean warnWhenPrintingUnfinalisedAct() {
        return warnOnPrintUnfinalised;
    }

    /**
     * Determines if an act is locked from changes.
     *
     * @param act the act
     * @return {@code true} if the act status is {@link ActStatus#POSTED}
     */
    public boolean isLocked(T act) {
        return ActStatus.POSTED.equals(act.getStatus());
    }
}
