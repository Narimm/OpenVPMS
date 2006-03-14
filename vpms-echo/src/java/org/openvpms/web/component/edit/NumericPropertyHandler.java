package org.openvpms.web.component.edit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;


/**
 * Validator for numeric nodes..
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class NumericPropertyHandler extends PropertyHandler {

    /**
     * Construct a new <code>NumericPropertyHandler</code>.
     *
     * @param descriptor the node descriptor.
     */
    public NumericPropertyHandler(NodeDescriptor descriptor) {
        super(descriptor);
    }

    public Object convert(Object object) {
        Object result = null;
        if (object instanceof String) {
            String value = (String) object;
            if (StringUtils.isEmpty(value)) {
                result = null;
            } else {
                try {
                    result = NumberUtils.createNumber(value);
                } catch (NumberFormatException exception) {
                    throwValidationException("Invalid number", exception);
                }
            }
        } else {
            // @todo - should convert numerics to target type, if they
            // don't match
            result = object;
        }
        return result;
    }

    /**
     * Helper to throw a validation exception.
     *
     * @param message the message
     * @param cause   the cause. May be <code>null</code>
     * @throws ValidationException
     */
    private void throwValidationException(String message, Throwable cause)
            throws ValidationException {
        ValidationError error = new ValidationError(
                getDescriptor().getName(), message);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        errors.add(error);
        ValidationException.ErrorCode code
                = ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype;
        throw new ValidationException(errors, code, cause);

    }

}
