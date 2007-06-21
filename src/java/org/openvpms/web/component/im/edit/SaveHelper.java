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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * Helper for saving {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SaveHelper {

    /**
     * Saves an object.
     *
     * @param object the object to save
     * @return <code>true</code> if the object was saved; otherwise
     *         <code>false</code>
     */
    public static boolean save(IMObject object) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return save(object, service);
    }

    /**
     * Saves an object.
     *
     * @param object  the object to save
     * @param service the archetype service
     * @return <code>true</code> if the object was saved; otherwise
     *         <code>false</code>
     */
    public static boolean save(IMObject object, IArchetypeService service) {
        boolean saved = false;
        try {
            service.save(object);
            saved = true;
        } catch (OpenVPMSException exception) {
            String displayName = DescriptorHelper.getDisplayName(object);
            String title = Messages.get("imobject.save.failed", displayName);
            ErrorHelper.show(title, displayName, exception);
        }
        return saved;
    }

    /**
     * Removes an object.
     *
     * @param object  the object to remove
     * @param service the archetype service
     * @return <code>true</code> if the object was removed; otherwise
     *         <code>false</code>
     */
    public static boolean remove(IMObject object, IArchetypeService service) {
        boolean removed = false;
        try {
            service.remove(object);
            removed = true;
        } catch (OpenVPMSException exception) {
            String displayName = DescriptorHelper.getDisplayName(object);
            String title = Messages.get("imobject.delete.failed", displayName);
            ErrorHelper.show(title, exception);
        }
        return removed;
    }

}
