package org.openvpms.web.component.edit;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Validation helper.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ValidationHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(ValidationHelper.class);


    /**
     * Validate an object.
     *
     * @param object the object to validate
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public static boolean isValid(IMObject object) {
        boolean valid = false;
        IArchetypeService service = ServiceHelper.getArchetypeService();
        try {
            service.validateObject(object);
            valid = true;
        } catch (ValidationException exception) {
            List<ValidationError> errors = exception.getErrors();
            if (!errors.isEmpty()) {
                ValidationError error = errors.get(0);
                ErrorDialog.show("Error: " + error.getNodeName(),
                        error.getErrorMessage());
            } else {
                ErrorDialog.show("Error", exception.getMessage());
            }
            _log.error(exception.getMessage(), exception);
        }
        return valid;
    }

}
