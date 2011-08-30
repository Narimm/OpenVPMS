/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Default editor for {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultIMObjectEditor extends AbstractIMObjectEditor {

    /**
     * Construct a new <code>DefaultIMObjectEditor</code>.
     *
     * @param object  the object to edit
     * @param context the layout context
     */
    public DefaultIMObjectEditor(IMObject object, LayoutContext context) {
        this(object, null, context);
    }

    /**
     * Construct a new <code>DefaultIMObjectEditor</code> for an object that
     * belongs to a collection.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public DefaultIMObjectEditor(IMObject object, IMObject parent,
                                 LayoutContext context) {
        super(object, parent, context);
    }

}
