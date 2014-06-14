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

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for product batches.
 *
 * @author Tim Anderson
 */
public class BatchParticipationEditor extends ParticipationEditor<Entity> {

    /**
     * Constructs a {@link BatchParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be {@code null}
     */
    public BatchParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
        getEntityEditor().setExpireAfter(parent.getActivityStartTime());
    }

    /**
     * Sets the product.
     *
     * @param product the product. May be {@code null}
     */
    public void setProduct(Product product) {
        getEntityEditor().setProduct(product);
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createEntityEditor(Property property) {
        IMObject parent = getParent();
        boolean setDefault = parent.isNew();
        return new BatchReferenceEditor(property, setDefault, getLayoutContext());
    }

    /**
     * Returns the participation entity editor.
     *
     * @return the participation entity editor
     */
    @Override
    protected BatchReferenceEditor getEntityEditor() {
        return (BatchReferenceEditor) super.getEntityEditor();
    }
}
