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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.policy;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Editor for <em>act.patientInsurancePolicy</em>.
 *
 * @author Tim Anderson
 */
public class PolicyEditor extends AbstractActEditor {

    /**
     * Constructs an {@link PolicyEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public PolicyEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        if (act.isNew()) {
            initParticipant("customer", context.getContext().getCustomer());
            initParticipant("patient", context.getContext().getPatient());
        }
        addStartEndTimeListeners();
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return {@code null}
     */
    @Override
    public IMObjectEditor newInstance() {
        return new PolicyEditor(reload(getObject()), getParent(), getLayoutContext());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PolicyLayoutStrategy();
    }

}
