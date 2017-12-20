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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

/**
 * Editor for <em>actIdentity.*</em> and <em>entityIdentity.*</em> identities, that only display an identity node.
 *
 * @author Tim Anderson
 */
public class IdentityEditor extends AbstractIMObjectEditor {

    /**
     * Constructs an {@link IdentityEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public IdentityEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Determines if the identity is null.
     *
     * @return {@code true} if the identity is {@code null}.
     */
    public boolean isNull() {
        Property identity = getProperty("identity");
        return identity != null && StringUtils.isEmpty(identity.getString());
    }
}
