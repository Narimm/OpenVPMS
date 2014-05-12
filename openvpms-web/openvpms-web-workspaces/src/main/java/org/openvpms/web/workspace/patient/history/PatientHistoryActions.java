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
package org.openvpms.web.workspace.patient.history;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.ActActions;


/**
 * Actions that may be performed on patient history acts.
 *
 * @author Tim Anderson
 */
public class PatientHistoryActions extends ActActions<Act> {

    /**
     * The singleton instance.
     */
    public static final PatientHistoryActions INSTANCE = new PatientHistoryActions();


    /**
     * Default constructor.
     */
    protected PatientHistoryActions() {
        super();
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act to check
     * @return {@code true} if the act isn't an invoice item, and its status isn't {@code POSTED}
     */
    @Override
    public boolean canEdit(Act act) {
        return !TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM) && super.canEdit(act);
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act to check
     * @return {@code true} if the act isn't an invoice item, and its status isn't {@code POSTED}
     */
    @Override
    public boolean canDelete(Act act) {
        if (act == null) {
            return false;
        }
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            return false;
        } else if (TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT, PatientArchetypes.CLINICAL_PROBLEM)) {
            return act.getSourceActRelationships().isEmpty();
        } else {
            for (ActRelationship rel : act.getTargetActRelationships()) {
                if (TypeHelper.isA(rel.getSource(), CustomerAccountArchetypes.INVOICE_ITEM)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if an act can be posted (i.e finalised).
     * <p/>
     * This implementation returns {@code true} if the act isn't an invoice item ant its status isn't {@code POSTED}
     * or {@code CANCELLED}.
     *
     * @param act the act to check
     * @return {@code true} if the act can be posted
     */
    @Override
    public boolean canPost(Act act) {
        return !TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM) && super.canPost(act);
    }
}
