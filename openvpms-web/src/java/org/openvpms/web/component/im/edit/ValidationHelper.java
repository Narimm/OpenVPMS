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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.spring.ServiceHelper;

import java.util.List;


/**
 * Validation helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ValidationHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(ValidationHelper.class);


    /**
     * Validates an object.
     *
     * @param object the object to validate
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public static boolean isValid(IMObject object) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return isValid(object, service);
    }

    /**
     * Validates an object.
     *
     * @param object  the object to validate
     * @param service the archetype service
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public static boolean isValid(IMObject object, IArchetypeService service) {
        boolean valid = false;
        try {
            service.validateObject(object);
            valid = true;
        } catch (ValidationException exception) {
            List<ValidationError> errors = exception.getErrors();
            if (!errors.isEmpty()) {
                ValidationError error = errors.get(0);
                String node = error.getNodeName();
                String title = DescriptorHelper.getDisplayName(object, node);
                if (title == null) {
                    title = node;
                }
                ErrorDialog.show(title, error.getErrorMessage());
                _log.debug(exception.getMessage(), exception);
            } else {
                ErrorHelper.show(exception);
            }
        }
        return valid;
    }
}
