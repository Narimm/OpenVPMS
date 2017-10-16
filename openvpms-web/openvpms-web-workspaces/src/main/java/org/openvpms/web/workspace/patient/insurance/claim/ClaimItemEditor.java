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
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;

import java.util.List;

/**
 * Editor for <em>act.patientInsuranceClaimItem</em> acts.
 *
 * @author Tim Anderson
 */
public class ClaimItemEditor extends AbstractClaimEditor {

    /**
     * The charges associated with the claim item.
     */
    private final ChargeCollectionEditor charges;

    /**
     * Constructs a {@link ClaimItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public ClaimItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, "total", context);
        charges = new ChargeCollectionEditor(getCollectionProperty("items"), act, context);
        charges.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onItemsChanged();
            }
        });
        getEditors().add(charges);
        addStartEndTimeListeners();
        getProperty("status").addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onStatusChanged();
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
        return charges.getActs();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        strategy.addComponent(new ComponentState(charges));
        return strategy;
    }

    /**
     * Invoked when the status changes.
     */
    private void onStatusChanged() {
        IMObjectLayoutStrategy layout = getView().getLayout();
        if (layout instanceof ClaimItemLayoutStrategy) {
            boolean euthanased = "EUTHANASED".equals(getStatus());
            ((ClaimItemLayoutStrategy) layout).setShowEuthanasiaReason(euthanased);
        }
    }

}
