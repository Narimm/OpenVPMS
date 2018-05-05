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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Default editor for {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class DefaultIMObjectEditor extends AbstractIMObjectEditor {

    /**
     * Constructs a {@link DefaultIMObjectEditor}.
     *
     * @param object  the object to edit
     * @param context the layout context
     */
    public DefaultIMObjectEditor(IMObject object, LayoutContext context) {
        this(object, null, context);
    }

    /**
     * Constructs a {@link DefaultIMObjectEditor} for an object that belongs to a collection.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public DefaultIMObjectEditor(IMObject object, IMObject parent, LayoutContext context) {
        super(object, parent, context);
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return a new instance
     * @throws OpenVPMSException if a new instance cannot be created
     */
    @Override
    public IMObjectEditor newInstance() {
        return new DefaultIMObjectEditor(reload(getObject()), getParent(), getLayoutContext());
    }
}
