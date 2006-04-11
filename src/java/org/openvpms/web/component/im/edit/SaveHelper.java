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

import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Helper for saving {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SaveHelper {

    /**
     * Save an object.
     *
     * @param object the object to save
     * @return <code>true</code> if the object was saved; otherwise
     *         <code>false</code>
     */
    public static boolean save(IMObject object) {
        return save(object, null, null);
    }

    /**
     * Save an object.
     *
     * @param object     the object to save
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent's descriptor. May be <code>null</code>
     * @return <code>true</code> if the object was saved; otherwise
     *         <code>false</code>
     */
    public static boolean save(IMObject object, IMObject parent, NodeDescriptor descriptor) {
        boolean saved = false;
        IArchetypeService service = ServiceHelper.getArchetypeService();
        if (ValidationHelper.isValid(object)) {
            try {
                if (parent == null) {
                    service.save(object);
                    saved = true;
                } else {
                    if (object.isNew()) {
                        descriptor.addChildToCollection(parent, object);
                        saved = true;
                    } else if (parent != null && !parent.isNew()) {
                        service.save(object);
                        saved = true;
                    } else {
                        // new parent, new child. Parent must be saved first.
                        // Not a failure, so return true.
                        saved = true;
                    }
                }
            } catch (ArchetypeServiceException exception) {
                ErrorDialog.show(exception);
            } catch (DescriptorException exception) {
                ErrorDialog.show(exception);
            }
        }
        return saved;
    }

}
