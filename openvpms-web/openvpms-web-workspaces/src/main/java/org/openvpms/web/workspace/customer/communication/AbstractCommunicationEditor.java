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

package org.openvpms.web.workspace.customer.communication;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Editor for <em>act.customerCommunication*</em> acts.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCommunicationEditor extends AbstractActEditor {

    /**
     * Determines if the patient node should be shown.
     */
    private boolean showPatient = true;

    /**
     * Constructs an {@link AbstractCommunicationEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public AbstractCommunicationEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        if (act.isNew()) {
            initParticipant("customer", context.getContext().getCustomer());
            if (context.getContext().getPatient() != null) {
                initParticipant("patient", context.getContext().getPatient());
            }
            initParticipant("location", context.getContext().getLocation());
        }
    }

    /**
     * Determines if the patient node should be shown.
     *
     * @param show if {@code true}, display the patient node
     */
    public void setShowPatient(boolean show) {
        this.showPatient = show;
    }

    /**
     * Determines if the patient node should be shown.
     *
     * @return {@code true} if the patient node should be displayed
     */
    public boolean getShowPatient() {
        return showPatient;
    }

}
