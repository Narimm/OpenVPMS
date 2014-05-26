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

package org.openvpms.web.workspace.workflow.otc;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;

/**
 * An editor for over-the-counter charges.
 * <p/>
 * This suppresses the status node, to avoid it being set POSTED by the user. This is to allow the charge to be
 * deleted if the Over-the-counter workflow is cancelled.
 *
 * @author Tim Anderson
 */
class OTCChargeEditor extends CustomerChargeActEditor {

    /**
     * Suppresses the status node.
     */
    protected static final ArchetypeNodes NODES = new ArchetypeNodes().exclude("status");

    /**
     * Constructs an {@link OTCChargeEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public OTCChargeEditor(FinancialAct act, IMObject parent, LayoutContext context) {
        this(act, parent, context, true);
    }

    /**
     * Constructs an {@link OTCChargeEditor}.
     *
     * @param act            the act to edit
     * @param parent         the parent object. May be {@code null}
     * @param context        the layout context
     * @param addDefaultItem if {@code true} add a default item if the act has none
     */
    protected OTCChargeEditor(FinancialAct act, IMObject parent, LayoutContext context, boolean addDefaultItem) {
        super(act, parent, context, addDefaultItem);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        ActRelationshipCollectionEditor items = getItems();
        ActLayoutStrategy strategy = new ActLayoutStrategy(items) {
            @Override
            protected ArchetypeNodes getArchetypeNodes() {
                return NODES;
            }
        };
        iniLayoutStrategy(strategy);
        return strategy;
    }
}
