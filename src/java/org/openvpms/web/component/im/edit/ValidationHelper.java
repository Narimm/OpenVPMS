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

import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.edit.Validator;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Collection;


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
        List<ValidationError> errors = validate(object, service);
        return (errors == null);
    }

    /**
     * Validates an object.
     *
     * @param object the object to validate
     * @return a list of validation errors, or <code>null</code> if the object
     *         is valid
     */
    public static List<ValidationError> validate(IMObject object,
                                                 IArchetypeService service) {
        List<ValidationError> errors = null;
        try {
            service.validateObject(object);
        } catch (ValidationException exception) {
            _log.debug(exception, exception);
            errors = exception.getErrors();
            if (errors.isEmpty()) {
                errors.add(new ValidationError(null, exception.getMessage()));
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return errors;
    }

    /**
     * Display the first error from a validator.
     *
     * @param validator the validator
     */
    public static void showError(Validator validator) {
        Collection<Modifiable> invalid = validator.getInvalid();
        if (!invalid.isEmpty()) {
            Modifiable modifiable = invalid.iterator().next();
            List<ValidationError> errors
                    = validator.getErrors(modifiable);
            if (!errors.isEmpty()) {
                ValidationError error = errors.get(0);
                String node = error.getNodeName();
                IMObject parent = null;
                if (modifiable instanceof IMObjectEditor) {
                    parent = ((IMObjectEditor) modifiable).getObject();
                } else if (modifiable instanceof IMObjectCollectionEditor) {
                    parent = ((IMObjectCollectionEditor) modifiable).getObject();
                }
                if (parent != null) {
                    String title = DescriptorHelper.getDisplayName(parent,
                                                                   node);
                    ErrorDialog.show(title, error.getErrorMessage());
                } else {
                    ErrorDialog.show(error.getErrorMessage());
                }
            }
        }
    }

}
