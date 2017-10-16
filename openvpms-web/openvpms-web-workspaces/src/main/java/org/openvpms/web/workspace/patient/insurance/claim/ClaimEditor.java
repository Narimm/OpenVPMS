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

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.identity.SingleIdentityCollectionEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Editor for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimEditor extends AbstractClaimEditor {

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
    public ClaimEditor(FinancialAct act, IMObject parent, LayoutContext context) {
        super(act, parent, "amount", context);
        if (act.isNew()) {
            initParticipant("patient", context.getContext().getPatient());
            initParticipant("location", context.getContext().getLocation());
            initParticipant("clinician", context.getContext().getClinician());
        }
        eClaim = canSubmitClaim(act);
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
        return new ClaimEditor((FinancialAct) reload(getObject()), getParent(), getLayoutContext());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        ClaimLayoutStrategy strategy = new ClaimLayoutStrategy();
        if (identityCollectionEditor != null) {
            strategy.addComponent(new ComponentState(identityCollectionEditor));
        } else {
            strategy.setInsuranceIdReadOnly(eClaim);
        }
        return strategy;
    }

    /**
     * Invoked when layout has completed.
     * <p>
     * This can be used to perform processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        getEditor("items").addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onItemsChanged();
            }
        });
    }

    /**
     * Returns the item acts to sum.
     *
     * @return the acts
     */
    @Override
    protected List<Act> getItemActs() {
        ActRelationshipCollectionEditor editor = (ActRelationshipCollectionEditor) getEditor("items");
        return editor.getActs();
    }

    /**
     * Determines if a claim can be submitted via an {@link InsuranceService}.
     *
     * @param act the claim act
     * @return {@code true} if the claim can be submitted
     */
    private boolean canSubmitClaim(Act act) {
        Claim claim = ServiceHelper.getBean(InsuranceFactory.class).createClaim(act);
        return ServiceHelper.getBean(InsuranceServices.class).canSubmit(claim);
    }

}
