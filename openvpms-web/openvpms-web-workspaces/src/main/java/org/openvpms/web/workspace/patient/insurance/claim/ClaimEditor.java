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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.internal.InsuranceArchetypes;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.edit.identity.SingleIdentityCollectionEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.system.ServiceHelper;

/**
 * Editor for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimEditor extends AbstractActEditor {

    /**
     * Determines if the claim will be submitted via an {@link InsuranceService}.
     * <p>
     * If so, users cannot edit the insuranceId node, as this will be generated.
     */
    private final boolean eClaim;

    /**
     * The insuranceId node editor, for non-eClaims.
     */
    private SingleIdentityCollectionEditor identityCollectionEditor;

    /**
     * Constructs an {@link ClaimEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public ClaimEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        if (act.isNew()) {
            initParticipant("patient", context.getContext().getPatient());
            initParticipant("location", context.getContext().getLocation());
            initParticipant("clinician", context.getContext().getClinician());
        }
        eClaim = hasInsuranceService(act);
        if (!eClaim) {
            CollectionProperty insuranceId = getCollectionProperty("insuranceId");
            if (insuranceId.getValues().isEmpty()) {
                identityCollectionEditor = new SingleIdentityCollectionEditor(insuranceId, act, context);
                IMObject identity = IMObjectCreator.create(InsuranceArchetypes.CLAIM_IDENTITY);
                insuranceId.add(identity);
                getEditors().add(identityCollectionEditor);
            }
        }
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return {@code null}
     */
    @Override
    public IMObjectEditor newInstance() {
        return new ClaimEditor(reload(getObject()), getParent(), getLayoutContext());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        if (identityCollectionEditor != null) {
            strategy.addComponent(new ComponentState(identityCollectionEditor));
        } else if (strategy instanceof ClaimLayoutStrategy) {
            ((ClaimLayoutStrategy) strategy).setInsuranceIdReadOnly(eClaim);
        }
        return strategy;
    }

    private boolean hasInsuranceService(Act act) {
        Claim claim = ServiceHelper.getBean(InsuranceFactory.class).createClaim(act);
        return ServiceHelper.getBean(InsuranceServices.class).canSubmit(claim);
    }

}
