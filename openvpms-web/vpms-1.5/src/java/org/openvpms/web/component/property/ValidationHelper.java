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
 *  $Id:ValidationHelper.java 2147 2007-06-21 04:16:11Z tanderson $
 */

package org.openvpms.web.component.property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Validation helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2007-06-21 04:16:11Z $
 */
public class ValidationHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ValidationHelper.class);


    /**
     * Validates an object.
     *
     * @param object the object to validate
     * @return <tt>true</tt> if the object is valid; otherwise <tt>false</tt>
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
     * @return <tt>true</tt> if the object is valid; otherwise
     *         <tt>false</tt>
     */
    public static boolean isValid(IMObject object, IArchetypeService service) {
        List<ValidatorError> errors = validate(object, service);
        return (errors == null);
    }

    /**
     * Validates an object.
     *
     * @param object the object to validate
     * @return a list of validation errors, or <tt>null</tt> if the object
     *         is valid
     */
    public static List<ValidatorError> validate(IMObject object,
                                                IArchetypeService service) {
        List<ValidatorError> result = null;
        try {
            service.validateObject(object);
        } catch (ValidationException exception) {
            log.debug(exception, exception);
            List<ValidationError> errors = exception.getErrors();
            result = new ArrayList<ValidatorError>();
            if (errors.isEmpty()) {
                result.add(new ValidatorError(exception.getMessage()));
            } else {
                for (ValidationError error : errors) {
                    result.add(new ValidatorError(error));
                }
            }
        } catch (OpenVPMSException exception) {
            log.debug(exception, exception);
            result = new ArrayList<ValidatorError>();
            result.add(new ValidatorError(exception.getMessage()));
        }
        return result;
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
            List<ValidatorError> errors = validator.getErrors(modifiable);
            if (!errors.isEmpty()) {
                ValidatorError error = errors.get(0);
                ErrorHelper.show(error.toString());
            }
        }
    }

}
