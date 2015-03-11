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

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Default implementation of the {@link EditableIMObjectCollectionEditor} interface.
 *
 * @author Tim Anderson
 */
public class DefaultIMObjectCollectionEditor
        extends IMObjectTableCollectionEditor {

    /**
     * Constructs a {@link DefaultIMObjectCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public DefaultIMObjectCollectionEditor(CollectionProperty property, IMObject object, LayoutContext context) {
        super(property, object, context);
    }
}
