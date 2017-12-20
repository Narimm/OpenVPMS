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

package org.openvpms.web.component.im.edit.identity;

import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SingleIMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Editor for collections of {@link ActIdentity} and {@link EntityIdentity} with 0..1 or 1..1 cardinality.
 *
 * @author Tim Anderson
 */
public class SingleIdentityCollectionEditor
        extends SingleIMObjectCollectionEditor {

    /**
     * Constructs a {@link SingleIdentityCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the parent object
     * @param context  the layout context
     */
    public SingleIdentityCollectionEditor(CollectionProperty property, IMObject object, LayoutContext context) {
        super(property, object, context);
    }

    /**
     * Returns the identity editor, or {@code null} if there is no current identity editor.
     *
     * @return the identity editor. May be {@code null}
     */
    protected IdentityEditor getIdentityEditor() {
        IMObjectEditor editor = getCurrentEditor();
        return (editor instanceof IdentityEditor) ? (IdentityEditor) editor : null;
    }

    /**
     * Determines if the identity is empty. This returns true if the identity is null andis optional.
     *
     * @return {@code true} if the identity is null
     */
    protected boolean isEmpty() {
        IdentityEditor editor = getIdentityEditor();
        return editor != null && getCollection().getMinCardinality() == 0 && editor.isNull();
    }

}
