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

package org.openvpms.web.component.im.print;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Interactive {@link IMPrinter}. Pops up a dialog with options to print,
 * preview, or cancel.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InteractiveIMObjectPrinter<T extends IMObject>
        extends InteractiveIMPrinter<T> {

    /**
     * Constructs a new <code>InteractiveIMPrinter</code>.
     *
     * @param printer the printer to delegate to
     */
    public InteractiveIMObjectPrinter(IMPrinter<T> printer) {
        super(printer);
    }

    /**
     * Returns a title for the print dialog.
     *
     * @return a title for the print dialog
     */
    @Override
    protected String getTitle() {
        List<T> objects = getObjects();
        IMObject object = !objects.isEmpty() ? objects.get(0) : null;
        String displayName = null;
        if (object != null) {
            displayName = DescriptorHelper.getDisplayName(object);
        }
        return Messages.get("imobject.print.title", displayName);
    }
}
