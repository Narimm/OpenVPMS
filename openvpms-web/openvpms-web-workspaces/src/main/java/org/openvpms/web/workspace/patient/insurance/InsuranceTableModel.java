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

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.patient.insurance.InsuranceRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * Table model for <em>act.patientInsurance*</em>.
 *
 * @author Tim Anderson
 */
public class InsuranceTableModel extends DescriptorTableModel<Act> {

    /**
     * Insurance rules.
     */
    private final InsuranceRules rules;

    /**
     * The nodes to display.
     */
    private static final String[] NAMES = new String[]{"insurer", "insurerId", "endTime", "status"};


    /**
     * Constructs a {@link DescriptorTableModel}.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public InsuranceTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
        rules = ServiceHelper.getBean(InsuranceRules.class);
        DescriptorTableColumn insurerId = getColumn("insurerId");
        if (insurerId != null) {
            insurerId.setHeaderValue(Messages.get("patient.insurance.policyClaimId"));
        }
        DescriptorTableColumn endTime = getColumn("endTime");
        if (endTime != null) {
            endTime.setHeaderValue(Messages.get("patient.insurance.expiry"));
        }
    }

    /**
     * Returns a list of node descriptor names to include in the table.
     *
     * @return the list of node descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return NAMES;
    }

    /**
     * Returns a value for a given column.
     *
     * @param object the object to operate on
     * @param column the column
     * @param row    the row
     * @return the value for the column
     */
    @Override
    protected Object getValue(Act object, DescriptorTableColumn column, int row) {
        Object result;
        String name = column.getName();
        if (name.equals("insurer")) {
            Party insurer = rules.getInsurer(object);
            result = (insurer != null) ? insurer.getName() : null;
        } else if (name.equals("endTime") && !TypeHelper.isA(object, InsuranceArchetypes.POLICY)) {
            // claims have no end-time
            result = null;
        } else if (name.equals("status") && !TypeHelper.isA(object, InsuranceArchetypes.CLAIM)) {
            // policies have no status
            result = null;
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }
}
