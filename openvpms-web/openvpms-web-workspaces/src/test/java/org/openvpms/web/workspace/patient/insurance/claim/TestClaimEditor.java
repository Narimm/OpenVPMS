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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.mockito.Mockito;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;

/**
 * A {@link ClaimEditor} that uses mock a implementation of the {@link InsuranceServices}.
 *
 * @author Tim Anderson
 */
class TestClaimEditor extends ClaimEditor {

    public TestClaimEditor(FinancialAct claim, LayoutContext layout) {
        super(claim, null, layout);
    }

    @Override
    protected ClaimContext createClaimContext(FinancialAct act, Party customer, Party patient, Context context) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        InsuranceServices insuranceServices = Mockito.mock(InsuranceServices.class);
        InsuranceRules rules = ServiceHelper.getBean(InsuranceRules.class);
        InsuranceFactory factory= ServiceHelper.getBean(InsuranceFactory.class);
        return new ClaimContext(act, context.getCustomer(), patient, context.getUser(), getLocation(), service, rules,
                                insuranceServices, factory);
    }
}
