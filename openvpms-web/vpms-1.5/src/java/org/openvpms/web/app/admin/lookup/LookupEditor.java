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

package org.openvpms.web.app.admin.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Default lookup editor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupEditor extends AbstractLookupEditor {

    /**
     * Construct a new <tt>LookupEditor</tt>.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>.
     */
    public LookupEditor(Lookup object, IMObject parent, LayoutContext context) {
        super(object, parent, context);
    }

}
