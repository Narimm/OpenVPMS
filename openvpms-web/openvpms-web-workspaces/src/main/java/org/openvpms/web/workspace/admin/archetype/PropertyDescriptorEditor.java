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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.archetype;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * An editor for <em>descriptor.assertionProperty</em>, <em>descriptor.propertyMap</em> and
 * <em>descriptor.PropertyList</em>.
 * <p/>
 * This does not make objects persistent, as this is handled via the parent object.
 *
 * @author Tim Anderson
 */
public class PropertyDescriptorEditor extends AbstractIMObjectEditor {

    /**
     * Constructs a {@link PropertyDescriptorEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public PropertyDescriptorEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Saves the object.
     */
    @Override
    protected void saveObject() {
        // no-op
    }
}
