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
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * Editor for a collection of <em>act.patientInsuranceClaimItem</em> acts.
 *
 * @author Tim Anderson
 */
class ClaimItemCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * The attachments.
     */
    private final AttachmentCollectionEditor attachments;

    /**
     * Constructs a {@link ClaimItemCollectionEditor}.
     *
     * @param property    the collection property
     * @param act         the parent act
     * @param attachments the attachments
     * @param context     the layout context
     */
    public ClaimItemCollectionEditor(CollectionProperty property, Act act, AttachmentCollectionEditor attachments,
                                     LayoutContext context) {
        super(property, act, context);
        this.attachments = attachments;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        return new ClaimItemEditor((Act) object, (Act) getObject(), attachments, context);
    }
}
