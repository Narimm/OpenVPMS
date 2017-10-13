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

package org.openvpms.web.workspace.patient.insurance;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Table model for <em>act.patientInsurance*</em>.
 *
 * @author Tim Anderson
 */
public class InsuranceTableModel extends DescriptorTableModel<Act> {

    /**
     * Constructs a {@link DescriptorTableModel}.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public InsuranceTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
        DescriptorTableColumn insuranceId = getColumn("insuranceId");
        if (insuranceId != null) {
            insuranceId.setHeaderValue(Messages.get("patient.insurance.policyClaimId"));
        }
    }

}
