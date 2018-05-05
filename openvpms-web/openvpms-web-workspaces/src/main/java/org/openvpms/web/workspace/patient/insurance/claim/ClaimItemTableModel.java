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
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;

/**
 * Table model for <em>act.patientInsuranceClaimItem</em>.
 *
 * @author Tim Anderson
 */
public class ClaimItemTableModel extends DescriptorTableModel<Act> {

    /**
     * Constructs a {@link ClaimItemTableModel}.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public ClaimItemTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

    /**
     * Returns an {@link ArchetypeNodes} that determines what nodes appear in the table.
     * This is only used when {@link #getNodeNames()} returns null or empty.
     *
     * @return the nodes to include
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return allSimpleNodesMinusIdAndLongText().exclude("euthanasiaReason");
    }
}
